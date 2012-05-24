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

import org.xml.sax.SAXException;

import com.arc.mw.util.Caller;
import com.metaware.guihili.Gui;

/**
 * Do a Java call.
 */
public class CallBuilder extends Builder {
    public CallBuilder(Gui gui) {
        super(gui);
    }

    public void setClass(String className) {
        _className = className;
    }

    public void setMethod(String methodName) {
        _methodName = methodName;
    }

    @Override
    public Object returnObject() throws SAXException {
        try {
            Class<?> klass = Class.forName(_className);
            return Caller.invokeStatic(klass, _methodName,
                    new Object[] { _gui });
        } catch (Exception x) {
            throw new SAXException(x);
        }
    }

    private String _className;

    private String _methodName;
}
