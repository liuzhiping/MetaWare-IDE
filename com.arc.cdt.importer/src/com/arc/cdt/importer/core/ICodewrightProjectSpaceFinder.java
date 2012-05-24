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

import org.eclipse.core.runtime.IProgressMonitor;


/**
 * Given a directory, this object is responsible for
 * finding all instances of an old MetaDeveloper 1
 * project-space file (.psp).
 * <P>
 * A lone instance of this interface can be accessed by 
 * {@link Factory#getProjectSpaceFinder() Factory.getProjectSpaceFinder()}.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface ICodewrightProjectSpaceFinder {
    
    /**
     * Given a directory, locate all ".psp" files
     * @param dir the directory to be searched.
     * @return an array of .psp files, or <code>null</code> if
     * there is none.
     */
    File[] findProjectSpaces(File dir);
    
    /**
     * Given a .psp file, read in its contents
     * an create a descriptor of its contents.
     * @param pspFile the PSP file.
     * @param monitor a progress monitor, if being called from the GUI.
     * @return the corresponding project space descriptor.
     * @exception PSPException if something is wrong with the file.
     * @exception IOException if an error occurred reading the file.
     */
    ICodewrightProjectSpace extractProjectSpace(File pspFile, IProgressMonitor monitor) throws IOException, PSPException;
}
