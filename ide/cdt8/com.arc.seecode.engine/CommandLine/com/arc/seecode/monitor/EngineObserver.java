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
package com.arc.seecode.monitor;

import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;
import com.arc.seecode.engine.IEngineAPI;
import com.arc.seecode.engine.IEngineObserver;

/**
 * The class that wraps the engine interface, breakpoint manager, etc.
 * @author David Pickens
 */
public class EngineObserver implements IEngineObserver{

    private boolean mNewLine;
    public EngineObserver() {
    }
 

    static void P(String s) {
        System.out.println(s);
    }

    /* (non-Javadoc)
     * @see com.arc.mw.seecode.IEngineObserver#processCreated(com.arc.mw.seecode.EngineInterface)
     */
    @Override
    public void processCreated(EngineInterface engine) {
        P("processCreated called");


    }

    /* (non-Javadoc)
     * @see com.arc.mw.seecode.IEngineObserver#processStarted(com.arc.mw.seecode.EngineInterface)
     */
    @Override
    public void processStarted(EngineInterface engine) {
        P("processStarted called");


    }


    /* (non-Javadoc)
     * @see com.arc.mw.seecode.IEngineObserver#processResumed(com.arc.mw.seecode.EngineInterface)
     */
    @Override
    public void processResumed(EngineInterface engine) {
        P("processResumed called");
    }
    
    
   @Override
public void threadResumed(EngineInterface engine, int thread) {
       P("threadResumed(" + thread + ") called");
   }

    /* (non-Javadoc)
     * @see com.arc.mw.seecode.IEngineObserver#processTerminated(com.arc.mw.seecode.EngineInterface)
     */
    @Override
    public void processTerminated(EngineInterface engine) {
        P("processTerminated called");
    }
    
    @Override
    public void processDisconnected(EngineInterface engine) {
        P("processDisconnected called");
    }

    /* (non-Javadoc)
     * @see com.arc.mw.seecode.IEngineObserver#threadCreated(com.arc.mw.seecode.EngineInterface, int)
     */
    @Override
    public void threadCreated(EngineInterface engine, int tid) {
        P("threadCreated called " + tid);

    }

    @Override
    public void threadStopped(EngineInterface engine, int tid) {
        P("threadStopped thread=" + tid);
        handleStop(engine, tid);
    }
    /* (non-Javadoc)
     * @see com.arc.mw.seecode.IEngineObserver#threadStopped(com.arc.mw.seecode.EngineInterface, int)
     */
    @Override
    public void processStopped(EngineInterface engine, int tid) {
        P("processStopped thread=" + tid);
        handleStop(engine, tid);
    }


    /**
     * @todo davidp needs to add a method comment.
     * @param engine
     * @param tid
     */
    private void handleStop (EngineInterface engine, int tid) {
        try {
            String reason = "???";
            int s = engine.queryState(0);
            switch (s) {
                case IEngineAPI.INVALID :
                    reason = "invalid";
                    break;
                case IEngineAPI.BREAKPOINT_HIT :
                    reason = "breakpoint (id=" + engine.getBreakpointHit(tid)+")";
                    break;
                case IEngineAPI.NOT_STARTED :
                    reason = "not started";
                    break;
                case IEngineAPI.RUNNING :
                    reason = "running";
                    break;
                case IEngineAPI.STOPPED_BY_USER :
                    reason = "stopped by user";
                    break;
                case IEngineAPI.WATCHPOINT_HIT :
                    reason = "watchpoint";
                    break;
                case IEngineAPI.TERMINATED :
                    reason = "terminated";
                    break;
                case IEngineAPI.UNKNOWN :
                    reason = "unknown";
                    break;
                case IEngineAPI.STEPPED:
                    reason = "stepped";
                    break;           
                default :
                    reason = s + "???";
            }
            P("   reason is: " + reason);
        } catch (EngineException e) {
            logError(engine,e.getMessage(),e);
        }
    }

    /* (non-Javadoc)
     * @see com.arc.mw.seecode.IEngineObserver#threadTerminated(com.arc.mw.seecode.EngineInterface, int)
     */
    @Override
    public void threadTerminated(EngineInterface engine, int tid) {
        P("threadTerminated " + tid);
    }

    /* (non-Javadoc)
     * @see com.arc.mw.seecode.IEngineObserver#displayError(com.arc.mw.seecode.EngineInterface, java.lang.String)
     */
    @Override
    public void displayError(EngineInterface engine, String msg) {
        System.err.println(">>>ERROR: " + msg);

    }
    
    /* (non-Javadoc)
     * @see com.arc.mw.seecode.IEngineObserver#displayError(com.arc.mw.seecode.EngineInterface, java.lang.String)
     */
    @Override
    public void displayFatal(EngineInterface engine, String msg) {
        System.err.println(">>>FATAL: " + msg);

    }

    /* (non-Javadoc)
     * @see com.arc.mw.seecode.IEngineObserver#logMessage(com.arc.mw.seecode.EngineInterface, java.lang.String)
     */
    @Override
    public void logMessage(EngineInterface engine, String line) {
        if (mNewLine)
            System.out.print("MSG: ");
        System.out.print(line);
        mNewLine = line.length() > 0 && line.charAt(line.length() - 1) == '\n';
    }

    /* (non-Javadoc)
     * @see com.arc.mw.seecode.IEngineObserver#newModuleLoaded(com.arc.mw.seecode.EngineInterface, int)
     */
    @Override
    public void moduleLoaded(EngineInterface engine, int moduleID) {
        P("moduleLoaded " + moduleID);

    }

    /* (non-Javadoc)
     * @see com.arc.mw.seecode.IEngineObserver#moduleUnloaded(com.arc.mw.seecode.EngineInterface, int)
     */
    @Override
    public void moduleUnloaded(EngineInterface engine, int moduleID) {
        P("module unloaded: " + moduleID);

    }

    /* (non-Javadoc)
     * @see com.arc.mw.seecode.IEngineObserver#setStatus(java.lang.String)
     */
    @Override
    public void setStatus(EngineInterface engine,String status) {
        P("STATUS: " + status);

    }


    /*override*/
    @Override
    public void displayNote(EngineInterface engine, String msg) {
        System.err.println(">>>NOTE: " + msg);
        
    }


    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineObserver#engineShutdown(com.arc.seecode.engine.EngineInterface)
     */
    @Override
    public void engineShutdown(EngineInterface engine) {
       System.out.println("Engine shutdown");
        
    }


    /*override*/
    @Override
    public void logError(EngineInterface engine, String msg, Throwable t) {
        System.out.println("Exception occurred: " + msg);
        if (t != null)      
            t.printStackTrace();
        
    }


    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineObserver#licensingFailure(com.arc.seecode.engine.EngineInterface, java.lang.String)
     */
    @Override
    public void licensingFailure(EngineInterface engine, String msg) {
        P("License failure: " + msg);
        
    }


    @Override
    public void writeToStderr (EngineInterface engine, byte[] data) {
        P("stdout: " + new String(data));
        
    }


    @Override
    public void writeToStdout (EngineInterface engine, byte[] data) {
        P("stderr: " + new String(data));       
    }
    
    @Override
    public void writeToErrorLog (EngineInterface engine, String msg) {
        P("errorLog: " + msg);       
    }

}
