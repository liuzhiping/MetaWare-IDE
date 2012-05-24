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
 * Panel for Watchpoint display. Instantiated by reflection.
 * @author David Pickens
 */
public class WatchPanel extends AbstractBreakWatchPanel {

    /**
     * 
     */
    public WatchPanel() {
        super();
    }
    
    @Override
    protected void addStaticComponents() {
        super.addStaticComponents();
        super.addStaticComponents2("watchpoint", true, false);
    }

}
