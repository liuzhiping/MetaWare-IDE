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
package com.arc.seecode.connect;


import com.arc.seecode.scwp.ScwpCommandPacket;
import com.arc.seecode.scwp.ScwpReplyPacket;


public interface IConnection {

    /**
     * Shutdown the connection. From here on,
     * the call to {@link #isDisconnected()} should return
     * true.
     *
     */

    public void shutdown ();

    /**
     * Return whether or not this connection is still active.
     * @return whether or not this connection is still active.
     */
    public boolean isDisconnected ();
    
    /**
     * Return whether or not a command can be read without the caller blocking.
     * @return true if {@link #readCommand} can be called without the caller blocking.
     */
    public boolean isCommandAvailable();

    /**
     * Read a command from the client.
     * @return a command from the client.
     * @throws InterruptedException client thread was somehow
     * interrupted (meaning that it is being forceably terminated).
     * @throws TimeoutException a timeout occurred; the client connection
     * must have been dropped.
     */
    public ScwpCommandPacket readCommand () throws InterruptedException, TimeoutException, VMDisconnectedException;

    /**
     * Send a command to the server and receive a reply.
     * @param packet the command to send to the server.
     * @return a reply from the server.
     * @throws TimeoutException if the default timeout expired waiting for the reply.
     * the server to reply.
     */
    public ScwpReplyPacket sendCommand (ScwpCommandPacket packet) throws TimeoutException, VMDisconnectedException;

    /**
     * Send a command to the server and receive a reply.
     * @param packet the command to send to the server.
     * @param timeout the timeout, in milliseconds, that we're to wait for a reply.
     * @param callback a callback to give the client an opportunity to extend the timeout
     * if necessary, or <code>null</code>.
     * @return a reply from the server.
     * @throws TimeoutException if the timeout expired waiting for the reply.
     * the server to reply.
     */
    public ScwpReplyPacket sendCommand (ScwpCommandPacket packet, int timeout, ITimeoutCallback callback)
        throws TimeoutException, VMDisconnectedException;

    /**
     * Send a reply to the client in response to
     * the last command it sent.
     * @param packet the command to send to the server.
     * @throws VMDisconnectedException if a timeout expired waiting for
     * the server to reply.
     */
    public void sendReply (ScwpReplyPacket packet) throws VMDisconnectedException;

}
