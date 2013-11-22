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

import com.arc.widgets.IButton;
import com.arc.xml.Element;
import com.metaware.guihili.Gui;

/**
 * Construct a radio button; acts like a checkbox button.
 */
public class RadioButtonBuilder extends CheckBoxBuilder {
    public RadioButtonBuilder(Gui gui) {
        super(gui);
    }

    @Override
    public IButton createButton(Element element) {
        return _gui.getComponentFactory().makeRadioButton(_gui.getParent());
    }
}
