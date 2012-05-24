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

import java.util.List;

import org.xml.sax.SAXException;

import com.arc.xml.Element;
import com.metaware.guihili.Gui;

/**
 * A card that is part of a card layout. See {@link CardLayoutBuilder}.
 * 
 * @author David Pickens May 22, 2002
 */
public class CardBuilder extends Builder {
    public CardBuilder(Gui gui) {
        super(gui);
    }

    @Override
    public void startNewInstance(Element element) {
        mElement = element;
    }

    public void setName(String name) {
    } // name retrieved from child element.

    @Override
    public Object returnObject() throws SAXException {
        List<Object> kids = getChildren();
        if (kids.size() == 0) {
            error(mElement, "\"card\" node lacks a component child");
            return null;
        }
        if (kids.size() > 1)
            error(mElement, "\"card\" node must have a single component child");
        return kids.get(0);
    }

    private Element mElement;
}
