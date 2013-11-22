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
package com.arc.seecode.internal.connect;

/**
 * This class implements threads that receive/send packets from/to the Virtual
 * Machine.
 *  
 */
abstract class AbstractPacketManager implements Runnable {

    /**
     * Thread that handles the communication the other way (e.g. if we are
     * sending, the receiving thread).
     */
    private Thread fPartnerThread;

    private boolean mDisconnected = false;

    private Exception mDisconnectException = null;

    /**
     * Creates new PacketManager.
     */
    protected AbstractPacketManager(String threadName) {
        fPartnerThread = new Thread(this,threadName);
        fPartnerThread.setDaemon(true);
    }
    
    public void start(){
        fPartnerThread.start();
    }

    protected void setDisconnected() {
        mDisconnected = true;
        if (Thread.currentThread() != fPartnerThread)
            fPartnerThread.interrupt();
        else
            synchronized(this){
                // Make thread waiting for command
                // queue to wake up.
                notifyAll();
            }
    }

    protected void setDisconnected(Exception x) {
        mDisconnectException = x;
        setDisconnected();
    }

    public boolean isDisconnected() {
        return mDisconnected;
    }

    public Exception getDisconnectException() {
        return mDisconnectException;
    }

    public void shutdown() {
        setDisconnected();
        fPartnerThread.interrupt();
    }

}
