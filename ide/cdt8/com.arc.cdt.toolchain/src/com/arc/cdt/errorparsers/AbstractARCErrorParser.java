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


import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;


public abstract class AbstractARCErrorParser implements IErrorParser{

    public AbstractARCErrorParser() {
        super();
    }

    protected void generateMarker (
        ErrorParserManager epm,
        String fileName,
        int lineNum,
        String desc,
        int severity,
        String varName) {
        IPath externalPath = null;

        IResource file = null;
        if (fileName != null) {
            file = epm.findFileName(fileName);
            
            if (file == null) {
                // If the file is not found in the workspace we attach the problem to the project
                // and add the external path to the file.
                desc = fileName + " " + desc; //$NON-NLS-1$
                file = epm.getProject();
                externalPath = new Path(fileName);
            }
        }

        epm.generateExternalMarker(file, lineNum, desc, severity, varName, externalPath);

    }

}
