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
package org.eclipse.cdt.managedbuilder.makegen;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;

/**
 * The MakefileAugmenter extension implements this interface for inserting
 * special stuff into the makefile.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface IMakefileAugmenter {
    /**
     * Generate macro definitions to be written near the top of the generate makefile.
     * @param config the application configuration in case any information from it is needed.
     * @return the string to write into the make file, or <code>null</code>.
     */
    String generateMacroDefinitions(IConfiguration config);
    
    /**
     * Return the command to delete file (e.g,. "rm -rf"), or <code>null</code> if
     * the command defined in the configuration is to be used.
     * @param config
     * @return the command to delete file (e.g,. "rm -rf"), or <code>null</code> if
     * the command defined in the configuration is to be used.
     */
    String getCleanCommand(IConfiguration config);
    
    /**
     * Do whatever is necessary to make the given path presentable to the shell. If it has spaces,
     * either escape them or quote the entire thing. Returns null if the caller is to apply
     * a default transformation.
     * @param path path to canonicalize.
     * @return a canonical representation of the path, or <code>null</code> if the caller is to
     * apply its own default.
     */
    String canonicalizePath(String path);
}
