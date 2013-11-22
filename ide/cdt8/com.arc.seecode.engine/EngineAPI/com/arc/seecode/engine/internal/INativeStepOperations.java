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

import com.arc.seecode.engine.IEngineAPI;
import com.arc.seecode.engine.IEngineObserver;
import com.arc.seecode.engine.Location;
import com.arc.seecode.engine.StackFrameRef;
import com.arc.seecode.engine.Value;

/**
 * A subset of methods in the engine that controls stepping, and that
 * must be invoked sequentially. Defined for historical reasons but should be part
 * of IEngineAPI.
 * @author David Pickens
 */
public interface INativeStepOperations {

    /**
     * Called 
     *  to implement "resume" operation.
     */
    public abstract void seecodeResume(int tid);

    /**
     * Called
     *  to implement "instructionStep" operation.
     */
    public abstract void seecodeInstructionStep(int tid, boolean over, int cnt);

    /**
     * Called 
     *  to implement "statementStep" operation.
     */
    public abstract void seecodeStatementStep(int tid, boolean over, int cnt);

    /**
     * Step until current function exists.
     * @param tid
     */
    public abstract void seecodeStepOut(int tid);

    /**
     * Invoked to
     * implement the "run-to-address" operation.
     * @param tid
     * @param location
     */
    public abstract void seecodeRunToAddress(int tid, Location location);
    
    /**
     * This is called while at least one thread is running so as
     * to poll for a stop event. If any process and/or thread stopped within a
     * reasonable time, this method returns true. Otherwise, it returns
     * false and the caller will need to call it again.
     * <P>
     * If we are {@linkplain IEngineAPI#isSimulator() simulator-based}, then the
     * call to this method actually drives the simulator. It should
     * be called repeatedly with no unnecessary delays.
     * <P>
     * If we are <i>not</i> {@linkplain IEngineAPI#isSimulator() simulator-based}, then
     * this method will simply do a poll to test if the hardware is stopped
     * running. For such a case, the caller should insert sleeps to avoid
     * the wait loop for swamping the CPU.
     * <P>
     * If the process and/or thread is already stopped, this method
     * will return true immediately. The process and/or thread will
     * need to be {@linkplain IEngineAPI#resume resumed} for this method
     * to return something other than true.
     * <P>
     * If the thread/process is terminated, this method is expected to
     * return true so as to avoid an infinite loop by the caller.
     * 
     * @return true if process is stopped.
     * @pre $none
     * @post !$result && queryState(tid) == RUNNING || $result && (queryState(tid) != RUNNING || !isActiveThread(tid))
     */
    public abstract boolean waitForStop();
    /**
     * Return the number of milliseconds that we need to wait after
     * a step or resume operation before we query the processor to see
     * if it is stopped (via {#link #waitForStop}).
     * @return the number if milliseconds of delay before we issue a
     * {@link #waitForStop} call.
     */
    public abstract int getPollingDelay();
    
    /**
     * Stop all threads of the process. The engine will
     * invoke {@link IEngineObserver#processStopped processStopped} 
     * when the processor actually stops.
     * <P>
     * If the process/thread is already stopped, the engine's
     * action is undefined.
     * 
     * @post $none
     */
    public abstract void seecodeSuspend(int tid);
    
    /**
     * Shutting down engine.
     *
     */
    public abstract void seecodeShutdown();
    
    /**
     * Return whether or not the thread is in a stopped state.
     * @return whether or not the engine is in a stopped state.
     */
    public abstract boolean isStopped(int tid);
    
    /**
     * Free the stackframe ID.
     * This is called from the garbage collector via 
     * {@link StackFrameRef#finalize}.
     */
    public abstract void seecodeFreeStackFrameID(int id);
    
    /**
     * Free the value "cookie".
     * This is called from the garbage collector via 
     * {@link Value#finalize}.
     */
    public abstract void seecodeFreeValueCookie(int id);
    

    
}
