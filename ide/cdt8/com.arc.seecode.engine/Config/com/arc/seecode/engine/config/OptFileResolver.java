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
package com.arc.seecode.engine.config;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.InputSource;

import com.metaware.guihili.IFileResolver;

/**
 * A file resolver to the Guihili options files.
 * 
 * @author David Pickens
 */
public class OptFileResolver implements IFileResolver {
	private static String SUBDIRS[] = {"mdb/opt/swing", "sc/opt/swing", "mdb/opt", "sc/opt" };
	private String _scdir;
	public OptFileResolver(String scdir){
		_scdir = scdir;
	}
    /*
     * (non-Javadoc)
     * 
     * @see com.metaware.guihili.IFileResolver#openFile(java.lang.String)
     */
    @Override
    public InputSource openFile(String name) {
		//NOTE: formerly, we made the wrong dicision in duplicating the .opt files in the IDE from
		// the SeeCode distribution. When a SeeCode .opt was corrected, the change didn't show up
		// in the IDE until the next release!
		// So, we now read the .opt files from the SeeCode distribution.

		if (!name.endsWith(".xml") && !name.endsWith(".opt"))
			name += ".opt";
		InputStream input = null;

		// We now read the guihili files from the SeeCode distribution.
        for (String s : SUBDIRS) {
            try {
                input = new FileInputStream(_scdir + "/" + s + "/" + name);
                break;
            }
            catch (FileNotFoundException x) {
                // try next one.
            }
        }
		
		// One of the .opt files in the SeeCode distribution had stuff that doesn't apply to the IDE: 
		// program1.opt.
    	// Later versions of these were corrected if "if" conditionals.
		// To distingish the corrected one, the header must
    	// have a comment with "IDE" in it. If we don't find this, then we read from the version we
		// have cached.
		if (input != null && name.equals("program1.opt")) {
			StringBuilder buf = new StringBuilder(50);
			boolean reject = false;
			try {
				int b = input.read();
				while (b != -1 && b != '\n'){
					buf.append((char)b);
					b = input.read();
				}
				if (buf.toString().indexOf("IDE") < 0) {
					reject = true;
				}
			} catch (IOException e) {
				reject = true; // shouldn't happen
			}
			if (reject) {
			    input = getClass().getResourceAsStream("guihili/" + name);
			}
		}
		if (input == null) {
			// If somehow missing from SeeCode, see if some legacy version is in the IDE
			input = getClass().getResourceAsStream("guihili/" + name);
		}
		if (input != null) {
			input = new BufferedInputStream(input, 4096);
			InputSource result = new InputSource(input);
			result.setSystemId(name);
			return result;
		}
		return null;
	}
}
