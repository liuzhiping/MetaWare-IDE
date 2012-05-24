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
 * Panel for Breakpoint display.
 * Instantiated by reflection.
 * @author David Pickens
 */
public class BrkPanel extends AbstractBreakWatchPanel {

    /**
     * 
     */
    public BrkPanel() {
        super();
        setDoubleClickAction("double_click");
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.internal.display.panels.AbstractBreakWatchPanel#addStaticComponents2(java.lang.String, boolean, boolean)
     */
    @Override
    protected void addStaticComponents() {
        super.addStaticComponents();
        super.addStaticComponents2("breakpoint", false, false);
        makeButtonAndMenuItem("Source","Show Source","show_source","Display source code");
    }
}
