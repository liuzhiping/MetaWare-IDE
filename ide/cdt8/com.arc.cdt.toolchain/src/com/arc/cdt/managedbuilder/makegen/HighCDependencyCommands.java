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
package com.arc.cdt.managedbuilder.makegen;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyCommands;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Called to manage MetaWare C and C++ compiler dependencies, and assembler.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
class HighCDependencyCommands implements IManagedDependencyCommands {
    

    private static final String DEPEND_DIR = "depend";

    private IPath source;
    private IBuildObject buildContext;
    private ITool tool;
    private IPath topBuildDirectory;

    /**
     * Constructor
     * 
     * @param source  The source file for which dependencies should be calculated
     *    The IPath can be either relative to the project directory, or absolute in the file system.
     * @param buildContext  The IConfiguration or IResourceConfiguration that
     *   contains the context in which the source file will be built
     * @param tool  The tool associated with the source file
     * @param topBuildDirectory  The top build directory of the configuration.  This is
     *   the working directory for the tool.  This IPath is relative to the project directory.
     */
    public HighCDependencyCommands(
                IPath source, IBuildObject buildContext, ITool tool, IPath topBuildDirectory) {
        this.source = source;
        this.buildContext = buildContext;
        this.tool = tool;
        this.topBuildDirectory = topBuildDirectory;
        
    }
    
    private IPath getDependencyDir(){
        //      The source file is project relative and the dependency file is top build directory relative
        //  Remove the source extension and add the dependency extension
        IPath depDirPath = source.removeLastSegments(1);
        //  Remember that the source folder hierarchy and the build output folder hierarchy are the same
        //  but if this is a generated resource, then it may already be under the top build directory
        if (!depDirPath.isAbsolute()) {
            if (topBuildDirectory.isPrefixOf(depDirPath)) {
                depDirPath = depDirPath.removeFirstSegments(topBuildDirectory.segmentCount());               
            }
            depDirPath = new Path(DEPEND_DIR).append(depDirPath);
        }
        return depDirPath;
        
    }
    
    public IPath[] getDependencyFiles() {
        IPath depFilePath = getDependencyDir();
        //  The source file is project relative and the dependency file is top build directory relative
        //  Remove the source extension and add the dependency extension
        String name = source.removeFileExtension().addFileExtension("u").lastSegment();
        IPath[] paths = new IPath[1];
        paths[0] = depFilePath.append(name);
        return paths;
    }

    public String[] getPreToolDependencyCommands () {
        return null;
    }
    
    private boolean isDefaultExtension(String ext){
        if (ext == null) return false;
        // Uppercase C denotes C++; hopefully this isn't an issue on Windows
        if (ext.equals("C"))
            return false;
        ext = ext.toLowerCase();
        if (tool.getBaseId().toLowerCase().indexOf("compiler") >= 0){
            return ext.equals("c") || ext.equals("cc") || ext.equals("cpp") || ext.equals("i") ||
                ext.equals("ii");
        }
        else if (tool.getBaseId().toLowerCase().indexOf("assembler") >= 0 ||
                 tool.getBaseId().toLowerCase().indexOf("asm") >= 0){
            return ext.equals("s") || ext.equals("asm");
        }
        return false;
    }
    
    private boolean hasDefaultExtension(IPath s){
        return isDefaultExtension(s.getFileExtension());
    }
    
    private boolean isCompiler(){
        return tool.getBaseId().toLowerCase().indexOf("compiler") >= 0;
    }
    
    private boolean isCCompiler(){
        if (isCompiler()){
            IInputType input = tool.getInputType(source.getFileExtension());
            return input.getSourceContentType().getId().endsWith("cSource");
        }
        return false;
    }

    private boolean isCPPCompiler(){
        if (isCompiler()){
            IInputType input = tool.getInputType(source.getFileExtension());
            return input.getSourceContentType().getId().endsWith("cxxSource");
        }
        return false;
    }
    
    private boolean isAssembler(){
        IInputType input = tool.getInputType(source.getFileExtension());
        return input != null && input.getSourceContentType().getId().endsWith("asmSource");
    }

    public String[] getDependencyCommandOptions () {
        List<String> options = new ArrayList<String>();
        if (!hasDefaultExtension(source)){
            if (isCCompiler()){
                options.add("-Hcext=" + source.getFileExtension());
            }
            else if (isCPPCompiler()){
                options.add("-Hcppext=" + source.getFileExtension());
            }
            else if (isAssembler()){
                options.add("-Hasext=" + source.getFileExtension());
            }
        }
        
        if (isCompiler()) {
            // -Humake
            options.add("-Humake"); //$NON-NLS-1$
            // -Hdepend="depend/..."
            options.add("-Hdepend=\"" + getDependencyDir() + "\""); //$NON-NLS-1$      
        }
        return options.toArray(new String[options.size()]);
    }

    public String[] getPostToolDependencyCommands () {
        // @todo Auto-generated method stub
        return null;
    }

    public boolean areCommandsGeneric () {
        return true;
    }

    public IPath getSource () {
        return source;
    }

    public IBuildObject getBuildContext () {
        return buildContext;
    }

    public ITool getTool () {
        return tool;
    }

    public IPath getTopBuildDirectory () {
        return topBuildDirectory;
    }

}
