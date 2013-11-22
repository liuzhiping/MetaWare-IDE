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
package com.arc.seecode.cmpd;

import com.arc.seecode.connect.IConnection;
import com.arc.seecode.internal.cmpd.CMPDController;
import com.arc.seecode.internal.cmpd.RemoteCmpdController;


public class CMPDFactory {

    /**
     * Connect to the CMPD controller that is wrapping the SeeCode engine that is
     * running as a separate process.
     * @param connection connection to SeeCode engine.
     * @param timeout timeout in milliseconds for a caller to wait.
     * @return a proxy for communicating witn the SeeCode engine running as a separate
     * process.
     */
    public static ICMPDController createRemote(IConnection connection, int timeout){
        return new RemoteCmpdController(connection, timeout);       
    }
    
    /**
     * Create a local CMPD controller for a debugger engine running in the same
     * process.
     * @return an instance of a CMPD controller that references the debugger engine within
     * the same process.
     */
    public static ICMPDController createLocal(){
        return CMPDController.Create();
    }
}
