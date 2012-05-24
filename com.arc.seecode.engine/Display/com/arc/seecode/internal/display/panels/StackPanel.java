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
 * The Toolbar of a SeeCode "Stack" display. It is instantiated by reflection
 * during display construction.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class StackPanel extends ExtensionsPanel {

    /**
     * Invoked by reflection
     * 
     */
    public StackPanel() {
    }

    @Override
    protected void addStaticComponents () {
        //The source and locals display are always in sync with the Debug View.
        // So, we don't support them under IDE.
//        this.makeButtonAndMenuItem("Locals","examine_button",
//            "Display locals of selected frame");
//        this.makeButtonAndMenuItem("Source","Show Source",
//                     "show_source","Display source code");
//        setDoubleClickAction("examine_button");       
    }
}
