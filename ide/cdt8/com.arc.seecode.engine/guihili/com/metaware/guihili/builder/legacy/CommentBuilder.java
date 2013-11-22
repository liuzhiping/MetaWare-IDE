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
package com.metaware.guihili.builder.legacy;

import org.xml.sax.SAXException;

import com.arc.xml.Element;
import com.arc.xml.IBinding;
import com.arc.xml.IBuilder;
import com.metaware.guihili.Gui;
import com.metaware.guihili.builder.Builder;

/**
 * The Guihili "comment". We simply ignore the contents
 */
public class CommentBuilder extends Builder {
    public CommentBuilder(Gui gui) {
        super(gui);
    }

    @Override
    public Object build(Element element, IBinding binding, IBuilder parenet) {
        return null;
    }

    @Override
    public Object returnObject() throws SAXException {
        return null;
    }

}
