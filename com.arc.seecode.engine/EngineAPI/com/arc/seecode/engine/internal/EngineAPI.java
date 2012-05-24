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
package com.arc.seecode.engine.internal;

import com.arc.seecode.engine.AssemblyRecord;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;
import com.arc.seecode.engine.Format;
import com.arc.seecode.engine.IBreakpointObserver;
import com.arc.seecode.engine.ICustomDisplayCallback;
import com.arc.seecode.engine.IEngineAPI;
import com.arc.seecode.engine.IEngineAPIObserver;
import com.arc.seecode.engine.IEngineObserver;
import com.arc.seecode.engine.IEnginePoller;
import com.arc.seecode.engine.JavaFactory;
import com.arc.seecode.engine.Location;
import com.arc.seecode.engine.LocationBreakpoint;
import com.arc.seecode.engine.RegisterContent;
import com.arc.seecode.engine.StackFrameRef;
import com.arc.seecode.engine.Value;
import com.arc.seecode.engine.Variable;
import com.arc.seecode.engine.Version;
import com.arc.seecode.engine.WatchpointHit;
import com.arc.seecode.engine.type.ITypeFactory;
import com.arc.seecode.server.EngineThread;

/**
 * The adapter around the C++-based SeeCode engine. There is one such object per
 * CMPD process.
 * <P>
 * If the engine was written in Java, this class would be broken down into
 * several classes (e.g, ProcessControl, Evaluator, BreakpointManager, etc.).
 * But since the underlying implementation is C++, we make it a single class so
 * as to simplify the C++-side of things.
 * <P>
 * Integer identifiers are used to reference threads, stackframes, breakpoints,
 * watchpoints, and modules. This reduces the need for the C++-based engine to
 * instantiate concrete Java classes.
 * <P>
 * The event model is simple: if one thread is stopped, due to, say, hitting a
 * breakpoint, then all threads stop. If, in the future, we desire to be able to
 * suspend a subset of threads while others run, then this interface will neeed
 * to be enhanced.
 * <P>
 * <B>NOTE: </B> In contrast to the previous version of SeeCode, the GUI
 * controls the engine, and is responsible for querying the engine to fill in
 * generic debugger displays. More specifically, the GUI is responsible for
 * maintaining such things as source, disassembly, register, stack-trace
 * displays, etc.
 * <P>
 * The only displays that are controlled by the engine in the old manner are
 * custom displays. The engine invokes methods in the
 * {@link ICustomDisplayCallback} to create and maintain such displays.
 * <P>
 * NOTE: because the SeeCode engine is single-threaded, we
 * employ a {@linkplain EngineThread run thread} to 
 * actually invoke the "run" and "step" commands. Other inquiries
 * into the engine are not done until it is in a stopped state.
 * <P>
 * <dl>
 * <dt> Version 32
 * <dd> Causes engine to treat the IDE as if its GUI version is 1102.
 * <dt> Version 31
 * <dd> {@link ICustomDisplayCallback#recordInitState}
 * <dt> Version 30
 * <dd> {@link ICustomDisplayCallback#setSelection}
 * <dd> {@link ICustomDisplayCallback#setStackFrame(int threadID, int stackLevel)}
 * <dt> Version 29
 * <dd> Guihili changes to handle new Break and Watchpoint displays (Oct 13, 2010)
 * <dt> Version 28
 * <dd> {@link IEngineAPIObserver#writeToErrorLog(IEngineAPI, String)}
 * <dt> Version 27
 * <dd> {@link #copyAllToClipboard}
 * <dd> {@link ICustomDisplayCallback#copyToClipboard(String)}
 * <dd> {@link ICustomDisplayCallback#copyVisibleToClipboard(int)}
 * <dt> Version 26
 * <dd> {@link ICustomDisplayCallback#showHelp}
 * <dt> Version 25
 * <dd> {@link ICustomDisplayCallback#promptForFile}
 * <dd> {@link ICustomDisplayCallback#showSplash}
 * <dt> Version 24
 * <dd> Added the ability to disconnect the debugger while leaving the target running.
 * <dd> {@link IEngineAPI#disconnect}
 * <dd> {@link IEngineAPI#canDisconnect}
 * <dt> Version 23
 * <dd> New font and style support for the new TAD.
 * <dt> Version 22
 * <dd> {@link IEngineAPI#setPC}
 * <dd> {@link Location#setAmbiguous}
 * <dd> {@link Location#isAmbiguous}
 * <dt> Version 21
 * <dd> Instructs the engine that we support PC arrow in left most corner for benefit
 * of Source Files display.
 * <dt>Version 20
 * <dd> Instructs the debugger engine that we can handle dynamically generated profiling columns.
 * <dt>Version 19
 * <dd> {@link ICustomDisplayCallback#onLicenseRequestStart}
 * <dd> {@link ICustomDisplayCallback#onLicenseRequestEnd}
 * <dt>Version 18
 * <dd> {@link #sendValueUpdate2} added.
 * <dt>Version 17
 * <dd>Globals display's popup menu now generated from engine.
 * <dt>Version 16
 * <dd> Added CMPDController class in debugger engine.
 * <dd> {@link IBreakpointObserver#breakpointAdded2(int, Location, int, String, int, int, String[])}
 * <dt>Version 15
 * <dd> {@link #getBreakpointHitCount}
 * <dt>Version 14
 * <dd> {@link ICustomDisplayCallback#animate}
 * <dt>Version 13
 * <dd> Flags {@link IEngineAPI#BP_DISABLED} and {@link IEngineAPI#WP_DISABLED} flags
 * for creating action points in a disabled state.
 * <dt>Version 12
 * <dd> {@link #createWatchpoint2}
 * <dd> {@link #createWatchpointReg}
 * <dd> {@link ICustomDisplayCallback#setProperty(String, String)}
 * <dd> {@link IBreakpointObserver#watchpointAdded2}
 * <dt>Version 11
 * <dd> {@link #refreshDisplays}
 * <dt>Version 10
 * <dd> {@link #writeStdin}
 * <dt>Version 9
 * <dd> {@link #setCmpdProcessID}
 * <dt>Version 8
 * <dd> {@link #getRegisterContent}
 * <dd> {@link #getRegisterContentArray}
 * <dt>Version 7
 * <dd> {@link #isLicenseFailurePending} added.
 * <dt>Version 6
 * <dd>{@link #getLicenseExpirationDays()} was added.
 * {@link #getSplashPath()} was added.
 * {@link #loadProgram} made return a status to indicate license failure.
 * <dt>Version 5
 * <dd>We added logical address to {@link Location} class so that we can support Sandisk overlay scheme.
 * <dt>Version 4
 * <dd>{@link #setMemoryBytes} was added to this interface.
 * <dt>Version 3
 * <dd>{@link ICustomDisplayCallback#queueCommand} was added.
 * <dt>Version 2
 * <dd>Improved versioning scheme for interface.
 * <dt>Version 1
 * <dd>Original interface.
 * </dl>
 * 
 * @author David Pickens
 */
public class EngineAPI implements IEngineAPI, INativeStepOperations, IEnginePoller {
    //TODO: make this path configurable
    //private static final String SEECODE_ENGINE = "crout";

    /**
     * The C++ object that this object wraps.
     * <P>
     * DO NOT ALTER THIS FIELD's NAME or TYPE UNLESS C++ CODE IS CHANGED IN
     * TANDEM!
     */
    private int mObject;

    private JavaFactory mFactory;

    private ITypeFactory mTypeFactory;
    
    private int mVersion;

    /**
     * @param version the engine interface version being used.
     * @param factory
     * @param typeFactory
     * @param CMPDProcessID if non-zero, the ordinal of the CMPD process that this object
     * references.
     */
    private EngineAPI(int version, JavaFactory factory, ITypeFactory typeFactory, int CMPDProcessID) {
        mObject = 0;
        mVersion = version;
        mFactory = factory;
        if (typeFactory == null)
                throw new IllegalArgumentException("type factory is null");
        mTypeFactory = typeFactory;

        initInterface();
        if (CMPDProcessID > 0)
            setCmpdProcessID(CMPDProcessID);
    }

//    static {
//        System.loadLibrary(SEECODE_ENGINE);
//    }

    //NOTE: this was formerly called "init", but since we didn't have version checking mechanism
    // in place, we renamed it so that earlier version of the GUI won't access the
    // newer engine interface by mistake.
    private native void initInterface();
    
    /**
     * The expected version of the engine interface. The value must be returned by
     * the call to the native {@link #getVersion getVersion()} method.
     * {@link #queryVersion} is called first with this value to inform the
     * engine that we prefer it to accommodate this version.
     */
    public static final int VERSION = 32; 
    
    /**
     * The earliest version of the engine that we will support. The engine's
     * {#link #getVersion()} must not return a value less than this.
     */
    public static final int EARLIEST_VERSION = 2;
    
    /**
     * Get the version of the engine interface. As of version 3, this method has
     * been replaced with {@link #queryVersion}, but is still called if the target engine
     * does not have {@link #queryVersion} defined.
     * @return the version that the engine is expecting to accommodate.
     */
    private static native int getVersion();
    
    /**
     * Called to retrieve the version that the debugger engine interface conforms to.
     * We pass the version of this interface to it in case the engine is actually <i>newer</i>.
     * It may be able to accomodate an older Java GUI front end.
     * @param preferredVersion the version that we prefer the engine to conform to.
     * @return the version that the engine interface expects to conform to.
     */
    private static native int queryVersion(int preferredVersion);

    /**
     * Create an instance for monitoring a single process.
     * @param factory factory for making objects that are passed to and from the engine.
     * @param typeFactory factory for making C/C++ type descriptors.
     * @param cmpdProcessID if non-zero, the ordinal of the CMPD process that this object
     * is to reference.
     * 
     * @return an EngineInterface instance.
     * @throws IllegalStateException if the engine interface version doesn't match what is expected.
     */
    public static EngineAPI Create (JavaFactory factory, ITypeFactory typeFactory, int cmpdProcessID) {
        int engineVersion = 1;
        try {
            engineVersion = queryVersion(VERSION);
        }
        catch (UnsatisfiedLinkError e) {
            // queryVersion(int) wasn't defined until version 3
            // So, if we got this exception, then the engine is version 2 or
            // earlier. Call the older "getVersion()" method.
            try {
                engineVersion = getVersion();
            }
            catch (UnsatisfiedLinkError e1) {
                // "getVersion()" did not exist in version 1.
                throwVersionException(1);
            }
        }
        // Version 4 can accomodate version 2 and 3. Version 3 merely defines
        // the ICustomDisplayMonitor::queueCommand method. A version 2 engine won't
        // ever call it, so we're safe.
        // Version 4 merely defined setMemoryBytes. We avoid calling it if
        // version is less than 4. 
        if (engineVersion > VERSION || engineVersion < EARLIEST_VERSION) {
            throwVersionException(engineVersion);
        }
        if (cmpdProcessID > 0 && engineVersion < 9){
            throw new IllegalStateException("<html>You are attempting to launch a CMPD session, but"+
                " the MetaWare Debugger that you have installed is too back level. The debugger "+
                "engine interface is " + engineVersion +"; it is required to be 9, or later." +
                "<p>The debugger launch is being aborted.");
        }
        return new EngineAPI(engineVersion, factory, typeFactory,cmpdProcessID);
    }

    /**
     * Throw the exception that indicates that the IDE and debugger are out of sync. The message is encoded in HTML with
     * the assumption that that the intereptor will display it in a OptionPane.
     */
    private static void throwVersionException (int engineVersion) {
        StringBuilder buf = new StringBuilder(300);
        buf.append("<html>Your MetaWare Debugger and the MetaWare IDE are evidently not in synch with each other.<br>" +
            "The debugger engine interface does not match what is required by the MetaWare IDE.<br>"+
            "The engine's interface version is " + engineVersion + "; it is required to be ");
       
        buf.append("between " + EARLIEST_VERSION + " and " + VERSION + ", inclusive");
        buf.append(".<br><br>The debugger launch is being aborted.");
        throw new IllegalStateException(buf.toString());
    }

    /**
     * The factory object by which the C++ code allocates Java-based objects
     * that it can then wrap.
     * 
     * @return factory for creating Java objects from C++.
     */
    public JavaFactory getJavaFactory() {
        return mFactory;
    }
    

    @Override
    public int getEngineInterfaceVersion() throws EngineException {
        return mVersion;
    }

    /**
     * The factory object by which the C++ engine can create type objects for
     * variables and values.
     * 
     * @return factory creating type descriptors.
     */
    public ITypeFactory getTypeFactory() {
        return mTypeFactory;
    }

    /**
     * Return the underlying C++ object address that shadows this object.
     * 
     * @return the underlying C++ object address that shadows this object.
     */
    public int cplusplusObject() {
        return mObject;
    }

    /**
     * Return true if a process is being debugged.
     * <P>
     * This method is used for assertions.
     * 
     * @return true if a process is being debugged.
     */
    @Override
    public native boolean isActive();

    /**
     * Return the CMPD process ID that this instance is controlling.
     * 
     * @return the CMPD process ID, or 0 if there is just one process.
     */
    @Override
    public native int getPID();

    /**
     * Set the callback interface for receiving notifications of
     * engine actions pertaining to processes and threads.
     * <P>
     * This should be set early before any other method of this
     * object is invoked.
     * @param observer the callback object.
     * @pre observer != null
     * @post $none
     */
    @Override
    public native void setEngineObserver(IEngineAPIObserver observer);

    /**
     * Set an observer for breakpoint-state-change events.
     * 
     * @param observer
     *            the callback object.
     */
    @Override
    public native void setBreakpointObserver(IBreakpointObserver observer);

    /**
     * Set the callback interface by which the engine can create
     * and maintain custom displays. NOTE: in constract to the
     * previous implementation of SeeCode, generic displays 
     * are not completely maintained by the UI and to not use
     * this mechanism.
     * 
     * @param monitor the callback object.
     * @pre monitor != null
     * @post $none
     */
    @Override
    public native void setCustomDisplayCallback(ICustomDisplayCallback monitor);

    /**
     * Send a property update on behalf of a custom display.
     * <P>
     * NOTE: this is used for the engine to receive feedback from
     * custom displays. In the former version of SeeCode, all
     * displays were "custom" and used this mechanism to communicate
     * with the engine. Now, all generic displays are maintained by
     * the UI and the engine has no knowledge of them.
     * <P>
     * @param displayID associated display ID.
     * @param property the name of a property to be updated.
     * @param value the value of the property.
     * @pre displayID >= 0 && property != null && isActive()
     * @post $none
     */
    @Override
    public native void sendValueUpdate(int displayID, String property,String value);
    
    /**
     * Send a property update on behalf of a custom display, but with a timeout.
     * If the timeout (in millisecond) expires before the value can be delivered to the
     * engine, it is not sent.
     * <P>
     * NOTE: this is used for the engine to receive feedback from
     * custom displays. In the former version of SeeCode, all
     * displays were "custom" and used this mechanism to communicate
     * with the engine. Now, all generic displays are maintained by
     * the UI and the engine has no knowledge of them.
     * <P>
     * @param displayID associated display ID.
     * @param property the name of a property to be updated.
     * @param value the value of the property.
     * @param timeout time out in milliseconds, or 0 if there is to be no timeout.
     * @return true if message was sent prior to timeout; false if timeout occurred prior to being sent.
     * @pre displayID >= 0 && property != null && isActive()
     * @post $none
     * <P>
     * Added in interface 18.
     */
    @Override
    public native boolean sendValueUpdate2(int displayID, String property,String value, int timeout);

    /**
     * Set the engine command-line arguments. This should be
     * called prior to calling {@link #loadProgram(String[])} or {@link #attach(int,String)}.
     * 
     * @param args the command-line arguments string [need more info].
     * @pre args != null
     */
    @Override
    public native void setEngineArguments(String args);

    /**
     * Load a program for debugging. The engine is expected to
     * communicate events through the {@link IEngineObserver} callback
     * interface. For example, the engine may start executing the
     * program and then stop at main. Such events would be properly
     * indicated by the {@link IEngineObserver#processCreated(EngineInterface) processCreated},
     * {@link IEngineObserver#processStarted(EngineInterface) processStarted}, and
     * {@link IEngineObserver#processStopped processStopped} calls.
     * <P>
     * If the program cannot be loaded for some reason, then
     * {@link IEngineObserver#displayError(EngineInterface,String)} is expected to be called
     * appropriately.
     * <P>
     * The command-line arguments, if any, also appear after
     * the exe-file path, separated by white space. Any argument,
     * including the exe file, that has white space must be
     * enclosed in double quotation marks ("...").
     * 
     * @param command the program with its command-line arguments.
     * @pre command != null
     * @post $none
     * @return true if program loaded okay. Otherwise, an error
     * was presumably sent to the {@linkplain IEngineObserver callback}.
     */
    @Override
    public native boolean loadProgram(String command[]);

    /**
     * Given a machine address (that is presumably in code), return a
     * {@link Location}object with as much information filled in as possible.
     * 
     * @param address
     *            a machine address.
     * @return {@link Location}object with as much information filled in as
     *         possible.
     */
    @Override
    public native Location computeLocation(long address);

    /**
     * Evaluate an expression that is to reference a code location (e.g., as
     * required for a breakpoint).
     * <P>
     * This method is called from {@link StackFrameRef#evaluateLocation(String)}.
     * <P>
     * The resulting location will have source/line information filled in if
     * available.
     * 
     * @param expression
     *            the expression to evaluate.
     * @param stackFrameID
     *            context of the lookup.
     * @return the {@link Location}object with as much information filled in as
     *         possible, or <code>null</code>.
     */
    @Override
    public native Location evaluateLocation(String expression,
            int stackFrameID);

    /**
     * Given a source path and a line number, return a {@link Location}object
     * with as much information filled in as possible.
     * <P>
     * The source path may be relative, in which case the
     * {@linkplain #setSourceDirectories(String[]) source directories}will be
     * searched for the filename.
     * 
     * @param source
     *            a source path, possibly relative.
     * @param line
     *            a line number.
     * @return a {@link Location}object with as much information filled in as
     *         possible, or <code>null</code>.
     */
    @Override
    public native Location lookupSource(String source, int line);

    /**
     * Attached to a running program The engine is expected to
     * communicate events through the {@link IEngineObserver} callback
     * interface. 
     * 
     * @param cmpdID the CMPD process id, or 0.
     * @param pid the id of the program to attached to.
     * @pre pid != null
     * @post $none
     * @return true if program attached okay. Otherwise, an error
     * was presumably sent to the {@linkplain IEngineObserver callback}.
     */
    @Override
    public native boolean attach(int cmpdID, String pid);

    /**
     * Query the state of a thread or process being debugged.
     * <P>
     * If the thread is stopped on a breakpoint, then
     * {@link #getBreakpointHit} can be called to get the breakpoint ID.
     * <P>
     * If the thread is stopped on a watchpoint, then
     * {@link #getBreakpointHit} can be called to get the watchpoint ID.
     *
     * <P>
     * The specified thread ID can be zero if the engine does not support independent
     * thread control.
     * <P>
     * @return the state of the process as enumerated by
     * {@link #RUNNING}, {@link #STOPPED_BY_USER}, {@link #BREAKPOINT_HIT},
     * {@link #WATCHPOINT_HIT}, {@link #NOT_STARTED}, or {@link #INVALID} to denote a non-existent pid.
     * 
     * @pre isActive()
     * @post $result == RUNNING || $result==INVALID || $result==STOPPED_BY_USER ||
     *     $result==NOT_STARTED || $result == BREAKPOINT_HIT ||
     *     $result==WATCHPOINT_HIT || $result == UNKNOWN ||
     *     $result==TERMINATED
     */
    @Override
    public native int queryState(int tid);

    /**
     * Return the breakpoint ID if the process stopped due to hitting
     * a breakpoint.
     * @return the breakpoint id or 0 if no breakpoint is active.
     * @pre queryState(tid) == BREAKPOINT_HIT 
     * @post $result > 0
     */
    @Override
    public native int getBreakpointHit(int tid);

    /**
     * Stop the specified thread of the process. The engine will
     * invoke {@link IEngineObserver#processStopped processStopped} 
     * when the processor actually stops.
     * <P>
     * If the process/thread is already stopped, the engine's
     * action is undefined.
     * 
     * @param tid the thread to stop, or 0 to stop all threads.
     * @post $none
     */
    @Override
    public void stop (int tid) throws EngineException {
        seecodeSuspend(tid);  // for historical reasons
    }
    
    /**
     * The {@linkplain EngineThread run thread}
     * calls this method to suspend a thread of the engine, or all threads if the
     * designated argument is 0.
     * @param tid the thread to stop, or 0 to stop all threads.
     *
     */
    @Override
    public native void seecodeSuspend(int tid);

    /**
     * This is called while at least on thread is running so as
     * to poll for a stop event. If any process and/or thread stopped within a
     * reasonable time, this method returns true. Otherwise, it returns
     * false and the caller will need to call it again.
     * <P>
     * If we are {@linkplain #isSimulator() simulator-based}, then the
     * call to this method actually drives the simulator. It should
     * be called repeatedly with no unnecessary delays.
     * <P>
     * If we are <i>not</i> {@linkplain #isSimulator() simulator-based}, then
     * this method will simply do a poll to test if the hardware is stopped
     * running. For such a case, the caller should insert sleeps to avoid
     * the wait loop for swamping the CPU.
     * <P>
     * If the process and/or thread is already stopped, this method
     * will return true immediately. The process and/or thread will
     * need to be {@linkplain #resume resumed} for this method
     * to return something other than true.
     * <P>
     * If the thread/process is terminated, this method is expected to
     * return true so as to avoid an infinite loop by the caller.
     * 
     * @return true if process is stopped.
     * @pre $none
     * @post !$result && queryState(tid) == RUNNING || $result && (queryState(tid) != RUNNING || !isActiveThread(tid))
     */
    @Override
    public native boolean waitForStop();

    /**
     * Returns true if underlying process is a simulator. This implies that we
     * must call {@link #waitForStop()}repeatedly to keep it running.
     * 
     * @return true if process is simulator-based.
     */
    @Override
    public native boolean isSimulator();

    /**
     * Called from the {@linkplain EngineThread run thread}
     *  to implement "resume" operation. If the specified thread is 0 or if
     *  the engine doesn't support independent thread control, then all threads will
     *  be resumed.
     */    
    @Override
    public native void seecodeResume(int tid);
    
    /**
     * Resume a process that is stopped. This is done by
     * having the {@linkplain EngineThread run thread}
     * invoke the {@link #seecodeResume}  method.
     * 
     * <P>
     * If the process is not stopped, the engine's
     * action is undefined.
     * 
     * @pre queryState() != RUNNING &&
     *      queryState() != INVALID
     * @post $none
     */
    @Override
    public void resume(final int tid) throws EngineException {
        seecodeResume(tid);  // for historical reasons.
    }

    /**
     * Perform an instruction step.
     *     This is done by
     * having the {@linkplain EngineThread run thread}
     * invoke the {@link #seecodeInstructionStep}  method.
     * <P>
     * The engine is expected to call {@link IEngineObserver#processResumed(EngineInterface) IEngineObserver.processResumed}/
     * {@link IEngineObserver#processStopped IEngineObserver.processStopped}  in
     * succession.
     * @param tid the id of the thread, or 0 if all threads are to
     * be stepped.
     * @param over if true, step over calls, otherwise step into calls.
     * 
     * @pre isStopped(tid)
     */
    @Override
    public void instructionStep(final int tid, final boolean over, final int cnt) throws EngineException{
        seecodeInstructionStep(tid,over,cnt); // for historical reasons.
    }
    
    /**
     * Called from the {@linkplain EngineThread run thread}
     *  to implement "instructionStep" operation.
     */    
    @Override
    public native void seecodeInstructionStep(int tid, boolean over, int cnt);

    /**
     * Perform a source statement step.
     *     This is done by
     * having the {@linkplain EngineThread run thread}
     * invoke the {@link #seecodeStatementStep}  method.
     * <P>
     * The engine is expected to call {@link IEngineObserver#processResumed(EngineInterface) IEngineObserver.processResumed}/
     * {@link IEngineObserver#processStopped IEngineObserver.processStopped}  in
     * succession.
     * @param tid the id of the thread, or 0 if all threads are to
     * be stepped.
     * @param over if true, step over calls, otherwise step into calls.
     * 
     * @pre isActive()
     * @post $none
     */
    @Override
    public void statementStep(final int tid, final boolean over, final int cnt) throws EngineException{
        seecodeStatementStep(tid,over,cnt); // for historical reasons
    }
    
    /**
     * Called from the {@linkplain EngineThread run thread}
     *  to implement "statementStep" operation.
     */  
    @Override
    public native void seecodeStatementStep(int tid, boolean over, int cnt);

    /**
     * Return the array of active thread ID's.
     * <P>
     * The resulting array object can be located in a cache, therefore,
     * each call may overwrite the previous contents.
     * @return an array of active thread ID's.
     * <P>
     * NOTE: this method is for consistency checking. The UI should be
     * able to track threads from the {@link IEngineObserver} object.
     * 
     * @pre isActive()
     */
    @Override
    public native int[] getThreads();

    //    /**
    //     * Translates a function or source code reference into
    //     * a {@link Location} object.
    //     * <P>
    //     * More specifically,
    //     * given a function name, source reference (path!line), or
    //     * a hex address (0xABC), compute a {@link Location} object
    //     * which fills in missing information.
    //     * <P>
    //     * <B>NOTE:</B> originally, this method was to be native, but
    //     * the engine's code for evaluating such expressions is
    //     * buried in the command processor (rce)!! Thus, we now
    //     * moving the expression parsing into Java.
    //     *
    //     * @param expression addressing expression to be evaluated.
    //     * @return corresponding {@link Location} objct.
    //     */
    //    public Location computeLocation(String expression) {
    //        return Evaluator.computeLocation(expression,this);
    //        
    //    }

    /**
     * Create a breakpoint. 
     * If an error occurs, the method {@link IEngineObserver#displayError(EngineInterface,String)}
     * is invoked by the engine.
     * <P>
     * @param location the location of the breakpoint.
     * @param hitCount if > 1, the number of hits before the
     * breakpoint takes.
     * @param conditional if not null, an expression to be evaluated
     * to true before breakpoint takes.
     * @param tid the thread ID if the breakpoint is to be tied to
     * a thread, or 0 if breakpoint is to be applied globally
     * @param flags union of {@link #BP_REGULAR}, {@link #BP_HARDWARE}, or
     * {@link #BP_TEMPORARY}.
     * @return a breakpoint ID number, or 0 if an error occured.
     * 
     * @pre location != null 
     * @post isValidBreakpoint($result)
     */
    @Override
    public native int createBreakpoint(Location location, int hitCount,
            String conditional, int tid, int flags);
    
    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPI#createWatchpoint(java.lang.String, java.lang.String, int, int)
     */
    @Override
    public native int createWatchpoint(String var, int length, String conditional, int tid, int flags, int stackFrameID) throws EngineException;

    /**
     * Called when a {@link LocationBreakpoint}object that wraps the breakpoint ID is
     * garbage-collected.
     * 
     * @param breakID
     */
    @Override
    public native void freeBreakpoint(int breakID);

    /**            
     * Set whether or not a breakpoint or watchpoint is to be enabled.
     * <P>
     * The method {@link IBreakpointObserver#breakpointStateChanged(int,boolean)}
     * will be called as a side-effect, if the state is indeed changed.
     * @param id breakpoint id number.
     * @param v true if enabling the breakpoint; false otherwise.
     * 
     * @pre isValidBreakpoint(id)
     * @post $none
     */
    @Override
    public native void setBreakpointEnabled(int id, boolean v);


    /**            
     * Delete a breakpoint or watchpoint.
     * <P>
     * The {@link IBreakpointObserver#breakpointRemoved(int)} method
     * will be called as a sideefect.
     * @param breakID breakpoint or watchpoint to delete.
     * @pre isValidBreakpoint(breakID)
     * @post !isValidBreakpoint(breakID)
     */
    @Override
    public native void removeBreakpoint(int breakID);



    /**
     * Return true if the given breakpoint ID is valid.
     * <P>
     * This method is used for assertion tests.
     * @param breakID breakpoint ID to check
     * @return true if the breakpoint ID is valid.
     * 
     * @pre isActive()
     * @post $none
     */
    @Override
    public native boolean isValidBreakpoint(int breakID);


    /**
     * Terminate the existing process.
     * The method {@link IEngineObserver#processTerminated(EngineInterface)} will
     * be invoked some time in the near future.
     * 
     * @pre isActive()
     * @post $none
     *
     */
    @Override
    public void terminate () {
        seecodeSuspend(0);
        seecodeTerminate();
    }
      
    
    
    /**
     * Terminate the current program being debugged.
     */
    private native void seecodeTerminate();


    /**
     * Return an ID that denotes the top stackframe ID of the given thread.
     * <P>
     * NOTE: because we garbage-collect these ID's via
     * {@link StackFrameRef#finalize}, each call must return a unique ID.
     * 
     * @param tid
     *            the thread ID.
     * @return a unique ID to denote a stackframe.
     */
    @Override
    public native int makeStackFrameID(int tid);

    /**
     * Given a register-set snapshot ID, compute a register-set snapshot of the
     * caller.
     * <P>
     * This method is only called from {@link StackFrameRef#getCallerFrame()},
     * which is responsible for freeing the result by calling
     * {@link #freeStackFrameID(int)}.
     * 
     * @param stackFrameID
     *            the register-set snapshot.
     * @return the register-set ID of the caller.
     */
    @Override
    public native int computeCallerFrame(int stackFrameID);

    /**
     * Return whether or not a thread ID corresponds to an active
     * thread.
     * <P>
     * This method is called to perform assertion testing.
     * 
     * @param tid the thread ID to be tested.
     * @return true if the thread ID is active, false otherwise.
     * 
     * @pre $none
     * @post $none
     */
    @Override
    public native boolean isActiveThread(int tid);

    /**
     * Return the local variables, including arguments, for a stack frame, if known.
     * Otherwise, returns null.
     * <P>
     * <B>NOTE:</B> this method is only called from 
     * {@link StackFrameRef#getLocals()}.
     * 
     * @param stackFrameID a register set contents.
     * 
     * @pre isValidRegSet(stackFrameID)
     * @post $none
     */
    @Override
    public native Variable[] getLocals(int stackFrameID);

    /**
     * Return all static/global variables that are within scope from a
     * particular program location.
     * <P>
     * TODO: handle thread-local variables?
     * <P>
     * 
     * @param pc
     *            the PC location to identify the scope from which the variables
     *            are to be sought.
     * @return all static/global variables that are within scope from a
     *         particular program counter.
     */
    @Override
    public native Variable[] getNonlocals(long pc);

    /**
     * Return whether or not the given register set snapshot is valid.
     * <P>
     * It becomes invalid when the associated thread steps out of the
     * stackframe, or makes any change to the registers.
     * <P>
     * 
     * @param stackFrameID
     *            a register set snapshot ID.
     * @return whether or not a register set snaptshot is valid.
     */
    @Override
    public native boolean isValidStackFrameID(int stackFrameID);

    /**
     * Return the exit code of the terminated process. If the 
     * process was forceably terminated by the debugger, then -1 is
     * returned.
     * 
     * @return the exit code of the terminated process, or -1 if
     * the process was forceably terminated.
     * 
     * @pre !isActive()
     * @post $none
     */
    @Override
    public native int getProcessExitCode();

    /**
     * Evaluate an expression in the context of a stopped thread's
     * stack frame.
     * Returns a value, or
     * null if the expression could not be evaluated.
     *  <P>
     * <B>NOTE:</B> this method is only called from
     * {@link StackFrameRef#evaluate(String)}.
     * <P>
     * <P>
     * TODO: once we have a C++ parser in Java, this method
     * no longer be called. The general expression evaluation will
     * be done from within a Java package.
     
     * @param expression the expression to be evaluated
     * @param stackFrameID the associated register-set contents.
     * @return the result of the evaluation, or null.
     * 
     * @pre isStopped()
     * @pre expression != null
     * @post $none
     */
    @Override
    public native Value evaluate(String expression, int stackFrameID);

    /**
     * This method is called by {@link Value#getElement(int)}to lazily retreive
     * the elements of an aggregate value.
     * 
     * @param cookie
     *            a "cookie" that the engine set in the parent {@link Value}
     *            that identifies the expression object.
     * @param elementIndex
     *            the element count.
     * @return the value that is an element of parent value.
     */
    @Override
    public native Value getValueElement(int cookie, int elementIndex);

    /**
     * Free underlying resource for an expression value. Called from
     * {@link Value#finalize}during garbage collection. Thus, a cookie can
     * only be referenced from a single value.
     * 
     * @param cookie
     *            value cookie to be freed up.
     */
    @Override
    public void freeValueCookie(final int cookie) throws EngineException{
	seecodeFreeValueCookie(cookie);
    }
    
    /**
     * Called from {@link EngineThread run thread} to free value cookie
     * that was used to lazily retrieve aggregate alements.
     */
    @Override
    public native void seecodeFreeValueCookie(int cookie);

    /**
     * Given an expression that should denote a variable, return
     * a corresponding {@link Variable} object.
     *
     * <P>
     * <B>NOTE:</B> this method is only called from
     * {@link StackFrameRef#lookupVariable(String)}.
     * <P>
     * @param name name of the variable to be looked up.
     * @param stackFrameID a register-set contents.
     * @return corresponding Variable object, or null if the name
     * doesn't correspond to a variable.
     * 
     * @pre isStopped()
     * @pre name != null
     * @post $none
     */
    @Override
    public native Variable lookupVariable(String name, int stackFrameID);

    /**
     * Lookup a global variable and return an
     * object that denotes it, or <code>null</code> if
     * the variable does not exist.
     *
     * <P>
     * @param name name of the global variable to lookup up.
     * @return corresponding Variable object, or null if the name
     * doesn't correspond to a variable.
     * 
     * @pre isStopped()
     * @pre name != null
     * @post $none
     */
    @Override
    public native Variable lookupGlobalVariable(String name) throws EngineException;
    /**
     * Given a range of source lines within a source, file, return
     * an array of line numbers that contain debug information.
     * The GUI will use this to somehow highlight lines for which 
     * valid breakpoints can be set.
     * @param source the source file in question.
     * @param rangeLo the first line of the range to be checked (inclusive).
     * @param rangeHi the last line of the range to be checked (inclusive).
     * @return and array of line numbers within the given range that contain
     * debug information; or null if the information isn't available.
     * 
     * @pre source != null && rangeLo <= rangeHi && rangeLo >= 0
     * @post $result == null || $result.forAll(int line, line >= rangeLo && line <= rangeHi)
     */
    @Override
    public native int[] getExecutableLines(String source, int rangeLo,
            int rangeHi);

    /**
     * Start the process that has been recently loaded. This is only
     * called once after initially loading the process and the
     * state of the engine is {@link #NOT_STARTED}.
     * <P>
     * <B>NOTE:</B> because of the engine's single-threaded design,
     * this method will not return until the process has stopped. 
     * Therefore, it should be called from a "run" thread
     * that is distinct from the AWT event thread or a command-line
     * monitor loop.
     * <P>
     * The method {@link IEngineObserver#processStarted(EngineInterface)} will
     * be invoked in the near future.
     * 
     * @pre queryState(0) == NOT_STARTED
     * @post $none
     *
     */
    @Override
    public native void start();

    /**
     * Given a stopped thread, make it step out of the function that
     * it is currently stopped in.
     * 
     * <P>
     * This is implemented by having the 
     * {@linkplain EngineThread run thread} invoke
     * the {@link #seecodeStepOut(int)} method.
     * @param tid the thread to be stepped out of, or 0 if all threads
     * are to be operated upon.
     * 
     * @pre tid == 0 || isActiveThread(tid)
     * @pre isStopped(tid)
     */
    @Override
    public  void stepOut(final int tid) throws EngineException{
        seecodeStepOut(tid);
    }
    
    /**
     * Step until current function exists.
     * @param tid
     */
    @Override
    public native void seecodeStepOut(int tid);

    /**
     * Run the given thread, or all threads, until a particular
     * location is reached.
     * 
     * This is implemented by invoking the corresponding
     * method in the {@linkplain EngineThread run thread}.
     * @param tid the thread to run, or 0, if all threads are to run.
     * @param location location that is to trigger a stop.
     * 
     * @pre tid == 0 || isActive(tid) 
     * @pre isStopped(tid)
     * @pre location != null
     * @post $none
     */
    @Override
    public void runToAddress(final int tid, final Location location) throws EngineException{
        seecodeRunToAddress(tid,location);
    }
    
    /**
     * Invoked from {@link EngineThread run thread} to
     * implement the "run-to-address" operation.
     * @param tid
     * @param location
     */
    @Override
    public native void seecodeRunToAddress(int tid, Location location);

    /**
     * Return the instruction counter of the given stopped thread.
     * <P>
     * <B>NOTE:<B> this method is only called from 
     * {@link StackFrameRef#getPC()}.
     * @param stackFrameID the register-set contents.
     * @return the address of the instruction counter.
     * 
     * @pre isStopped()
     * @post $none
     */
    @Override
    public native long getPC(int stackFrameID);

    /**
     * Called when the lone reference to the stackframe ID is being garbage
     * collected. (See {@link StackFrameRef#finalize}.)
     * 
     * @param stackFrameID
     *            the stackframe ID being freed.
     */
    @Override
    public void freeStackFrameID(final int stackFrameID) throws EngineException{
	seecodeFreeStackFrameID(stackFrameID);
    }
    
    /**
     * Called from {@link EngineThread} to free stack frame ID.
     */
    @Override
    public native void seecodeFreeStackFrameID(int stackframeID);

    /**
     * Return the stack pointer of the given stopped thread.
     * <P>
     * <B>NOTE:<B> this method is only called from 
     * {@link StackFrameRef#getStackPointer()}.
     * @param stackFrameID the register-set ID.
     * @return the stack pointer value.
     * @post $none
     */
    @Override
    public native long getStackPointer(int stackFrameID);

    /**
     * Return a name to be associated with a thread, if there is one.
     * The GUI will use this name to denote the thread. If there is no
     * such name, it will use the thread ID in hex.
     * @param tid the thread whose name we request
     * @return the name of the thread, or null if there is no name.
     * 
     * @pre tid != 0 && isActiveThread(tid)
     * @post $none
     */
    @Override
    public native String getThreadName(int tid);

    /**
     * Return the frame pointer of the given stopped thread.
     * @param stackFrameID the register set from which the frame
     * pointer is being extracted.
     * 
     * @return the stack pointer value.
     * @pre tid == 0 || isActiveThread(tid)
     * @pre isStopped(tid)
     * @post $none
     */
    @Override
    public native long getFramePointer(int stackFrameID);

    /**
     * Return the number of register banks that the target machine
     * supports.
     * @return the number of register banks that the target machine
     * supports.
     * 
     * @pre $none
     * @post $result > 0
     */
    @Override
    public native int getRegisterBankCount();

    /**
     * Return the name of a register bank.
     * @param bank a register bank number, starting with 0
     * @return the name of the bank.
     * @pre bank >= 0 && bank < getRegisterBankCount()
     * @post $result != null
     */
    @Override
    public native String getRegisterBankName(int bank);

    /**
     * Retrieve the register ID's within a bank.
     * <P>
     * <B>NOTE:</B> register IDs must be unique across all
     * register banks.
     * @return an array of register IDs associated with a bank.
     * @pre bank >= 0 && bank < getRegisterBankCount()
     * @post $result != null
     */
    @Override
    public native int[] getRegisterIDsFromBank(int bank);

    /**
     * @return whether or not a register bank is active.
     * @pre bank >= 0 && bank < getRegisterBankCount()
     */
    @Override
    public native boolean isRegisterBankActive(int bank);

    /**
     * Given a register ID, return the register's name.
     * 
     * @param regID
     *            one of the ID's returned from
     *            {@link #getRegisterIDsFromBank(int)}.
     * @return the name of the register (e.g. "r10").
     */
    @Override
    public native String getRegisterName(int regID);

    /**
     * Return the value of a register.
     * <P>
     * The type of the value returned can be one of the following:
     * <dl>
     * <dt> Integer or Long
     * <dd>the value of the register as an integer.
     * <dt> int[] or long[] 
     * <dd> the of a large vector-like register that can't fit
     * within a single integer.
     * <dt> String
     * <dd> an ascii representation of the register that is
     * to be displayed (which is used for, say, a control-field value).
     * </dl>
     * 
     * @param stackFrameID the register-set ID.
     * If a register isn't available, it comes back as "???".
     * @param regID the register ID
     * @param format the requested format. Must be one of the
     * menifest constants in {@link Format}.
     * @return an ASCII representation of the register value, or
     * <code>null</code> if the value isn't known or isn't available.
     * 
     * @pre isStopped()
     * @pre bank >= 0 && bank < getRegisterBankCount()
     * @pre isValidRegister(bank,reg)
     * @post $result != null
     */
    @Override
    public native String getRegisterValue(int stackFrameID, int regID,
            int format);

    /**
     * Set an integer-like register to a value.
     * <P>
     * Only the top most stackframe of a thread can have its
     * registers altered.
     * @param tid the thread for which a register value is to
     * be set. The top-most stack frame is used.
     * @param regID the register ID.
     * @param value the new value of the register.
     * @return true if successfully; false if the register could not
     * be set.
     * 
     * @pre tid == 0 || isActiveThread(tid)
     * @pre isStopped(tid)
     * @pre isValidRegister(bank,regNo) && value != null
     * @post $none
     */
    @Override
    public native boolean setRegisterValue(int tid, int regID, String value);

    /**
     * Return an array of module ID numbers.
     * @return an array of module ID numbers.
     * 
     * @pre isActive()
     * @post $result != null
     */
    @Override
    public native int[] getModules();

    /**
     * Return the name of a module as a short name that would appear
     * in a display.
     * @param moduleID id of module
     * @return the name of the module.
     * 
     * @pre isValidModuleID(moduleID)
     * @post $result != null
     */
    @Override
    public native String getModuleName(int moduleID);

    /**
     * Return the base address of the module's code segment.
     * <P>
     * <B>NOTE:</B> this assumes a simple model where the code
     * and data segments of a DLL are all relocated relative to
     * a fixed base address.
     * <P>
     * @param moduleID the module ID
     * @return the base address of the module's code segment.
     * @pre isvalidModuleID(moduleID)
     * @post $none
     */
    @Override
    public native long getModuleBaseAddress(int moduleID);

    /**
     * Return the size of the module's code segment.
     * <P>
     * <B>NOTE:</B> this assumes a simple model where the code
     * and data segments of a DLL are all relocated relative to
     * a fixed base address.
     * <P>
     * @param moduleID the module ID
     * @return the size of the code segment in bytes.
     * @pre isvalidModuleID(moduleID)
     * @post $none
     */
    @Override
    public native long getModuleSize(int moduleID);

    /**
     * Return the functions defined in a module.
     * <P>
     * This is used to create a list of functions to 
     * set breakpoints on, etcl.
     * <P>
     * @param moduleID the module ID
     * @return array of function locations
     * @pre isvalidModuleID(moduleID)
     * @post $none
     */
    @Override
    public native Location[] getFunctionsWithinModule(int moduleID);

    /**
     * Disassemble instructions at a location, or near a location.
     * If the engine can determine that the location is in the middle
     * of the instruction, it can adjust accordingly.
     * 
     * @param addr the address from which to disassemble(approximate)
     * @param instructionCount the (approximate) number of instructions
     * to disassemble.
     * @return an array of assembly records.
     * @pre loc != null && instructionCount > 0
     * @post $result != null
     */
    @Override
    public native AssemblyRecord[] disassemble(long addr, int instructionCount);

    /**
     * Set source directories that the engine is to use to location
     * source files. When the engine returns a source location,
     * it includes the full path.
     * @param dirs source directories.
     * 
     * @pre dirs != null
     * @post $none
     */
    @Override
    public native void setSourceDirectories(String dirs[]);

    /**
     * Return whether or not an absolute memory address is valid.
     * @param address the memory address to be tested.
     * @param flags is 0 for data reference, {@link #CODE_ACCESS}
     * for code reference.
     * @return true if the memory address if valid; false otherwise.
     * 
     * @pre isActive()
     * @post $none
     */
    @Override
    public native boolean isValidMemory(long address, int flags);

    /**
     * Return the contents of memory into a byte array for a particular
     * amount.
     * <P>
     * If the start address is valid, but the memory become invalid
     * within the range, then the resulting array may be shorter then
     * the amount requested.
     * @param address starting address of the memory retrieval.
     * @param length the maximum number of bytes to retrieve.
     * @param flags the union of one of the following:
     * <dl>
     * <dt>{@link #CODE_ACCESS}
     * <dd> read from code-space; otherwise data space.
     * <dt>{@link #BYPASS_CACHE}
     * <dd> bypass the cache, otherwise, read from cache.
     * </dl>
     * @return a byte array of up to "length" bytes with the content
     * of memory. The array length can be shorter than "length" if invalid
     * memory was encountered. If the starting addres is invalid,
     * the result will be null.
     * 
     * @pre length > 0 && isActive()
     * @post $result != null && $result.length <= length ||
     *  $result == null && !isValidMemory(address)
     */
    @Override
    public native byte[] getMemoryBytes(long address, int length, int flags);

    //    /**
    //     * Return the contents of memory into a word array for a particular
    //     * amount.
    //     * <P>
    //     * The words will be endian-swapped if the host-endian order is
    //     * different from the target's.
    //     * <P>
    //     * If the start address is valid, but the memory become invalid
    //     * within the range, then the resulting array may be shorter then
    //     * the amount requested.
    //     * @param address starting address of the memory retrieval.
    //     * @param wordCount the maximum number of words to retrieve.
    //     * @return a byte array of up to "length" bytes with the content
    //     * of memory. The array length can be shorter than "length" if invalid
    //     * memory was encountered. If the starting addres is invalid,
    //     * the result will be null.
    //     *
    //     * @pre length > 0 && isActive()
    //     * @post $result != null && $result.length <= length ||
    //     * $result == null && !isValidMemory(address)
    //     */
    //    public native int[] getMemoryWords(int address, int wordCount);
    //    
    /**
     * Set a particular memory byte to a new value.
     * @param address byte address.
     * @param value new value to assign at the address.
     * @param flags a union of flags; see {@link #CODE_ACCESS}
     * and {@link #BYPASS_CACHE}.
     * @return false if the write failed.
     * 
     * @pre isValidMemory(address)
     * @post $none
     */
    @Override
    public native boolean setMemoryByte(long address, int value, int flags);
    
    /**
     * Set a range of bytes.
     * @param address byte address.
     * @param buffer where bytes are to be retrieved. 
     * @param offset offset within buffer from which to extract bytes.
     * @param length number of bytes
     * @param flags a union of flags; see {@link #CODE_ACCESS}
     * and {@link #BYPASS_CACHE}.
     * @return the number of bytes written; considered an error if less than "length".
     * 
     * @pre isValidMemory(address)
     * @pre offset >= 0 && offset + length <= buffer.length
     * @post $none
     * <P>
     * <b>NOTE:</b> Added in version 4 of the interface.
     */
    @Override
    public native int setMemoryBytes (long address, byte[] buffer, int offset, int length, int flags);

    /**
     * Set a particular memory word to a new value.
     * @param address word-aligned byte address.
     * @param value new value to assign at the address.
     * @param flags a union of flags; see {@link #CODE_ACCESS}
     * and {@link #BYPASS_CACHE}.
     * 
     * @pre address % 4 == 0 && isValidMemory(address) && isValidMemory(address+3)
     * @post $none
     */
    @Override
    public native boolean setMemoryWord(long address, int value, int flags);

    /**
     * Set variable to new value, which will be a simple scalar. For an
     * aggregate, each member must be updated individually.
     * <P>
     * If an error occurs, the an appropriate diagnostic is generated, and this
     * method returns false.
     * 
     * @param var
     *            the variable.
     * @param value
     *            the value to be assigned to the variable.
     * @return true if update was successful.
     */
    @Override
    public native boolean setVariable(Variable var, String value);

    /**
     * Set a conditional expression for a breakpoint, replacine whatever is
     * there. If the expression is null, then remove all conditions.
     * 
     * @param breakID
     *            the breakpoint whose condition is being altered.
     * @param expression
     *            the new condition or null if the breakpoint is to be made
     *            unconditional.
     */
    @Override
    public native void setBreakpointCondition(int breakID, String expression);

    /**
     * Set the breakpoint hit count.
     * 
     * @param breakID
     *            the breakpoint whose hit count is to be altered.
     * @param count
     *            the new hit count.
     */
    @Override
    public native void setBreakpointHitCount(int breakID, int count);

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPI#shutdown()
     */
    @Override
    public void shutdown() {
        terminate();
        seecodeShutdown();
    }
    /**
     * Called to wait for engine to be ready for new commands.
     */
    @Override
    public boolean waitForStop(int timeout) throws InterruptedException{
        return true;
    }
    
    /**
     * Called from {@linkplain EngineThread run thread}
     * when the engine is shutting down.
     *
     */
    @Override
    public native void seecodeShutdown();

    /*override*/
    @Override
    public boolean isStopped(int tid) {
        return queryState(tid) != RUNNING;
    }

    /*override*/
    @Override
    public native boolean restart(String args[]);
    
    @Override
    public native void setToggle(String name, boolean on);

    /*override*/
    @Override
    public native WatchpointHit[] getWatchpointHits(int tid) throws EngineException;

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPI#getErrorMessage()
     */
    @Override
    public native String[] getErrorMessage();

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPI#onShutdown()
     */
    @Override
    public void onShutdown() {
         // Called when the client acknowledges that
        // the engine has shutdown. It does nothing for
        // direct connection. For proxy connection,
        // it disconnects the socket and terminates
        // the dispatching threads.
        
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPI#setValueElement(int, int, java.lang.String)
     */
    @Override
    public native boolean setValueElement(int cookie, int elementIndex, String newValue, int frameID) throws EngineException;

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPI#getExceptionIDs()
     */
    @Override
    public native int[] getExceptionIDs() throws EngineException;

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPI#getExceptionName(int)
     */
    @Override
    public native String getExceptionName(int id) throws EngineException;

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPI#getExceptionDescription(int)
     */
    @Override
    public native String getExceptionDescription(int id) throws EngineException, IllegalArgumentException;

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPI#getExceptionHit()
     */
    @Override
    public native int getExceptionHit(int tid) throws EngineException;

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPI#setSuspendOnException(int, boolean)
     */
    @Override
    public native void setSuspendOnException(int id, boolean suspend) throws EngineException, IllegalArgumentException;

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPI#setIgnoreException(int, boolean)
     */
    @Override
    public native void setIgnoreException(int id, boolean ignore) throws EngineException, IllegalArgumentException ;

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPI#clearPendingException()
     */
    @Override
    public native void clearPendingException() throws EngineException;

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPI#throwException(int)
     */
    @Override
    public native void throwException(int id) throws EngineException;

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPI#createDisplay(java.lang.String, int)
     */
    @Override
    public native void createDisplay(int id, String kind);

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPI#invokeCommand(java.lang.String)
     */
    @Override
    public native void invokeCommand(String command);

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPI#getDisplaySelectors()
     */
    @Override
    public native String[] getDisplaySelectors();

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPI#progressDisplayCanceled(int)
     */
    @Override
    public native void progressDisplayCanceled(int progressDisplayID) throws EngineException;

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPI#closeDisplay(int)
     */
    @Override
    public native void closeDisplay(int displayID) throws EngineException ;

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPI#setArgsFilePattern(java.lang.String)
     */
    @Override
    public native void setArgsFilePattern(String path) throws EngineException;

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPI#setWorkingDirectory(java.lang.String)
     */
    @Override
    public native void setWorkingDirectory(String wd) throws EngineException;

  
    @Override
    public native void setStackFrame(int displayID, int frameID) throws EngineException;
    
    @Override
    public native int getPollingDelay();

    @Override
    public native void setBreakpointThreads (int breakID, int[] threadIDs) throws EngineException;

    @Override
    public native boolean hasThreadControl();
    
    private Object mDelayLock = new Object();
    private boolean mInterruptPollDelay = false;
    
    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param delayIfRunning
     * @return true if running.
     */
    @Override
    public boolean poll (boolean delayIfRunning) {

        boolean isStopped;
        int pollDelay;
        mInterruptPollDelay = false;
        isStopped = this.waitForStop();
        if (!isStopped && delayIfRunning) {
            // System.out.println(">>>calling waitForStop returns " + isStopped);
            pollDelay = this.getPollingDelay();

            if (pollDelay > 0) {
                try {
                    synchronized(mDelayLock) {
                        if (!mInterruptPollDelay)
                            mDelayLock.wait(pollDelay);
                    }
                }
                catch (InterruptedException e) {
                }
            }
        }
        //System.out.println(">>>pollEngine returns " + !isStopped);
        return !isStopped;

    }
    
    @Override
    public void interruptPollDelay(){
        synchronized(mDelayLock){
            mInterruptPollDelay = true;
            mDelayLock.notifyAll();
        }
    }

    @Override
    public String[] getEngineVersionStrings () throws EngineException {
        try {
            return Version.getEngineVersionStrings();
        } catch(UnsatisfiedLinkError x){
            return null;
        }
    }

    // Added in version 6.
    @Override
    public native String getSplashPath () throws EngineException;

    // Added in version 6
    @Override
    public native int getLicenseExpirationDays ();

    // Added in version 7
	@Override
    public native boolean isLicenseFailurePending() throws EngineException;

	// Added in version 8
    @Override
    public native RegisterContent getRegisterContent (int frameID, int regID) throws EngineException;

    // Added in version 8
    @Override
    public native RegisterContent[] getRegisterContentArray (int frameID, int[] regIDs) throws EngineException;
    
    // Added in Version 8
    @Override
    public native int getDefaultBank() throws EngineException;
    
    /**
     * Called during construction to indicate that this is a CMPD process with the given
     * ID. The ID will be the ordinal number of the CMPD process, starting at 1.
     * <P>
     * Added in Version 9 of the interface.
     */
    private native void setCmpdProcessID(int id);
    
    // Added in Version 10 of the interface.
    /**
     * Pass the given data along as input from stdin.
     * @param data the stdin data.
     */
    @Override
    public native void writeStdin(byte[] data);
    
    // Added Version 11
    @Override
    public native void refreshDisplay(int id) throws EngineException;
    
 // Added Version 11
    @Override
    public native void refreshDisplays() throws EngineException;

    // Added Version 12
    @Override
    public native int createWatchpoint2 (
        String var,
        int length,
        String conditional,
        int tid,
        int flags,
        int stackFrameID,
        String[] attributes) throws EngineException;

    // Added Version 12
    @Override
    public native int createWatchpointReg (int regID, String conditional, int tid, int flags, String[] attributes)
        throws EngineException;
    
    // Added version 15
    @Override
    public native int getBreakpointHitCount(int breakID) throws EngineException;

    // added version 22
    @Override
    public native void setPC (int tid, Location location) throws EngineException;
    
    // added version 24
    @Override
    public native void disconnect() throws EngineException;
    
    // added version 24
    @Override
    public native boolean canDisconnect() throws EngineException;
    
    // Added Version 27
    @Override
    public native void copyAllToClipboard(int displayID) throws EngineException;
}
