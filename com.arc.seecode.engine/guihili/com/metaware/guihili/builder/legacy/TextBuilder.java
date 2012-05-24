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
package com.metaware.guihili.builder.legacy;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.arc.widgets.IComponent;
import com.arc.widgets.ITextField;
import com.metaware.guihili.Gui;
import com.metaware.guihili.ITextWrapper;
import com.metaware.guihili.builder.TextFieldBuilder;

/**
 * Guihili "text" node:
 * 
 * Attributes are:
 * <dl>
 * <dt>property
 * <dd> name of property to set
 * <dt>expandable
 * <dd> stretches in layout
 * </dl>
 */
public class TextBuilder extends TextFieldBuilder {
    public TextBuilder(Gui gui) {
        super(gui);
    }

    public void setDefault(String def) {
        mDefault = def;
    }

    public void setEnter_is_save(boolean b) {
        mEnterFiresOKAction = b;
    }

    @Override
    protected IComponent makeComponent() {
        mTextField = (ITextField) super.makeComponent();
        if (mDefault != null) {
            mTextField.setText(mDefault);
        }
        if (mEnterFiresOKAction) {
            // Listen for "enter" key, and dispose of enclosing window.
            mTextField.addActionListener(new ActionListener() {
                // Supposedly getKeyCode doesn't work for keyTyped
                @Override
                public void actionPerformed(ActionEvent event) {
                    ActionListener a = _gui.getAction("OK");
                    if (a != null)
                        a.actionPerformed(event);
                }
            });
        }
        return mTextField;
    }
    
    @Override
    protected ITextWrapper getActionValueWrapper(IComponent c){
        return new ITextWrapper() {
            @Override
            public String getText() {
                return mTextField.isEnabled() ? mTextField.getText() : null;
            }

            @Override
            public void setText(String s) {
            }
        };
    }

    private String mDefault;

    private boolean mEnterFiresOKAction;

    private ITextField mTextField;

}
