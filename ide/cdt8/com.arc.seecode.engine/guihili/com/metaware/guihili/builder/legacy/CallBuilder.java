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


import java.util.ArrayList;
import java.util.List;

import org.dom4j.Attribute;
import org.xml.sax.SAXException;

import com.arc.mw.util.Cast;
import com.arc.xml.Element;
import com.arc.xml.IBinding;
import com.arc.xml.IBuilder;
import com.metaware.guihili.Gui;
import com.metaware.guihili.IEnvironment;
import com.metaware.guihili.builder.Builder;
import com.metaware.guihili.builder.Environment;


/**
 * Processes a "call" node.
 * 
 * <Pre>
 * 
 * &lt;call name="name" [parm1="..." ...] &gt;
 * 
 * </pre>
 */
public class CallBuilder extends Builder {

    public CallBuilder(Gui gui) {
        super(gui);
        mEnv = Environment.create(gui.getEnvironment());
    }

    public void setName (String name) {
        mName = name;
    }

    @Override
    public void unknownAttribute (Element e, IBinding binding, Attribute a) throws SAXException {
        try {
            mEnv.putSymbolValue(a.getName(), _gui.getEvaluator().parseAction(a.getValue()));
        }
        catch (Exception x) {
            error(e, x.getMessage(), x);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object build (Element element, IBinding binding, IBuilder parent) throws SAXException {
        doAttributes(element, binding);
        Element proc = _gui.getGuiProc(mName);
        if (proc == null) {
            error(element, "call to unknown proc: " + mName);
            return null;
        }
        IEnvironment save = _gui.getEnvironment();
        _gui.setEnvironment(mEnv);
        ArrayList<Object> list = new ArrayList<Object>();
        try {
            List<Element> elements = Cast.toType(proc.elements());
            for (Element kid: elements){        
                Object o = doChild(kid, binding.getParent());
                if (o != null) {
                    if (o instanceof List)
                        list.addAll((List<Object>) o);
                    else
                        list.add(o);
                }
            }
        }
        finally {
            _gui.setEnvironment(save);
        }
        cleanup();
        if (list.size() == 0)
            return null;
        if (list.size() == 1)
            return list.get(0);
        return list;
    }

    @Override
    public Object returnObject () throws SAXException {
        return null;
    }

    private String mName;

    private IEnvironment mEnv;
}
