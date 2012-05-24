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

import com.arc.cdt.importer.internal.core.CodewrightProjectSpaceFinder;
import com.arc.cdt.importer.internal.core.ProjectCreator;


/**
 * A factory for creating instances of API interfaces.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class Factory {
    private static ICodewrightProjectSpaceFinder sFinder = null;
    
    /**
     * Return the project space finder.
     * @return the project space finder.
     */
    public static ICodewrightProjectSpaceFinder getProjectSpaceFinder(){
        if (sFinder == null){
            sFinder = new CodewrightProjectSpaceFinder();
        }
        return sFinder;       
    }
    
    /**
     * Create a project creator and return it.
     * The result may have internal state and should not be reused for
     * other project imports.
     * @return the new project creator.
     */
    public static IProjectCreator createProjectCreator(){
        return new ProjectCreator();
    }

}
