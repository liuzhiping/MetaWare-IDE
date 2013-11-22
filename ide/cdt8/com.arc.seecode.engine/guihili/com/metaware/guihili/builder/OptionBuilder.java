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

import com.arc.xml.Element;
import com.metaware.guihili.Gui;
import com.metaware.guihili.ITextWrapper;

/**
 * Construct an option under a select node.
 * Attributes are:
 * <ul>
 * <li>text
 * <li>value
 * </ul>
 */
public class OptionBuilder extends Builder implements ITextWrapper {
    public OptionBuilder(Gui gui) {
        super(gui);
    }

    @Override
    public void startNewInstance(Element element) {
    }

    @Override
    public String getText() {
        return _text;
    }
    public String getValue() {
        return _value;
    }

    @Override
    public void setText(String text) {
        _text = text;
    }
    public void setValue(String value) {
        _value = value;
    }
    
    /**
     * Handle "arg_action" attribute.
     *  "-arg_action=..."
     * This is an action procedure that evaluates to a string.
     * It is appended to the "ARG_ACTION" property list
     */
    public void setArg_action(Object o) {
        mArgAction = o;
    }
    @Override
    public Object returnObject() {
        if (_value == null)
            _value = _text;
        return this;
    }

    @Override
    public String toString() {
        return _text;
    }
    
    public void setCond(boolean v){
        _cond = v;        
    }
    
    public boolean getCond() { return _cond; }
    
    public Object getArg_action() { return mArgAction; }

    private String _text;
    private String _value;
    private Object mArgAction = null;
    private boolean _cond = true;
}
