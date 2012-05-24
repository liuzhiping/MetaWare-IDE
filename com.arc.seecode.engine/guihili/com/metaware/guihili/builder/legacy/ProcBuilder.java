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

import com.arc.xml.Element;
import com.arc.xml.IBinding;
import com.arc.xml.IBuilder;
import com.metaware.guihili.Gui;
import com.metaware.guihili.IEnvironment;
import com.metaware.guihili.ProcBody;
import com.metaware.guihili.builder.Builder;

/**
 * The Guihili "proc".
 * 
 * <pre>
 * 
 *  &lt;proc name=&quot;name&quot; body=&quot;...&quot; /&gt;
 *  
 * </pre>
 * 
 * We merely save the the body by calling
 * {@link IEnvironment#putProcedure(String,ProcBody) Gui.getEnvironment().putProcedure(name,body)}.
 */
public class ProcBuilder extends Builder {
    public ProcBuilder(Gui gui) {
        super(gui);
    }

    public void setName(String name) {
        mName = name;
    }

    public void setBody(Object body) {
        mBody = body;
    }

    @SuppressWarnings("unchecked")
	@Override
    public Object build(Element element, IBinding binding, IBuilder parent)
            throws SAXException {
        doAttributes(element, binding);
        if (mBody != null && mName != null) {
            if (!(mBody instanceof List))
                error(element, "body attribute must be a list!");
            _gui.getEnvironment().putProcedure(mName,
                    new ProcBody((List<Object>) mBody, _gui.getEnvironment()));
        }
        cleanup();
        return null;
    }

    @Override
    public Object returnObject() throws SAXException {
        return null;
    }

    private String mName;

    private Object mBody;
}
