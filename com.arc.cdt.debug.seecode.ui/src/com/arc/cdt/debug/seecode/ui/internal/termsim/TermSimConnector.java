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
package com.arc.cdt.debug.seecode.ui.internal.termsim;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.event.ICDIRestartedEvent;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.ui.PlatformUI;

import com.arc.cdt.debug.seecode.core.cdi.ICDISeeCodeSession;


/**
 * Connector for the Terminal View framework. Simulates a terminal as driven by
 * the debugger engine's UART simulator.
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class TermSimConnector implements ITerminalConnector, ICDIEventListener {
    

    private OutputStream fOutputStream;
    private InputStream fInputStream;
    private Socket fSocket;
    private ITerminalControl fControl;
    private TermSimConnection fTermSimConnection;
    private int tcpIpPort;
    private int uartPort;
    private ICDISeeCodeSession session;
    
    public TermSimConnector(int tcpIpPort, int uartPort, ICDISeeCodeSession session){
        if (tcpIpPort < 0) 
            throw new IllegalArgumentException("Bad TCP port number: " + tcpIpPort);
        if (session == null)
            throw new IllegalArgumentException("Session is null");
        this.tcpIpPort = tcpIpPort;
        this.uartPort = uartPort;
        this.session = session;
    }

  
    @Override
    public String getId() {
        return getClass().getName();
    }
   
    @Override
    public void connect(ITerminalControl control) {
        Logger.log("entered."); //$NON-NLS-1$
        fControl=control;       
        final TermSimWorker worker = new TermSimWorker(this,control,tcpIpPort);
        worker.start();
        session.addSessionDisposeListener(new ICDISeeCodeSession.ISessionDisposeListener(){

            @Override
            public void onSessionDisposed (ICDISeeCodeSession session1) {
                worker.terminate();
                
            }});
        ((ICDISession)session).getEventManager().addEventListener(this);
    }
    
    void setTerminalTitle(){
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable(){
            @Override
            public void run(){
                fControl.setTerminalTitle("Terminal UART Port " + uartPort);
            }});
    }
    @Override
    public void disconnect() {
        Logger.log("entered."); //$NON-NLS-1$
    
        if (getSocket() != null) {
            try {
                getSocket().close();
            } catch (Exception exception) {
                Logger.logException(exception);
            }
        }
    
        if (getInputStream() != null) {
            try {
                getInputStream().close();
            } catch (Exception exception) {
                Logger.logException(exception);
            }
        }
    
        if (getOutputStream() != null) {
            try {
                getOutputStream().close();
            } catch (Exception exception) {
                Logger.logException(exception);
            }
        }
        cleanSocket();
        setState(TerminalState.CLOSED);
    }
    @Override
    public boolean isLocalEcho() {
        if(fTermSimConnection!=null)
            return false;
        return fTermSimConnection.localEcho();
    }
    @Override
    public void setTerminalSize(int newWidth, int newHeight) {
        if(fTermSimConnection!=null)
            fTermSimConnection.setTerminalSize(newWidth, newHeight);
        
    }
    public InputStream getInputStream() {
        return fInputStream;
    }
    public OutputStream getOutputStream() {
        return fOutputStream;
    }
    private void setInputStream(InputStream inputStream) {
        fInputStream = inputStream;
    }
    private void setOutputStream(OutputStream outputStream) {
        fOutputStream = outputStream;
    }
    Socket getSocket() {
        return fSocket;
    }
    
    /**
     * sets the socket to null
     */
    void cleanSocket() {
        fSocket=null;
        setInputStream(null);
        setOutputStream(null);
    }
    
    void setSocket(Socket socket) throws IOException {
        if(socket==null) {
            cleanSocket();
        } else {
            fSocket = socket;
            setInputStream(socket.getInputStream());
            setOutputStream(socket.getOutputStream());
        }

    }
    void setTermSimConnection(TermSimConnection connection) {
        fTermSimConnection=connection;       
    }
    public void writeToTerminal(byte bytes[], int offset, int length) throws IOException {
        fControl.getRemoteToTerminalOutputStream().write(bytes,offset,length);
        
    }
    public void setState(TerminalState state) {
        fControl.setState(state);
        
    }
   
    public String getStatusString(String strConnected) {
        return strConnected; //TODO
    }
    @Override
    public void load(ISettingsStore store) {
        //TODO
        
    }
    @Override
    public void save(ISettingsStore store) {
       //TODO
    }
    public boolean isInstalled() {
        return true;
    }


    @Override
    public ISettingsPage makeSettingsPage () {
        // @todo Auto-generated method stub
        return null;
    }
    
    private void handleRestartedEvent(ICDIRestartedEvent event){
        // Done implicitly
//        // We re-set the debug view when the last target restarts.
//        // Don't know how else to do it. If only a subset of CMPD processes are
//        //restarted, do we, or do we not, re-generated the UART simulator?
//        if (event.getSource() instanceof ICDITarget) {
//            ICDITarget targets[] = ((ICDISession)session).getTargets();
//            if (targets.length > 0 && targets[targets.length-1] == event.getSource()){
//                this.disconnect();
//            }
//        }
    }


    @Override
    public void handleDebugEvents (ICDIEvent[] events) {
        for (ICDIEvent event: events){
            if (event.getSource().getTarget().getSession() == session){
                if (event instanceof ICDIRestartedEvent){
                    handleRestartedEvent((ICDIRestartedEvent)event);
                }
            }
        }
        
    }


    /* (non-Javadoc)
     * @see org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector#getInitializationErrorMessage()
     */
    @Override
    public String getInitializationErrorMessage () {
        // TODO Auto-generated method stub
        return null;
    }


    /* (non-Javadoc)
     * @see org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector#getName()
     */
    @Override
    public String getName () {
        return "UART Simulator Display, port " + this.uartPort;
    }


    /* (non-Javadoc)
     * @see org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector#getSettingsSummary()
     */
    @Override
    public String getSettingsSummary () {
        if (isInitialized()) return "Connected";
        return "Closed";
    }


    /* (non-Javadoc)
     * @see org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector#getTerminalToRemoteStream()
     */
    @Override
    public OutputStream getTerminalToRemoteStream () {
        return getOutputStream();
    }


    /* (non-Javadoc)
     * @see org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector#isInitialized()
     */
    @Override
    public boolean isInitialized () {
        return getOutputStream() != null;
    }


    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter (Class adapter) {
        // TODO Auto-generated method stub
        return null;
    }

}
