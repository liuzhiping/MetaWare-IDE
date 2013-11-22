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
package com.metaware.guihili;


import java.io.Reader;
import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import com.metaware.guihili.parser.XMLReaderFactory;

/**
 * This class is used to parse a guihili file into 
 * document.
 * It isn't actually used by the {@link Gui} class, but is rather used
 * to parse a stripped down guihili that the debugger gui receives from
 * the engine.
 * @author David Pickens, May 20, 2002
 */
public class GuihiliParser {

	/**
	 * Read XML from path.
	 * If the name ends in ".opt", we assume it to be guihili, otherwise XML.
	 */
	public static Document parseString(String specification)
			throws DocumentException {
		Reader r = new StringReader(specification);
		InputSource input = new InputSource(r);
		input.setSystemId("<string>.opt");
		return parse(input);
	}

	public static Document parse(InputSource input) throws DocumentException {
		SAXReader reader = new SAXReader(XMLReaderFactory.makeReader());
		return reader.read(input);
	}
}

