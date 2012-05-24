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
 * The toolbar panel of a "Examine" display. It is instantiated
 * via reflection.
 * @author David Pickens
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class ExaminePanel extends ExtensionsPanel {

    /**
     * Instantiate (only called by reflection during display construction).
     * 
     */
    public ExaminePanel() {
        super();
    }
    
    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * <P>
     * Add widgets specific to Examine display.
     */
    @Override
    protected void addStaticComponents() {
        super.addStaticComponents();
        makeChoiceWidget("Examine: ","Examine:","Enter variable to examine",
        "examine_expr");
        makeButtonAndMenuItem("Watch", "watchpoint",
                "Set or reset watchpoint on selected line");

        makeButtonAndMenuItem("Change", "change", "Change value at selected address");
        makeButtonAndMenuItem("Follow","examine_in_place_button", "Follow selected item");
        
        makeMenuItem("Memory","memory", "Bring up memory display");
        makeMenuItem("Remove","remove_button", "Remove what");
        makeBooleanToggle(
                "shallow_deep", true, "Shallow", "Deep",
                "Shallow binding",
                "Deep binding",false);
       
    }

}
