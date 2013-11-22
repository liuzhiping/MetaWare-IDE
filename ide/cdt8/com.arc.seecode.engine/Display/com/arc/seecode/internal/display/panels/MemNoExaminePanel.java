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
 * The Memory display with no "examine" capibility.
 * This is instantiated by reflection.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class MemNoExaminePanel extends MemPanel {

    /**
     * Instantiated by reflection.
     * 
     */
    public MemNoExaminePanel() {
        super();
    }

    @Override
    protected boolean allowExamine () {
        return false;
    }
}