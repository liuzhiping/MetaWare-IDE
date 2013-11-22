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
package com.arc.seecode.engine;


/**
 * The adapter around the C++-based SeeCode engine. There is one such object per
 * CMPD process. The direct native connection is
 * {@link com.arc.seecode.engine.internal.EngineAPI EngineAPI}.
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
 * to be enhansed.
 * <P>
 * <B>NOTE: </B> In contrast to the previous version of SeeCode, the GUI
 * controls the engine, and is responsible for querying the engine to fill in
 * generic debugger displays. More specifically, the GUI is responsible for
 * maintaining such things as source, disassembly, register, stack-trace
 * displays, etc.
 * <P>
 * The only displays that are controlled by the engine in the old manner are
 * custom displays. The engine invokes methods in the
 * {@link ICustomDisplayCallback}to create and maintain such displays.
 * 
 * @author David Pickens
 */
public interface IEngineAPI {

    /**
     * Return true if a process is being debugged.
     * <P>
     * This method is used for assertions.
     * @return true if a process is being debugged.
     */
    public abstract boolean isActive() throws EngineException;

    /**
     * Return the CMPD process ID that this instance is controlling.
     * @return the CMPD process ID, or 0 if there is just one process.
     */
    public abstract int getPID() throws EngineException;

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
    public abstract void setEngineObserver(IEngineAPIObserver observer) throws EngineException;

    /**
     * Set an observer for breakpoint-state-change events.
     * @param observer the callback object.
     */
    public abstract void setBreakpointObserver(IBreakpointObserver observer) throws EngineException;

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
    public abstract void setCustomDisplayCallback(ICustomDisplayCallback monitor) throws EngineException;

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
    public abstract void sendValueUpdate(int displayID, String property,
            String value) throws EngineException;

    /**
     * Send a property update on behalf of a custom display, but with a timeout.
     * If the timeout (in millisecond) expires before the value can be delivered to the
     * engine, it is not sent.
     * <P>
     * If the timeout is zero, then the command will execute only if it be done immediately.
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
     * @param timeout time out in milliseconds.
     * @return true if message was sent prior to timeout; false if timeout occurred prior to being sent.
     * @pre displayID >= 0 && property != null && isActive()
     * @post $none
     * <P>
     * Added in interface 18.
     */
    public abstract boolean sendValueUpdate2(int displayID, String property,String value, int timeout)
            throws EngineException;
    /**
     * Set the engine command-line arguments. This should be
     * called prior to calling {@link #loadProgram(String[])} or {@link #attach(int,String)}.
     * 
     * @param args the command-line arguments string [need more info].
     * @pre args != null
     */
    public abstract void setEngineArguments(String args) throws EngineException;

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
     * @param command the exe path with its command-line arguments.
     * @pre command != null
     * @post $none
     * @return true if program loaded okay. Otherwise, an error
     * was presumably sent to the {@linkplain IEngineObserver callback}.
     */
    public abstract boolean loadProgram(String command[]) throws EngineException;
    
    /**
     * If a program is running or has terminated, then restart
     * it.
     * <P>
     * The difference between a restart and a terminate/loadProgram
     * is that the break table is not cleared.
     * @param args new arguments to apply, or null if that previous
     * arguments are to be reused.
     * @return true if restart was successful.
     * @throws EngineException if some sort of error occurs.
     */
    public abstract boolean restart(String args[]) throws EngineException;

    /**
     * Given a machine address (that is presumably in code),
     * return a {@link Location} object with as much information filled
     * in as possible.
     * @param address a machine address.
     * @return {@link Location} object with as much information
     * filled in as possible.
     */
    public abstract Location computeLocation(long address) throws EngineException;

    /**
     * A message ID that can be set by the call to
     * {@link #lookupSource(String,int)} to denote
     * a missing source file. Its argument is the
     * name of the file.
     */
    public static final String SOURCE_FILE_NOT_FOUND = "SOURCE_FILE_NOT_FOUND";
    /**
     * A message ID that can be set by the call to
     * {@link #lookupSource(String,int)} to denote
     * an ambiguous source file. It has 3 arguments:
     * the source file, and 2 for which it is ambiguous.
     */
    public static final String SOURCE_FILE_IS_AMBIGUOUS = "SOURCE_FILE_IS_AMBIGUOUS";
    /**
     * A message ID that can be set by the call to
     * {@link #lookupSource(String,int)} to denote
     * a line number that is out of range.
     */
    public static final String LINE_NOT_VALID_IN_SOURCE = "LINE_NOT_VALID_IN_SOURCE";
    /**
     * A message ID that can be set by the call to
     * {@link #lookupUniqueSource(String,int)} to denote
     * a line number that is ambiguous due to being inlined in multiple places.
     */
    public static final String LINE_NOT_UNIQUE_IN_SOURCE = "LINE_NOT_UNIQUE_IN_SOURCE";
    /**
     * A message ID that can be set by the call to
     * {@link #lookupVariable(String,int)} to indicate that
     * the stack frame ID doesn't appear to be valid.
     * It has one argument, the variable name.
     */
    public static final String UNKNOWN_SCOPE = "UNKNOWN_SCOPE";
    /**
     * A message ID that can be set by the call to
     * {@link #lookupVariable(String,int)} to indicate that
     * the variable is not declared, or is out of scope.
     * It has one argument, the variable name.
     */
    public static final String VARIABLE_NOT_FOUND = "VARIABLE_NOT_FOUND";
    /**
     * Return an error message that was generated from
     * the last call to one of the following methods:
     * <dl>
     * <dt> {@link #evaluateLocation(String,int)}
     * <dt> {@link #lookupSource(String,int)}
     * <dt> {@link #evaluate(String,int)}
     * <dt> {@link #lookupVariable(String,int)}
     * <dt> {@link #setVariable(Variable,String)}
     * <dt> {@link #setValueElement(int,int,String,int)}
     * <dt> {@link #setRegisterValue(int,int,String)}
     * <dt> {@link #lookupGlobalVariable(String)}
     * <dt> {@link #createWatchpoint(String,int,String,int,int,int)}

     * </dl>
     * If no error occurred, then this methods results
     * are undefined.
     * <P>
     * The result consists of a single message in English (result array will
     * have one element),
     * or a message-ID followed by arguments (the result array
     * will have 1 or more elements). The message-ID must
     * be one of the following, or else it will be
     * interpreted as plain Enlish:
     * <dl>
     * <dt>SYMBOL_NOT_DECLARED <i>symbol</i>
     * <dd> indicates that the symbol is not declared 
     * in the associated context.
     * <dt> {@link #SOURCE_FILE_NOT_FOUND} <i>source-file-name</i>
     * <dd> indicates that the given source file wasn't
     * found.
     * <dt> {@link #SOURCE_FILE_IS_AMBIGUOUS} <i>original, file1, file2</i>
     * <dd> indicates that the given file is ambiguous with at
     * least two others.
     * <dt> {@link #LINE_NOT_VALID_IN_SOURCE} <i>source, line</i>
     * <dd> indicates that the line is out of range.
     * <dt> SYMBOL_IS_NOT_LOCATION <i>symbol</i>
     * <dd> Symbol does not have a static location.
     * <P>
     * Its up to the caller to deal with thread-synchronization issues
     * in invoking this message, after invoking a method
     * that affects it.
     * @return error message pertaining to last
     * evaluation call.
     */
    public abstract String[] getErrorMessage() throws EngineException;
    /**
     * Evaluate an expression that is to reference a code location
     * (e.g., as required for a breakpoint).
     * <P>
     * This method is called from {@link StackFrameRef#evaluateLocation(String)}.
     * <P>
     * The resulting location will have source/line information
     * filled in if available.
     * <P>
     * If an error should occur, then the method returns
     * <code>null</code> and an error string can
     * be retrieved by calling {@link #getErrorMessage()}.
     * @param expression the expression to evaluate.
     * @param stackFrameID context of the lookup.
     * @return the {@link Location} object with as much information
     * filled in as possible, or <code>null</code>.
     */
    public abstract Location evaluateLocation(String expression,
            int stackFrameID) throws EngineException;

    /**
     * Given a source path and a line number, return a
     * {@link Location} object with as much information filled in
     * as possible.
     * <P>
     * The source path may be relative, in which case the
     * {@linkplain #setSourceDirectories(String[]) source directories}
     * will be searched for the filename.
     * <P>
     * If an error should occur in evaluating the source or
     * line, then <code>null</code> will be returned and the
     * method {@link #getErrorMessage()} will contain
     * the an appropriate messages as to the cause.
     * @param source a source path, possibly relative.
     * @param line a line number.
     * @return a
     * {@link Location} object with as much information filled in
     * as possible, or <code>null</code>.
     */
    public abstract Location lookupSource(String source, int line) throws EngineException;

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
    public abstract boolean attach(int cmpdID, String pid) throws EngineException;

    /**
     * A process state that denotes that the process is running.
     * This is one of the possible values returned by 
     * {@link #queryState}.
     */
    public static final int RUNNING = 0;

    /**
     * A process state that denotes that the process id is not valid.
     * This is one of the possible values returned by 
     * {@link #queryState}.
     */
    public static final int INVALID = -1;

    /**
     * A process state that denotes that the process was stopped
     * by a user request.
     * This is one of the possible values returned by 
     * {@link #queryState}.
     */
    public static final int STOPPED_BY_USER = 1;

    /**
     * A process state that denotes that the process was stopped
     * by hitting a a location breakpoint. The actual breakpoint can
     * be retrieved by calling {@link #getBreakpointHit}.
     * <P>
     * This is one of the possible values returned by 
     * {@link #queryState}.
     */
    public static final int BREAKPOINT_HIT = 2;

    /**
     * A process state that denotes that the process was stopped
     * by hitting a watchpoint. The actual watchpoint can
     * be retrieved by calling {@link #getWatchpointHits}.
     * <P>
     * This is one of the possible values returned by 
     * {@link #queryState}.
     */
    public static final int WATCHPOINT_HIT = 3;

    /**
     * A process state that denotes that the process was 
     * loaded, but not yet started.
     * <P>
     * This is one of the possible values returned by 
     * {@link #queryState}.
     */
    public static final int NOT_STARTED = 4;

    /**
     * A process state that denotes that the process was 
     * stopped for some unknown reason.
     * <P>
     * This is one of the possible values returned by 
     * {@link #queryState}.
     */
    public static final int UNKNOWN = 5;

    /**
     * A process state that denotes termination.
     * <P>
     * This is one of the possible values returned by
     * {@link #queryState}.
     */
    public static final int TERMINATED = 6;

    /**
     * A process state that denotes a step operation was completed.
     * <P>
     * This is one of the possible values returned by
     * {@link #queryState}.
     */
    public static final int STEPPED = 7;
    
    /**
     * A process state that denotes that the engine has shutdown.
     * <P>
     * This is one of the possible values returned by
     * {@link #queryState}.
     */
    public static final int SHUTDOWN = 8;
    
    /**
     * A process state that denotes that an exception
     * has been encountered for which the engine was
     * to suspend execution.
	 *<P>
     * This is one of the possible values returned by
     * {@link #queryState}.
     */
    public static final int EXCEPTION_HIT = 9;
    
    /**
     * A process state that denotes that the engine has
     * aborted for some unknown reason.
     *<P>
     * This is one of the possible values returned by
     * {@link #queryState}.
     */
    public static final int DISCONNECTED = 10;
    
    /**
     * A state to indicate that a "step" command has been issued to the engine,
     * but the engine hasn't yet responded.
     */
    public static final int STEP_PENDING = 11;
    
    /**
     * A state to indicate that a "resume" command has been issued to the engine,
     * but the engine hasn't yet responded.
     */
    public static final int RESUME_PENDING = 12;
    
    /**
     * A cached value that indicates that the query state needs to be refreshed.
     */
    public static final int UNDEFINED = 0xDEADBEEF;
    

    /**
     * Query the state of a particular thread.
     * <P>
     * If independent thread control is not supported, then the argument is
     * ignored, and this method returns the state of the process.
     * <P>
     * If the process is stopped on a breakpoint, then
     * {@link #getBreakpointHit} can be called to get the breakpoint ID.
     * <P>
     * If the process is stopped on a watchpoint, then
     * {@link #getBreakpointHit} can be called to get the watchpoint ID.
     *  <P>
     * If the process is stopped by an exception, then
     * {@link #getExceptionHit} can be called to get the exception ID.
     *
     * <P>
     * @return the state of the process as enumerated by
     * {@link #RUNNING}, {@link #STOPPED_BY_USER}, {@link #BREAKPOINT_HIT},
     * {@link #WATCHPOINT_HIT}, {@link #NOT_STARTED}, 
     * {@link #EXCEPTION_HIT}, or {@link #INVALID} to denote a non-existent pid.
     * 
     * @pre isActiveThread(tid)
     * @post $result == RUNNING || $result==INVALID || $result==STOPPED_BY_USER ||
     *     $result==NOT_STARTED || $result == BREAKPOINT_HIT ||
     *     $result==WATCHPOINT_HIT || $result == UNKNOWN ||
     *     $result==TERMINATED || $result == EXCEPTION_HIT
     */
    public abstract int queryState(int tid) throws EngineException;

    /**
     * Return the breakpoint ID if the thread stopped due to hitting
     * a breakpoint.
     * @param tid the thread ID that was stopped by a breakpoint.
     * @return the breakpoint id or 0 if no breakpoint is active.
     * @pre queryState(tid) == BREAKPOINT_HIT 
     * @post $result > 0
     */
    public abstract int getBreakpointHit(int tid) throws EngineException;

    /**
     * Return the watchpoint IDs if the thread stopped due to hitting
     * a watchpoint. More than one watchpoint can contribute to
     * the top.
     * @param tid the thread ID that was stopped by a watchpoint.
     * @return the watchpoint hits.
     * @pre queryState(tid) == WATCHPOINT_HIT 
     */
    public abstract WatchpointHit[] getWatchpointHits(int tid) throws EngineException;

    /**
     * If the engine does not support indenpendent thread control, then
     * this method will cause all threads of the process to stop. The engine will
     * invoke {@link IEngineObserver#processStopped processStopped} 
     * when the processor actually stops.
     * <P>
     * If the engine <em>does</em> support independent thread control, then only the
     * designated thread will be stopped, unless 0 is specified. 0 is interpreted to mean
     * stop all threads. As each thread is stopped, the method
     * {@link IEngineObserver#threadStopped} will be invoked.
     * <P>
     * If the process/thread is already stopped, the engine's
     * action is undefined.
     * @param tid the thread to be stopped, or 0 if all threads are to be stopped.
     * @pre tid == 0 || isActiveThread(tid)
     * @pre !isStopped(tid)
     * @post $none
     */
    public abstract void stop(int tid) throws EngineException;

 

    /**
     * Returns true if underlying process is a simulator.
     * @return true if process is simulator-based.
     */
    public abstract boolean isSimulator() throws EngineException;
    
    /**
     * Returns true if the engine supports independent thread control.
     * If false, then all threads are stopped when any of them stop, and
     * all are resumed when any are resumed.
     *
     * @return whether or not the engine supports independent thread control.
     * @throws EngineException
     */
    public abstract boolean hasThreadControl() throws EngineException;

    /**
     * Resume a process that is stopped. If the engine does not support independent thread control,
     * then all threads will be resumed by this method and the method
     *  {@link IEngineObserver#processResumed(EngineInterface)} will be invoked when the process actually resumes.
     *  
     * <P>
     * If independent thread control <em>is</em> supported, then only the given thread will be
     * resumed, unless <code>tid</code> is 0. Any others that are stopped will remain stopped. As the
     * thread actually resumes, the method {@link IEngineObserver#threadResumed} will be invoked.
     * <P>
     * A thread ID of 0 means to resume all stopped threads.
     * 
     * <P>
     * If the thread is not stopped, the engine's
     * action is undefined.
     * 
     * @exception EngineException engine is not responding.
     * 
     * @pre tid == 0 || isActiveThread(tid)
     * @pre isStopped(tid)
     */
    public abstract void resume(int tid) throws EngineException;

    /**
     * Perform an instruction step.
     * <P>
     * The engine is expected to call {@link IEngineObserver#processResumed(EngineInterface) IEngineObserver.processResumed}/
     * {@link IEngineObserver#processStopped IEngineObserver.processStopped}  in
     * succession.
     * @param tid the id of the thread, or 0 if all threads are to
     * be stepped.
     * @param over if true, step over calls, otherwise step into calls.
     * @param cnt the number of consecutive steps to take (must be >= 1).
     * @exception EngineException engine is not responding.
     * 
     * @pre isStopped(tid)
     * 
     */
    public abstract void instructionStep(int tid, boolean over, int cnt) throws EngineException;

    /**
     * Perform a source statement step.
     * <P>
     * The engine is expected to call {@link IEngineObserver#processResumed(EngineInterface) IEngineObserver.processResumed}/
     * {@link IEngineObserver#processStopped IEngineObserver.processStopped}  in
     * succession.
     * @param tid the id of the thread, or 0 if all threads are to
     * be stepped.
     * @param over if true, step over calls, otherwise step into calls.
     * @param cnt the number of consecutive steps to take (must be >= 1).
     * @exception EngineException engine is not responding.
     * @pre isActive()
     * @post $none
     */
    public abstract void statementStep(int tid, boolean over, int cnt) throws EngineException;

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
     * 
     * @pre isActive()
     */
    public abstract int[] getThreads() throws EngineException;

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
    //        return Evaluator.computeLocation(expression,this) throws EngineException;
    //        
    //    }

    /**
     * Breakpoint flag to denote an ordinary breakpoint.
     * Passed as flags argument to {@link #createBreakpoint(Location,int,String,int,int)}.
     */
    final static public int BP_REGULAR = 0x0;

    /**
     * Breakpoint flag to denote a temporary breakpoint.
     * Passed as flags argument to {@link #createBreakpoint(Location,int,String,int,int)}.
     */
    final static public int BP_TEMPORARY = 0x1;

    /**
     * Breakpoint flag to denote a hardware breakpoint.
     * Passed as flags argument to {@link #createBreakpoint(Location,int,String,int,int)}.
     */
    final static public int BP_HARDWARE = 0x2;
    
    /**
     * Breakpoint flag to denote a software breakpoint.
     * Passed as flags argument to {@link #createBreakpoint(Location,int,String,int,int)}.
     */
    final static public int BP_SOFTWARE = 0x4;
    
    /**
     * Breakpoint flag to indicate that the watchpoint is to be created in a disabled state.
     * Passed as flags argument to {@link #createBreakpoint}.
     * <P>
     * <B>NOTE</B>: this flags was added in version 13 of this interface.
     */
    final static public int BP_DISABLED = 0x10;

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
    public abstract int createBreakpoint(Location location, int hitCount,
            String conditional, int tid, int flags) throws EngineException;
    
    /**
     * Breakpoint flag to denote an ordinary breakpoint.
     * Passed as flags argument to {@link #createWatchpoint(String,int,String,int,int,int)}.
     */
    final static public int WP_WRITE = 0x4;

    /**
     * Breakpoint flag to denote a temporary breakpoint.
     * Passed as flags argument to {@link #createWatchpoint(String,int,String,int,int,int)}.
     */
    final static public int WP_READ = 0x8;

    /**
     * Breakpoint flag to denote a hardware breakpoint.
     * Passed as flags argument to {@link #createWatchpoint(String,int,String,int,int,int)}.
     */
    final static public int WP_HARDWARE = BP_HARDWARE;
    
    /**
     * Watchpiont flag to indicate that the watchpoint is to be created in a disabled state.
     * Passed as flags argument to {@link #createWatchpoint(String,int,String,int,int,int)}.
     * <P>
     * <B>NOTE</B>: this flags was added in version 13 of this interface.
     */
    final static public int WP_DISABLED = BP_DISABLED;
    
    /**
     * Create a watchpoint. 
     * If an error occurs, the method {@link IEngineObserver#displayError(EngineInterface,String)}
     * is invoked by the engine.
     * <P>
     * If an error should occur in evaluating the
     * variable expression, 0 will be returned an the
     * method {@link #getErrorMessage()} may be returned
     * to get the reason.
     * <P>
     * @param var variable on which watchpoint is to be set.
     * @param length the number of bytes to watch, or 0 if the
     * length is to be derived from the variable.
     * @param conditional if not null, an expression to be evaluated
     * to true before watchpoint takes.
     * @param tid the thread ID if the breakpoint is to be tied to
     * a thread, or 0 if breakpoint is to be applied globally
     * @param flags union of {@link #WP_WRITE}, {@link #WP_READ}, or {@link #BP_HARDWARE}.
     * @param stackFrameID the stack frame in which to evaluate the
     * variable expression.
     * @return a watchpoint ID number, or 0 if an error occured.
     * 
     * @pre location != null 
     * @post isValidBreakpoint($result)
     */
    public abstract int createWatchpoint(String var, 
            int length,
            String conditional, int tid, int flags, int stackFrameID) throws EngineException;
    
    /**
     * This method, similar to {@link #createWatchpoint} but also supports additional
     * attributes, e.g., "mask=0x..." and "value=0x...".
     * <P>
     * This method was added in version 12 of the interface to support mask and value.
     * <P>
     * @param var variable on which watchpoint is to be set.
     * @param length the number of bytes to watch, or 0 if the
     * length is to be derived from the variable.
     * @param conditional if not null, an expression to be evaluated
     * to true before watchpoint takes.
     * @param tid the thread ID if the breakpoint is to be tied to
     * a thread, or 0 if breakpoint is to be applied globally
     * @param flags union of {@link #WP_WRITE}, {@link #WP_READ}, or {@link #WP_HARDWARE}.
     * @param stackFrameID the stack frame in which to evaluate the
     * variable expression.
     * @param attributes a list of attributes of the form "key=value".
     * @return a watchpoint ID number, or 0 if an error occured.
     * @throws EngineException
     */
    public abstract int createWatchpoint2(String var, 
        int length,
        String conditional, int tid, int flags, int stackFrameID,
        String[] attributes) throws EngineException;
    
    /**
     * This method, similar to {@link #createWatchpoint2} but applies to
     * a register instead of a memory location.
     * <P>
     * This method was added in version 12 of the interface to support mask and value.
     * <P>
     * @param regID the ID of the register.
     * @param conditional if not null, an expression to be evaluated
     * to true before watchpoint takes.
     * @param tid the thread ID if the breakpoint is to be tied to
     * a thread, or 0 if breakpoint is to be applied globally
     * @param flags union of {@link #WP_WRITE}, {@link #WP_READ}, or {@link #WP_HARDWARE}.
     * @param attributes a list of attributes of the form "key=value".
     * @return a watchpoint ID number, or 0 if an error occured.
     * @throws EngineException
     */
    public abstract int createWatchpointReg(int regID, 
        String conditional, int tid, int flags,
        String[] attributes) throws EngineException;


    /**
     * Called when a {@link LocationBreakpoint} object that wraps the
     * breakpoint ID is garbage-collected.
     * @param breakID
     */
    public abstract void freeBreakpoint(int breakID) throws EngineException;

    /**            
     * Set whether or not a breakpoint/watchpoint is to be enabled.
     * <P>
     * The method {@link IBreakpointObserver#breakpointStateChanged(int,boolean)}
     * will be called as a side-effect, if the state is indeed changed.
     * @param id breakpoint or watchpoint id number.
     * @param v true if enabling the breakpoint; false otherwise.
     * 
     * @pre isValidBreakpoint(id)
     * @post $none
     */
    public abstract void setBreakpointEnabled(int id, boolean v) throws EngineException;


    /**            
     * Delete a break- or watchpoint.
     * <P>
     * The {@link IBreakpointObserver#breakpointRemoved(int)} method
     * will be called as a sideefect.
     * @param breakID breakpoint to delete.
     * @pre isValidBreakpoint(breakID)
     * @post !isValidBreakpoint(breakID)
     */
    public abstract void removeBreakpoint(int breakID) throws EngineException;


    /**
     * Return true if the given break- or watch-point ID is valid.
     * <P>
     * This method is used for assertion tests.
     * @param breakID breakpoint ID to check
     * @return true if the breakpoint ID is valid.
     * 
     * @pre isActive()
     * @post $none
     */
    public abstract boolean isValidBreakpoint(int breakID) throws EngineException;


    /**
     * Terminate the existing process.
     * The method {@link IEngineObserver#processTerminated(EngineInterface)} will
     * be invoked some time in the near future.
     * 
     * @pre isActive()
     * @post $none
     *
     */
    public abstract void terminate() throws EngineException;
    
    /**
     * Shutdown the engine altogether.
     * It will no longer take commands after this
     * method is executed.
     *
     */
    public abstract void shutdown() throws EngineException;

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
    public abstract boolean isActiveThread(int tid) throws EngineException;

    /**
     * Return the local variables, including arguments, for a stack frame, if known.
     * Otherwise, returns null.
     * <P>
     * <B>NOTE:</B> this method is only called from 
     * {@link StackFrameRef#getLocals()}.
     * 
     * @param regSetSnapshot a register set contents.
     * 
     * @pre isValidRegSet(regSetSnapshot)
     * @post $none
     */
    public abstract Variable[] getLocals(int regSetSnapshot) throws EngineException;

    /**
     * Return all static/global variables that
     * are within scope from a particular program location.
     * <P>
     * TODO: handle thread-local variables?
     * <P>
     * @param pc the PC location to identify the scope from which
     * the variables are to be sought.
     * @return all static/global variables that are within scope
     * from a particular program counter.
     */
    public abstract Variable[] getNonlocals(long pc) throws EngineException;

    /**
     * Return whether or not the given register set snapshot is valid.
     * <P>
     * It becomes invalid when the associated thread steps out
     * of the stackframe, or makes any change to the registers.
     * <P>
     * @param stackFrameID a stackframe ID.
     * @return whether or not a register set snaptshot is valid.
     */
    public abstract boolean isValidStackFrameID(int stackFrameID) throws EngineException;

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
    public abstract int getProcessExitCode() throws EngineException;

    /**
     * Evaluate an expression in the context of a stopped thread's
     * stack frame.
     * Returns a value, or
     * null if the expression could not be evaluated.
     * <P>
     * If the expression can't be evaluated, then
     * {@link #getErrorMessage()} can be called to get
     * the reason.
     *  <P>
     * <B>NOTE:</B> this method is only called from
     * {@link StackFrameRef#evaluate(String)}.
     * <P>
     * <P>
     * TODO: once we have a C++ parser in Java, this method
     * no longer be called. The general expression evaluation will
     * be done from within a Java package.

     * @param expression the expression to be evaluated
     * @param regSetSnapshot the associated register-set contents.
     * @return the result of the evaluation, or null.
     * 
     * @pre isStopped()
     * @pre expression != null
     * @post $none
     */
    public abstract Value evaluate(String expression, int regSetSnapshot) throws EngineException;

    /**
     * This method is called by {@link Value#getElement(int)} to
     * lazily retrieve the elements of an aggregate value.
     * @param cookie a "cookie" that the engine set in the
     * parent {@link Value} that identifies the expression object.
     * @param elementIndex the element count.
     * @return the value that is an element of parent value.
     */
    public abstract Value getValueElement(int cookie, int elementIndex) throws EngineException;

    /**
     * This method is called by {@link Value#setElement(int,String,StackFrameRef)} to
     * set the value of a scalar elements of an aggregate.
     * @param cookie a "cookie" that the engine set in the
     * parent {@link Value} that identifies the expression object.
     * @param elementIndex the element count.
     * @param newValue the new value for the element.
     * @param frameID frame in which value is evaluated.
     * @return true if successful, otherwise {@link #getErrorMessage()} gives
     * the reason for the failure.
     */
    public abstract boolean setValueElement(int cookie, int elementIndex, String newValue, int frameID) throws EngineException;
    /**
     * Free underlying resource for an expression value.
     * Called from {@link Value#finalize()} during garbage collection.
     * Thus, a cookie can only be referenced from a single value.
     * @param cookie value cookie to be freed up.
     * @exception EngineException engine is not responding.
     */
    public abstract void freeValueCookie(int cookie) throws EngineException;

    /**
     * Given an expression that should denote a variable, return
     * a corresponding {@link Variable} object.
     * <P>
     * If the expression can't be evaluated for some reason,
     * then <code>null</code> will be returned and 
     * the method {@link #getErrorMessage()} can be
     * invoked to get the reason.
     * <P>
     * <B>NOTE:</B> this method is only called from
     * {@link StackFrameRef#lookupVariable(String)}.
     * <P>
     * @param name name of the variable to be looked up.
     * @param regSetSnapshot a register-set contents.
     * @return corresponding Variable object, or null if the name
     * doesn't correspond to a variable.
     * 
     * @pre isStopped()
     * @pre name != null
     * @post $none
     */
    public abstract Variable lookupVariable(String name, int regSetSnapshot) throws EngineException;
    
    /**
     * Lookup a global variable and return an
     * object that denotes it, or <code>null</code> if
     * the variable does not exist.
     * <P>
     * If the variable does not exist.
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
    public abstract Variable lookupGlobalVariable(String name) throws EngineException;

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
    public abstract int[] getExecutableLines(String source, int rangeLo,
            int rangeHi) throws EngineException;

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
    public abstract void start() throws EngineException;

    /**
     * Given a stopped thread, make it step out of the function that
     * it is currently stopped in.
     * 
     * <P>
     * <B>NOTE:</B> because of the engine's single-threaded design,
     * this method will not return until the process has stopped. 
     * Therefore, it should be called from a "run" thread
     * that is distinct from the AWT event thread or a command-line
     * monitor loop.
     * @param tid the thread to be stepped out of, or 0 if all threads
     * are to be operated upon.
     * @exception EngineException engine is not responding.
     * 
     * @pre tid == 0 || isActiveThread(tid)
     * @pre isStopped(tid)
     */
    public abstract void stepOut(int tid) throws EngineException;

    /**
     * Run the given thread, or all threads, until a particular
     * location is reached.
     * 
     * <P>
     * <B>NOTE:</B> because of the engine's single-threaded design,
     * this method will not return until the process has stopped. 
     * Therefore, it should be called from a "run" thread
     * that is distinct from the AWT event thread or a command-line
     * monitor loop.
     * @param tid the thread to run, or 0, if all threads are to run.
     * @param location location that is to trigger a stop.
     * @exception EngineException engine is not responding.
     * 
     * @pre tid == 0 || isActive(tid) 
     * @pre isStopped(tid)
     * @pre location != null
     * @post $none
     */
    public abstract void runToAddress(int tid, Location location) throws EngineException;

    /**
     * Return the instruction counter of the given stopped thread.
     * <P>
     * <B>NOTE:<B> this method is only called from 
     * {@link StackFrameRef#getPC()}.
     * @param regSetSnapshot the register-set contents.
     * @return the address of the instruction counter.
     * 
     * @pre isStopped()
     * @post $none
     */
    public abstract long getPC(int regSetSnapshot) throws EngineException;

    /**
     * Called when the lone reference to the stackframe ID is
     * being garbage collected. (See {@link StackFrameRef#finalize()}.)
     * @param stackFrameID the stackframeID ID being freed.
     * @exception EngineException engine is not responding.
     */
    public abstract void freeStackFrameID(int stackFrameID) throws EngineException;

    /**
     * Return the stack pointer of the given stopped thread.
     * <P>
     * <B>NOTE:<B> this method is only called from 
     * {@link StackFrameRef#getStackPointer()}.
     * @param regSetSnapshot the register-set ID.
     * @return the stack pointer value.
     * @post $none
     */
    public abstract long getStackPointer(int regSetSnapshot) throws EngineException;

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
    public abstract String getThreadName(int tid) throws EngineException;

    /**
     * Return the frame pointer of the given stopped thread.
     * @param regSetSnapshot the register set from which the frame
     * pointer is being extracted.
     * 
     * @return the stack pointer value.
     * @pre tid == 0 || isActiveThread(tid)
     * @pre isStopped(tid)
     * @post $none
     */
    public abstract long getFramePointer(int regSetSnapshot) throws EngineException;

    /**
     * Return the number of register banks that the target machine
     * supports.
     * @return the number of register banks that the target machine
     * supports.
     * 
     * @pre $none
     * @post $result > 0
     */
    public abstract int getRegisterBankCount() throws EngineException;

    /**
     * Return the name of a register bank.
     * @param bank a register bank number, starting with 0
     * @return the name of the bank.
     * @pre bank >= 0 && bank < getRegisterBankCount()
     * @post $result != null
     */
    public abstract String getRegisterBankName(int bank) throws EngineException;

    /**
     * Retrieve the register ID's within a bank.
     * <P>
     * <B>NOTE:</B> register IDs must be unique across all
     * register banks.
     * @return an array of register IDs associated with a bank.
     * @pre bank >= 0 && bank < getRegisterBankCount()
     * @post $result != null
     */
    public abstract int[] getRegisterIDsFromBank(int bank) throws EngineException;

    /**
     * @return whether or not a register bank is active.
     * @pre bank >= 0 && bank < getRegisterBankCount()
     */
    public abstract boolean isRegisterBankActive(int bank) throws EngineException;

    /**
     * Given a register ID, return the register's name.
     * @param regID one of the ID's returned from {@link #getRegisterIDsFromBank(int)}.
     * @return the name of the register (e.g. "r10").
     */
    public abstract String getRegisterName(int regID) throws EngineException;

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
     * @param stackframeID the register-set ID.
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
    public abstract String getRegisterValue(int stackframeID, int regID,
            int format) throws EngineException;

    /**
     * Set an integer-like register to a value.
     * <P>
     * Only the top most stackframe of a thread can have its
     * registers altered.
     * @param frameID the top-most stack frame of a the
     * associated suspended thread.
     * @param regID the register ID.
     * @param value the new value of the register.
     * @return true if successfully; false if the register could not
     * be set, in which case {@link #getErrorMessage()} will
     * have the reason.
     * 
     * @pre isValidRegister(bank,regNo) && value != null
     * @post $none
     */
    public abstract boolean setRegisterValue(int frameID, int regID, String value) throws EngineException;

    /**
     * Return an array of module ID numbers.
     * @return an array of module ID numbers.
     * 
     * @pre isActive()
     * @post $result != null
     */
    public abstract int[] getModules() throws EngineException;

    /**
     * Return the name of a module as a short name that would appear
     * in a display.
     * @param moduleID id of module
     * @return the name of the module.
     * 
     * @pre isValidModuleID(moduleID)
     * @post $result != null
     */
    public abstract String getModuleName(int moduleID) throws EngineException;

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
    public abstract long getModuleBaseAddress(int moduleID) throws EngineException;

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
    public abstract long getModuleSize(int moduleID) throws EngineException;

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
    public abstract Location[] getFunctionsWithinModule(int moduleID) throws EngineException;

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
    public abstract AssemblyRecord[] disassemble(long addr, int instructionCount) throws EngineException;

    /**
     * Set source directories that the engine is to use to location
     * source files. When the engine returns a source location,
     * it includes the full path.
     * @param dirs source directories.
     * 
     * @pre dirs != null
     * @post $none
     */
    public abstract void setSourceDirectories(String dirs[]) throws EngineException;

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
    public abstract boolean isValidMemory(long address, int flags) throws EngineException;

    /**
     * A flag passed to {@link #getMemoryBytes} to
     * indicate that code is being read from. Otherwise, data.
     */
    public static final int CODE_ACCESS = 1 << 0;

    /**
     * A flag passed to {@link #getMemoryBytes} to
     * indicate that the cache is to be bypassed and the actual
     * RAM is to be read.
     */
    public static final int BYPASS_CACHE = 1 << 1;

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
    public abstract byte[] getMemoryBytes(long address, int length, int flags) throws EngineException;

    /**
     * Set a particular byte to a new value.
     * @param address address of the byte.
     * @param value the new value of the byte.
     * @param flags the union of one of the following:
     * <dl>
     * <dt>{@link #CODE_ACCESS}
     * <dd> read from code-space; otherwise data space.
     * <dt>{@link #BYPASS_CACHE}
     * <dd> bypass the cache, otherwise, read from cache.
     * </dl>
     * @return true if successful.
     * @throws EngineException
     */
    public abstract boolean setMemoryByte(long address, int value, int flags) throws EngineException;
    
    /**
     * Set an array of bytes.
     * @param address address where the data is to be written.
     * @param buffer the source of the data.
     * @param offset the offset within the buffer where the data is to be extracted.
     * @param length the number of bytes to set.
     * @param flags the union of one of the following:
     * <dl>
     * <dt>{@link #CODE_ACCESS}
     * <dd> read from code-space; otherwise data space.
     * <dt>{@link #BYPASS_CACHE}
     * <dd> bypass the cache, otherwise, read from cache.
     * </dl>
     * <P>
     * NOTE: care must be taken to avoid making "length" too large. It blocks the
     * main GUI thread until the operation is completed. The caller should divide the
     * call into modest-sized chunks if necessary.
     * @return number of bytes actually written; will be less than "length" if error occurred.
     * @throws EngineException something went wrong.
     */
    public abstract int setMemoryBytes(long address, byte[] buffer, int offset, int length, int flags) throws EngineException;

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
    public abstract boolean setMemoryWord(long address, int value, int flags) throws EngineException;

    /**
     * Set variable to new value, which will be 
     * a simple scalar. For an aggregate, each member must be
     * updated individually.
     * <p>
     * If the expression can't be evaluated or the
     * variable can't be updated, then
     * {@link #getErrorMessage()} can be called to get
     * the reason.
     *  <P>
     * @param var the variable.
     * @param value the value to be assigned to the variable.
     * @return true if update was successful.
     */
    public abstract boolean setVariable(Variable var, String value) throws EngineException;

    /**
     * Set a conditional expression for a breakpoint, replacine whatever
     * is there. If the expression is null, then remove all conditions.
     * @param breakID the breakpoint whose condition is being altered.
     * @param expression the new condition or null if the breakpoint
     * is to be made unconditional.
     */
    public abstract void setBreakpointCondition(int breakID, String expression) throws EngineException;

    /**
     * Set the breakpoint hit count.
     * @param breakID the breakpoint whose hit count is to be altered.
     * @param count the new hit count.
     */
    public abstract void setBreakpointHitCount(int breakID, int count) throws EngineException;
    
    /**
     * Set the breakpoint threads filters
     * @param breakID the break point whose thread filters are being altered.
     * @param threadIDs thread IDs
     */
    public abstract void setBreakpointThreads(int breakID, int threadIDs[]) throws EngineException;
    
    /**
     * Create a stackframe ID to refernece the top stackframe
     * of the given thread.
     * <P>
     * The resulting ID will consume resources until
     * freed by calling {@link #freeStackFrameID(int)}. 
     * <P>
     * One way to do this is to make sure only single
     * object references it (e.g., {@link StackFrameRef},
     * and have its {@link StackFrameRef#finalize() finalize} method free it.
     * 
     * @param tid
     *            the thread ID.
     * @return a new stackframe ID that references the top
     * stackframe of the thread.
     */
    public abstract int makeStackFrameID(int tid) throws EngineException;
    
    /**
     * Given a stackframe ID, compute a stackframe of the
     * caller.
     * <P>
     * This method is only called from {@link StackFrameRef#getCallerFrame()},
     * which is responsible for freeing the result by calling
     * {@link #freeStackFrameID(int)}.
     * 
     * @param stackFrameID
     *            a stackframe ID.
     * @return the stackframe ID of the caller, or 0 if
     * there is no caller, or the caller could not be computed.
     */
    public abstract int computeCallerFrame(int stackFrameID) throws EngineException;
    
    /**
     * Wait for the engine to be in a stopped state. If this method returns true, then
     * all threads of the application being debugged are stopped.
     * @param timeoutMillis maximum number of milliseconds to wait.
     * @return true if the engine stopped before the timeout expired.
     */
    public abstract boolean waitForStop(int timeoutMillis) throws InterruptedException, EngineException;
    
    /**
     * Set a engine tracing toggle.
     * @param toggleName name of toggle.
     * @param on if true, turns it on, otherwise turns it off.
     */
    public abstract void setToggle(String toggleName, boolean on) throws EngineException;
    
    /**
     * Called in acknowledgement that the engine has shutdown.
     * For the {@linkplain com.arc.seecode.client.Client proxy} implementation
     * of this interface, it terminates the dispatchings threads.
     */
    public abstract void onShutdown();
    
    /**
     * Return the ID's of all potential exceptions (i.e., UNIX "signals")
     * that can be fired. The debugger can be made to
     * stop for such.
     * @return the array of all exception IDs that are
     * recognized.
     */
    public abstract int[] getExceptionIDs() throws EngineException;
    
    /**
     * Return the mnemonic name of an exception (e.g. "SIGSEGV")
     * @param id the exception ID returned by {@link #getExceptionIDs()}.
     * @return the mnemonic name of the exception.
     * @throws IllegalArgumentException if id is not one of those returned
     * by {@link #getExceptionIDs()}.
     */
    public abstract String getExceptionName(int id)throws EngineException;
    
    /**
     * Return the description of the given exception. E.g.,
     * "Illegal Memory Reference"
     * @param id one of the exception IDs returned by {@link #getExceptionIDs()}.
     * @return a description of the exception.
     * @throws IllegalArgumentException if id is not one of those returned
     * by {@link #getExceptionIDs()}.
     */
    public abstract String getExceptionDescription(int id)throws EngineException,IllegalArgumentException;
    
    /**
     * Return the exception that was responsible for stopping
     * the thread, assuming {@link #queryState} returned
     * {@link #EXCEPTION_HIT}.
     * @param tid the thread that was stopped by an exception.
     * @return the exception ID that caused the process
     * to be suspended.
     * @pre queryState() == EXCEPTION_HIT
     * @post $result != 0
     */
    public abstract int getExceptionHit(int tid)throws EngineException;
    
    /**
     * Designate whether or not the debugger is to suspend
     * when an exception is hit.
     * @param id one of the IDs returned by {@link #getExceptionIDs()}.
     * @param suspend it true, indicate that the debugger is to suspend
     * the process
     * @throws IllegalArgumentException if id is not one of those returned
     * by {@link #getExceptionIDs()}.
     */
    public abstract void setSuspendOnException(int id, boolean suspend)throws EngineException,IllegalArgumentException;
    
    /**
     * This method is called to set whether or not an exception is to
     * be ignored. If ignored, the user's process will not see it.
     * @param id one of the IDs returned by {@link #getExceptionIDs()}.
     * @param ignore it true, indicate that the debugger is to prevent
     * the corresponding exception from being sent to the process.
     * @throws IllegalArgumentException if id is not one of those returned
     * by {@link #getExceptionIDs()}.
     */
    public abstract void setIgnoreException(int id, boolean ignore)throws EngineException, IllegalArgumentException;
    
    /**
     * If stopped on an exception, clear it so that a subsequent call
     * to {@link #resume} will ignore the exception.
     * 
     */
    public abstract void clearPendingException() throws EngineException;
    
    /**
     * Cause the given exception to be passed to the target process.
     * Arrange for the subsequent call to {@link #resume} to execute
     * the exception-handling code.
     * @param id one of the IDs returned by {@link #getExceptionIDs()} that
     * corresponds to the exception to be thrown.
     * @throws EngineException
     */
    public abstract void throwException(int id) throws EngineException;

    /**
     * Create a custom display that is to be driven
     * by the {@linkplain #setCustomDisplayCallback custom-display-monitor}.
     * <P>
     * The GUI portion is generated by the engine
     * calling {@link ICustomDisplayCallback#createDisplay}.
     * <P>
     * The kind of display is the "kind" property value of
     * the menu item that was selected. (The menu items are
     * contructed by the properties returned 
     * from {@link #getDisplaySelectors}).
     * <P>
     * The caller is responsible for associating a unique
     * window number to the soon-to-be-generated display. This
     * number is passed to the {@link ICustomDisplayCallback#createDisplay} method.
     * @param id the unique integer by which this display will be
     * referenced.
     * @param kind the kind of display (which is a "kind"
     * defined by one of the elements returned by {@link #getDisplaySelectors}).
     */
    public abstract void createDisplay(int id, String kind) throws EngineException;
    
    /**
     * This method passes a command to the engine's
     * command processor. It is to be considered an
     * anachronism because the engine's command processor
     * will eventually be phased out and replaced by
     * an all-Java command processor.
     * <P>
     * Moreover, the engine command don't always fire
     * the necessary engine events so the GUI will
     * not get updated.
     * <P>
     * This method is provided so that the "Change" dialog can
     * operate using the legacy mechanism. A change dialog is created when the engine
     * invokes {@link ICustomDisplayCallback#doChangeDialog}.
     * Is uses this method as a callback
     * to inform the engine of a change.
     * 
     * @param command the command to be invoked by
     * the engine (typically "eval ...").
     */
    public abstract void invokeCommand(String command) throws EngineException;
    
    /**
     * Return an array of properties, each of which
     * can be used to construct a menu item that is
     * responsible for invoking {@link #createDisplay}
     * with the appropriate arguments.
     * <P>
     * Each menu item is described by set of properties. Here are the recognized 
     * property names:
     * <dl>
     * <dt><code>menu_name</code>
     * <dd><i>(required)</i> the name of the menu (e.g., "Registers").
     * <dt><code>parent_menu_name</code>
     * <dd><i>(optional)</i> the name of a submenu that this
     * menu item is to be defined in. Upon the first occurrence,
     * the submenu is created.
     * <dt><code>id</code>
     * <dd><i>(required)</i> the kind of display to
     * be created when the generated menu item is selected.
     * Displays are created by invoking {@link
     * IEngineAPI#createDisplay} with this value as its "kind" 
     * argument.
     * <dt><code>toolbar</code>
     * <dd> <i>(optional)</i>An alternate, perhaps shorter, name to
     * appear in a floating toolbar if one is generated
     * from this menu.
     * <dt><code>shortcut</code>
     * <dd><i>no longer used</i>
     * <dt><code>menu_name_suffix</code>
     * <dd><i>(optional)</i>a suffix that is appended to
     * the menu name. <i>Don't know what purpose this serves</i>.
     * </dl>
     * @return the array of property strings, each of
     * which can be read into a <code>Properties</code> object, or empty array
     * if there are isn't enough information to generate them.
     */
    String[] getDisplaySelectors() throws EngineException;
    
    /**
     * Indicate that a progress display was canceled
     * by the user. The progress display is materialized
     * by the engine via the {@link ICustomDisplayCallback#createProgressDisplay}
     * method.
     * @param progressDisplayID the progress display that was canceled.
     */
    void progressDisplayCanceled(int progressDisplayID) throws EngineException;
    
    /**
     * Delete the given display. This method is called when the
     * container of the display is being closed by the user's
     * action.
     * <P>
     * The engine is expected to subsequently call 
     * {@link ICustomDisplayCallback#deleteDisplay}.
     * <P>The display is will not
     * be officially disposed until
     * {@link ICustomDisplayCallback#deleteDisplay} is called.
     * @param displayID the ID of the display being deleted.
     */
    void closeDisplay(int displayID) throws EngineException;
    /**
     * Inform the engine of the file path of where
     * the argument file generated by "options" dialog is
     * written. The path contains a "%s" substring that is
     * to be replaced with the file prefix.
     * <P>
     * Example:
     * <P>
     *"C:\\workspace\\MyProject\\.%s.args"
     * <P>
     * @param pattern the path of the argument file generated
     * by the options window, with "%s" substring to denote
     * where the prefix is to be inserted.
     */
    void setArgsFilePattern(String pattern) throws EngineException;
    
    /**
     * Set the working directory of the target process, or
     * where future processes will be located.
     * @param wd teh working directory
     * @throws EngineException
     */
    void setWorkingDirectory(String wd) throws EngineException;
    
    /**
     * Set the stack frame for a SeeCode custom display so that the
     * engine will synchronize with it.
     * @param displayID the display to be affected.
     * @param frameID the stackframe "cookie" that is to be associated with
     * the display.
     * @throws EngineException
     */
    void setStackFrame(int displayID, int frameID) throws EngineException;
    
    /**
     * Return the version of the interface that we're communicating with.
     * Caller may need to know what methods can and cannot be called based on the
     * version.
     * @return the version of the engine interface that we're talking to.
     */
    int getEngineInterfaceVersion() throws EngineException;
    
    /**
     * Return an array of 3 version strings pertaining to the engine.
     * <P>
     * The first is the product version; e.g., 8.0.1
     * <P>
     * The second is the build date; e.g. 20061030.14342853
     * <P>
     * The third is the internal build ID; e.g., 1263
     * @return array of version strings pertaining to the engine.
     * @throws EngineException
     */
    String[] getEngineVersionStrings() throws EngineException;
    
    /**
     * Return the number of days before the debugger license expires.
     * Returns a negative number if there is no expiration.
     * The returned value is not necessarily accurate until a program is loaded.
     * <p>
     * Added in version 6 of this interfce.
     * <P>
     * @return the number of days before the debugger license expires, or -1 if there is no
     * expiration date.
     * @throws EngineException
     */
    int getLicenseExpirationDays() throws EngineException;
    
    /**
     * Return the path of the debugger's splash screen image file so that it can appear in an "About" box.
     * <P>
     * Added in version 6 of this interface.
     * <P>
     * @return the path of the debugger's splash screen image file.
     * @throws EngineException
     */
    String getSplashPath() throws EngineException;
    
    /**
     * This can be called after {@link #loadProgram} returns false to see if the problem was due
     * to a license failure. If so, we may want to delay throwing a "program load failure" error while
     * a "License Failure"  dialog may have popped up.
     * @return true if a license failed occur during the last attempt to load a program.
     * <P>
     * Added in version 7 of this interface.
     * @throws EngineException
     */
    boolean isLicenseFailurePending() throws EngineException;
    
    /**
     * Return the content of the register with the given ID.
     * @param frameID the stackframe ID.
     * @param regID the ID of the register whose contents we desire.
     * @return the content of the register.
     * @throws EngineException if something went wrong.
     * <P>
     * Added in version 8 of this interface.
     */
    RegisterContent getRegisterContent(int frameID, int regID)throws EngineException;
    
    /**
     * Return the content of a list of registers with the given IDs.
     * This method was added to reduce the communication traffic when retreiving the values
     * of an entire bank of registers (or even all registers), such as is required to
     * populate a register display.
     * @param frameID the stackframe ID.
     * @param regIDs the IDs of the registers whose contents we desire.
     * @return a list if register contents, one-for-one with the regIDs.
     * @throws EngineException if something went wrong.
     * <P>
     * Added in version 8 of this interface.
     */
    RegisterContent[] getRegisterContentArray(int frameID, int[] regIDs)throws EngineException;
    
    /**
     * Return the default bank that is to be initially shown in a register display. Returns -1 if
     * all banks are to be shown by default.
     * @return the default bank that is to be initially shown in a register display, or -1 to denote
     * all banks.
     * @throws EngineException if something went wrong.
     * <P>
     * Added in version 8 of this interface.
     */
    int getDefaultBank() throws EngineException;
    
    /**
     * Pass along data read from "stdin" to the target process.
     * @param data the data read from "stdin", or <code>null</code> to indicate end-of-file.
     * @throws EngineException if somethign went wrong.
     * <P>
     * Added in version 10 of this interface.
     */
    void writeStdin(byte[] data) throws EngineException;
    
    /**
     * Refresh a display. This is called explicitly by a user action when
     * he has an environment in which a display content may have changed even 
     * though the target process appears to be suspended since the last update.
     * @param id the ID number of the display to refresh.
     * @throws EngineException
     * <P>
     * Added in version 11 of this interface.
     */
    void refreshDisplay(int id) throws EngineException;
    
    /**
     * Refresh all displays. This is called explicitly by a user action when
     * he has an environment in which a display content may have changed even 
     * though the target process appears to be suspended since the last update.
     * @throws EngineException
     * <P>
     * Added in version 11 of this interface.
     */
    void refreshDisplays() throws EngineException;
    
    /**
     * Given a breakpoint ID, return the number of times that it has been hit.
     * <B>NOTE:</B> this count is the total number of times the instruction containing
     * the breakpoint was executed. If the breakpoint is conditional, this number may be greater
     * than the number of times a break actually occurred.
     * @param breakID
     * @return the number of times a breakpoint address has been executed or hit.
     * @throws EngineException
     * <P>
     * Added in version 15 of this interface.
     */
    public int getBreakpointHitCount(int breakID) throws EngineException;
    
    /**
     * Set the PC to the given location in behalf of a thread ID. T
     * @param tid the thread ID.
     * @param location the location to set the PC to.
     * @throws EngineException if the location is a source line that
     * doesn't exist, or a single machine address
     * cannot be derived from the location due to ambiguities, or if the thread 
     * isn't stopped.
     * <P>
     * Added in version 22 of the interface.
     */
    public void setPC(int tid, Location location) throws EngineException;
    
    /**
     * Disconnect the process from the debugger. It will continue to run after the debugger terminates.
     * <P>
     * This method is only operational if {@link #canDisconnect()} returns <code>true</code>.
     * <P>
     * The engine is expected to call {@link IEngineAPIObserver#processDisconnected} when the operation completes.
     * @throws EngineException
     * <P>
     * Added in version 24 of the interface.
     */
    public void disconnect() throws EngineException;
    
    /**
     * @return whether or not a the target process can be disconnected.
     * 
     * @throws EngineException
     * <P>
     * Added in version 24 of the interface.
     */
    public boolean canDisconnect() throws EngineException;
    
    /**
     * The implementer is to either invoke {@link ICustomDisplayCallback#copyToClipboard(String)} or
     * {@link ICustomDisplayCallback#copyVisibleToClipboard(int)}.
     * <P>
     * Rationale: logically, we should have implemented a <code>getDisplayContent(int id)</code> method,
     * to get the entire content of a display when performing a "Select All/Copy" operation. But we cannot
     * afford to have the GUI lock up as the engine attempts to assemble the content of a very large display
     * (e.g., history display). Also, the engine's implementation for this operation uses a callback
     * mechanism for the benefit of customer-supplied DLLs. Thus, it makes sense that the copy-to-clipboard
     * operation conform to the paradigm of the engine's existing implementation.
     * 
     * @throws EngineException
     * <P>
     * Added in version 27 of the interface.
     */
    public void copyAllToClipboard(int displayID) throws EngineException;
}
