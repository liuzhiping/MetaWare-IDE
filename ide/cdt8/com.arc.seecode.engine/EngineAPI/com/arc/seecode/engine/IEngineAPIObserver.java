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
 * Observer for {@link IEngineAPI} events. But it is
 * always wrapped so that {@link IEngineObserver} is used
 * instead.
 * @author David Pickens
 */
public interface IEngineAPIObserver {
    /**
     * Called by the engine when a new process is created.
     * A subsequent call to {@link #processStarted(IEngineAPI)} will occur
     * when the process is started.
     * @param engine the associated engine object.
     * 
     * @pre engine != null && engine.isActive()
     * @post $none
     */
    void processCreated(IEngineAPI engine);
    /**
     * Called by the engine when a process is initially started.
     * @param engine the associated engine object.
     * 
     * @pre engine != null && engine.queryState(0) == EngineInterface.RUNNING
     * @post $none
     */
    void processStarted(IEngineAPI engine);
    
    /**
     * Called by the engine when all threads of a process are stopped as the
     * result of a user request, breakpoint, or watch point.
     * The method {@link EngineInterface#queryState queryState(0)} can be invoked to
     * get additional information.
     * <P>
     * NOTE: if the engine does not support independent thread control, then this
     * method will be called when <em>any</em> thread stops, because such an action
     * will necessarily stop all threads.
     * <P>
     * If the engine does support independent thread control, then this method will be
     * called only if the user has requested that all threads be stopped, or if an action point
     * has been reached by a thread, and the action point has the property that all threads
     * are to be stopped.
     * <P>
     * If the engine supports independent thread control, and only a single thread stopps,
     * then {@link #threadStopped} will be
     * invoked instead of this method being invoked.
     * @param engine the associated engine object.
     * @param tid the thread ID of the thread that caused the process to stop, or 0 if the
     * stop was due to user action.
     * 
     * @pre engine != null && engine.isStopped(0)
     * @post $none
     */
    void processStopped(IEngineAPI engine, int tid);
    
    /**
     * Called by the engine when all threads of a process are resumed after
     * being stopped.
     * <P>
     * NOTE: this method will be called if the engine does <em>not</em> support independent
     * thread control. It will also be called if resume all threads after all have been stopped.
     * <P>
     * If the engine supports independent thread control, then {@link #threadResumed} will be
     * invoked if a single thread is being resumed.
     * @param engine the associated engine object.
     * 
     * @pre engine != null && engine.queryState(0) == EngineInterface.RUNNING
     * @post $none
     */
    void processResumed(IEngineAPI engine);
    
    /**
     * Called by the engine when a process is terminated.
     * @param engine the associated engine object.
     * 
     * @pre engine != null && !engine.isActive()
     */
    void processTerminated(IEngineAPI engine);
    
    /**
     * Called by the engine when a process had been disconnected from the debugger.
     * That is, it is running detached.
     * <P>
     * Once a process is disconnected, it cannot be restarted. Presumably, the debugger
     * will subsequently be shut down.
     * @param engine the associated engine object.
     * 
     * @pre engine != null && !engine.isActive()
     * 
     * Valid for interface version 24 and later.
     * @new
     */
    void processDisconnected(IEngineAPI engine);
    
    /**
     * Called by the engine sometime after a thread is created.
     * <P>
     * If the engine may not necessarily report such things until
     * the process stops. In such a case, this method is assumed to
     * be called <i>before</i> the {@link #processStopped} method
     * is invoked.
     * 
     * @param engine the associated engine object.
     * @param tid the thread id.
     * 
     * @pre engine != null && engine.isActive()
     * @post $none
     */
    void threadCreated(IEngineAPI engine,  int tid);
    
    /**
    * Called by the engine when a thread is terminated.
    * <P>
    * If the engine may not necessarily report such things until
    * the process stops. In such a case, this method is assumed to
    * be called <i>before</i> the {@link #processStopped} method
    * is invoked.
    * <P>
    * The engine is <i>not</i> expected to call this if
    * the entire process has terminated.
    * <P>
    * The thread id is not expected to be invalid until after
    * this method returns.
    * <P>
    * NOTE: it is assumed that the engine implicitly deletes any
    * breakpoint that is tied to this thread. Thus, any breakpoint
    * table maintained on this side of the interface must be explicitly
    * purged of stale breakpoints. <I>Or should we have the engine
    * report stale breakpoints in this interface?</I>
    * <P>
    * @param engine the associated engine object.
    * @param tid the thread id.
    * 
    * @pre tid != 0 && engine != null && engine.isStopped(tid)
    * @post $none
    */
   void threadTerminated(IEngineAPI engine, int tid);
   
   /**
    * Called to indicate that a particular thread is stopped.
    * <P>
    * This will be invoked only if the thread supports independent thread control. Otherwise,
    * {@link #processStopped} will be invoked instead.
    * @param engine
    * @param tid the thread that stopped.
    */
   void threadStopped(IEngineAPI engine, int tid);
   
   /**
    * Called to indicate that a particular thread is resumed.
    * <P>
    * This will be invoked only if the thread supports independent thread control. Otherwise,
    * {@link #processResumed} will be invoked instead.
    * @param engine
    * @param tid the thread that resumed.
    */
   void threadResumed(IEngineAPI engine, int tid);
   
//   /**
//    * Indicate that a watchpoint became enabled or disabled, 
//    * presumably as a result of an associated stackframe terminating.
//    * @param engine
//    * @param watchpointID
//    * @param value
//    */
//   void watchpointEnabled(IEngineAPI engine, int watchpointID, boolean value);
    
    /**
     * Display a non-fatal error dialog to indicate that some sort of
     * problem occured within the engine. 
     * @param engine the associated engine object.
     * @param msg a message to be displayed in the error box; may 
     * be HTML.
     * 
     * @pre msg != null
     * @post $none
     */
    void displayError(IEngineAPI engine, String msg);
    /**
     * Display a note or warning message.
     * @param engine the associated engine object.
     * @param msg a message to be displayed in the dialog box; may 
     * be HTML.
     * 
     * @pre msg != null
     * @post $none
     */
    void displayNote(IEngineAPI engine, String msg);
    
    /**
     * Display a fatal error dialog to indicate that a 
     * catastrophic error has occurred. 
     * <P>
     * The engine will presumably shut itself down subsequently.
     * 
     * @param engine the associated engine object.
     * @param msg a message to be displayed in the error box; may 
     * be HTML.
     * 
     * @pre msg != null
     * @post $none
     */
    void displayFatal(IEngineAPI engine, String msg);
    
    /**
     * Log an error message on the error log.
     * @param engine the associated engine object.
     * @param text the text to be appended to the console log.
     * @param t the exception that caused the problem, or null.
     * 
     * @pre line != null
     * @post $none
     * @nojni  called from Java only
     */
    void logError(IEngineAPI engine, String text, Throwable t);
   
    /**
     * Log a message on the console.
     * @param engine the associated engine object.
     * @param text the text to be appended to the console log.
     * @pre line != null
     * @post $none
     */
    void logMessage(IEngineAPI engine, String text);
   
    /**
     * A new module was loaded (e.g., a DLL).
     * <P>
     * If the engine may not necessarily report such things until
     * the process stops. In such a case, this method is assumed to
     * be called <i>before</i> the {@link #processStopped} method
     * is invoked.
     * <P>
     * <B>NOTE:</B> the SeeCode engine does not report the module
     * that was loaded. I.e., <code>moduleID</code> is 0. The
     * UI must retrieve the update list of modules from the
     * {@linkplain IEngineAPI#getModules() engine interface}.
     * @param engine the associated engine object.
     * @param moduleID the ID number of the module, or <code>0</code>.
     */ 
    void moduleLoaded(IEngineAPI engine, int moduleID);
    
    /**
     * A module was unloaded (e.g., a DLL).
     * <P>
     * If the engine may not necessarily report such things until
     * the process stops. In such a case, this method is assumed to
     * be called <i>before</i> the {@link #processStopped} method
     * is invoked.
     * <P>
     * <B>NOTE:</B> the SeeCode engine does not report the module
     * that was loaded. I.e., <code>moduleID</code> is 0. The
     * UI must retrieve the update list of modules from the
     * {@linkplain IEngineAPI#getModules() engine interface}.
     * @param engine the associated engine object.
     * @param moduleID the ID number of the module.
     */ 
    void moduleUnloaded(IEngineAPI engine, int moduleID);
    
    /**
     * Set a status value in some appropriate place (e.g.,
     * status bar).
     */
    void setStatus(IEngineAPI engine, String status);
    
    /**
     * Engine is shutting down. It will no longer be
     * useable after this.
     * @param engine
     */
    void engineShutdown(IEngineAPI engine);
   
    /**
     * Called when licensing failure occurs.
     * @param engine the originator.
     * @param msg the associated message.
     */
    void licensingFailure(IEngineAPI engine, String msg);
    
    /**
     * Invoked to inform the client that the target program has written the given data
     * to "standard output".
     * <P>
     * Supported only if interface version is 10 or later.
     * @param engine the associated engine object.
     * @param data the data that the target program has written to standard output.
     * @new
     */
    void writeToStdout(IEngineAPI engine, byte[] data);
    
    /**
     * Invoked to inform the client that the target program has written the given data
     * to "standard error".
     * <P>
     * Supported only if interface version is 10 or later.
     * @param engine the associated engine object.
     * @param data the data that the target program has written to standard error.
     * @new
     */
    void writeToStderr(IEngineAPI engine, byte[] data);
    
    /**
     * Write a string to the debugger's error log. No implicit newline is appended.
     * <P>
     * Supported only if interface version is 28 or later.
     * @param engine the associated engine object.
     * @param msg the string to be written to the error log.
     * @new
     */
    void writeToErrorLog(IEngineAPI engine, String msg);

}
