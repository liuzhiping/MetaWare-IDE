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
package org.eclipse.cdt.debug.core.sourcelookup;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * 
 * This permits source files to be accessed that are outside the workspace but relative to the 
 * project ("../../source/foo.c").
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class RelativePathSourceContainer extends AbsolutePathSourceContainer {
    
//    /**
//     * Unique identifier for the absolute source container type
//     * (value <code>org.eclipse.cdt.debug.core.containerType.absolutePath</code>).
//     */
//    @SuppressWarnings("hiding")
//    public static final String TYPE_ID = CDebugCorePlugin.getUniqueIdentifier() + ".containerType.relativePath";     //$NON-NLS-1$


    private IPath fWorkingDir;

    public RelativePathSourceContainer(IPath workingDir) {
        fWorkingDir = workingDir;
    }

    @Override
    public Object[] findSourceElements (String name) throws CoreException {
        if ( name != null ) {
            File file = new File( name );
            if ( !file.isAbsolute() ) {
                file = new File(fWorkingDir.append(file.getPath()).toOSString());
                return super.findSourceElementByFile(file);
            }
        }
        return new Object[0];
    }

    @Override
    public String getName () {
        return "Relative File Path outside of Workspace";
    }
    
//    @Override
//    public ISourceContainerType getType() {
//        return getSourceContainerType( TYPE_ID );
//    }
//
//    @Override
//    public int hashCode() {
//        return TYPE_ID.hashCode();
//    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RelativePathSourceContainer))
            return false;
        return true;
    }

}
