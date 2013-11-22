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
package com.arc.cdt.debug.seecode.internal.ui;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;

/**
 * An interface for dynamically updating the
 * Eclipse menubar.
 * @author David Pickens
 */
public interface IMenuBarUpdateManager {
    /**
     * Insert a menu at an appropriately location
     * in the menubar.
     * @param action a pop-down-style action.
     */
    void insertMenu(IAction action);
    
    /**
     * Remove a previously-installed pop-down-style action.
     * @param action a pop-down-style action to remove.
     */
    void removeMenu(IAction action);
    
    /**
     * Remove all menus that we have previously added.
     *
     */
    void removeAll();
    
    /**
     * Return the delegate menu manager.
     * @return the delegate menu manager
     */
    IMenuManager getMenuManager();
    
    /**
     * Apply changes to the delegate menu manager.
     *
     */
    void update();
    
    /**
     * Return whether or not an action has been added.
     * @param action
     * @return true if action has been added.
     */
    boolean contains(IAction action);
    

}
