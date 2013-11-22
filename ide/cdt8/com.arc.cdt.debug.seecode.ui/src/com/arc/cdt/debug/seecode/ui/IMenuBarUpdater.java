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
package com.arc.cdt.debug.seecode.ui;

import com.arc.seecode.display.MenuDescriptor;
import com.arc.seecode.engine.EngineInterface;

/**
 * This is the hook by which we add new menus to the Eclipse menubar. The lone
 * instance of this interface is retrieved from by invoking
 * <code>UISeeCodePlugin.getDefault().{@link UISeeCodePlugin#getMenuBarUpdater getMenuBarUpdator()}.
 * @author David Pickens
 */
public interface IMenuBarUpdater {
    /**
     * Called when engine has generated a new menu to appear in the menubar
     * (e.g., RTOS-awareness menu).
     * 
     * @param engine
     *            the engine instance to be associated with the menu.
     * @param label
     *            label for the menu.
     * @param menu
     *            a dynamically-generated menu from the engine.
     */
    public void addMenu(EngineInterface engine, String label,
            MenuDescriptor menu);
}
