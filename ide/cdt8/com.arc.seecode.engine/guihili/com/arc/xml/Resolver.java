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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import org.xml.sax.InputSource;

class Resolver {

    /**
     * Convert {@link File} to an {@link InputSource}.
     */
    static InputSource createInputSource(File f) throws IOException {
	InputSource is = new InputSource(new FileReader(f));
	is.setSystemId(f.getPath());
	return is;
	}
    /**
     * Convert {@link URL} to an {@link InputSource}.
     */
    static InputSource createInputSource(URL url) throws IOException {
	InputSource is = new InputSource(url.openStream());
	is.setSystemId(url.toString());
	return is;
	}
    }
