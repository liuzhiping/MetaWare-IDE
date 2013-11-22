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

import com.arc.seecode.cmpd.ICMPDController;
import com.arc.seecode.engine.EngineException;


public class CMPDController implements ICMPDController {
    
    /**
     * The C++ object that this object wraps.
     * <P>
     * DO NOT ALTER THIS FIELD's NAME or TYPE UNLESS C++ CODE IS CHANGED IN
     * TANDEM!
     */
    private int mObject;
    
    private int mVersion;

    /**
     * @param version the engine interface version being used.
     * @param factory
     * @param typeFactory
     * @param CMPDProcessID if non-zero, the ordinal of the CMPD process that this object
     * references.
     */
    private CMPDController(int version) {
        mObject = 0;
        mVersion = version;
        if (version > 0)
            initInterface();
    }

//    static {
//        System.loadLibrary(SEECODE_ENGINE);
//    }

    // Initialize this object on the C++ end.
    private native void initInterface();
    
    /**
     * The expected version of the engine interface. The value must be returned by
     * the call to the native {@link #queryVersion queryVersion()} method.
     */
    public static final int VERSION = 1; 
    
    /**
     * The earliest version of the engine that we will support. The engine's
     * {#link #getVersion()} must not return a value less than this.
     * 0 means the older engine that has no special CMPD support.
     */
    public static final int EARLIEST_VERSION = 0;
    
    
    /**
     * Called to retrieve the version that the debugger engine interface conforms to.
     * We pass the version of this interface to it in case the engine is actually <i>newer</i>.
     * It may be able to accomodate an older Java GUI front end.
     * @param preferredVersion the version that we prefer the engine to conform to.
     * @return the version that the engine interface expects to conform to.
     */
    private static native int queryVersion(int preferredVersion);

    /**
     * Create an instance.
     * 
     * @return an instance of this class with appropriate version checks.
     * @throws IllegalStateException if the engine interface version doesn't match what is expected.
     */
    public static CMPDController Create () {
        int version = 0;
        try {
            version = queryVersion(VERSION);
        } catch (UnsatisfiedLinkError e1) {
            // Older engine doesn't have new CMPD support
        }
       
        if (version > VERSION || version < EARLIEST_VERSION) {
            throwVersionException(version);
        }
        return new CMPDController(version);
    }

    /**
     * Throw the exception that indicates that the IDE and debugger are out of sync. The message is encoded in HTML with
     * the assumption that that the intereptor will display it in a OptionPane.
     */
    private static void throwVersionException (int engineVersion) {
        StringBuilder buf = new StringBuilder(300);
        buf.append("<html>Your MetaWare Debugger and the MetaWare IDE are evidently not in synch with each other.<br>" +
            "The debugger engine's CMPD interface does not match what is required by the MetaWare IDE.<br>"+
            "The engine's CMPD interface version is " + engineVersion + "; it is required to be ");
        
        buf.append("between " + EARLIEST_VERSION + " and " + VERSION + ", inclusive");
        buf.append(".<br><br>The debugger launch is being aborted.");
        throw new IllegalStateException(buf.toString());
    }
    
    /**
     * Return the underlying C++ object address that shadows this object.
     * 
     * @return the underlying C++ object address that shadows this object.
     */
    public int cplusplusObject() {
        return mObject;
    }

    @Override
    public native void instructionStep (boolean over) throws EngineException;

    @Override
    public native void invokeCommand (String command) throws EngineException;

    @Override
    public native void resume () throws EngineException;

    @Override
    public native void statementStep (boolean over) throws EngineException;

    @Override
    public native void stepOut () throws EngineException;

    @Override
    public native void suspend () throws EngineException;
    
    @Override
    public native void restart () throws EngineException;
    
    public int getEngineInterfaceVersion() {
        return mVersion;
    }
}
