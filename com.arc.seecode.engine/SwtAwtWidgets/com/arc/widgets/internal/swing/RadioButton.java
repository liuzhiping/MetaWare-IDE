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
import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import com.arc.widgets.IContainer;

class RadioButton extends Button {

    RadioButton(IContainer parent, IComponentMapper mapper) {
        super(parent,mapper);
        if (parent != null) {
            mGroup = sGroupMap.get(parent);
            if (mGroup == null) {
                mGroup = new ButtonGroup();
                sGroupMap.put(parent, mGroup);
            }
        }
    }

    @Override
    public int getButtonKind() {
        return RADIO;
    }

    @Override
    protected Component instantiate() {
        JRadioButton b = new JRadioButton();
        if (mGroup != null)
            mGroup.add(b);
        return b;
    }
    private ButtonGroup mGroup;
    private static Map <IContainer,ButtonGroup>sGroupMap = new HashMap<IContainer,ButtonGroup>();
}
