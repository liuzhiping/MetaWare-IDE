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
import org.xml.sax.SAXParseException;

import com.arc.mw.util.ColorTable;
import com.arc.widgets.IComponent;
import com.arc.xml.Element;
import com.metaware.guihili.Gui;

/**
 * This class constructs an instance of TabComponent for a
 * TabbedPane.
 */
public class TabBuilder extends Builder {
    public TabBuilder(Gui gui) {
        super(gui);
    }
    
    @Override
    public void startNewInstance(Element element) throws SAXException {
        _tab = new TabComponent();
    }

    @Override
    public void addChild(Object object, Element element) throws SAXException {
        if (_tab.getComponent() != null)
            throw new SAXParseException(
                "A tab may have only one component",
                null,
                element.getSource(),
                element.getLineNumber(),
                0);
        _tab.setComponent((IComponent) object);
    }

    public void setTooltip(String s) {
        _tab.setTooltip(s);
    }
    public void setTitle(String s) {
        _tab.setTitle(s);
    }
    public void setIcon(String s) {
        _tab.setIcon(s);
    }
    public void setForeground(String s) {
        _tab.setForeground(ColorTable.decode(s));
    }
    public void setBackground(String s) {
        _tab.setBackground(ColorTable.decode(s));
    }

    @Override
    public Object returnObject() throws SAXException {
        return _tab;
    }
    private TabComponent _tab;
}
