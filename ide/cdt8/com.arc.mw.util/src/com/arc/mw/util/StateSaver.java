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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * This class is responsible for saving and restoring the
 * state of the debugger.
 * It creates XML elements and has components fill them in.
 */
public class StateSaver {
    /**
     * Save the state of the root object into the given file.
     * @param path file inwhich to be saved.
     * @param object object whose state is to be saved.
     * @exception IOException error occurred in writing file.
     */
    public static void saveState(String path, IXMLSavable object)
        throws IOException
    {
        FileWriter f = new FileWriter(path);
        BufferedWriter out = new BufferedWriter(f);
        saveState(out,object);
        out.close();
    }
    /**
     * Save the state of the root object into the given file.
     * @param out ascii stream to write to.
     * @param object object whose state is to be saved.
     * @exception IOException error occurred in writing file.
     */
    public static void saveState(Writer out, IXMLSavable object)
        throws IOException {
        DocumentFactory factory = DocumentFactory.getInstance();
        Document document = factory.createDocument();
        Element e = factory.createElement("root");
        object.saveState(e);
        document.setRootElement(e);
        XMLWriter writer = new XMLWriter(out, OutputFormat.createPrettyPrint());
        writer.write(document);       
    }
    
    public static void restoreState(String path, IXMLSavable object) throws DocumentException{
        SAXReader reader = new SAXReader();
        Document doc = reader.read(path);
        object.restoreState(doc.getRootElement());
    }
    

}
