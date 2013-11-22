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

import javax.swing.JToggleButton;

import com.arc.widgets.IContainer;

/**
 * @author David Pickens
 */
class ToggleButton extends Button {

    /**
     * @param parent
     * @param mapper
     */
    ToggleButton(IContainer parent, IComponentMapper mapper) {
        super(parent, mapper);
    }
    
    
    /* (non-Javadoc)
     * @see com.arc.widgets.IButton#getButtonKind()
     */
    @Override
    public int getButtonKind() {
        return TOGGLE;
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.swing.SwingComponent#instantiate()
     */
    @Override
    protected Component instantiate() {
        return new JToggleButton();
    }
}
