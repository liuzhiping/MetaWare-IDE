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

/**
 * Panel for dissassembly display.
 * Invoked by reflection.
 * @author David Pickens
 */
public class DisasmPanel extends SourceBasedPanel {

    /**
     * 
     */
    public DisasmPanel() {
        super();
    }
    
    @Override
    protected void addStaticComponents() {
        super.addStaticComponents();
        makeChoiceWidget("Goto: ","addr","Specify new address","code_addr");
        makeButton("Break","breakpoint", "Set or reset breakpoint on selected line");
        
        setSourceWindowMenuItems();
        this.setDoubleClickAction("breakpoint");
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.internal.display.panels.ExtensionsPanel#finish()
     */
    @Override
    public void finish() {
        super.finish();
        makeButton("PC","show_pc","Show source of PC");
    }
}
