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


import org.dom4j.Attribute;
import org.xml.sax.SAXException;

import com.arc.xml.Element;
import com.arc.xml.IBinding;
import com.arc.xml.IBuilder;
import com.metaware.guihili.Gui;
import com.metaware.guihili.IEnvironment;
import com.metaware.guihili.builder.Builder;


/**
 * A node for setting environment symbols.
 * 
 * <pre>
 * 
 *  &lt;set name=&quot;name&quot; value=&quot;...&quot; /&gt;
 *  
 * </pre>
 * 
 * We merely save the the body by calling
 * {@link IEnvironment#putSymbolValue(String,Object) Gui.getEnvironment().putSymbolValue(name,value)}.
 */
public class SetBuilder extends Builder {

    public SetBuilder(Gui gui) {
        super(gui);
    }

    @Override
    protected void unknownAttribute (Element e, IBinding b, Attribute a) throws SAXException {
        _gui.getEnvironment().putSymbolValue(a.getName(), a.getValue());
    }

    @Override
    public Object build (Element element, IBinding binding, IBuilder parent) throws SAXException {
        doAttributes(element, binding);
        return null;
    }

    @Override
    public Object returnObject () throws SAXException {
        return null;
    }
}
