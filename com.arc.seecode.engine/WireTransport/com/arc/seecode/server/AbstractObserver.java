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
package com.arc.seecode.server;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.arc.mw.util.Log;
import com.arc.mw.util.Toggle;
import com.arc.seecode.engine.IEngineAPI;
import com.arc.seecode.scwp.ScwpCommandPacket;
import com.arc.seecode.serialize.IMethodFilter;
import com.arc.seecode.serialize.MethodSerializationHandler;

/**
 * This is an engine callback that invokes methods in the client (GUI). However,
 * we have a deadlock problem: When any method in this class is called, the
 * {@link IEngineAPI}object is locked. If the corresponding method in the
 * client sends back a command (e.g., read registers), the packet receiver
 * thread will deadlock.
 * <P>
 * Thus, we must dispatch things in a separate thread (via the
 * {@link CallbackThread#enqueue(ScwpCommandPacket)}method.
 * 
 * @author David Pickens
 */
abstract class AbstractObserver{

    private static final Toggle sTrace = Toggle.define("CALLBACK", false);

    private MethodSerializationHandler mDispatcher;

    private int mObjectID;
    
    private CallbackThread mCallbackThread;

    protected AbstractObserver(CallbackThread callbackThread, int objectID) {
        mCallbackThread = callbackThread;
        mObjectID = objectID;
        mDispatcher = new MethodSerializationHandler(this, null,
                new IMethodFilter() {

                    @Override
                    public boolean includeMethod(Method method) {
                        return (method.getModifiers() & Modifier.PUBLIC) != 0;
                    }
                }, null, null);

    }
    
    protected ScwpCommandPacket makePacket(String methodName, Object[] args){
        byte[] buf = mDispatcher.serializeArguments(methodName, args);
        ScwpCommandPacket cmd = new ScwpCommandPacket(mObjectID, methodName);
        cmd.setData(buf);
        return cmd;
    }
    
    /**
     * Attempt to update an outgoing packet before it is actually sent. Return true
     * if successful.
     * @param cmd
     * @param methodName
     * @param args
     * @return true if successfully updated.
     */
    protected boolean updatePacket(ScwpCommandPacket cmd, String methodName, Object[] args){
        byte[] buf = mDispatcher.serializeArguments(methodName, args);
        return cmd.updateDataIfPossible(buf);
    }
    
    /**
     * Remove a enqueued packet if it hasn't already been sent.
     * @param cmd the command to remove.
     * @return true if successfully removed.
     */
    protected boolean removePacket(ScwpCommandPacket cmd){
        return cmd != null && !cmd.isSent() && mCallbackThread.remove(cmd);
    }

 

    protected ScwpCommandPacket dispatch(String methodName, Object[] args) {
        if (isTracing()) {
            StringBuffer buf = new StringBuffer();
            buf.append(methodName);
            buf.append("(obj=");
            buf.append(mObjectID);
            buf.append(",");
            for (int i = 0; i < args.length; i++) {
                if (i > 0) buf.append(',');
                buf.append(args[i]);
            }
            buf.append(')');
            buf.append("  queue size=");
            buf.append(mCallbackThread.getQueueSize());
            trace(buf.toString());
        }
        ScwpCommandPacket cmd = makePacket(methodName,args);
        // Enqueue so as to be invoked in separate thread
        // that does not have the engine locked.
        mCallbackThread.enqueue(cmd);
        return cmd;
    }

 

    protected static boolean isTracing() {
        return sTrace.on();
    }

    protected static void trace(String msg) {
        if (isTracing()) {
            Log.log("callback", msg);
        }
    }
}

