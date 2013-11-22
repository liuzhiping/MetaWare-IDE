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
package com.arc.seecode.display;

/**
 * A callback passed to the {@link MenuDescriptor}
 * to generate a menu in such a way as to be independent
 * of the underlying GUI system.
 * @author David Pickens
 */
public interface IMenuGenerator {
    public interface IItemObserver {
        void menuItemSelected(String name);
    }
    
    public interface ICheckBoxObserver {
        void selectionChanged(String name, boolean selection);
    }
    /**
     * Create a menu item within the given parent.
     * @param parent the parent (a menu).
     * @param name the ID of the menu.
     * @param label the label text.
     * @param observer a callback observer to be
     * notified when the menu is selected.
     * @param enabled enabled state of menu.
     */
    void makeMenuItem(Object parent, String name, String label, IItemObserver observer, boolean enabled);
    
    /**
     * Create a checkbox menu item within the given parent.
     * @param parent the parent (a menu).
     * @param name the ID of the menu.
     * @param label the label text.
     * @param initValue the initial setting.
     * @param enabled enable state.
     * @param observer a callback observer to be
     * notified when the menu's selection changes
     */
    void makeCheckBoxItem(Object parent, String name, String label, ICheckBoxObserver observer,
                boolean enabled,
            boolean initValue);
    
    /**
     * Create a radio-style menu item within the given parent.
     * @param parent the parent (a menu).
     * @param name the ID of the menu.
     * @param label the label text.
     * @param observer a callback observer to be
     * notified when the menu's selection changes
     * @param enabled indicate enable state.
     * @param initValue the initial setting.
     * @param groupName the group that the radio item is
     * to be part of; only one radio item in a group will be selected.
     */
    void makeRadioItem(Object parent, String name, String label, ICheckBoxObserver observer,
            boolean enabled,
            boolean initValue,
            String groupName);
    
    /**
     * Make a submenu
     * @param parent the parent menu.
     * @param name the name to be associated with the widget.
     * @param label the label for the submenu.
     * @return the submenu that may appear as a parent
     * in calls to {@link #makeMenuItem} or {@link #makeCheckBoxItem}.
     */
    Object makeSubmenu(Object parent, String name, String label);
    
    /**
     * Called to add a separator to the parent menu.
     * @param parent the parent menu.
     */
    void makeSeparator(Object parent);

}
