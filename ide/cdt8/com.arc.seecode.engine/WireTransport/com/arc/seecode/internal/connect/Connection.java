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

import com.arc.seecode.connect.IConnection;
import com.arc.seecode.connect.ITimeoutCallback;
import com.arc.seecode.connect.SocketTransport;
import com.arc.seecode.connect.TimeoutException;
import com.arc.seecode.connect.VMDisconnectedException;
import com.arc.seecode.scwp.ScwpCommandPacket;
import com.arc.seecode.scwp.ScwpReplyPacket;


/**
 * @author David Pickens
 */
public class Connection implements IConnection {
    private SocketTransport mTransport;
    private PacketSender mSender;
    private PacketReceiver mReceiver;

    public Connection(SocketTransport transport, int defaultTimeout){
        //We're not connected to client; setup threads for
        // sending packets and for receiving.
        mTransport = transport;
        mSender = new PacketSender(mTransport);
        mReceiver = new PacketReceiver(mTransport,defaultTimeout);

        mSender.start();
        mReceiver.start();
    }
    
    public Connection(SocketTransport transport){
        this(transport,30000);
    }
    
    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     */
    
    @Override
    public void shutdown(){
        mSender.shutdown();
        mReceiver.shutdown();
        assert isDisconnected();
    }
    
   
    @Override
    public boolean isDisconnected(){
        return mReceiver.isDisconnected();
    }
    
   
    @Override
    public ScwpCommandPacket readCommand() throws InterruptedException, TimeoutException, VMDisconnectedException{
        return mReceiver.getCommand(-1);
    }
    
    @Override
    public boolean isCommandAvailable(){
        return mReceiver.isCommandPending();
    }
    
    @Override
    public ScwpReplyPacket sendCommand(ScwpCommandPacket packet) throws TimeoutException, VMDisconnectedException{
        mSender.sendPacket(packet);
        return mReceiver.getReply(packet);       
    }
    
    @Override
    public ScwpReplyPacket sendCommand(ScwpCommandPacket packet, int timeout, ITimeoutCallback callback) throws TimeoutException, VMDisconnectedException{
        mSender.sendPacket(packet);
        try {
            return mReceiver.getReply(packet,timeout);   
        }
        catch(TimeoutException x){
            if (callback == null)
                throw x;
            return mReceiver.getReply(packet,callback.getNewTimeout(timeout));          
        }
    }
    
    @Override
    public void sendReply(ScwpReplyPacket packet) throws VMDisconnectedException{
        mSender.sendPacket(packet);  
    }
}
