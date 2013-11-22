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

import com.arc.seecode.display.IContext;

/**
 * The extensions panel for the Locals display.
 * Instantiated by reflection.
 * @author David Pickens
 */
public class LocalsPanel extends ExtensionsPanel {
    private boolean engineSuppliesComponents = false;
    /**
     * 
     */
    public LocalsPanel() {
        super();
        setDoubleClickAction("double_click");
    }

    @Override
    protected void addStaticComponents() {
        super.addStaticComponents();
        if (!engineSuppliesComponents) { // newer engine supplies widgets, includeing new Expressions
            makeButton("Examine","examine_button","Examine content of selected line");
            makeButton("Watch","watchpoint","Set or reset watchpoint on selected variable");
            makeButton("Change","change","Change value at selected address");
            
            makeMenuItem("Examine","examine_button","Examine content of selected line");
            makeMenuItem("Watch","watchpoint","Set or reset watchpoint on selected variable");
            makeMenuItem("Change","change","Change value at selected address");
        }
    }
    
    @Override
    protected void setContext(IContext context){
        engineSuppliesComponents = context.getEngineBuildID().compareTo("1515") >= 0;
    }
}
