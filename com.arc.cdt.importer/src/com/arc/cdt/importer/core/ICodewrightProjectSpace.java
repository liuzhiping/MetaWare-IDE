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
package com.arc.cdt.importer.core;

import java.io.File;
import java.io.IOException;


/**
 * A Codewright (MetaDeveloper 1) project space description.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface ICodewrightProjectSpace {
    /**
     * Return the CodeWright projects in thie project space.
     * If there is more than 1, then in CDT we must model
     * each as a configuration of a single Eclipse project.
     * @todo davidp needs to add a method comment.
     * @return the Codewright projects within this project
     * space; result will always be non-null.
     * @throws IOException if an error occurred reading a project
     * file (.pjt) or a .elx file.
     * @throws PSPException if the files being read don't appear
     * to be of the proper format.
     */
    ICodewrightProject[] getProjects() throws IOException, PSPException;
    
    /**
     * Return the PSP file from which this project space
     * was derived.
     * @return  the PSP file from which this project space
     * was derived.
     */
    File getPspFile();
    
    /**
     * @return the name of this project space. 
     */
    String getName();
    
    /**
     * Return the apparent root directory for this project space. It is
     * the directory that contains the project's source files. It may, or may
     * not be, the same directory as the .psp file. The .psp file may be
     * in a subdirectory.
     * @return the directory of the source files.
     */
    File getLocation();

}
