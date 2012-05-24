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

import javax.swing.JLabel;

import com.arc.widgets.IContainer;
import com.arc.widgets.ILabel;

class Label extends SwingComponent implements ILabel {

    private String mText;

    Label(IContainer parent, String text, IComponentMapper mapper) {
        super(parent, mapper);
        mText = text;
    }

    @Override
    public String getText() {
        if (mComponent != null)
            return ((JLabel) mComponent).getText();
        return mText;
    }

    @Override
    public void setText(String s) {
        mText = s;
        if (mComponent != null)
            ((JLabel) mComponent).setText(s);
    }

    @Override
    protected Component instantiate() {
        return new JLabel(mText);
    }

    @Override
    public void setWrap (boolean v) {
        // What do we do??
        
    }
}
