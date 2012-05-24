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

import org.xml.sax.SAXException;

import com.arc.widgets.IComponent;
import com.metaware.guihili.Gui;

/**
 * Process a "button" tag under a "textField" tag.
 * This must be a previously defined button that is
 * to trigger the update of the text property.
 */
public class TextFieldButtonBuilder extends Builder {
    public TextFieldButtonBuilder(Gui gui) {
        super(gui);
    }
    public void setName(String name) {
        _name = name;
    }

    @Override
    public Object returnObject() throws SAXException {
        IComponent c = _gui.getComponent(_name);
        if (c == null)
            throw new SAXException(
                _name + " must be a previously defined component name.");
        return c;
    }
    private String _name;
}
