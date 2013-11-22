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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


/**
 * Widget for defining debugger command to execute at a breakpoint.
 * @author davidp
 *
 */
public class CommandBreakpointActionComposite extends Canvas {
    private CommandBreakpointPage commandActionPage;
    private Text commands;

    public CommandBreakpointActionComposite(Composite parent, int style, CommandBreakpointPage actionPage) {
        super(parent, style);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        setLayout(gridLayout);

        this.commandActionPage = actionPage;

        final Label messageToLogLabel = new Label(this, SWT.NONE);
        messageToLogLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));
        messageToLogLabel.setText("Enter debugger commands, one per line"); //$NON-NLS-1$

        commands = new Text(this, SWT.BORDER | SWT.WRAP);
        commands.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1));

        String commandStrings[] = this.commandActionPage.getAction().getCommands();
        if (commandStrings.length == 1) commands.setText(commandStrings[0]);
        else {
            StringBuilder buf = new StringBuilder();
            for (String s: commandStrings){
                if (buf.length() > 0) buf.append('\n');
                buf.append(s);
            }
            commands.setText(buf.toString());
        }
    }

    public String getCommand() {
        return commands.getText();
    }
}
