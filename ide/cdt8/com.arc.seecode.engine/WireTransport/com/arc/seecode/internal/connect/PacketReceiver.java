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

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.ListIterator;

import com.arc.mw.util.Log;
import com.arc.mw.util.Toggle;
import com.arc.seecode.connect.SocketTransport;
import com.arc.seecode.connect.TimeoutException;
import com.arc.seecode.connect.VMDisconnectedException;
import com.arc.seecode.scwp.ScwpCommandPacket;
import com.arc.seecode.scwp.ScwpPacket;
import com.arc.seecode.scwp.ScwpReplyPacket;

/**
 * This class implements a thread that receives packets from a
 * client process. The {@link AbstractPacketManager#start() start()}
 * method must be invoked subsequent to construction for the thread
 * to start running.
 * <P>
 * A received packet will either be a command (i.e. a method
 * to invoke), or a reply to command that was previously sent to
 * the client.
 * <P>
 * {@link #getCommand(long)} retrieves a command; 
 * {@link #getReply(int,long)} or {@link #getReply(ScwpCommandPacket)}
 * retrieve a reply to a specific command that was previously
 * sent.
 * <P>
 * The call to {@link #shutdown()} will terminate this
 * thread -- possibly generating an InterruptedException for
 * any waiting threads.
 * <P>
 * <b>Acknowledgment</b><br>
 * This class was adapted from the JDT <code>PacketReceiveManager</code>.
 * 
 * @author David Pickens
 * @since April 29, 2004
 *  
 */
public class PacketReceiver extends AbstractPacketManager {

    /** Generic timeout value for not blocking. */
    public static final int TIMEOUT_NOT_BLOCKING = 0;

    /** Generic timeout value for infinite timeout. */
    public static final int TIMEOUT_INFINITE = -1;
    
    private static final Toggle sTrace = Toggle.define("PACKETRECEIVER",false);

    /** Input Stream from Virtual Machine. */
    private InputStream fInStream;

    /** List of Command packets received from Virtual Machine. */
    private LinkedList<ScwpCommandPacket> fCommandPackets;

    /** List of Reply packets received from Virtual Machine. */
    private LinkedList<ScwpReplyPacket> fReplyPackets;

    private int fTimeout;


    /**
     * Create a new thread that receives packets from a client process.
     * The {@link #start()} method must be invoked for the
     * thread to start running.
     * @param transport the transport packet stream.
     * @param timeout time out in milliseconds for a reply.
     */
    public PacketReceiver(SocketTransport transport, int timeout) {
        super("PacketReceiver");

        try {
            fInStream = transport.getInputStream();
        } catch (IOException e) {
            setDisconnected(e);
            return;
        }
        fTimeout = timeout;
        fCommandPackets = new LinkedList<ScwpCommandPacket>();
        fReplyPackets = new LinkedList<ScwpReplyPacket>();
    }

    public PacketReceiver(SocketTransport transport){
        this(transport,30000);
    }
    /**
     * Thread's run method.
     */
    @Override
    public void run() {
        try {
            while (!isDisconnected()) {
                // Read a packet from the input stream.
                readAvailablePacket();
            }
        } catch (InterruptedIOException e) {
            setDisconnected(e);
        } catch (IOException e) {
            setDisconnected(e);
        }
    }

    /**
     * @return Returns a specified Command Packet from the Virtual Machine.
     */
    public synchronized ScwpCommandPacket getCommand(long timeToWait)
            throws InterruptedException, TimeoutException, VMDisconnectedException {
        ScwpCommandPacket packet = null;
        long remainingTime = timeToWait;
        long timeBeforeWait;
        long waitedTime;

        // Wait until command is available.
        while (!isDisconnected() && (packet = removeCommandPacket()) == null
                && (timeToWait < 0 || remainingTime > 0)) {
            timeBeforeWait = System.currentTimeMillis();
            waitForPacketAvailable(remainingTime);
            waitedTime = System.currentTimeMillis() - timeBeforeWait;
            remainingTime -= waitedTime;
        }

        // Check for an IO Exception.
        if (isDisconnected()) {
            String message;
            if (getDisconnectException() == null) {
                message = ConnectMessages
                        .getString("PacketReceiveManager.Got_IOException_from_Virtual_Machine_1"); //$NON-NLS-1$
            } else {
                String exMessage = getDisconnectException().getMessage();
                if (exMessage == null) {
                    message = MessageFormat
                            .format(
                                    ConnectMessages
                                            .getString("PacketReceiveManager.Got_{0}_from_Virtual_Machine_1"),
                                    getDisconnectException()
                                            .getClass().getName()); 
                } else {
                    message = MessageFormat
                            .format(
                                    ConnectMessages
                                            .getString("PacketReceiveManager.Got_{0}_from_Virtual_Machine__{1}_1"),              
                                            getDisconnectException().getClass()
                                                    .getName(), exMessage); 
                }
            }
            throw new VMDisconnectedException(message);
        }

        // Check for a timeout.
        if (packet == null) throw new TimeoutException();

        return packet;
    }

    /**
     * @return Returns a specified Reply Packet from the Virtual Machine.
     */
    public synchronized ScwpReplyPacket getReply(int id, long timeToWait) throws TimeoutException, VMDisconnectedException {
        ScwpReplyPacket packet = null;
        long remainingTime = timeToWait;
        long timeBeforeWait;
        long waitedTime;

        // Wait until reply is available.
        while (!isDisconnected() && (packet = removeReplyPacket(id)) == null
                && (timeToWait < 0 || remainingTime > 0)) {
            timeBeforeWait = System.currentTimeMillis();
            try {
                waitForPacketAvailable(remainingTime);
            } catch (InterruptedException e) {
            }
            waitedTime = System.currentTimeMillis() - timeBeforeWait;
            remainingTime -= waitedTime;
        }

        // Check for an IO Exception.
        if (isDisconnected())
                throw new VMDisconnectedException(
                        ConnectMessages
                                .getString("PacketReceiveManager.Got_IOException_from_Virtual_Machine_2")); //$NON-NLS-1$

        // Check for a timeout.
        if (packet == null) throw new TimeoutException("reply ID=" + id);

        return packet;
    }

    /**
     * @param commandPacket the packet for which we want a reply.
     * @return Returns a specified Reply Packet from the Virtual Machine.
     * @throws TimeoutException if the timeout expired before the reply was received.
     */
    public ScwpReplyPacket getReply(ScwpCommandPacket commandPacket) throws TimeoutException, VMDisconnectedException {
        return getReply(commandPacket.getId(), fTimeout);
    }
    
    /**
     * @param commandPacket the packet for which we want a reply.
     * @param timeout number of milliseconds to wait before timing out.
     * @return Returns a specified Reply Packet from the Virtual Machine.
     * @throws TimeoutException if the timeout expired before the reply was received.
     */
    public ScwpReplyPacket getReply(ScwpCommandPacket commandPacket, int timeout) throws TimeoutException, VMDisconnectedException {
        return getReply(commandPacket.getId(), timeout);
    }


    /**
     * Wait for an available packet from the Virtual Machine.
     */
    private void waitForPacketAvailable(long timeToWait)
            throws InterruptedException {
        if (timeToWait == 0)
            return;
        else if (timeToWait < 0)
            wait();
        else
            wait(timeToWait);
    }
    
    public synchronized boolean isCommandPending(){
        return fCommandPackets.size() > 0;
    }

    /**
     * @return Returns and removes a specified command packet from the command
     *         packet list.
     */
    private ScwpCommandPacket removeCommandPacket() {
        if (fCommandPackets.size() > 0)
            return fCommandPackets.removeFirst();
        return null;
    }

    /**
     * @return Returns a specified reply packet from the reply packet list.
     */
    private ScwpReplyPacket removeReplyPacket(int id) {
        ListIterator<ScwpReplyPacket> iter = fReplyPackets.listIterator();
        while (iter.hasNext()) {
            ScwpReplyPacket packet = iter.next();
            if (packet.getId() == id) {
                iter.remove();
                return packet;
            }
        }
        return null;
    }

    /**
     * Add a command packet to the command packet list.
     */
    private synchronized void addCommandPacket(ScwpCommandPacket packet) {
        if (isTracing()) trace("PacketReceiver: received command " + packet);
        
        fCommandPackets.add(packet);
        notifyAll();
    }

    /**
     * Add a reply packet to the reply packet list.
     */
    private synchronized void addReplyPacket(ScwpReplyPacket packet) {
        if (isTracing())
           trace("PacketReceiver: received reply " + packet);
        
        fReplyPackets.add(packet);
        notifyAll();
    }

    /**
     * Read a packet from the input stream and add it to the appropriate packet
     * list.
     */
    private void readAvailablePacket() throws IOException {
        // Read a packet from the Input Stream.
        ScwpPacket packet = ScwpPacket.read(fInStream);

        // Add packet to command or reply queue.
        if (packet instanceof ScwpCommandPacket) {
            addCommandPacket((ScwpCommandPacket)packet);
        }
        else {
            addReplyPacket((ScwpReplyPacket) packet);
        }
    }
    
    private static boolean isTracing(){
        return sTrace.on();
    }
    
    private void trace(String s){
        if (isTracing())
            Log.log("PacketReceiver",s);
    }
}
