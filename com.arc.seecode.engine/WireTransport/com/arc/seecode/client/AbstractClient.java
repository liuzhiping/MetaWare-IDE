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
package com.arc.seecode.client;

import java.io.IOException;

import com.arc.mw.util.Log;
import com.arc.mw.util.Toggle;
import com.arc.seecode.connect.ICommandReceiverRouter;
import com.arc.seecode.connect.IConnection;
import com.arc.seecode.connect.ITimeoutCallback;
import com.arc.seecode.connect.TimeoutException;
import com.arc.seecode.connect.VMDisconnectedException;
import com.arc.seecode.engine.EngineDisconnectedException;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineTimeoutException;
import com.arc.seecode.engine.IBreakpointObserver;
import com.arc.seecode.engine.ICustomDisplayCallback;
import com.arc.seecode.engine.IEngineAPI;
import com.arc.seecode.engine.IEngineAPIObserver;
import com.arc.seecode.engine.IRunner;
import com.arc.seecode.engine.JavaFactory;
import com.arc.seecode.engine.type.ITypeFactory;
import com.arc.seecode.scwp.ScwpCommandPacket;
import com.arc.seecode.scwp.ScwpReplyPacket;
import com.arc.seecode.serialize.IMethodSerializer;
import com.arc.seecode.serialize.MethodSerializationHandler;

/**
 * Instance of this class becomes a client for talking to the SeeCode engine
 * that is running as a separate process.
 * 
 * @author David Pickens
 */
public abstract class AbstractClient implements IEngineAPI {
    
    private static final int REQUIRED_CHANNELS = ScwpCommandPacket.REQUIRED_CHANNELS;

    private IConnection mConnection;

    private IEngineAPIObserver mEngineObserver;

    private JavaFactory mJavaFactory;

    private ITypeFactory mTypeFactory;
    
    /**
     * Number of milliseconds for timeout when waiting for reply from engine when a 
     * program is being loaded.
     */
    protected int mLoadTimeout;  
    
    private transient int mTimeout;
    /**
     * Number of milliseconds for timeout when waiting for reply from engine.
     */
    private int mDefaultTimeout;

    protected ITimeoutCallback mLoadTimeoutCallback;
    private transient ITimeoutCallback mTimeoutCallback;

    private ICommandReceiverRouter fRouter;

    private int mCmpdNumber;

    private static final Toggle sTrace = Toggle.define("CLIENT", false);
    
    static class AnException extends RuntimeException {

        AnException(Throwable cause) {
            super(cause);
        }
    }
    /**
     * Create a client to talk to the engine.
     * 
     * @param connection
     *            the engine connection.
     * @param router routes the callbacks from the engine.
     * @param jFactory factory for creating argument objects that are passed to the
     * server.
     * @param typeFactory factory for making type objects if they are returned back as results.
     * @param displayCallbackRunner the callback we use to run engine calls into the 
     *  <code>ICustomDisplayMonitor</code> interface; typically to run things in UI thread.
     *  @param defaultTimeout the default timeout in milliseconds when waiting for a reply
     *  from the engine.
     *  @param loadTimeout timeout for when the engine is loading a program, which is typically
     *  larger than the default timeout.
     *  @param loadTimeoutCallback a hook by which the client has an opportunity to extend the
     * time if a program load operation exceeds its timeout value; may be <code>null</code>.
     * @param cmpdProcessNumber the process number if the target is a CMPD process; 0 if 
     * there is a single non-CMPD process as the target.
     *  
     */
    public AbstractClient(
            IConnection connection,
            ICommandReceiverRouter router,
            JavaFactory jFactory,
            ITypeFactory typeFactory,
            final IRunner displayCallbackRunner,
            int defaultTimeout,
            int loadTimeout,
            ITimeoutCallback loadTimeoutCallback,
            int cmpdProcessNumber) {
        if (cmpdProcessNumber <= 0){
            throw new IllegalArgumentException("process number not greater than 0");
        }
        mTypeFactory = typeFactory;
        mJavaFactory = jFactory;
        mDefaultTimeout = defaultTimeout;
        mTimeout = defaultTimeout;
        mLoadTimeout = loadTimeout;
        mLoadTimeoutCallback = loadTimeoutCallback;
        mTimeoutCallback = null;
        fRouter = router;
        mConnection = connection;
        mCmpdNumber = cmpdProcessNumber;
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
        ScwpCommandPacket cmd = new ScwpCommandPacket(mCmpdNumber*REQUIRED_CHANNELS+ScwpCommandPacket.ENGINE,
                ms.getName());
        cmd.setData(ms.serializeArguments(args));
        if (isTracing()) {
            StringBuffer buf = new StringBuffer();
            buf.append("Engine #" + mCmpdNumber + " calling: " + ms.getName() + "(");
            for (int i = 0; i < args.length; i++) {
                if (i > 0) buf.append(", ");
                buf.append(args[i]);
            }
            buf.append(")");
            trace(buf.toString());
        }

        try {
            // Reset timeout and timeout callback to default in case we have
            // calls from engine callbacks while a preceeding call is waiting.
            int timeout = mTimeout;
            mTimeout = mDefaultTimeout;
            ITimeoutCallback callback = mTimeoutCallback;
            mTimeoutCallback = null;
            ScwpReplyPacket result = mConnection.sendCommand(cmd,timeout,callback);
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
            // Don't complain. As SeeCode displays shutdown after the engine has died,
            // we'll get too many of them.
            //mEngineObserver.displayError(null,"Engine disconnected");
            //Force shutdown!
            if (mEngineObserver != null)
                mEngineObserver.engineShutdown(null);
            throw new EngineDisconnectedException("Socket connection to engine closed");
        }
    }
    
    /**
     * Invoke a method that is likely to have a longer delay than normal, such as the 
     * @{link #loadProgram} method.
     * 
     * @param ms the method to invoke on the server.
     * @param args the arguments to pass to the method.
     * @param timeout the timeout to use (which will typically be longer than the default).
     * @return
     * @throws EngineException
     */
    protected Object invokeRemoteMethod(IMethodSerializer ms, Object[] args, int timeout,
        ITimeoutCallback callbackTimeout)
    throws EngineException {
    	int saveDefaultTimeout = mDefaultTimeout;
    	ITimeoutCallback saveCallback = mTimeoutCallback;
    	mTimeout = timeout;
    	mTimeoutCallback = callbackTimeout;
    	try {
    		return invokeRemoteMethod(ms,args);
    	}
    	finally{
    		mDefaultTimeout = saveDefaultTimeout;
    		mTimeoutCallback = saveCallback;
    	}
    }
    
    /**
     * Called as an acknowledgement that the engine
     * connection is to be disconnected.
     */
    @Override
    public void onShutdown(){
        mConnection.shutdown();       
    }

    /* override */
    @Override
    public void setBreakpointObserver(IBreakpointObserver observer) {
        MethodSerializationHandler h = new MethodSerializationHandler(observer,
                IBreakpointObserver.class, null, mJavaFactory, mTypeFactory);
        fRouter.setMethodHandler(mCmpdNumber*REQUIRED_CHANNELS+ScwpCommandPacket.BREAKPOINT_OBSERVER, h, false);
    }

    /* override */
    @Override
    public void setCustomDisplayCallback(ICustomDisplayCallback monitor) {
        MethodSerializationHandler h = new MethodSerializationHandler(monitor,
            ICustomDisplayCallback.class, null, mJavaFactory, mTypeFactory);
        fRouter.setMethodHandler(mCmpdNumber*REQUIRED_CHANNELS+ScwpCommandPacket.CUSTOM_DISPLAY_MONITOR, h, true);
    }

    /* override */
    @Override
    public void setEngineObserver(IEngineAPIObserver observer) {
        mEngineObserver = observer;
        MethodSerializationHandler h = new MethodSerializationHandler(observer,
            IEngineAPIObserver.class, null, mJavaFactory, mTypeFactory);
        fRouter.setMethodHandler(mCmpdNumber*REQUIRED_CHANNELS+ScwpCommandPacket.ENGINE_OBSERVER, h, false);

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
