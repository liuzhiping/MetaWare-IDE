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
package com.arc.cdt.debug.seecode.core.launch;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;

import com.arc.seecode.connect.SocketTransport;

/**
 * Launches the "server" process that wraps the MetaWare debugger engine.
 * The IDE communicates to it by means of <i>remote method calls</i> from the EngineInterface 
 * object.
 * <P>
 * {@link #makeConnection} is called for each connection. There is one connection per CMPD process.
 * Otherwise, there is just one connection. Then {@link #launch} is invoked to spawn the process.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface IServerLauncher {

    /**
     * Make a connection and return the TCP/IP transport that will be used to communicate
     * with it. For CMPD, there will be one call per CMPD process.
     * <P>
     * Connections are made prior to calling {@link #launch}.
     * @return returns the socket transport from which the caller will accept a connection
     * after the launch.
     * @throws IOException if an error occurred establishing the socket.
     */
    public SocketTransport makeConnection () throws IOException;
    
    /**
     * When launching the "server", arrange for it to be attachable by the Eclipse JDT debugger at
     * the given port.
     * @param port the TCP/IP port at which the Eclipse JDT debugger is expected to connect.
     */
    public void setDebuggerAttachPort(int port);
    
    /**
     * Set arguments to be passed to the server process, typically toggles such as "-X<i>toggleName</i>".
     * @param args additional arguments to pass to the server process.
     */
    public void setEngineToggles(List<String> args);
    
    /**
     * Set the CMPD process ID numbers. If non-CMPD, then this will be an single
     * element array containing "1".
     * @param pid the array of CMPD process ID numbers.
     */
    public void setPids(int pid[]);

    /**
     * After the various <code>set...</code> and <code>make...</code> methods have been called, this method is called to
     * launch the server process that "wraps" the debugger engine. The transport objects that
     * were previously returned by {@link #makeConnection} will be the channel by which <i>remove method calls</i>
     * are used to communicate with the server.
     * <P>
     * The <code>cpu</code> designation identifies the particular debugger installation to invoke. 
     * For example, "ac" will invok "scac"; "arm" will invoke "scarm". For CMPD, "ac" will suffice.
     * @param cpu the target CPU (e.g. "ac","arm",etc.) or "ac" if we have a CMPD session.
     * @param workingDirectory the working directory in which the server will be launched from.
     * @param environment the environment, or <code>null</code> if the callers environment is used.
     * @return the process object.
     * @throws IOException if an error occurred in invoking the server process.
     */
    public Process launch (String cpu, File workingDirectory, String[] environment) throws IOException, CoreException;

}
