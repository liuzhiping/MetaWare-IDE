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

import javax.swing.JMenuBar;

import com.arc.widgets.IContainer;
import com.arc.widgets.IMenuBar;

/**
 * @author David Pickens
 */
class MenuBar extends AbstractContainer implements IMenuBar {

    /**
     * @param parent
     * @param mapper
     */
    public MenuBar(IContainer parent, IComponentMapper mapper) {
        super(parent, mapper);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.swt.Component#instantiate()
     */
    @Override
    protected Component instantiate() {
        return new JMenuBar();
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IContainer#getContainerStyle()
     */
    @Override
    public int getContainerStyle() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IContainer#setHorizontalSpacing(int)
     */
    @Override
    public void setHorizontalSpacing(int pixels) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IContainer#setVerticalSpacing(int)
     */
    @Override
    public void setVerticalSpacing(int pixels) {
        // TODO Auto-generated method stub

    }

}
