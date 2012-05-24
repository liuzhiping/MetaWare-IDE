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
package com.arc.seecode.internal.connect;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.BitSet;

import com.arc.mw.util.Log;
import com.arc.mw.util.Toggle;
import com.arc.seecode.connect.ICommandReceiverRouter;
import com.arc.seecode.connect.IConnection;
import com.arc.seecode.connect.TimeoutException;
import com.arc.seecode.connect.VMDisconnectedException;
import com.arc.seecode.engine.IRunner;
import com.arc.seecode.scwp.ScwpCommandPacket;
import com.arc.seecode.scwp.ScwpReplyPacket;
import com.arc.seecode.serialize.MethodSerializationHandler;

/**
 * Thread that received commands from the debugger engine and then routes them to the 
 * approprate method.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class CommandReceiverRouter implements ICommandReceiverRouter, Runnable {

    private IConnection fConnection;

    private MethodSerializationHandler fHandlerTable[] = new MethodSerializationHandler[MAX_OBJECT_IDS];

    private BitSet fInUIThread = new BitSet(MAX_OBJECT_IDS);

    private ICommandReceiverRouter.IErrorLogger fLogger;

    private ByteArrayOutputStream fUIReplyBuf;

    private DataOutputStream fUIReplyOut;

    private IRunner fUIThreadRunner;

    private String fThreadName;

    private static final Toggle sTrace = Toggle.define("COMMANDROUTER", false);

    private static final byte[] EMPTY_REPLY = new byte[0];
    
    private static final String[] CALLBACK_NAME = new String[ScwpCommandPacket.REQUIRED_CHANNELS];
    static {
        CALLBACK_NAME[ScwpCommandPacket.ENGINE] = "ENGINE";
        CALLBACK_NAME[ScwpCommandPacket.BREAKPOINT_OBSERVER] = "BP";
        CALLBACK_NAME[ScwpCommandPacket.CUSTOM_DISPLAY_MONITOR] = "DISPLAY";
        CALLBACK_NAME[ScwpCommandPacket.ENGINE_OBSERVER] = "EVENT";
    }

    public CommandReceiverRouter() {
    }

    @Override
    public void setMethodHandler (int objectID, MethodSerializationHandler handler, boolean inUIThread) {
        if (objectID < 0 || objectID >= MAX_OBJECT_IDS) {
            throw new IllegalArgumentException("Too many objects to route: " +
                objectID +
                "; limit is " +
                MAX_OBJECT_IDS);
        }
        if (fHandlerTable[objectID] != null && fHandlerTable[objectID] != handler) {
            throw new IllegalArgumentException("object ID already in use: " + objectID);
        }
        fHandlerTable[objectID] = handler;
        fInUIThread.set(objectID, inUIThread);
    }

    @Override
    public void start (IConnection connection, IRunner uiThreadRunner, IErrorLogger logger, String threadName) {
        if (connection == null) throw new IllegalArgumentException("connection is null");
        if (uiThreadRunner == null) throw new IllegalArgumentException("thread runner is null");
        if (logger == null) throw new IllegalArgumentException("logger is null");
        fConnection = connection;
        fLogger = logger;
        fUIThreadRunner = uiThreadRunner;
        fThreadName = threadName != null?threadName:"MethodServiceThread";
        Thread t = new Thread(this, fThreadName);
        t.setDaemon(true);
        t.start();
    }

    /**
     * Invoke the appropriate method that the given command references. We pass the reply buffer so as to avoid having
     * to allocate it afresh on each invocation.
     * @param cmd the command to be decoded.
     * @param inUIThread of true, we're being invoked from the special UI thread.
     * @param replyBuf where to place the reply.
     * @param replyOut the wrapper around replyBuf.
     */

    private void routeCommand (
        final ScwpCommandPacket cmd,
        boolean inUIThread,
        final ByteArrayOutputStream replyBuf,
        final DataOutputStream replyOut) {
        try {
            ScwpReplyPacket reply = new ScwpReplyPacket(cmd.getId());
            
            final MethodSerializationHandler handler = fHandlerTable[cmd.getObject()];
            if (handler != null) {
                if (!fInUIThread.get(cmd.getObject()) || inUIThread) {
                    if (isTracing()) {
                        trace("[" + cmd.getObject()/ScwpCommandPacket.REQUIRED_CHANNELS + "]" + CALLBACK_NAME[cmd.getObject()%ScwpCommandPacket.REQUIRED_CHANNELS] + ": " + cmd.getMethodName());              
                    }
                    replyBuf.reset();
                    try {
                        handler.invoke(cmd.getMethodName(), cmd.dataInStream(), replyOut);
                    }
                    finally {
                        if (replyBuf.size() > 0)
                            reply.setData(replyBuf.toByteArray());
                        else
                            // Avoid overhead of building
                            // empty reply from ByteOutputStream
                            reply.setData(EMPTY_REPLY);
                        fConnection.sendReply(reply);
                    }
                }
                else {

                    Runnable runnable;
                    boolean async = handler.getSerializerFor(cmd.getMethodName()).isAsynchronous();
                    if (async) {
                        runnable = new Runnable() {

                            @Override
                            public void run () {
                                routeCommand(cmd, true, fUIReplyBuf, fUIReplyOut);
                            }
                        };
                    }
                    else {
                        runnable = new Runnable() {

                            @Override
                            public void run () {
                                routeCommand(cmd, true, replyBuf, replyOut);
                            }
                        };
                    }
                    if (isTracing()) {
                        trace("[" + cmd.getObject()/ScwpCommandPacket.REQUIRED_CHANNELS + "]" + CALLBACK_NAME[cmd.getObject()%ScwpCommandPacket.REQUIRED_CHANNELS] + ": (async=" + async+") " + cmd.getMethodName());              
                    }
                    fUIThreadRunner.invoke(runnable, async);
                }
            }
            else {
                fLogger.logException("Command router received bogus ID: " + cmd.getObject(), null);
                reply.setData(EMPTY_REPLY);
                fConnection.sendReply(reply);
            }
        }
        catch (InterruptedException e) {
            fLogger.logException(fThreadName + " interrupted", e);
        }
        catch (TimeoutException e) {
            fLogger.logException(fThreadName + " timed out; restarting", e);
        }
        catch (VMDisconnectedException e) {
            assert fConnection.isDisconnected();
            // engine died.
        }
        catch (RuntimeException e) {
            fLogger.logException("RuntimeException", e);
        }
        catch(Exception e){
            fLogger.logException("Exception", e);
        }
        catch (Error e) {
            fLogger.logException("Error", e);
        }
        catch (Throwable e) {
            // Shouldn't get here
            e.printStackTrace();
        }
    }

    @Override
    public void run () {
        fUIReplyBuf = new ByteArrayOutputStream();
        fUIReplyOut = new DataOutputStream(fUIReplyBuf);
        ByteArrayOutputStream replyBuf = new ByteArrayOutputStream();
        DataOutputStream replyOut = new DataOutputStream(replyBuf);
        try {
            while (!fConnection.isDisconnected()) {
                try {
                    ScwpCommandPacket cmd = fConnection.readCommand();
                    routeCommand(cmd, false, replyBuf, replyOut);
                }
                catch (InterruptedException e) {
                    fLogger.logException(fThreadName + " interrupted", e);
                }
                catch (TimeoutException e) {
                    fLogger.logException(fThreadName + " timed out; restarting", e);
                }
                catch (VMDisconnectedException e) {
                    assert fConnection.isDisconnected();
                }

            }
        }
        catch (Throwable e) {
            // @todo Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected static boolean isTracing () {
        return sTrace.on();
    }

    protected static void trace (String msg) {
        if (isTracing()) {
            Log.log("COMMANDROUTER", msg);
        }
    }
}
