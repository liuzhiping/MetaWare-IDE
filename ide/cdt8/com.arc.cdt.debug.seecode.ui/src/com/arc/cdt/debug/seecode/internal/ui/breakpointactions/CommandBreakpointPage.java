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

import org.eclipse.cdt.debug.core.breakpointactions.IBreakpointAction;
import org.eclipse.cdt.debug.ui.breakpointactions.IBreakpointActionPage;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.swt.widgets.Composite;


/**
 * Command page as a breakpoint action.
 * @author davidp
 *
 */
public class CommandBreakpointPage extends PlatformObject implements IBreakpointActionPage {

    /**
     * 
     */
    public CommandBreakpointPage() {
        // TODO Auto-generated constructor stub
    }

    private CommandBreakpointAction commandAction;
    private CommandBreakpointActionComposite editor;


    @Override
    public void actionDialogCanceled() {
    }

    @Override
    public void actionDialogOK() {
        commandAction.setCommand(editor.getCommand());
    }

    @Override
    public Composite createComposite(IBreakpointAction action, Composite composite, int style) {
        commandAction = (CommandBreakpointAction) action;
        editor = new CommandBreakpointActionComposite(composite, style, this);
        return editor;
    }
    
    public CommandBreakpointAction getAction() { return commandAction; }


}
