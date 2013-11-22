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
package com.arc.widgets.internal.swing;

import java.awt.Component;

import com.arc.widgets.IContainer;

class GenericComponent extends SwingComponent {

    GenericComponent(IContainer parent, Component c, IComponentMapper map) {
        super(parent, map);
        mTheComponent = c;
    }

    @Override
    protected Component instantiate() {
        return mTheComponent;
    }

    private Component mTheComponent;
}
