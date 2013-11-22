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

import javax.swing.JCheckBox;

import com.arc.widgets.IContainer;

class CheckBox extends Button {

    CheckBox(IContainer parent, IComponentMapper mapper) {
        super(parent, mapper);
    }

    @Override
    public int getButtonKind() {
        return CHECKBOX;
    }

    @Override
    protected Component instantiate() {
        return new JCheckBox();
    }
}
