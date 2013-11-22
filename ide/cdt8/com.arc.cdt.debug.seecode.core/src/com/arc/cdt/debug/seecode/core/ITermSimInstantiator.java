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
package com.arc.cdt.debug.seecode.core;

import com.arc.cdt.debug.seecode.core.cdi.ICDISeeCodeSession;


/**
 * Callback for the debugger to create a Terminal Simulator view if it has detected that the
 * user has requested such.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface ITermSimInstantiator {
    /**
     * Show and/or create the terminal simulator view and connect it to the debugger at the given
     * port.
     * @param session the debugger session.
     * @param tcpIpPort the TCP/IP port that through which the debugger engine
     * will communicate.
     * @param uartPort the UART port number; there can be more than one terminal.
     * @throws Exception 
     */
    public void createTermSimView(ICDISeeCodeSession session, int tcpIpPort, int uartPort) throws Exception;
    
    /**
     * A session has been restarted. Reconnect terminal views by re-estabilishing the TCP/IP
     * connection.
     * @param session the session being restarted.
     * @throws Exception
     */
    public void reconnectTermSimViews(ICDISeeCodeSession session) throws Exception;
}
