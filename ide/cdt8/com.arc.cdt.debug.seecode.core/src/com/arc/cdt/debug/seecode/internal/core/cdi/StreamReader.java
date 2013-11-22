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
package com.arc.cdt.debug.seecode.internal.core.cdi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Reads input streams from a process and pipes them
 * to an output stream.
 * <P>
 * Care is taken so that this thread can be terminated
 * before exhausting the input so that we can reroute
 * the input to the debug console when it is created.
 * @author David Pickens
 */
class StreamReader implements Runnable {
    private InputStream mInput;
    private OutputStream mOutput;
    private boolean mTerminated = false;
    private Thread mThread = null;
    private static final int MAX_DRAIN_INPUT_WAIT = 4000; // 4 seconds
    private static final int DRAIN_INPUT_WAIT = MAX_DRAIN_INPUT_WAIT/8;
    private int mBytesRead = 0;

    StreamReader(InputStream input, OutputStream output){
        mInput = input;
        mOutput = output;
    }
    
    /**
     * Drain input in preparation for closing it.
     */
    synchronized void drainInput() {
        long startTime = System.currentTimeMillis();
        int elapsedTime = 0;
        while (elapsedTime < MAX_DRAIN_INPUT_WAIT) {
            int bytesReadAtStartOfDrain = mBytesRead;
            try {
                wait(Math.min(DRAIN_INPUT_WAIT,MAX_DRAIN_INPUT_WAIT-elapsedTime));   
                if (bytesReadAtStartOfDrain == mBytesRead) break;
            }
            catch (InterruptedException e) {              
            }
            elapsedTime = (int)(System.currentTimeMillis() - startTime);
        }
    }
    
    synchronized void terminate() {
        try {
            mOutput.flush();
        } catch (IOException e) {
        }
        mTerminated = true;
        notifyAll();
        if (mThread != null) mThread.interrupt();
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        byte[] buffer = new byte[4096];
        mThread = Thread.currentThread();
        try {
            while (!mTerminated) {
                int cnt = Math.min(mInput.available(), buffer.length);
                if (cnt == 0) {
                    if (mTerminated) break;
                    synchronized (this) {
                        // Poll so that we can terminate
                        // before the inputstream is consumed.
                        wait(200);
                    }
                } else {
                    //Read only the number of bytes
                    // available so as not to block.
                    // We need to be able to terminate
                    // this thread at any point.
                    cnt = mInput.read(buffer, 0, cnt);
                    if (cnt < 0 || cnt == 0 && mTerminated) 
                        break;
                    mBytesRead += cnt;
                    mOutput.write(buffer, 0, cnt);
                    mOutput.flush();
                }
            }
        } catch (InterruptedException e) {
            // Terminating
        } catch (IOException e) {
            // EOF?
        }

    }
    


}
