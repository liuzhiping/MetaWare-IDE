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
package com.arc.cdt.debug.seecode.ui.internal.termsim;

import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;


public class TermSimWorker extends Thread {
    private final ITerminalControl fControl;
    private final TermSimConnector fConn;
    private int fPort;
    private boolean fTerminated = false;
    protected TermSimWorker(TermSimConnector conn,ITerminalControl control, int tcpPort) {
        super("TermSimInstantiator");
        fControl = control;
        fConn = conn;
        fPort = tcpPort;
        fControl.setState(TerminalState.CONNECTING);
        fControl.setMsg("");
    }
    public void terminate(){
        fTerminated = true;
    }
    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(fPort);
            serverSocket.setPerformancePreferences(1,100,0);
            serverSocket.setSoTimeout(5 * 1000);
            Socket socket = null;
            //long expireTime = System.currentTimeMillis() + 45000; // TODO: make this configurable
            while (!fTerminated && socket == null) {
                try {
                     socket = serverSocket.accept();
                }
                catch(SocketTimeoutException x){
                    // Don't time out! The user may not have resumed from the initial stop at main.
                    // This loop will terminate if either a socket connects, or the session terminates.
//                    if (System.currentTimeMillis() >= expireTime)
//                        throw x;
                }
            }
            if (fTerminated || socket==null) return;
            socket.setTcpNoDelay(true);
            socket.setPerformancePreferences(0,100,1); // maximize importance of latency

            // This next call causes reads on the socket to see TCP urgent data
            // inline with the rest of the non-urgent data.  Without this call, TCP
            // urgent data is silently dropped by Java.  This is required for
            // TELNET support, because when the TELNET server sends "IAC DM", the
            // IAC byte is TCP urgent data.  If urgent data is silently dropped, we
            // only see the DM, which looks like an ISO Latin-1 '�' character.

            socket.setOOBInline(true);
            
            fConn.setSocket(socket);

            TermSimConnection connection=new TermSimConnection(fConn, socket);
            socket.setKeepAlive(true);
            fConn.setTermSimConnection(connection);
            connection.start();
            fControl.setState(TerminalState.CONNECTED);
            fConn.setTerminalTitle();
        } catch (UnknownHostException ex) {
            String txt="Unknown host: " + ex.getMessage(); //$NON-NLS-1$
            connectFailed(txt,"Unknown host: " + ex.getMessage() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (SocketTimeoutException socketTimeoutException) {
            connectFailed(socketTimeoutException.getMessage(), 
                "Waring: Launch Configuration has requested a terminal simulator to be\n"+
                "activated, but the application doesn't appear to activate the UART simulator.\n"+
                "Connection time out occurred.");
        } catch (ConnectException connectException) {
            connectFailed(connectException.getMessage(),"Connection refused!"); //$NON-NLS-1$
        } catch (Exception exception) {
            Logger.logException(exception);

            connectFailed(exception.getMessage(),""); //$NON-NLS-1$
        }
        finally {
            if (serverSocket != null)
                try {
                    serverSocket.close();
                }
                catch (IOException e) {
                    Logger.logException(e);
                    connectFailed(e.getMessage(),""); //$NON-NLS-1$
                }
            
        }
    }

    private void connectFailed(String terminalText, String msg) {
        Logger.log(terminalText);
        fControl.displayTextInTerminal(terminalText);
        fConn.cleanSocket();
        fControl.setMsg(msg);
        fControl.setState(TerminalState.CLOSED);
    }
}
