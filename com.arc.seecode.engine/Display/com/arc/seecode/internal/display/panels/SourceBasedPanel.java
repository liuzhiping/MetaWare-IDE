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
package com.arc.seecode.internal.display.panels;

import com.arc.seecode.display.MenuDescriptor;


/**
 * @author davidp
 *
 */
public class SourceBasedPanel extends ExtensionsPanel {

    private int fLine;
    private int fStartColumn;
    private int fEndColumn;

    /**
     * 
     */
    public SourceBasedPanel() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void setSelection (int line, int startColumn, int endColumn, String selection) {
        super.setSelection(line, startColumn, endColumn, selection);
        fLine = line;
        fStartColumn = startColumn;
        fEndColumn = endColumn;       
    }
    
    /**
     * Refactored out so as to be callable from {@link DisasmPanel} for disassembly display.
     * @param p the panel.
     */
    protected void setSourceWindowMenuItems(){
        makeMenuItem("Breakpoint", "breakpoint","Set a breakpoint at the selected location");
        makeMenuItem("Hardware Breakpoint", "hwbreakpoint", "Set a hardware breakpoint at the selected location");
        makeMenuItem("Software Breakpoint", "swbreakpoint", "Set a s breakpoint at the selected location");
        makeMenuItem("Run to cursor","run_to_here", "Run to the selected line");
        makeMenuItem("Set PC","set_pc_at","Set the PC to the selected location");
        makeMenuItem("Evaluate","quickeval",new MenuDescriptor.IActionObserver() {

            @Override
            public void actionPerformed(String name) {
                mSender.sendValueUpdate("quickeval", 
                    " " + (fStartColumn+1) + " " + fEndColumn + " " + fLine);
            }
        });
        
    }


}
