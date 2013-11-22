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
package com.arc.cdt.debug.seecode.ui.termsim;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.arc.cdt.debug.seecode.core.ITermSimInstantiator;
import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.core.cdi.ICDISeeCodeSession;
import com.arc.cdt.debug.seecode.ui.UISeeCodePlugin;
import com.arc.cdt.debug.seecode.ui.internal.termsim.TermSimConnector;


public class TermSimInstantiator implements ITermSimInstantiator {
    public final static String TERMINAL_VIEW_ID = "org.eclipse.tm.arc.terminal.view.TerminalView";
    static class TermSimInstance {
        TermSimInstance(ICDISeeCodeSession session, int tcpIpPort, int uartPort){
            this.session = session;
            this.tcpIpPort = tcpIpPort;
            this.uartPort = uartPort;
            this.viewSecondaryId = null;
        }
        ICDISeeCodeSession session;
        int tcpIpPort;
        int uartPort;
        String viewSecondaryId;
    }
    private  List<TermSimInstance> instances = new ArrayList<TermSimInstance>();
    
    /**
     * Show the terminal simulator view and connect it to the debugger at the given
     * port.
     * @param session the debugger session.
     * @param tcpIpPort the TCP/IP port that through which the debugger engine
     * will communicate.
     * @param uartPort the UART port number; there can be more than one terminal.
     * @throws PartInitException 
     */
    @Override
    public void createTermSimView(ICDISeeCodeSession session, int tcpIpPort, int uartPort) throws PartInitException{
        
        // Look for any terminal view and see if we can hijack it. This is preferable from
        // just arbitrarily creating a new one, because the user may have it where he wants it.
        TermSimInstance reuse = null;
        final Set<String> inUse = new HashSet<String>();
        final boolean nullSecondarySeen[] = new boolean[1];
        for (TermSimInstance i: instances){
            if (i.session != session && i.session.isTerminated()){
                i.session = session;
                i.tcpIpPort = tcpIpPort;
                i.uartPort = uartPort;
                reuse = i;
            }
            else if (i.viewSecondaryId != null) {
                inUse.add(i.viewSecondaryId);
            }
            else nullSecondarySeen[0] = true;
        }
        Display display = PlatformUI.getWorkbench().getDisplay();
        final TermSimInstance i = reuse != null?reuse:new TermSimInstance(session,tcpIpPort,uartPort);
        i.viewSecondaryId = "";
       

        display.syncExec(new Runnable(){

            @Override
            public void run () {
                IWorkbench workbench = PlatformUI.getWorkbench();
                IViewReference views[] = workbench.getActiveWorkbenchWindow().getActivePage().getViewReferences();
                for (IViewReference view:views){
                    if (TERMINAL_VIEW_ID.equals(view.getId())) {
                        IViewPart viewPart = view.getView(false);
                        if (viewPart != null){
                            ITerminalViewControl control = (ITerminalViewControl)viewPart.getAdapter(ITerminalViewControl.class);
                            if (control != null && control.getState() == TerminalState.CLOSED &&
                                // Boundary condition can have "state" as disconnected while it is in
                                // the process of being connected.
                                !(view.getSecondaryId() != null && inUse.contains(view.getSecondaryId()) &&
                                !(view.getSecondaryId() == null && nullSecondarySeen[0])))
                            {
                                i.viewSecondaryId = view.getSecondaryId();
                            }
                            else if (view.getSecondaryId() != null){
                                inUse.add(view.getSecondaryId());
                            }
                            else nullSecondarySeen[0] = true;
                        }
                    }
                }
                
                
            }});
        if (i.viewSecondaryId != null && i.viewSecondaryId.length() == 0) {
            i.viewSecondaryId = nullSecondarySeen[0]?computeSecondaryId(inUse):null;
        }
        if (reuse == null)
            instances.add(i);
        showView(TERMINAL_VIEW_ID,i.viewSecondaryId,session,tcpIpPort,uartPort);
    }
    
    /**
     * Compute a secondary ID for a terminal view.
     * @param exclude the set of IDs that are currently in use (so as not to 
     * hijack an existing active terminal view!).
     * @return an unused secondary Id.
     */
    private static String computeSecondaryId(Set<String>exclude){
        int i = 0;
        while (true) {
            i++;
            String s = i+"";
            if (!exclude.contains(s)){
                return s;
            }
        }       
    }
    
    private static void showView(final String viewId, final String secondaryId, final ICDISeeCodeSession session,
                    final int tcpPort, final int uartPort) throws PartInitException{
        IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench.getDisplay().getThread() != Thread.currentThread()){
            // Note that we do the work asynchronously so that we don't lock this thread. It is used
            // to launch the debugger engine.
            workbench.getDisplay().asyncExec(new Runnable(){

                @Override
                public void run () {
                    try {
                        showView(viewId,secondaryId,session,tcpPort,uartPort);
                    }
                    catch (PartInitException e) {
                        SeeCodePlugin.log(e);
                    }
                    
                }});
                
        }
        else {
            // Assertion: we're in the UI thread.
            final IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
            IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
            
            final IViewPart viewPart = activePage.
                showView(viewId,secondaryId,IWorkbenchPage.VIEW_ACTIVATE);
          
           
            ITerminalViewControl control = (ITerminalViewControl)viewPart.getAdapter(ITerminalViewControl.class);
            if (control.getState() == TerminalState.CLOSED){
                control.disconnectTerminal();
            }
            // When switching to Debug perspective, make sure terminal view doesn't vanish.
            activeWorkbenchWindow.addPerspectiveListener(new IPerspectiveListener(){

                @Override
                public void perspectiveChanged (IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {        
                }

                @Override
                public void perspectiveActivated (IWorkbenchPage page, IPerspectiveDescriptor perspective) {
                    if (!session.isTerminated()) {
                        try {
                            page.showView(viewId,secondaryId,IWorkbenchPage.VIEW_VISIBLE);
                        }
                        catch (PartInitException e) {
                            UISeeCodePlugin.log(e);
                        }
                    }
                    else 
                        activeWorkbenchWindow.removePerspectiveListener(this); // session over
                    
                }});
            control.setConnector(new TermSimConnector(tcpPort,uartPort,session));
            control.connectTerminal();
        }
    }

    @Override
    public void reconnectTermSimViews (ICDISeeCodeSession session) throws Exception {
        // Check if we're doing a "restart". In such a case, re-use any existing terminal view.
        for (TermSimInstance i: instances){
            if (i.session == session){
                showView(TERMINAL_VIEW_ID,i.viewSecondaryId,session,i.tcpIpPort,i.uartPort);
            }
        }
        
    }

}
