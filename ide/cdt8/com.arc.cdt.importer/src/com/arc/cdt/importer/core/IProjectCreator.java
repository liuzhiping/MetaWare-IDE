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

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;


/**
 * Creates a C or C++ project in such a way as to be conveniently initialized
 * from an old Codewright project.
 * <P>
 * An instance of this interface can be retrieved from
 * {@link Factory#createProjectCreator() Factory.createProjectCreator()}.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface IProjectCreator {
    /**
     * Create a C or C++ project.
     * @param locationDir the location of the directory where the new project
     * is, or <code>null</code> if a default location is to be used. The referenced
     * directory may, or may not, exist.
     * @param cpp if true, then this is a C++ project, otherwise a C project.
     * @param name the name of the project. If no location directory is specified,
     * the default one is derived from this name.
     * @param type the project type (e.g. ARCompact executable).
     * @param monitor a performance monitor or <code>null></code>.
     * @return newly created C or C++ project.
     * @throws CoreException if the project could not be made for some reason.
     * @throws BuildException 
     */
    ICProject createProject(File locationDir, boolean cpp, String name,  IProjectType type, IProgressMonitor monitor) throws CoreException, BuildException;
    
    /**
     * Import a source file into the project, creating intermediate folders
     * as required.
     * @param project the project being imported into.
     * @param file the file being imported (copied) into the project.
     * @param relativeTo the directory of the file that is considered its "root".
     */
    void importSourceFile(IProject project, File file, File relativeTo, IProgressMonitor monitor) throws CoreException;
    
    /**
     * Create a link to asource file, creating intermediate folders
     * as required.
     * @param project the project being imported into.
     * @param file the file being linked to.
     * @param relativeTo the directory of the file that is considered its "root".
     */
    void linkSourceFile(IProject project, File file, File relativeTo, IProgressMonitor monitor) throws CoreException;
    
    /**
     * Indicate that the given project in an existing directory is to reference
     * an existing source file there.
     * @param project
     * @param file
     * @param relativeTo
     */
    void useInplaceSourceFile(IProject project, File file, File relativeTo);
    
    /**
     * Create a configuration
     * @param project the C/C++ project being affected.
     * @param name the name of the configuration.
     * @param type the project type.
     * @param oldProj the old MetaDeveloper project from which the
     * new configuration is to be derived.
     * @param original the configuration from which the new one is to be based.
     * @return newly created configuration.
     * @throws CoreException 
     * @throws BuildException 
     */
    IConfiguration createConfiguration(ICProject project, String name, IProjectType type, ICodewrightProject oldProj, IConfiguration original) throws CoreException, BuildException;
    
    
}
