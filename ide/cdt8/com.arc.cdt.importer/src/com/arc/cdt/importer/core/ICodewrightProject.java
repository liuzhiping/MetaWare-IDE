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
import java.util.Map;


/**
 * A description of a single Codewright (MetaDeveloper 1)
 * project.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface ICodewrightProject {
    /**
     * 
     * Return the associated project space.
     * @return the associated project space.
     */
    ICodewrightProjectSpace getProjectSpace();
    
    /**
     * Return the source files in this project.
     * @return the source files in this project.
     */
    File[] getSourceFiles();
    
    /**
     * Return the name of this project.
     * (I.e., the .pjt file with the suffix removed).
     * @return the name of this project.
     */
    String getName();
    
    /**
     * Return the associated .pjt file from which this
     * object was generated.
     * @return the associated .pjt file from which this
     * object was generated.
     */
    File getPjtFile();
    
    /**
     * Return the associated .elx file from which this
     * object was generated.
     * @return the associated .elx file from which this
     * object was generated.
     */
    File getElxFile();
    
    /**
     * Return a string that denotes the target CPU. For example
     * "ac", "arm", "vc", "arc", etc.
     * @return a string that denotes the target CPU
     */
    String getTarget();
    
    /**
     * Return the generel compiler options.
     * @return the compiler options.
     */
    String[] getCompilerOptions();
    
    /**
     * Return the list of include directories to be passed to the compiler.
     * @return the list of include directories to be passed to the compiler
     */
    File[] getCompilerIncludes();
    
    /**
     * Return the list of preprecessor defines. Each has the syntax "symbol=value".
     * @return the list of preprocessor defines
     */
    String[] getCompilerDefines();
    /**
     * Return the list of preprecessor undefs. 
     * @return the list of preprocessor undefs.
     */
    String[] getCompilerUndefines();
    
    /**
     * Return the assembler options.
     * @return the assembler options.
     */
    String[] getAssemblerOptions();
    /**
     * Return the list of include directories to be passed to the compiler.
     * @return the list of include directories to be passed to the compiler
     */
    File[] getAssemblerIncludes();
    
    /**
     * Return the list of preprecessor defines. Each has the syntax "symbol=value".
     * @return the list of preprocessor defines
     */
    String[] getAssemblerDefines();
    /**
     * Return the list of preprecessor undefs. 
     * @return the list of preprocessor undefs.
     */
    String[] getAssemblerUndefines();
    
    /**
     * Return the linker options.
     * @return the linker options.
     */
    String[] getLinkerOptions();
    
    /**
     * Return the Vr3 command files. A zero-length array is returned
     * if there are none.
     * @return the Vr3 command files.
     */
    File[] getSvr3CommandFiles();
    
    /**
     * Return the Vr4 command files. A zero-length array is returned
     * if there are none.
     * @return the Vr3 command files.
     */
    File[] getSvr4CommandFiles();
    
    /**
     * Return additional object files and libraries
     */
    File[] getObjectFilesAndLibraries();
    
    /**
     * Return the SeeCode options. The options are Key, Value pairs where
     * the key is the "guihili" property that is to be set.
     * @return the SeeCode options.
     */
    Map<String,String> getSeeCodeOptions();
    
    /**
     * Returns whether or not this is a C++ project.
     * @return whether or not this is a C++ project.
     */
    boolean isCPlusPlus();
    
    /**
     * Get name of output file that this project generates.
     * @return the name of the output fiel that this project generates.
     */
    String getOutputFile();
    
    /**
     * Return whether or not the target is a library; otherwise its assumed
     * to be an executable.
     * @return whether or not the target is a library; otherwise its assumed
     * to be an executable.
     */
    boolean isLibrary();
}
