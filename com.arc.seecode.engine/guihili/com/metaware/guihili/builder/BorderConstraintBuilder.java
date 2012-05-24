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

import java.awt.BorderLayout;
import java.awt.Component;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.arc.xml.Element;
import com.metaware.guihili.Gui;

/**
 * This class constructs a constrained component that is appropriate for a
 * border layout panel. The constraints are the strings "North", "South",
 * "East", "West", and "Center".
 */
public class BorderConstraintBuilder extends Builder {
    public BorderConstraintBuilder(Gui gui) {
        super(gui);
    }

    @Override
    public void startNewInstance(Element element) throws SAXException {
        String tag = element.getTagName();
        if (tag.equals("center"))
            _constraint = BorderLayout.CENTER;
        else if (tag.equals("south"))
            _constraint = BorderLayout.SOUTH;
        else if (tag.equals("north"))
            _constraint = BorderLayout.NORTH;
        else if (tag.equals("east"))
            _constraint = BorderLayout.EAST;
        else if (tag.equals("west"))
            _constraint = BorderLayout.WEST;
        else
            throw new SAXException("Unrecognized border constraint: " + tag);
    }

    @Override
    public void addChild(Object object, Element element) throws SAXException {
        if (_component != null)
            throw new SAXParseException(
                    "A border constraint may have only one component", null,
                    element.getSource(), element.getLineNumber(), 0);
        _component = (Component) object;
    }

    @Override
    public Object returnObject() throws SAXException {
        if (_component == null)
            throw new SAXException("Border constraint is missing component");
        return new ConstrainedComponent(_component, _constraint);
    }

    private String _constraint;

    private Component _component;
}
