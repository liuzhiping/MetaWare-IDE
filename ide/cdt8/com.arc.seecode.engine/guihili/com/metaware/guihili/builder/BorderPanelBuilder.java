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
import java.awt.Container;

import com.metaware.guihili.Gui;

/**
 * Class for constructing a panel with a border layout. The child nodes are
 * border constraints ("north", "south", "center", etc.)
 * 
 */
public class BorderPanelBuilder extends LayoutContainerBuilder {

    public BorderPanelBuilder(Gui gui) {
        super(gui);
    }

    @Override
    protected void setLayout(Container container) {
        container.setLayout(new BorderLayout());
    }
}
