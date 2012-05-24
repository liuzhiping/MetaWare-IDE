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
package com.arc.widgets.internal.swing;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.arc.widgets.IComponent;
import com.arc.widgets.IMenuItem;

/**
 * Wrapper for a Menu Item. Also base class for Menu.
 * @author David Pickens
 */
class MenuItem extends Button implements IMenuItem {
    private IComponent mParent;
    /**
     * @param parent an IMenu or IMenuBar
     * @param mapper
     */
    public MenuItem(IComponent parent, IComponentMapper mapper) {
        super(null, mapper);
        mParent = parent;
    }
    

    /* (non-Javadoc)
     * @see com.arc.widgets.swing.SwingComponent#instantiate()
     */
    @Override
    protected Component instantiate() {
       return new JMenuItem();
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.IComponent#getComponent()
     */
    @Override
    public Object getComponent() {
        if (mComponent == null){
            super.getComponent();
            JMenuItem item = (JMenuItem)instantiate();
            Component p = (Component)mParent.getComponent();
            if (p instanceof JMenu)
                ((JMenu)p).add(item);
            else if (p instanceof JPopupMenu){
                ((JPopupMenu)p).add(item);
            }
            else
                ((Container)p).add(item);
        }
        return super.getComponent();
    }
}
