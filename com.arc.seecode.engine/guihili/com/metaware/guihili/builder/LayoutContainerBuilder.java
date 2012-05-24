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

import java.awt.Container;

import org.xml.sax.SAXException;

import com.arc.xml.Element;
import com.metaware.guihili.Gui;

/**
 * Class for constructing a panel with a layout that require constraints
 * 
 */
public abstract class LayoutContainerBuilder extends ContainerBuilder {

    public LayoutContainerBuilder(Gui gui) {
        super(gui);
    }

    @Override
    public void startNewInstance(Element element) throws SAXException {
        super.startNewInstance(element);
        Container c = (Container) getComponent();
        setLayout(c);
    }

    protected abstract void setLayout(Container container);

    protected void addChild(Container panel, Object child, Element element) {
        ConstrainedComponent constraint = (ConstrainedComponent) child;
        panel.add(constraint.getComponent(), constraint.getConstraint());
    }
}
