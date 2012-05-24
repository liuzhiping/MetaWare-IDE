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


import org.xml.sax.Locator;


/**
 * An exception that is thrown when we encounter bad XML
 */
public class BadXmlException extends Exception {

    public BadXmlException(Element node, String msg) {
        super("XML error on node " + nodeName(node) + ": " + msg);
    }

    public BadXmlException(String msg) {
        super("XML processing error : " + msg);
    }

    static String nodeName (Element node) {
        Locator loc = node.getLocator();
        if (loc != null)
            return node.getName() + "(@" + loc + ")";
        return node.getName();
    }
}
