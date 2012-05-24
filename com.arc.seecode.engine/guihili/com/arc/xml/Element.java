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
package com.arc.xml;

import org.dom4j.QName;
import org.dom4j.tree.DefaultElement;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

/**
 * Our own implementation of Element with additional properties.
 */
public class Element extends DefaultElement {
    Element(QName qname) {
        super(qname);
    }

    Element(QName qname, String source, int line) {
        super(qname);
        mSource = source;
        mLine = line;
    }

    @Override
    public Object getData() {
        return mData;
    }

    @Override
    public void setData(Object object) {
        mData = object;
    }

    @Override
    public void setName(String name) {
        setQName(new QName(name));
    }

    /**
     * Use DOM name.
     */
    public String getAttribute(String name) {
        String v = this.attributeValue(name);
        if (v == null)
            return "";
        return v;
    }

    /**
     * Use DOM name.
     */
    public String getTagName() {
        return this.getName();
    }

    public String getSource() {
        return mSource;
    }

    public int getLineNumber() {
        return mLine;
    }

    public Locator getLocator() {
        LocatorImpl l = new LocatorImpl();
        l.setSystemId(mSource);
        l.setLineNumber(mLine);
        return l;
    }

    private String mSource;

    private int mLine;

    private Object mData;
}
