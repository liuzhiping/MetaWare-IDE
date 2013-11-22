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

import com.arc.seecode.engine.IEngineAPI;
import com.arc.seecode.engine.IEngineAPIObserver;
import com.arc.seecode.scwp.ScwpCommandPacket;


/**
 * This is the engine observer that sends calls back to
 * the client (GUI). However, we have a deadlock problem:
 * When any method in this class is called, the {@link IEngineAPI} object
 * is locked. If the corresponding method in the client
 * sends back a command (e.g., read registers), the 
 * packet receiver thread will
 * deadlock.
 * <P>
 * Thus, we must dispatch things in a separate thread (via
 * the {@link #dispatch(String,Object[])} method.
 * @author David Pickens
 */
class EngineAPIObserver extends AbstractObserver implements IEngineAPIObserver {

    private Server mServer;
    
    private StringBuffer mLogMessage = new StringBuffer();

    EngineAPIObserver(Server server, CallbackThread callback, int processID){
        super(callback,processID * ScwpCommandPacket.REQUIRED_CHANNELS+ScwpCommandPacket.ENGINE_OBSERVER);
        mServer = server;
    }
    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#processCreated(com.arc.seecode.engine.IEngineAPI)
     */
    @Override
    public void processCreated(IEngineAPI engine) {
        dispatch("processCreated",new Object[]{null});

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#processStarted(com.arc.seecode.engine.IEngineAPI)
     */
    @Override
    public void processStarted(IEngineAPI engine) {
        dispatch("processStarted",new Object[]{null});

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#processStopped(com.arc.seecode.engine.IEngineAPI)
     */
    @Override
    public void processStopped(IEngineAPI engine, int tid) {
        dispatch("processStopped",new Object[]{null,tid});

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#processResumed(com.arc.seecode.engine.IEngineAPI)
     */
    @Override
    public void processResumed(IEngineAPI engine) {
        dispatch("processResumed",new Object[]{null});

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#processTerminated(com.arc.seecode.engine.IEngineAPI)
     */
    @Override
    public void processTerminated(IEngineAPI engine) {
        dispatch("processTerminated",new Object[]{null});

    }
    
    @Override
    public void processDisconnected(IEngineAPI engine) {
        dispatch("processDisconnected",new Object[]{null});

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#threadCreated(com.arc.seecode.engine.IEngineAPI, int)
     */
    @Override
    public void threadCreated(IEngineAPI engine, int tid) {
        dispatch("threadCreated",new Object[]{null, new Integer(tid)});

    }
    
    @Override
    public void threadStopped(IEngineAPI engine, int tid) {
        dispatch("threadStopped",new Object[]{null, new Integer(tid)});
    }
    
    @Override
    public void threadResumed(IEngineAPI engine, int tid) {
        dispatch("threadResumed",new Object[]{null, new Integer(tid)});
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#threadTerminated(com.arc.seecode.engine.IEngineAPI, int)
     */
    @Override
    public void threadTerminated(IEngineAPI engine, int tid) {
        dispatch("threadTerminated",new Object[]{null, new Integer(tid)});
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#displayError(com.arc.seecode.engine.IEngineAPI, java.lang.String)
     */
    @Override
    public void displayError(IEngineAPI engine, String msg) {
        dispatch("displayError",new Object[]{null, msg});
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#displayNote(com.arc.seecode.engine.IEngineAPI, java.lang.String)
     */
    @Override
    public void displayNote(IEngineAPI engine, String msg) {
        dispatch("displayNote",new Object[]{null, msg});

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#displayFatal(com.arc.seecode.engine.IEngineAPI, java.lang.String)
     */
    @Override
    public void displayFatal(IEngineAPI engine, String msg) {
        dispatch("displayFatal",new Object[]{null, msg});

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#logError(com.arc.seecode.engine.IEngineAPI, java.lang.String, java.lang.Throwable)
     */
    @Override
    public void logError(IEngineAPI engine, String text, Throwable t) {
        //This shouldn't be called from engine
        dispatch("logError",new Object[]{null, text,null});

    }
    
    private ScwpCommandPacket fLastLog = null;
    private String fLastLogText = "";

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#logMessage(com.arc.seecode.engine.IEngineAPI, java.lang.String)
     */
    @Override
    public void logMessage(IEngineAPI engine, String text) {
        // The engine can transmit log messages in pieces.
        // Buffer it up into a line so as to prevent
        // unnecessary TCP/IP traffic.
        int i = text.lastIndexOf('\n');
        if (i >= 0){
            String xmit;
            if (mLogMessage.length() > 0){
                mLogMessage.append(text.substring(0,i+1));
                xmit = mLogMessage.toString();
                mLogMessage.setLength(0);
            }
            else xmit = text.substring(0,i+1);
            boolean dispatched = false;
            //If the preceeding log message hasn't yet been sent, then append to it, if we can.
            if (fLastLog != null && !fLastLog.isSent() && fLastLogText.length() + xmit.length() < 65000){
                String newText = fLastLogText + xmit;
                if (this.updatePacket(fLastLog, "logMessage", new Object[]{null,newText})){
                    fLastLogText = newText;
                    dispatched = true;
                }
            }
            if (!dispatched){
                fLastLog = dispatch("logMessage",new Object[]{null, xmit});
                fLastLogText = xmit;
            }
            text = text.substring(i+1);
        }
        mLogMessage.append(text);
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#moduleLoaded(com.arc.seecode.engine.IEngineAPI, int)
     */
    @Override
    public void moduleLoaded(IEngineAPI engine, int moduleID) {
        dispatch("moduleLoaded",new Object[]{null, new Integer(moduleID)});


    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#moduleUnloaded(com.arc.seecode.engine.IEngineAPI, int)
     */
    @Override
    public void moduleUnloaded(IEngineAPI engine, int moduleID) {
        dispatch("moduleUnloaded",new Object[]{null, new Integer(moduleID)});
    }
    
    private ScwpCommandPacket fLastStatus = null;

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#setStatus(com.arc.seecode.engine.IEngineAPI, java.lang.String)
     */
    @Override
    public void setStatus(IEngineAPI engine, String status) {
        // If the last "setStatus" hasn't been sent yet, then zap it
        if (fLastStatus != null && !fLastStatus.isSent()){
            if (this.updatePacket(fLastStatus,"setStatus",new Object[]{null,status})){
                return;
            }
        }
        fLastStatus = dispatch("setStatus",new Object[]{null, status});

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#engineShutdown(com.arc.seecode.engine.IEngineAPI)
     */
    @Override
    public void engineShutdown(IEngineAPI engine) {
        dispatch("engineShutdown",new Object[]{null});
        mServer.shutdown();

    }
    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineAPIObserver#licensingFailure(com.arc.seecode.engine.IEngineAPI, java.lang.String)
     */
    @Override
    public void licensingFailure(IEngineAPI engine, String msg) {
       dispatch("licensingFailure",new Object[]{null,msg});       
    }
    
    private ScwpCommandPacket fLastWriteToStderr = null;
    private byte[] fLastStderrData = null;
    
    @Override
    public void writeToStderr (IEngineAPI engine, byte[] data) {
        // If we're writing rapidly, and there is a pending one that has yet been written,
        // then append to it.
        if (fLastWriteToStderr != null && !fLastWriteToStderr.isSent() &&
            fLastStderrData.length + data.length < 65536){
            byte[] newData = new byte[data.length + fLastStderrData.length];
            System.arraycopy(fLastStderrData,0,newData,0,fLastStderrData.length);
            System.arraycopy(data, 0, newData, fLastStderrData.length, data.length);
            if (this.updatePacket(fLastWriteToStderr,"writeToStderr",new Object[]{null,newData})){
                fLastStderrData = newData;
                return;
            }
        }
        fLastStderrData = data;
        fLastWriteToStderr = dispatch("writeToStderr",new Object[]{null,data});        
        
    }
    
    private ScwpCommandPacket fLastWriteToStdout = null;
    private byte[] fLastStdoutData = null;
    
    @Override
    public void writeToStdout (IEngineAPI engine, byte[] data) {
        // If we're writing rapidly, and there is a pending one that has yet been written,
        // then append to it.
        if (fLastWriteToStdout != null && !fLastWriteToStdout.isSent() &&
            fLastStdoutData.length + data.length < 65536){
            byte[] newData = new byte[data.length + fLastStdoutData.length];
            System.arraycopy(fLastStdoutData,0,newData,0,fLastStdoutData.length);
            System.arraycopy(data, 0, newData, fLastStdoutData.length, data.length);
            if (this.updatePacket(fLastWriteToStdout,"writeToStdout",new Object[]{null,newData})){
                fLastStdoutData = newData;
                return;
            }
        }
        fLastStdoutData = data;
        fLastWriteToStdout = dispatch("writeToStdout",new Object[]{null,data});          
    }
    
    @Override
    public void writeToErrorLog(IEngineAPI engine, String msg){
        dispatch("writeToErrorLog",new Object[]{null,msg});
    }
}
