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
 * The toolbar for a Memory display. Instantiated by reflection.
 * @author David Pickens
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class MemPanel extends ExtensionsPanel {

    private boolean fSaveStateOfGotoBox = false;
    /**
     * Instantiated by reflection
     * 
     */
    public MemPanel() {
        super();
        // @todo Auto-generated constructor stub
    }

    @Override
    protected void addStaticComponents () {
        super.addStaticComponents();
        //can't save state until we can get the resolved symbol from
        // a choose-from-list dialog.
        String key = "data_addr";
        if (this.fSaveStateOfGotoBox){
            key = "data_addr_eval";
        }
        this.makeChoiceWidget("Goto: ","addr","Address of memory block to display",key,
                    this.fSaveStateOfGotoBox);  
        if (allowExamine()){
            this.makeMenuItem("Examine","examine_button",
                    "Examine content of selected line");
        }
    }
    
    /**
     * Return whether or not we are to support "examine" as a menu item.
     * The {@link MemNoExaminePanel} overrides this to return false.
     * @return whether or not we are to support "examine" as a menu item.
     */
    protected boolean allowExamine(){
        return true;
    }
    
    @Override
    public void finish () {
        this.setDoubleClickAction("change");

        this.makeButtonAndMenuItem("Change","change","Change value at selected address");
        this.makeBooleanToggle("direction", true, "memup", "memdown",
            "Display increasing addresses",
            "Display decreasing addresses",true);
        this.makeButton("Refresh", "refresh", "Refresh the selected view.");
        
        super.finish();
    }

    @Override
    public void setContext (IContext context) {
        // Engine can only handle persistent saving of Goto: box since build id 1337.
        this.fSaveStateOfGotoBox = context.getEngineBuildID().compareTo("1337") >= 0;
    }
}
