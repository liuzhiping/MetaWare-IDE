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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import com.arc.seecode.connect.IConnection;
import com.arc.seecode.connect.TimeoutException;
import com.arc.seecode.connect.VMDisconnectedException;
import com.arc.seecode.scwp.ScwpCommandPacket;

/**
 * @author David Pickens
 */
class CallbackThread implements Runnable {

    private static final String UI_COMMAND_QUEUE_TXT = "ui_command_queue.txt";

    /**
     * The size that the queue gets to indicate that we should take evasive action.
     */
    private static int PATHOLOGICALLY_LARGE_QUEUE_SIZE = 1000;
    
    private IConnection mConnection;

    private LinkedList<ScwpCommandPacket> mQueue = new LinkedList<ScwpCommandPacket>();

    private transient Exception mException = null;

    private Thread mThread;

    private boolean mSending = false;
    private long mTimeOfLastSend;
    
    private boolean mSwampedErrorPending = false;
    
    private ScwpCommandPacket fLastCommand;

    CallbackThread(IConnection connection) {
        mConnection = connection;
        mThread = new Thread(this, "EventCallback");
        mThread.setDaemon(true);
        mThread.start();
    }
    
    public int getQueueSize() {
        return mQueue.size();
    }

    /**
     * Enqueue a packet to be sent from our dispatch thread. (As we said
     * earlier, we send packets in a separate thread so that the engine isn't
     * locked. This permits clients to make calls into the engine.
     * 
     * @param cmd
     */
    public synchronized void enqueue(ScwpCommandPacket cmd) {   
        // If we have already observed that the IDE is swamped, then
        // the debugger is about to shutdown. Don't continue queueing if
        // that is the case.
        if (mSwampedErrorPending)
            return;
        
        // If queue is getting really big. Then we have some major problems.
        // The UI is hung, or someone's semantic inspection DLL is going berserk.
        // Start throwing things away if the queue doesn't seem to be
        // doing anything. What else can I do?
        if (mQueue.size() >= PATHOLOGICALLY_LARGE_QUEUE_SIZE){
            // If things do seem to be moving, then sleep a bit.
            if (System.currentTimeMillis() - mTimeOfLastSend < 1000){
                try {
                    wait(500); // stall 500 milliseconds.
                }
                catch (InterruptedException e) {                  
                } 
            }
            else {
                PrintStream out;
                try {
                    out = new PrintStream(new FileOutputStream(UI_COMMAND_QUEUE_TXT));
                }
                catch (IOException e) {
                    out = System.out;
                }
                out.println("Callback queue content:");
                out.println("  last command was " + fLastCommand + " issued " + this.getTimeElapsedSinceLastSend() + " msec ago");
                int cnt = 0;
                for (ScwpCommandPacket c: mQueue){
                    out.println("" + (++cnt)+": "+ c);                   
                }
                out.close();
                mSwampedErrorPending = true;
                throw new Error("The debugger engine appears to be emitting UI requests faster than the IDE\n" +
                    "can service. This problem is often due to a semantic-inspection DLL issuing \"update_windows\" invocations\n"+
                    "faster than the UI can service them. The content of the UI command queue is being\n"+
                    "written to the file \"" + UI_COMMAND_QUEUE_TXT + "\".\n" +
                    "\nThe debugger engine will now abort.");
                // The queue is very large, and nothing seems to be happening
                // resort to throwing things away from the front.
                // Perhaps they are just "component update" actions from
                // update timer.
//                while (mQueue.size() >= PATHOLOGICALLY_LARGE_QUEUE_SIZE)
//                    mQueue.removeFirst(); // start throwing things away
            }
        }
        mQueue.addLast(cmd);
        //System.out.println("" + mQueue.size() + ": Adding " + cmd.getMethodName());
        notifyAll();
    }
    
    /**
     * Remove a previously enqueued command, if possible.
     * @param cmd the command to remove.
     * @return true if command successfully removed.
     */
    public synchronized boolean remove(ScwpCommandPacket cmd) {
        return mQueue.remove(cmd);
    }
    
    /**
     * Wait until no new action is pending.
     * Presumably called from the same thread that calls {@link #enqueue}.
     * 
     * @throws InterruptedException
     */
    public synchronized boolean waitUntilEmpty(int timeout)
            throws InterruptedException 
        {
            return stallUntilQueueSizeIsAt(0,timeout);
        }
    
    public synchronized boolean waitUntilChange (int timeout) throws InterruptedException {
        int size = mQueue.size();
        long start = System.currentTimeMillis();
        while (mQueue.size() == size) {
            wait(timeout);
            if (timeout > 0) {
                long elapsed = System.currentTimeMillis() - start;
                timeout -= elapsed;
                if (timeout <= 0)
                    break;
                start += elapsed;
            }
        }
        return mQueue.size() != size;
    }

    /**
     * Wait until no new action is pending.
     * @throws InterruptedException
     */
    public synchronized boolean stallUntilQueueSizeIsAt(int size, int timeout)
            throws InterruptedException {
        long start = System.currentTimeMillis();
        while (mQueue.size() > size || size == 0 && mSending) {
            wait(timeout); // Will awaken after "run()" transmit a command.
            if (timeout > 0) {
                long elapsed = System.currentTimeMillis() - start;
                timeout -= elapsed;
                if (timeout <= 0) break;
                start += elapsed;
            }
        }
        return mQueue.size() <= size && (size > 0 || !mSending);
    }
    
    Exception getExceptionAndClear() {
        Exception e = mException;
        mException = null;
        return e;
    }
    
    public synchronized int getPendingQueueSize(){
        return mQueue.size();
    }
    
    /**
     * Retrieve pending commands so that we can display them in case the queue appear to
     * not being emptying.
     * @return pending commands.
     */
    public synchronized Collection<ScwpCommandPacket> getPendingCommands(){
        return new ArrayList<ScwpCommandPacket>(mQueue);
    }
    
    public long getTimeElapsedSinceLastSend(){
        return System.currentTimeMillis() - mTimeOfLastSend;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            while (true) {
                try {
                    ScwpCommandPacket cmd;
                    synchronized (this) {
                        if (mSending) {
                            mSending = false;
                            notifyAll();
                        }
                        while (mQueue.size() == 0) {                           
                            wait();
                        }
                        cmd = mQueue.removeFirst();
                        mSending = true;
                    }
                    fLastCommand = cmd;
                    mTimeOfLastSend = System.currentTimeMillis();
                    try {
                        mConnection.sendCommand(cmd); // we don't care about "void" reply
                    } catch (TimeoutException e) {
                        mException = e;
                    }
                } catch (InterruptedException e) {
                }
            }
        } catch (VMDisconnectedException e) {
            //trace("socket connection dropped");
        } finally {
            synchronized (this) {
                // Prevent sibling threads from hanging.
                mSending = false;
                notifyAll();
            }
        }
    }

}
