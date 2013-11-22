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
package com.arc.seecode.internal.cmpd;

import java.io.IOException;

import com.arc.mw.util.Log;
import com.arc.mw.util.Toggle;
import com.arc.seecode.cmpd.ICMPDController;
import com.arc.seecode.connect.IConnection;
import com.arc.seecode.connect.TimeoutException;
import com.arc.seecode.connect.VMDisconnectedException;
import com.arc.seecode.engine.EngineDisconnectedException;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineTimeoutException;
import com.arc.seecode.scwp.ScwpCommandPacket;
import com.arc.seecode.scwp.ScwpReplyPacket;
import com.arc.seecode.serialize.IMethodSerializer;


public abstract class AbstractRemoteCmpdController implements ICMPDController {

    private int fTimeout;
    private IConnection fConnection;
    //NOTE: toggle is actually defined elsewhere, but would like to reference it.
    private static final Toggle sTrace = Toggle.lookup("CLIENT", true);
    
    protected AbstractRemoteCmpdController(IConnection connection, int timeout){
        fConnection = connection;
        fTimeout = timeout;
    }
    /**
     * Called by each of the API methods to invoke
     * 
     * @param ms
     * @param args
     * @return the result of the call.
     */
    protected Object invokeRemoteMethod(IMethodSerializer ms, Object[] args)
            throws EngineException {
        ScwpCommandPacket cmd = new ScwpCommandPacket(ScwpCommandPacket.CMPD,ms.getName());
        cmd.setData(ms.serializeArguments(args));
        if (isTracing()) {
            StringBuffer buf = new StringBuffer();
            buf.append("CMPD calling: " + ms.getName() + "(");
            for (int i = 0; i < args.length; i++) {
                if (i > 0) buf.append(", ");
                buf.append(args[i]);
            }
            buf.append(")");
            trace(buf.toString());
        }

        try {
           
            ScwpReplyPacket result = fConnection.sendCommand(cmd,fTimeout,null);
            Object resultObject = ms.deserializeResult(result.dataInStream());
            if (isTracing())
                trace("Client receives " + resultObject);

            return resultObject;
        } catch (TimeoutException e) {
            trace("timeout");
            throw new EngineTimeoutException("Engine Server timeout", e);
        } catch (IOException e) {
            trace("IOException");
            throw new EngineException("Engine Server transport error", e);
        } catch (VMDisconnectedException e) {
            trace("VMDisconnected");
            throw new EngineDisconnectedException("Socket connection to engine closed");
        }
    }
    
    protected static boolean isTracing() {
        return sTrace.on();
    }

    protected static void trace(String msg) {
        if (isTracing()) {
            Log.log("CLIENT", msg);
        }
    }
}
