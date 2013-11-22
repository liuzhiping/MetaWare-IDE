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
 * Panel that goes on a Register display. It is instantiated via
 * reflection.
 * @author David Pickens
 */
public class RegPanel extends ExtensionsPanel {

    @Override
    protected void addStaticComponents() {
        super.addStaticComponents();
        makeNameChoice();
        setDoubleClickAction("change"); //cr91753
        makeButton("Change", "change", "Change value of selected register");
        makeButton("Watch", "watchpoint",
                "Set or reset watchpoint on selected line");

        makeMenuItem("Change", "change", "Change value of selected register");
        makeMenuItem("Watch", "watchpoint",
                "Set or reset watchpoint on selected line");
    }
}
