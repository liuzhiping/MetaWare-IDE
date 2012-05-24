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
package com.arc.mw.util;

/**
 * To avoid firing a frequently-occurring state change too frequently, we use
 * this timer class to buffer up changes and only fire them when the state
 * change persists for some length of time.
 * 
 * @author David Pickens
 */
public class BusyTimer extends Thread {
    private boolean mTerminated;
    private boolean mStopped;
    private int mTime; //milliseconds
    private ICallback mCallback;
    private Object mTimerLock = new Object();
    private int mStartCount = 0;

    public interface ICallback {
        public void busyEvent();
    }

    /**
     * Create timer in a stopped state. The method {@link #restartTimer()} must
     * be invoked to start the timer.
     * 
     * @param name
     *                the thread name.
     * @param millisec
     *                the number if millisecs that the timer should run before
     *                calling the callback method.
     * @param callback
     *                the callback method that is called when givem
     *                milli-seconds have elapsed.
     */
    public BusyTimer(String name, int millisec, ICallback callback) {
        super(name);
        mTime = millisec;
        mStopped = true;
        mCallback = callback;
    }

    /**
     * Terminate this timer altogether.
     */
    public void terminate() {
        mTerminated = true;
        interrupt();
    }
    /**
     * Restart the timer after being stopped.
     */
    public void restartTimer() {
        synchronized(mTimerLock){
            mStopped = false;
            mStartCount++;
            mTimerLock.notifyAll();
        }
    }
    /**
     * Stop the timer such that when it is restarted, it will begin counting
     * from the full timer amount.
     */
    public void stopTimer() {
        synchronized(mTimerLock) {
            if (!mStopped) {
                mStopped = true;
                mTimerLock.notifyAll();
            }
        }
    }

    /**
     * Return true if timer is ticking.
     */
    public boolean isTicking() {
        return !mStopped;
    }

    /**
     * Run loop. The callback is fired after the required elapsed time unless
     * interrupted, in which case the timer will start all over again.
     */
    @Override
    public void run() {
        while (!mTerminated) {
            int originalStartCount;
            try {
                synchronized (mTimerLock) {
                    // Wait for the time to start                    
                    while (mStopped)
                        mTimerLock.wait();
                    // Remember count in case it is stopped and
                    // restarted before we awaken.
                    originalStartCount = mStartCount;
                    mTimerLock.wait(mTime);
                }
                if (!mStopped && originalStartCount == mStartCount) {
                    mCallback.busyEvent();
                }
            }
            catch (InterruptedException x) {
            }
        }
    }
}
