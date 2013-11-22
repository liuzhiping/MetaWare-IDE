/*
 * XMLWriter
 * $Revision$
 * $Date$
 *
 * CONFIDENTIAL AND PROPRIETARY INFORMATION.
 * Copyright 2008 ARC International (Unpublished).
 * All Rights Reserved.
 * This document, material and/or software contains confidential and
 * proprietary information of ARC International and is protected by copyright,
 * trade secret and other state, federal, and international laws, and may be
 * embodied in patents issued or pending. Its receipt or possession does not
 * convey any rights to use, reproduce, disclose its contents, or to
 * manufacture, or sell anything it may describe.  Reverse engineering is
 * prohibited, and reproduction, disclosure or use without specific written
 * authorization of ARC International is strictly forbidden.  ARC and the ARC
 * logotype are trademarks of ARC International.
 */
package com.arc.tests.database;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;


public class XMLWriter {

    /**
     * Class for writing an XML document to a file. (If there is one in the Java API,
     * I couldn't find it!).
     * @param doc
     * @param output
     * @throws TransformerException
     * @throws IOException
     */
    public static void serializeDocument(Document doc, OutputStream output) throws TransformerException, IOException {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
        DOMSource source = new DOMSource(doc);
        StreamResult outputTarget = new StreamResult(output);
        transformer.transform(source, outputTarget);          
    }

}
