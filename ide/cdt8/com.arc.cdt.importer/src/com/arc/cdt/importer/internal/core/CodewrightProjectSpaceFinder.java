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
package com.arc.cdt.importer.internal.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.arc.cdt.importer.core.ICodewrightProjectSpace;
import com.arc.cdt.importer.core.ICodewrightProjectSpaceFinder;
import com.arc.cdt.importer.core.PSPException;


/**
 * The implementation of the CodeWright project space finder.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class CodewrightProjectSpaceFinder implements ICodewrightProjectSpaceFinder {

    
    public File[] findProjectSpaces (File dir) {
        File[] files = dir.listFiles(new FileFilter(){

            public boolean accept (File pathname) {
                return pathname.getName().toLowerCase().endsWith(".psp");
            }});
        return files;
    }

    
    public ICodewrightProjectSpace extractProjectSpace (File pspFile, IProgressMonitor monitor) throws IOException, PSPException {
        FileReader reader = new FileReader(pspFile);
        List<File> fileList = new ArrayList<File>();
        BufferedReader input = new BufferedReader(reader);
        try {
        
            for (String line = input.readLine(); line != null; line = input.readLine()) {
                if (line.startsWith("[Project.")) {
                    if (line.endsWith("]")) line = line.substring(0,line.length()-1);
                    String projectFile = line.substring(9);
                    if (projectFile.startsWith("\"") && projectFile.endsWith("\"")){
                        projectFile = projectFile.substring(1,projectFile.length()-1);
                    }
                    File f = null;
                    if (new File(projectFile).isAbsolute()) {
                        f = new File(projectFile);
                    }
                    else {
                        f = new File(pspFile.getParentFile(), projectFile);
                    }
                    if (!f.exists()) {
                        throw new PSPException("Project file \""
                                + projectFile
                                + "\"\n"
                                + "does not exist in project space \""
                                + pspFile
                                + "\"");
                    }
                    // Make sure the "foo.elx" file exists.
                    File elx = CodewrightProject.computeElxFileFrom(f);
                    if (!elx.exists()) {
                        throw new PSPException("Aux file \""
                                + elx
                                + "\"\n"
                                + "does not exist for project \""
                                + projectFile
                                + "\"\n"
                                + "in project space \""
                                + pspFile
                                + "\".");
                    }
                    fileList.add(f.getCanonicalFile());
                }
            }
        }
        finally {
            input.close();
        }
        if (fileList.size() == 0) {
            throw new PSPException("Project space file \"" + pspFile + "\" has no projects");
        }
        return new CodewrightProjectSpace(pspFile, fileList.toArray(new File[fileList.size()]),monitor);
    }

}
