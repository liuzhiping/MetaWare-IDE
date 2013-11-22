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

import com.arc.seecode.internal.connect.CommandReceiverRouter;
import com.arc.seecode.internal.connect.Connection;

/**
 * Factory for the interfaces in this pacakge.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class ConnectionFactory {
    /**
     * Create a connection from a transport object.
     * @param transport the transport object through which the connection is made.
     * @param defaultTimeout the timeout, in milliseconds, for replies on this connection.
     * @return a new connection.
     */
    public static IConnection makeConnection(SocketTransport transport, int defaultTimeout){
        return new Connection(transport,defaultTimeout);
    }
    
    /**
     * Create a connection from a transport object with a default timeout.
     * @param transport the transport object through which the connection is made.
     * @return a new connection.
     */
    public static IConnection makeConnection(SocketTransport transport){
        return new Connection(transport);
    }
    
    public static ICommandReceiverRouter makeCommandReceiverRouter(){
        return new CommandReceiverRouter();
    }

}
