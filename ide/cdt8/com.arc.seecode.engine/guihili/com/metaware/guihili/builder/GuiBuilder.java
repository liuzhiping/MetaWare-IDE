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
import com.arc.xml.Element;
import com.metaware.guihili.Gui;

/**
 * This class processes a "gui" node, which doesn't do anything interesting.
 */
public class GuiBuilder extends Builder {
    public GuiBuilder(Gui gui) {
        super(gui);
    }

    @Override
    public void addChild(Object obj, Element element) throws SAXException {
        if (obj instanceof IComponent) {
            //
            // If title given, then it will be the title of the enclosing
            // dialog.
            // Set the "title" property.
            //
            String title = element.attributeValue("title");
            if (title != null) {
                // System.out.println("title=" + title);
                try {
                    _gui.setProperty("title", _gui.getEvaluator()
                            .evaluateStringExpression(title,
                                    _gui.getEnvironment()));
                    // System.out.println("evaluated(title)=" +
                    // _gui.getProperty("title"));
                } catch (Exception x) {
                }
            }
            //
            // Make sure there is a "name" attribute. If one isn't given,
            // default to
            // "main".
            //
            String name = element.getAttribute("name");
            _gui.trace("add root " + obj.getClass().getName() + " for "
                    + element.getName() + "; name=" + name);
            if ((name == null || name.length() == 0)
                    && _gui.getParent() == null) {
                // Guihili doesn't name the top component.
                // So we do it ourselves if "main" hasn't be userped.
                if (_gui.getComponent("main") == null) {
                    name = "main";
                    ((IComponent) obj).setName(name);
                } else
                // May have been set programattically
                if (_gui.getComponent("main") != obj) {
                    error(element,
                            "Top level component must have name attribute: "
                                    + element.getName()
                                    + "; main is "
                                    + _gui.getComponent("main").getClass()
                                            .getName());
                    name = null;
                }
            }
            if (name != null)
                _gui.setComponent(name, (IComponent) obj);
        }
    }

    @Override
    public Object returnObject() {
        return _gui;
    }
}
