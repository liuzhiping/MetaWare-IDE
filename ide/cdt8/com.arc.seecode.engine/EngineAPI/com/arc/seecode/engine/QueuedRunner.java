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
package com.arc.seecode.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * A thread for queuing simple tasks that must run
 * in a thread other than the one from which the
 * task was invoked. 
 * <P>
 * The finalize methods that free up engine resources
 * uses this so that the finalizer thread isn't suspended
 * waiting for the engine.
 * <P>
 * When we move to JRE 1.5, we can probably replace this
 * class with an ExecutorService object.
 * @author David Pickens
 */
class QueuedRunner extends Thread {
    public interface ITerminateQuery{
        boolean isTerminated();
    }
    private ITerminateQuery fTerminateQuery;
    QueuedRunner(String name, ITerminateQuery terminateQuery){
        super(name);
        setDaemon(true);
        fTerminateQuery = terminateQuery;
    }
    
    static class KeyRun {
        KeyRun(Runnable run, Object key){
            this.run = run;
            this.key = key;
        }
        Runnable run;
        Object key;
    }
    
    private List<KeyRun> mQueue = new ArrayList<KeyRun>();
    
    private boolean isTerminated(){
        return fTerminateQuery.isTerminated();
    }
    
    @Override
    public void run(){
        if (Thread.currentThread() != this)
            return;
        while (!isTerminated()){
            KeyRun runners[];
            synchronized(mQueue){
                while (mQueue.size() == 0 && !isTerminated()){
                    try {
                        mQueue.wait(60000);  // Wake up ever so often to check if we're to terminated
                    } catch (InterruptedException e) {
                    }
                }
                if (isTerminated()) break;
                runners = mQueue.toArray(new KeyRun[mQueue.size()]);
                mQueue.clear();
            }
            for (int i = 0; i <  runners.length; i++){
                try {
                    runners[i].run.run();
                } catch (RuntimeException e) {
                    // Shouldn't get here!
                    e.printStackTrace();
                }              
            }           
        }
    }
    
    /**
     * Enqueue a method to be invoked in this thread.
     * <P>
     * If "key" is not null, then it identifies this run object. If there is 
     * an existing queued run object with the same key, it will be replaced.
     * <P>
     * <i>Caveat</i>: all exceptions need to be intercepted!
     * @param run
     */
    public void enqueue (Runnable run, Object key) {
        if (!isTerminated()) {
            synchronized (mQueue) {
                // If key'd, then replace any pending run object with same key.
                // This prevent queue from getting too large if engine hangs.
                if (key != null) {
                    for (KeyRun kr : mQueue) {
                        if (kr.key == key) {
                            kr.run = run;
                            return;
                        }
                    }
                }
                mQueue.add(new KeyRun(run, key));
                mQueue.notifyAll();
            }
        }
    }
    
}
