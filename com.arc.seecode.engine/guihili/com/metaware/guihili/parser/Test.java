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
package com.metaware.guihili.parser;

import java.io.File;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.XMLReader;


public class Test {
    public static void main(String args[]) throws Exception{
	XMLReader reader = XMLReaderFactory.makeReader();
	SAXReader saxReader = new SAXReader(reader);
	Document doc = saxReader.read(new File(args[0]));
	XMLWriter writer = new XMLWriter(System.out, OutputFormat.createPrettyPrint());
	writer.write(doc);
	writer.close();
	}
    }
