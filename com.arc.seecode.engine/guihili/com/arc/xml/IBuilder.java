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
 * Classes that implement this interface are used to construct
 * objects that correspond to XML nodes.
 * <P>
 * The implementing class {@link AbstractBuilder} does a
 * good share of the work.
 *
 * @author J. David Pickens
 * @version 5/8/2002
 */

public interface IBuilder {
    /**
     * Build an object from the given element and its children.
     * This is called prior to having processed any of the child elements.
     *
     * @param element the XML element from which we're building an object.
     * @param binding the corresponding binding descriptor.
     * @param parentBuilder the builder of the node above us, in
     * case state information needs to be extracted.
     * @exception SAXException when something wrong occurs.
     */
    public Object build(Element element, IBinding binding, IBuilder parentBuilder)
	throws SAXException;

    }
