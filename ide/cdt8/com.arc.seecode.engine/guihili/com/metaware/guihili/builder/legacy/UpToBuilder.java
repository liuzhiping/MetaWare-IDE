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

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.arc.mw.util.Cast;
import com.arc.xml.Element;
import com.arc.xml.IBinding;
import com.arc.xml.IBuilder;
import com.metaware.guihili.Gui;
import com.metaware.guihili.IEnvironment;
import com.metaware.guihili.builder.Builder;
import com.metaware.guihili.builder.Environment;

/**
 * The Guihili "upto".
 * 
 * <pre>
 * 
 *  &lt;upto var=&quot;name&quot; lo=&quot;lo&quot; hi=&quot;hi&quot;&gt;
 *       &lt;component...&gt;
 *  &lt;upto&gt;
 *  
 * </pre>
 * 
 * We implement this by creating a child for each iteration.
 * 
 */
public class UpToBuilder extends Builder {
    public UpToBuilder(Gui gui) {
        super(gui);
    }

    public void setVar(String name) {
        mVar = name;
    }

    public void setLo(int lo) {
        mLo = lo;
    }

    public void setHi(int hi) {
        mHi = hi;
    }

    /**
     * Expand the
     * 
     * @param var
     *            the name of the index variable
     * @param value
     *            the current value to be assigned to index variable
     * @param upto
     *            the "upto" node from which we get the body.
     * @param binding
     *            the binding of the "upto" node.
     * @param list
     *            list of components to append do.
     */
    @SuppressWarnings("unchecked")
    private void doIteration(String var, int value, Element upto,
            IBinding binding, List<Object> list, IBuilder parent)
            throws SAXException {
        _gui.getEnvironment().putSymbolValue(var, new Integer(value));
        List<Element> kids = Cast.toType(upto.elements());
        for (Element e : kids) {
            Object o = doChild(e, binding);
            if (o != null) {
                if (o instanceof List)
                    list.addAll((List<Object>)o);
                else
                    list.add(o);
            }
        }
    }
    

    @Override
    public Object build(Element element, IBinding binding, IBuilder parent)
            throws SAXException {
        startNewInstance(element);
        doAttributes(element, binding);
        if (mHi - mLo > 500)
            throw new SAXParseException("Range on \"upto\" node too large ("
                    + mLo + ".." + mHi + ")", element.getLocator());
        IEnvironment saveEnv = _gui.getEnvironment();
        try {
            List<Object> list = new ArrayList<Object>();
            for (int i = mLo; i <= mHi; i++) {
                // Each iteration needs its own environment in case
                // there are calls being done. Otherwise the value
                // of the index variable will appear to always be
                // equal to lsat value.
                _gui.setEnvironment(Environment.create(saveEnv));
                doIteration(mVar, i, element, binding, list, parent);
            }
            if (list.size() == 0)
                return null;
            if (list.size() == 1)
                return list.get(0);
            return list;
        } finally {
            _gui.setEnvironment(saveEnv);
            cleanup();
        }
    }

    @Override
    protected Object returnObject() {
        return null;
    } // not called.

    private String mVar;

    private int mLo;

    private int mHi;
}
