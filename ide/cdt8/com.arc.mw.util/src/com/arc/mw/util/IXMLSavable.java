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
package com.arc.mw.util;

import org.dom4j.DocumentException;
import org.dom4j.Element;

/**
 * Interface for saving and restoring the state of an object
 * via an XML element.
 * @author David Pickens
 */
public interface IXMLSavable {
    
    /**
     * Record the state of this object into an XML 
     * element, by setting its name, attributes, and
     * adding appropriate sub-elements.
     * @param element an uninitialized element to be assigned a name,
     * attributes, and sub-elements.

     */
    public void saveState(Element element);
    
    /**
     * Given an XML element that was saved by {@link #saveState(Element)},
     * restore this object to that state.
     * @param element previously saved element to be restored to.
     * @exception DocumentException if element's contents is somehow unexpected.
     */
    public void restoreState(Element element) throws DocumentException;

}
