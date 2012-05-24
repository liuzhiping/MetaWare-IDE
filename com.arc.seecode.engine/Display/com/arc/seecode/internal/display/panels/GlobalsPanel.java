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
 * The toolbar panel of a "globals" display. It is instantiated by
 * reflection.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class GlobalsPanel extends ExtensionsPanel {

    private boolean fPopupsAreDynamic = false;
    private boolean fWidgetsAreDynamic = false;

    /**
     * Instantiated by reflection.
     * 
     */
    public GlobalsPanel() {
        super();
        this.setDoubleClickAction("double_click");
    }
    
    @Override
    protected void setContext(IContext context){
        fPopupsAreDynamic  = context.getEngineBuildID().compareTo("1353") >= 0;
        fWidgetsAreDynamic = context.getEngineBuildID(). compareTo("1515") >= 0;
    }

    @Override
    protected void addStaticComponents () {
        super.addStaticComponents();
        if (!fPopupsAreDynamic) {
            makeNameChoice();
            this.makeButtonAndMenuItem("Examine", "examine_button", "Examine content of selected line");
            this.makeButtonAndMenuItem("Watch", "watchpoint", "Set or reset watchpoint on selected line");
            this.makeButtonAndMenuItem("Change", "change", "Change value at selected address");
            this.makeButton("Source", "show_source", "Display source code");
        }
        else if(!fWidgetsAreDynamic){
            //New way: engine does the popup menus itself.
            this.makeButton("Examine", "examine_button", "Examine content of selected line");
            this.makeButton("Watch", "watchpoint", "Set or reset watchpoint on selected line");
            this.makeButton("Change", "change", "Change value at selected address");
            this.makeButton("Source", "show_source", "Display source code");
        }
    }
}
