/*******************************************************************************
 * Copyright (c) 2005-2012 Synopsys, Incorporated
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Synopsys, Inc - Initial implementation 
 *******************************************************************************/
package com.arc.seecode.server;


import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.IEnginePoller;


/**
 * The one and only thread that makes calls into the engine. The engine is single threaded and some DLLs that attach to
 * it may use thread-local storage. So, it is important that all calls into it are from the same thread.
 * <P>
 * Other threads must queue up the engine invocations by calling {@link #enqueue}. The engine thread
 * then removes the invocation and invokes it. It also "polls" the engine in case it is in a run state
 * that needs to be "primed".
 *
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class EngineThread implements Runnable, IEngineRunQueue {

    private static final int MAX_QUEUE_SIZE = 50;
    private static final int HIGH_WATER_MARK = 10; // Take action if queue gets passed this point

    private boolean mTerminated = false;

    private IEngineInvocation[] mQueue = new IEngineInvocation[MAX_QUEUE_SIZE];

    private int mQueueTail = 0;

    private int mQueueHead = 0;

    private int mQueueSize = 0;

    private Thread mThread;

    private Throwable mPendingException = null;

    private IEnginePoller mPoller;

    private Object mSyncLock;

    /**
     * Create thread to service method calls into the engine, and performs
     * polling of the engine.
     * <P>
     * If the engine is actually a simulator, repeated polling can make this thread
     * so dominate the CPU that the UI appears sluggish. For that reason, we reduce
     * the priority of this thread one notch. It has a dramatic affect on the responsiveness
     * of the UI when the simulator is running full boar.
     * 
     * @param poller
     * @param syncLock
     * @param reducePriority if true, then engine is actually a simulator that executes
     * by "polling".
     */
    public EngineThread(IEnginePoller poller, Object syncLock, boolean reducePriority) {
        mPoller = poller;
        mThread = new Thread(this,"Engine Thread");
        // Lower propriety so as not to starve the UI thread.
        // This can become apparent if several instances of the debugger engine are in
        // run mode.
        if (reducePriority)
            mThread.setPriority(mThread.getPriority()-1);
        mThread.start();
        mSyncLock = syncLock;
    }
    
    @Override
    public int getQueueSize(){
        return mQueueSize;
    }

    @Override
    public void enqueue (IEngineInvocation run) throws EngineException {
        // If an exception occurred in last dispatch to the
        // engine, we throw it here. It won't quite correspond
        // to the actual call, but will but close enough
        // for government work.
        if (mPendingException != null) {
            Throwable e = mPendingException;
            mPendingException = null;
            if (e instanceof EngineException){
                throw (EngineException)e;
            }
            else if (e instanceof Error){
                throw (Error)e;
            }
            else if (e instanceof RuntimeException){
                throw (RuntimeException)e;
            }
            throw new Error(e);
        }
        synchronized (mQueue) {
            if (mQueueSize == MAX_QUEUE_SIZE) {
                // If queue is getting ahead of the engine, then do some stalls
                try {
                    mQueue.wait(5000);
                }
                catch (InterruptedException e) {
                }
                if (mQueueSize == MAX_QUEUE_SIZE) {
                    throw new Error("The debugger engine appears to be hung; if is not responding to the IDE.\n"+
                            "The engine will be forceably aborted.");
                }
            }
            if (mQueueTail == MAX_QUEUE_SIZE)
                mQueueTail = 0;
            mQueue[mQueueTail++] = run;
            mQueueSize++;
            //System.out.println("ENQUEUE: size is now " + mQueueSize);
            mQueue.notifyAll(); // Queue size has changed.
            //In case the engine thread is in a wait state on behalf of a poll, wake it up.
            // This could only happen if the queue was previously empty.
            if (mQueueSize == 1) {
                //System.out.println("ENQUEUE: interrupting poll delay");
                mPoller.interruptPollDelay();
            }

            // If queue is getting backed up, insert some delays so that the engine can catch up.
            if (mQueueSize > HIGH_WATER_MARK) {
                //System.out.println("ENQUEUE: stalling");
                try {
                    mQueue.wait(Math.min(1000,(mQueueSize - HIGH_WATER_MARK) *100));
                }
                catch (InterruptedException e) {
                }
            }
        }
    }

    @Override
    public void shutdown () {
        if (!mTerminated) {
            mTerminated = true;
            mThread.interrupt();
        }
    }

    @Override
    public void run () {
        if (mThread != Thread.currentThread())
            return;
        boolean needToPoll = false;
        synchronized (mSyncLock) { // keep engine locked so no other thread can access it!
            while (!mTerminated) {
                IEngineInvocation run = null;
                synchronized (mQueue) {
                    while (!needToPoll && mQueueSize == 0 && !mTerminated) {
                        try {
                            mQueue.wait();
                        }
                        catch (InterruptedException e) {
                        }
                    }
                    if (!mTerminated && mQueueSize > 0) {
                        if (mQueueHead == MAX_QUEUE_SIZE)
                            mQueueHead = 0;
                        run = mQueue[mQueueHead++];
                        mQueueSize--;
                        if (mQueueSize < HIGH_WATER_MARK)
                            mQueue.notifyAll(); // Queue size has changed.
                    }
                }
                if (!mTerminated) {
                    //System.out.println("DEQUEUE: size now " + mQueueSize);
                    if (run != null) {
                        try {
                            run.invoke();
                        }
                        catch (EngineException e) {
                            mPendingException = e;
                        }
                        catch (Error e){
                            mPendingException = e;
                        }
                        catch (RuntimeException e){
                            mPendingException = e;
                        }
                    }
                    // Don't poll if queue is commands are getting stacked up.
                    if (mQueueSize < HIGH_WATER_MARK) {
                       //System.out.println("DEQUEUE: about to poll");
                        try {
                           needToPoll = mPoller.poll(mQueueSize == 0);
                        }
                        catch(Error e){
                            mPendingException = e;
                        }
                       //System.out.println("DEQUEUE: left poll");
                    }
                }
            }
        }
    }

    @Override
    public boolean waitUntilEmpty (int timeout) {
        if (mThread == Thread.currentThread())
            throw new IllegalStateException("EngineThread deadlock!");
        synchronized (mQueue) {
            long startTime = System.currentTimeMillis();
            int t = timeout;
            while (mQueueSize > 0 && !mTerminated) {
                try {
                    mQueue.wait(t);
                }
                catch (InterruptedException e) {
                }
                long elapsed = System.currentTimeMillis() - startTime;
                t = (int) (timeout - elapsed);
                if (t <= 0)
                    break;
            }
            return mQueueSize == 0;
        }
    }
    
    @Override
    public Thread getThread() { return mThread; }
}
