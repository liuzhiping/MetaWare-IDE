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

import com.arc.xml.Element;
import com.metaware.guihili.Gui;

/**
 * Construct a File Filter suffix that goes under a filter node.
 */
public class FileFilterSuffixBuilder extends Builder {
    public FileFilterSuffixBuilder(Gui gui) {
        super(gui);
    }

    @Override
    public void startNewInstance(Element element) {
    }

    public void setText(String text) {
        _text = text;
    }

    @Override
    public Object returnObject() {
        return _text;
    }

    private String _text;
}
