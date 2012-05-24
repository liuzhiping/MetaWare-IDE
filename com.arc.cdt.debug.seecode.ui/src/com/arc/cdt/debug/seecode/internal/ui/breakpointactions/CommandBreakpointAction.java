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
package com.arc.cdt.debug.seecode.internal.ui.breakpointactions;

import org.eclipse.cdt.debug.core.breakpointactions.AbstractBreakpointAction;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IBreakpoint;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.seecode.command.ICommandProcessor;


/**
 * Causes a MetaWare debugger command to be executed when a breakpoint is hit.
 * @author davidp
 *
 */
public class CommandBreakpointAction extends AbstractBreakpointAction {
    private String[] fCommands = new String[0];
    /**
     * 
     */
    public CommandBreakpointAction() {
        // TODO Auto-generated constructor stub
    }

   
    @Override
    public IStatus execute (IBreakpoint breakpoint, IAdaptable context, IProgressMonitor monitor) {
        ICDIThread thread = (ICDIThread)context.getAdapter(ICDIThread.class);
        IStatus status = Status.OK_STATUS;
        if (thread != null) {
            ICDITarget target = thread.getTarget();
            if (target instanceof IAdaptable){
                ICommandProcessor proc = (ICommandProcessor)((IAdaptable)target).getAdapter(ICommandProcessor.class);
                if (proc != null) {
                    try {
                        for (String c: fCommands)
                            proc.processCommand(c);
                    }
                    catch (Exception e) {
                        status = SeeCodePlugin.makeErrorStatus(e.getMessage(),e);
                    }
                }
            }
        }
        return status;
    }

    
    @Override
    public String getDefaultName () {
        return "Debugger Commands";
    }

    
    @Override
    public String getIdentifier () {
        return "com.arc.cdt.debug.seecode.ui.commandAction";
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.breakpointactions.IBreakpointAction#getMemento()
     */
    @Override
    public String getMemento () {
        if (fCommands.length == 1) return fCommands[0];
        StringBuilder buf = new StringBuilder();
        for (String c: fCommands){
            if (buf.length() > 0) buf.append('\n');
            buf.append(c);
        }
        return buf.toString();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.breakpointactions.IBreakpointAction#getSummary()
     */
    @Override
    public String getSummary () {
        return fCommands[0] + (fCommands.length > 1?"...":"");
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.breakpointactions.IBreakpointAction#getTypeName()
     */
    @Override
    public String getTypeName () {
        return "Debugger Command Action";
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.breakpointactions.IBreakpointAction#initializeFromMemento(java.lang.String)
     */
    @Override
    public void initializeFromMemento (String data) {
        fCommands = data.split("\n");
    }
    
    /**
     * Set commands; may be multiple, one per line.
     * @param command
     */
    public void setCommand(String command){
        if (command.indexOf("\r\n") >= 0)
            fCommands = command.split("\\r\\n");
        else if (command.indexOf("\n") >= 0)
            fCommands = command.split("\\n");
        else fCommands = command.split("\\r"); // MAC?
    }
    
    public String[] getCommands(){
        return fCommands;
    }

}
