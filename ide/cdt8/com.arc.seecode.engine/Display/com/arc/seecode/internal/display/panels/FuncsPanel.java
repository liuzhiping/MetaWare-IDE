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
 * Toolbar panel for "Functions" display. Instantiated by reflection.
 * @author David PIckens
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class FuncsPanel extends ExtensionsPanel {


    /**
     * Always instantiated by reflection.
     * 
     */
    public FuncsPanel() {
        super();
    }
    
    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * 
     */
    @Override
    protected void addStaticComponents () {
        super.addStaticComponents();
        makeNameChoice();
        makeButtonAndMenuItem("Break","breakpoint", "Set or reset breakpoint on selected line");
        makeButtonAndMenuItem("Source","show_source", "Display source code");
        setDoubleClickAction("breakpoint");
    }
   
}
