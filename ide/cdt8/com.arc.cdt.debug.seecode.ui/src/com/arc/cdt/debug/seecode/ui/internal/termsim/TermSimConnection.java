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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;


class TermSimConnection extends Thread {
    /**
     * Size of buffer for processing data received from remote endpoint.
     */
    protected static final int BUFFER_SIZE = 2048;

    /**
     * Holds raw bytes received from the remote endpoint, prior to any TELNET
     * protocol processing.
     */
    protected byte[] rawBytes = new byte[BUFFER_SIZE];

    /**
     * This field holds the width of the Terminal screen in columns.
     */
    protected int width = 0;

    /**
     * This field holds the height of the Terminal screen in rows.
     */
    protected int height = 0;

    /**
     * This field holds a reference to the {@link ITerminalControl} singleton.
     */
    protected TermSimConnector terminalControl;

    /**
     * This method holds the Socket object for the TELNET connection.
     */
    protected Socket socket;

    /**
     * This field holds a reference to an {@link InputStream} object used to
     * receive data from the remote endpoint.
     */
    protected InputStream inputStream;

    /**
     * This field holds a reference to an {@link OutputStream} object used to
     * send data to the remote endpoint.
     */
    protected OutputStream outputStream;

    /**
     * UNDER CONSTRUCTION
     */
    protected boolean localEcho = true;

    /**
     * This constructor just initializes some internal object state from its
     * arguments.
     */
    public TermSimConnection(TermSimConnector terminalControl, Socket socket) throws IOException {
        super("TermSimConnection");

        Logger.log("entered"); //$NON-NLS-1$

        this.terminalControl = terminalControl;
        this.socket = socket;

        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
    }

    /**
     * Returns true if the TCP connection represented by this object is
     * connected, false otherwise.
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    /**
     * This method sets the terminal width and height to the supplied values. 
     */
    public void setTerminalSize(int newWidth, int newHeight) {
        Logger.log("Setting new size: width = " + newWidth + ", height = " + newHeight); //$NON-NLS-1$ //$NON-NLS-2$
        width = newWidth;
        height = newHeight;
    }

    /**
     * Returns true if local echoing is enabled for this TCP connection, false
     * otherwise.
     */
    public boolean localEcho() {
        return localEcho;
    }

    private void writeToTerminal(byte bytes[], int offset, int length) throws IOException {
        terminalControl.writeToTerminal(bytes,offset,length);
    }
    
    private static final String SET_COLOR_RED = new String(new char[]{0x1B,'[','3','1','m'});
    private static final String SET_COLOR_DEFAULT = new String(new char[]{0x1B,'[','0','m'});
    
    void writeInRed(String msg) throws IOException{
        byte bytes[] = (SET_COLOR_RED + msg + SET_COLOR_DEFAULT).getBytes();
        writeToTerminal(bytes,0,bytes.length);
    }
    
    /**
     * This method runs in its own thread. It reads raw bytes from the debugger
     * engine and passes them to  TerminalDisplay object for
     * display.
     */
    @Override
    public void run() {
        Logger.log("Entered"); //$NON-NLS-1$

        try {
            writeInRed("\rConnection established...\r\n");
            while (socket.isConnected()) {
                int nRawBytes = inputStream.read(rawBytes);

                if (nRawBytes == -1) {
                    // End of input on inputStream.
                    Logger.log("End of input reading from socket!"); //$NON-NLS-1$

                    // Announce to the user that the remote endpoint has closed the
                    // connection.

                    writeInRed("\rConnection closed\r\n"); //$NON-NLS-1$ 

                    // Tell the ITerminalControl object that the connection is
                    // closed.
                    terminalControl.setState(TerminalState.CLOSED);
                    break;
                } else {
                    Logger.log("Received " + nRawBytes + " bytes: '" + //$NON-NLS-1$ //$NON-NLS-2$
                            new String(rawBytes, 0, nRawBytes) + "'"); //$NON-NLS-1$

                    if (nRawBytes > 0) {
                        writeToTerminal(rawBytes, 0, nRawBytes);
                    }
                }
            }
        } catch (SocketException ex) {
            String message = ex.getMessage();

            // A "socket closed" exception is normal here. It's caused by the
            // user clicking the disconnect button on the Terminal view toolbar.
            // We're also seeing "Connection reset" that appears to be benign.

            if (message != null && !message.equals("socket closed") && !message.equals("Connection reset")) //$NON-NLS-1$
            {
                Logger.logException(ex);
            }
            // Tell the ITerminalControl object that the connection is
            // closed.
            terminalControl.setState(TerminalState.CLOSED);
            try {
                writeInRed("\rSocket dropped\n");
            }
            catch (IOException e) {
                Logger.logException(e);
            }
        } catch (Exception ex) {
            Logger.logException(ex);
        }
    }
}
