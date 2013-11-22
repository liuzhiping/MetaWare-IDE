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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

import com.arc.mw.util.Log;
import com.arc.mw.util.Toggle;
import com.arc.seecode.internal.connect.ConnectMessages;

public class SocketTransport {
    
    private static final Toggle sTrace = Toggle.define("SOCKETS",false);

    public static final int MIN_PORTNR = 0;

    public static final int MAX_PORTNR = 65535;

    /** Handshake bytes used just after connecting VM. */
    private static final byte[] handshakeBytes = "SCWP-Handshake".getBytes(); //$NON-NLS-1$

    /** Socket on which VM is connected. */
    private Socket fSocket = null;

    /** ServerSocket used to listen to connecting VMs. */
    private ServerSocket fServerSocket = null;

    /**
     * Constructs new SocketTransportImpl.
     */
    public SocketTransport() {
    }
    
    private static final int ATTACH_TIMEOUT = 20*1000;

    /**
     * Establishes a client connection to a virtual machine.
     */
    public void attach(String hostname, int port) throws IOException {
        fSocket = new Socket();
        fSocket.setTcpNoDelay(true); // to improve latency
        fSocket.setPerformancePreferences(1,100,0);
        InetSocketAddress addr = new InetSocketAddress(hostname, port);
        trace("Client attempt to connect to server at port " + port);
        fSocket.connect(addr,ATTACH_TIMEOUT);
        trace("Client connected!");
        PerformHandshake();
    }

    /**
     * Listens for connections initiated by target VMs.
     */
    public void listen(int port) throws IOException {
        closeListen();
        fServerSocket = new ServerSocket(port);
        fServerSocket.setPerformancePreferences(1,100,0);
        fServerSocket.setSoTimeout(15 * 1000);
    }

    /**
     * @return Returns port number that is listened to.
     */
    public int listeningPort() {
        if (fServerSocket != null)
            return fServerSocket.getLocalPort();
		return 0;
    }

    /**
     * Closes socket connection.
     */
    public void closeListen() throws IOException {
        if (fServerSocket == null) return;

        fServerSocket.close();
        fServerSocket = null;
    }

    /**
     * Accepts connections initiated by target VMs.
     */
    public void accept() throws IOException {
        if (fServerSocket == null) { return; }
        trace("Server attaching to client!");
        fSocket = fServerSocket.accept();
        fSocket.setTcpNoDelay(true);
        fSocket.setPerformancePreferences(0,100,1); // maximize importance of latency
        trace("...attach successful!");
        PerformHandshake();
    }

    /**
     * Sets timeout on accept.
     */
    public void setAcceptTimeout(int timeout) throws SocketException {
        if (fServerSocket == null) { return; }
        fServerSocket.setSoTimeout(timeout);
    }

    /**
     * @return Returns true if we have an open connection.
     */
    public boolean isOpen() {
        return fSocket != null;
    }

    /**
     * Closes socket connection.
     */
    public void close() {
        if (fSocket == null) return;

        try {
            fSocket.close();
        } catch (IOException e) {
        } finally {
            fSocket = null;
        }
    }

    /**
     * @return Returns InputStream from Virtual Machine.
     */
    public InputStream getInputStream() throws IOException {
        return fSocket.getInputStream();
    }

    /**
     * @return Returns OutputStream to Virtual Machine.
     */
    public OutputStream getOutputStream() throws IOException {
        return fSocket.getOutputStream();
    }

    /**
     * Performs handshake protocol.
     */
    private void PerformHandshake() throws IOException {
        trace("About write handshake");
        DataOutputStream out = new DataOutputStream(fSocket.getOutputStream());
        out.write(handshakeBytes);
        trace("...just sent handshake bytes");

        try {
            DataInputStream in = new DataInputStream(fSocket.getInputStream());
            byte[] handshakeInput = new byte[handshakeBytes.length];
            in.readFully(handshakeInput);
            if (!Arrays.equals(handshakeInput, handshakeBytes))
                    throw new IOException(
                            ConnectMessages
                                    .getString("SocketTransportImpl.Incorrect_handshake_reply_received___1")
                                    + new String(handshakeInput)); 
        } catch (EOFException e) {
            throw new IOException(
                    ConnectMessages
                            .getString("SocketTransportImpl.EOF_encoutered_during_handshake_2")); //$NON-NLS-1$
        }
    }
    
    private static boolean isTracing(){
        return sTrace.on();
    }
    
    private static void trace(String msg){
        if (isTracing())
            Log.log("SOCKETS",msg);
    }
}
