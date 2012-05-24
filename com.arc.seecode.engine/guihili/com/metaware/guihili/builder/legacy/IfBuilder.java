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

import java.util.List;

import org.xml.sax.SAXException;

import com.arc.mw.util.Cast;
import com.arc.xml.Element;
import com.arc.xml.IBinding;
import com.arc.xml.IBuilder;
import com.metaware.guihili.Gui;
import com.metaware.guihili.builder.Builder;

/**
 * The Guihili "if". <if cond="name" <component...> <component...> </if>
 * 
 * 
 */
public class IfBuilder extends Builder {
    public IfBuilder(Gui gui) {
        super(gui);
    }

    public void setCond(boolean v) {
        mCond = v;
    }

    @Override
    public Object build(Element element, IBinding binding, IBuilder parent)
            throws SAXException {
        startNewInstance(element);
        doAttributes(element, binding);
        List<Element> kids = Cast.toType(element.elements());
        if (kids.size() < 1 | kids.size() > 2)
            error(element, "child count of 'if' is wrong");
        Object result = null;
        if (mCond)
            result = doChild( kids.get(0), binding);
        else if (kids.size() >= 2)
            result = doChild( kids.get(1), binding);
        cleanup();
        return result;
    }

    @Override
    public Object returnObject() throws SAXException {
        return null;
    }

    private boolean mCond;
}
