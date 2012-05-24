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
package com.metaware.guihili.builder;

import java.beans.PropertyVetoException;

import org.xml.sax.SAXException;

import com.arc.widgets.IButton;
import com.arc.widgets.IComponent;
import com.arc.widgets.IContainer;
import com.arc.xml.Element;
import com.metaware.guihili.Gui;
import com.metaware.guihili.ITextWrapper;
import com.metaware.guihili.builder.legacy.RadioButtonGroupBuilder;

/**
 * Construct a checkbox button
 */
public class CheckBoxBuilder extends ButtonBuilder {
    public CheckBoxBuilder(Gui gui) {
        super(gui);
    }

    @Override
    public IButton createButton(Element element) {
        IContainer parent = _gui.getParent();
        // Old legacy guihili generates checkboxes within radio button
        // groups. Go figure. We change them to radio buttons here.
        if (parent != null && RadioButtonGroupBuilder.isRadioButtonGroup(parent))
            return _gui.getComponentFactory().makeRadioButton(parent);
        return _gui.getComponentFactory().makeCheckBox(parent);
    }

    public void setTarget(String t) {
        // Ignored; appears in options file, but doesn't seem to do anything!
    }

    public void setDefault(boolean v) {
        mDefault = v;
        mDefaultSpecified = true;
    }
    // Don't know the difference between this and setDefault
    public void setDefault_evaluated(boolean v) {
        mDefault = v;
        mDefaultSpecified = true;
    }

    public void setAction(Object action) {
        super.setActionProc(action);
    }
    
    @Override
    public Object returnObject() throws SAXException {
        // If we have a "arg_action" then set "VALUE" to the
        // value  of checkbox.
        final IButton b = (IButton) super.returnObject();
        if (getValue() == null) {
            String name = super.getProperty();
            if (name != null && (mDefaultSpecified ||
                    _gui.getProperty(name) == null))
                try {
                    _gui.setProperty(
                        name,
                        mDefault ? Boolean.TRUE : Boolean.FALSE);
                }
                catch (PropertyVetoException x) {
                }
        }
        return b;
    }
    
    @Override
    protected ITextWrapper getActionValueWrapper(IComponent c){
        final IButton b = (IButton)c;
        return new ITextWrapper() {
            @Override
            public String getText() {
                String s = b.isEnabled() ? (b.isSelected() ? "1" : "0") : null;
                // System.out.println("VALUE of " + b.getName() + " is " + s);
                return s;
            }
            @Override
            public void setText(String s) {
            }
        };
    }
    private boolean mDefault = false;
    private boolean mDefaultSpecified;
}
