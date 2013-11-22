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
import java.net.SocketException;

import com.arc.seecode.connect.SocketTransport;


/**
 * Before we launch the engine process, this thread
 * does the "accept" operation on the port so that
 * the "connect" operation on the engine process will
 * work.
 * <P>
 * For some unknown reason, if the "connect" is done
 * prior to the "accept", it fails, even though the
 * "connect" has a reasonable timeout value.
 * @author David Pickens
 */
class SocketAcceptor implements Runnable {

    private SocketTransport mTransport;
    private Exception mException = null;
    private boolean mAccepted = false;

    SocketAcceptor(SocketTransport transport){
        mTransport = transport;
    }
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            mTransport.setAcceptTimeout(60 * 1000);
            mTransport.accept();
            synchronized(this){
                mAccepted = true;
                notifyAll();
            }
        } catch (Exception e) {
            synchronized(this){
                mException = e;
                notifyAll();
            }
        }
    }
    
    synchronized void waitForAccept() throws SocketException, IOException, InterruptedException{
        while (!mAccepted && mException == null){
            wait();
        }
        if (mException != null){
            if (mException instanceof SocketException){
                throw (SocketException)mException;
            }
            if (mException instanceof IOException){
                throw (IOException)mException;
            }
            if (mException instanceof InterruptedException){
                throw (InterruptedException)mException;
            }
            throw (RuntimeException)mException;
        }
    }

}
