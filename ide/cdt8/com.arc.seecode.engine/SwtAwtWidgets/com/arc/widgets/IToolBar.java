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
package com.arc.widgets;

/**
 * A toolbar contains borderless buttons and 
 * dropdown menus.
 * <P>
 * It is made distinct from a panel with embedded
 * buttons because SWT has a separate widget for this.
 * @author David Pickens
 */
public interface IToolBar extends IComponent {
 
    /**
     * Create a tool item with this toolbar.
     * @param style on of {@link IToolItem#CHECK},
     * {@link IToolItem#PUSH}, {@link IToolItem#RADIO}, or {@link IToolItem#PULLDOWN}
     * @return the new toolbar item.
     */
    public IToolItem makeItem(int style);
    
    /**
     * Return the items that make up this toolbar.
     * @return all items.
     */
    public IToolItem[] getItems();
    /**
     * Add a separator to the toolbar.
     *
     */
    public void addSeparator();
}
