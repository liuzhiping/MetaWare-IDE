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
package com.arc.cdt.errorparsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IMarkerGenerator;


/**
 * Parses error messages from the assemblers.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class AssemblerErrorParser extends AbstractARCErrorParser {
    private static final Pattern PATTERN = Pattern.compile("^\\[[^\\]]*\\]\\s*\"([^\"]*)\"\\s*,?\\s*[lL]ine\\s*(\\d*)\\s*:\\s*(.*)$");
    // See interface for javadoc.
    public boolean processLine (String line, ErrorParserManager eoParser) {
        Matcher m = PATTERN.matcher(line);
        if (m.matches()){
            // Assembler errors look like this:
            // [asac] "file.s",Line 123: an error message.
            //
            // Warnings look like this:
            // [asac] "file.s",Line 123: (Warning) a warning message.
            // or like this:
            // [asac] "file.s",Line 123: warning(#12) a warning message.
            //
            String fileName = m.group(1);
            String lineNumberString = m.group(2);
            String msg = m.group(3);
            boolean isWarning = false;
            if (msg.toLowerCase().startsWith("(warning)")) {
                isWarning = true;
                msg = msg.substring("(warning)".length());
            }
            else if (msg.toLowerCase().startsWith("warning")) {
                isWarning = true;
            }
            int lineNumber = Integer.parseInt(lineNumberString);
            int severity = isWarning?IMarkerGenerator.SEVERITY_WARNING:IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
            generateMarker(eoParser,fileName,lineNumber,msg,severity,null);
            return true;
        }
        return false;    
    }

}
