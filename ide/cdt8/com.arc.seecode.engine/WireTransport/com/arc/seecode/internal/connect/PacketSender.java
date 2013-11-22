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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.LinkedList;

import com.arc.mw.util.Log;
import com.arc.mw.util.Toggle;
import com.arc.seecode.connect.SocketTransport;
import com.arc.seecode.connect.VMDisconnectedException;
import com.arc.seecode.scwp.ScwpCommandPacket;
import com.arc.seecode.scwp.ScwpPacket;
import com.arc.seecode.scwp.ScwpReplyPacket;

/**
 * This class implements a thread that sends available packets to server
 * process. It works in tandemo with {@link PacketReceiver} thread, which
 * is running on the target process.
 * <P>
 * When a {@linkplain ScwpCommandPacket command packet} is
 * sent via {@link #sendPacket(ScwpPacket) sendPacket()},
 * the receiver is expected to reply with a {@linkplain  ScwpReplyPacket reply packet}
 * that will be read from the corresponding {@link PacketReceiver} thread.
 * <P>
 * When this process receives a command, it must send replies via
 * {@link #sendPacket(ScwpPacket) sendPacket()} also.
 * <P>
 * The method {@link #start()} must be called after
 * construction for this thread to start.
 * <P>
 * To shutdown this thread, the method {@link #shutdown()} is
 * called.
 * <P>
 * <b>Acknowledgment</b><br>
 * This class was adapted from the JDT <code>PacketSendManager</code>.
 * 
 * @author David Pickens
 * @since April 29, 2004
 *  
 */
public class PacketSender extends AbstractPacketManager {
    private static final Toggle sTrace = Toggle.define("PACKETSENDER",false);

    /** Output Stream to Virtual Machine. */
    private OutputStream fOutStream;

    /** List of packets to be sent to Virtual Machine */
    private LinkedList<ScwpPacket> fOutgoingPackets;

    /**
     * Create a new thread that send packets to the Virtual Machine.
     */
    public PacketSender(SocketTransport transport) {
        super("PacketSender");
        try {
            fOutStream = transport.getOutputStream();
            fOutgoingPackets = new LinkedList<ScwpPacket>();
        } catch (IOException e) {
            setDisconnected(e);
        }
    }

    /**
     * Thread's run method.
     */
    @Override
    public void run() {
        while (!isDisconnected()) {
            try {
                trace("Calling sendAvailablePackets");
                sendAvailablePackets();
                trace("Returned from sendAvailablePackets");
            } catch (InterruptedException e) {
                trace("interrupted");
            } catch (InterruptedIOException e) {
                trace("InterruptedIO");
            } catch (IOException e) {
                trace("IOException while sending (" + e + "); disconnecting");
                setDisconnected(e);
            }
        }
        trace("thread terminated");
    }

    /**
     * Add a packet to be sent to the Virtual Machine.
     */
    public synchronized void sendPacket(ScwpPacket packet) throws VMDisconnectedException {
        if (isDisconnected()) {
            String message;
            if (getDisconnectException() == null) {
                message = ConnectMessages
                        .getString("PacketSendManager.Got_IOException_from_Virtual_Machine_1"); //$NON-NLS-1$
            } else {
                String exMessage = getDisconnectException().getMessage();
                if (exMessage == null) {
                    message = MessageFormat
                            .format(
                                    ConnectMessages
                                            .getString("PacketSendManager.Got_{0}_from_Virtual_Machine_1"),
                                    getDisconnectException()
                                            .getClass().getName()); 
                } else {
                    message = MessageFormat
                            .format(
                                    ConnectMessages
                                            .getString("PacketSendManager.Got_{0}_from_Virtual_Machine__{1}_1"),    
                                            getDisconnectException().getClass()
                                                    .getName(), exMessage); 
                }
            }
            throw new VMDisconnectedException(message);
        }
        
        if (isTracing())
            trace("sending " + packet);

        // Add packet to list of packets to send.
        fOutgoingPackets.add(packet);

        // Notify PacketSendThread that data is available.
        notifyAll();
    }

    /**
     * Send available packets to the Virtual Machine.
     */
    private synchronized void sendAvailablePackets()
            throws InterruptedException, IOException {

        while (fOutgoingPackets.size() == 0) {
            trace("THREAD waiting");
            wait();
            trace("THREAD wokeup with size="+fOutgoingPackets.size());
        }

        // Put available packets on Output Stream.
        while (fOutgoingPackets.size() > 0) {
            
            ScwpPacket packet = fOutgoingPackets.removeFirst();

            // Buffer the output until a complete packet is available.
            BufferedOutputStream bufferOutStream = new BufferedOutputStream(
                    fOutStream, packet.getLength());
            if (isTracing()){
                trace("About to write " + packet);
            }
            packet.write(bufferOutStream);
            bufferOutStream.flush();
            if (isTracing()){
                trace("Write completed for " + packet);
            }
        }
    }
    
    private static boolean isTracing(){
        return sTrace.on();
    }
    
    private void trace(String s){
        if (isTracing())
            Log.log("PacketSender",s);
    }
    
    private static final int MAX_WAIT = 2000;
    private static final int WAIT_INCREMENT = 200;
    /* (non-Javadoc)
     * @see com.arc.seecode.connect.AbstractPacketManager#shutdown()
     */
    @Override
    public void shutdown() {
        // Before we shutdown, wait for outgoing 
        // queue to drain. We don't want to fail to
        // reply to pending packet.
        for (int i = 0; i < MAX_WAIT; i += WAIT_INCREMENT){
            synchronized (this) {
                if (fOutgoingPackets.size() == 0) {
                    break;
                }
            }
            try {
                Thread.sleep(WAIT_INCREMENT);
            } catch (InterruptedException e) {
            }
        }
        super.shutdown();
    }
}
