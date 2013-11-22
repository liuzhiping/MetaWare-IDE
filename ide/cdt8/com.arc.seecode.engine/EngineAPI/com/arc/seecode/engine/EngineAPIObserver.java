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

import java.util.HashSet;
import java.util.Set;


/**
 * A private wrapper that converts {@link IEngineAPIObserver}
 * events into those based on {@link EngineInterface}.
 * @author David Pickens
 */
class EngineAPIObserver implements IEngineAPIObserver {

    private EngineInterface mEngine;
    private IEngineObserver mObserver;
    private Set<Watchpoint> mWptHits = new HashSet<Watchpoint>(); // watchpoints that caused stop

    /**
     * 
     */
    public EngineAPIObserver(EngineInterface engine, IEngineObserver observer) {
        mEngine = engine;
        mObserver = observer;
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#processCreated(com.arc.seecode.engine.IEngineAPI)
     */
    @Override
    public void processCreated(IEngineAPI engine) {
        mObserver.processCreated(mEngine);
        mEngine.clearRestartPending();

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#processStarted(com.arc.seecode.engine.IEngineAPI)
     */
    @Override
    public void processStarted(IEngineAPI engine) {
        mObserver.processStarted(mEngine);
        mEngine.clearRestartPending();

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#processStopped(com.arc.seecode.engine.IEngineAPI)
     */
    @Override
    public void processStopped (IEngineAPI engine, int tid) {
        int state;
        mEngine.invalidateCacheForThread(tid);
        try {
            state = mEngine.queryState(tid);
            // Check for bug in engine that doesn't recognize state change until after this function returns.
            if (state == IEngineAPI.RUNNING)
                state = IEngineAPI.STOPPED_BY_USER;
        }
        catch (EngineException e1) {
            // presumably caught later.
            state = IEngineAPI.STOPPED_BY_USER;
        }
        mEngine.onStopped(tid, state);
        mObserver.processStopped(mEngine, tid);
        try {
            switch (state) {
                case IEngineAPI.WATCHPOINT_HIT:
                    setWatchpointHits(tid);
                    //$FALL-THROUGH$
                case IEngineAPI.BREAKPOINT_HIT:
                    mEngine.getBreakpointManager().fireHit(mEngine.getBreakpointHit(tid));
            }
        }
        catch (EngineException e) {
            // Ignore; problems will be encountered later.
        }

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#processResumed(com.arc.seecode.engine.IEngineAPI)
     */
    @Override
    public void processResumed(IEngineAPI engine) {
        clearWatchpointHits();
        mEngine.onResume(0);
        mObserver.processResumed(mEngine);
        mEngine.clearRestartPending();
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#processTerminated(com.arc.seecode.engine.IEngineAPI)
     */
    @Override
    public void processTerminated(IEngineAPI engine) {
        try {
            //engine may be null!
            
            // NOTE: if engine is doing a restart, then the thread that
            // issued the restart is hanging until the restart is complete.
            // Therefore, we'll deadlock if we call back into the engine.
            
            mEngine.setExitCode(mEngine.isRestartPending()?-1:mEngine.getAPI().getProcessExitCode());
        } catch (EngineException e) {
            mEngine.setExitCode(0xDeadBeef);
        }
        mObserver.processTerminated(mEngine);
    }
    
    @Override
    public void processDisconnected(IEngineAPI engine) {
        mObserver.processDisconnected(mEngine);
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#threadCreated(com.arc.seecode.engine.IEngineAPI, int)
     */
    @Override
    public void threadCreated(IEngineAPI engine, int tid) {
        mEngine.invalidateCache();
        mObserver.threadCreated(mEngine,tid);

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#threadTerminated(com.arc.seecode.engine.IEngineAPI, int)
     */
    @Override
    public void threadTerminated(IEngineAPI engine, int tid) {
        mEngine.invalidateCache();
        mObserver.threadTerminated(mEngine,tid);

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#displayError(com.arc.seecode.engine.IEngineAPI, java.lang.String)
     */
    @Override
    public void displayError(IEngineAPI engine, String msg) {
        mObserver.displayError(mEngine,msg);
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#displayNote(com.arc.seecode.engine.IEngineAPI, java.lang.String)
     */
    @Override
    public void displayNote(IEngineAPI engine, String msg) {
        mObserver.displayNote(mEngine,msg);

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#displayFatal(com.arc.seecode.engine.IEngineAPI, java.lang.String)
     */
    @Override
    public void displayFatal(IEngineAPI engine, String msg) {
        mObserver.displayFatal(mEngine,msg);

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#logMessage(com.arc.seecode.engine.IEngineAPI, java.lang.String)
     */
    @Override
    public void logMessage(IEngineAPI engine, String text) {
        mObserver.logMessage(mEngine,text);


    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#moduleLoaded(com.arc.seecode.engine.IEngineAPI, int)
     */
    @Override
    public void moduleLoaded(IEngineAPI engine, int moduleID) {
        mObserver.moduleLoaded(mEngine,moduleID);

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#moduleUnloaded(com.arc.seecode.engine.IEngineAPI, int)
     */
    @Override
    public void moduleUnloaded(IEngineAPI engine, int moduleID) {
        mObserver.moduleUnloaded(mEngine,moduleID);

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#setStatus(com.arc.seecode.engine.IEngineAPI, java.lang.String)
     */
    @Override
    public void setStatus(IEngineAPI engine, String status) {
        mObserver.setStatus(mEngine,status);

    }
    
    @Override
    public void engineShutdown(IEngineAPI engine){
        // We could be called when the engine suddenly faults,
        // so be prepared to be called more than once if, say, it
        // faults during shutdown.
        if (!mEngine.isShutdown()){
            mEngine.setShutdown();
            mObserver.engineShutdown(mEngine);
        }
        //Too early to call this. The engineShutdown
        // servicing may need to shutdown processes and
        // such from an event thread. Thus, it is the
        // responsibility of the GUI to call "onShutdown()"
        // when it is truly shutdown the session.
        //mEngine.onShutdown();
    }

    /*override*/
    @Override
    public void logError(IEngineAPI engine, String text, Throwable t) {
        mObserver.logError(mEngine,text,t);
        
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#licensingFailure(com.arc.seecode.engine.IEngineAPI, java.lang.String)
     */
    @Override
    public void licensingFailure(IEngineAPI engine, String msg) {
        mObserver.licensingFailure(mEngine,msg);
    }

    @Override
    public void threadResumed (IEngineAPI engine, int tid) {
        clearWatchpointHits();
        mEngine.onResume(tid);
        mObserver.threadResumed(mEngine,tid); 
    }

    @Override
    public void threadStopped (IEngineAPI engine, int tid) {
        int state;
        mEngine.invalidateCacheForThread(tid);
        try {
            state = mEngine.queryState(tid);
        }
        catch (EngineException e1) {
            state = IEngineAPI.STOPPED_BY_USER;
            // presumably caught later
        }
        mEngine.onStopped(tid, state);
        mObserver.threadStopped(mEngine, tid);
        try {
            switch (state) {
                case IEngineAPI.WATCHPOINT_HIT:
                    setWatchpointHits(tid);
                    //$FALL-THROUGH$
                case IEngineAPI.BREAKPOINT_HIT:
                    mEngine.getBreakpointManager().fireHit(mEngine.getBreakpointHit(tid));
            }
        }
        catch (EngineException e) {
            // Ignore; problems will be encountered later.
        }
    }

    private void setWatchpointHits (int tid) throws EngineException {
        WatchpointHit hits[] = mEngine.getWatchpointHits(tid);
        for (WatchpointHit hit: hits){
            Watchpoint w = (Watchpoint)mEngine.getBreakpointManager().getBreakpointFromID(hit.getWatchpointID());
            if (w != null){
                mWptHits.add(w);
                w.setPendingHit(hit);
            }
        }
    }

    @Override
    public void writeToStderr (IEngineAPI engine, byte[] data) {
        mObserver.writeToStderr(mEngine,data);
        
    }

    @Override
    public void writeToStdout (IEngineAPI engine, byte[] data) {
        mObserver.writeToStdout(mEngine,data);       
    }

    private void clearWatchpointHits(){
        if (mWptHits.size() > 0){
            for (Watchpoint w: mWptHits){
                w.setPendingHit(null);
            }
            mWptHits.clear();
        }
    }

    @Override
    public void writeToErrorLog (IEngineAPI engine, String msg) {
        mObserver.writeToErrorLog(mEngine,msg);        
    }
}
