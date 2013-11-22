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

import org.xml.sax.SAXException;

/**
 * A dummy builder that does nothing. Use when no object is associated with a
 * binding.
 */
public class DummyBuilder extends AbstractBuilder {

    /**
     * Return the object that was created.
     */
    @Override
    public Object returnObject() throws SAXException {
        return null;
    }

}
