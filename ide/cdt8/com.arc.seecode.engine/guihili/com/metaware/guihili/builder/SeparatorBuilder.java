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

import com.metaware.guihili.Gui;

/**
 * Construct a separator for a tool bar or menu.
 */
public class SeparatorBuilder extends Builder {
    public SeparatorBuilder(Gui gui) {
        super(gui);
    }

    @Override
    public Object returnObject() {
        return this;
    }
}
