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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITargetPlatform;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IPathVariableChangeEvent;
import org.eclipse.core.resources.IPathVariableChangeListener;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.arc.cdt.importer.ImporterPlugin;
import com.arc.cdt.importer.core.ICodewrightProject;
import com.arc.cdt.importer.core.IProjectCreator;
import com.arc.mw.util.StringUtil;


/**
 * Manages the building of C projects from old Codewright project information.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class ProjectCreator implements IProjectCreator {
    private Map<File,IFile>mSourceFileMap = new HashMap<File,IFile>();
    private Map<File,IFolder>mSourceDirMap = new HashMap<File,IFolder>();
    private Map<IPath, String> mPathVarMap = null;
    private String mProjectLocation = null;

  
    @SuppressWarnings("deprecation")
    public ICProject createProject (
            File locationDir,
            boolean cpp,
            String name,
            IProjectType type,
            IProgressMonitor monitor) throws CoreException, BuildException {
        if (name == null)
            throw new IllegalArgumentException("project name is null");
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        else monitor = new SubProgressMonitor(monitor,1);
        monitor.beginTask("Creating project",3+(cpp?1:0));
        ICProject cProject;
        try {
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IProjectDescription desc = workspace.newProjectDescription(name);
            IPath locationPath = null; // default to workspace
            if (locationDir != null) {
                mProjectLocation = locationDir.getAbsolutePath();
                locationPath = new Path(locationDir.getAbsolutePath());
            }
            desc.setLocation(locationPath);
            IProject projectHandle = workspace.getRoot().getProject(name);
            IProject newProject = CCorePlugin.getDefault().createCProject(
                    desc,
                    projectHandle,
                    monitor,
                    ManagedBuilderCorePlugin.MANAGED_MAKE_PROJECT_ID);
            if (mProjectLocation == null){
                mProjectLocation = newProject.getLocation().toString();
            }
            monitor.subTask("Add nature");
            ManagedCProjectNature.addManagedNature(newProject, new SubProgressMonitor(monitor, 1));
            monitor.subTask("Add builder");
            ManagedCProjectNature.addManagedBuilder(newProject, new SubProgressMonitor(monitor, 1));
            if (cpp) {
                // Add C++ Nature to the newly created project.
                monitor.subTask("Converting to C++");
                CCorePlugin.getDefault().convertProjectFromCtoCC(newProject, new SubProgressMonitor(monitor, 1));
            }
            monitor.subTask("Wiring in build info, etc.");
            cProject = CoreModel.getDefault().create(newProject);
            if (cProject == null)
                return null;
            IProject project = cProject.getProject();
            IManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
            // Need a temporary default so that NullPointerException doesn't
            // occur if we somehow mess up before settting the real
            // default configuration.
            // It will be corrected later.
            info.setDefaultConfiguration(type.getConfigurations()[0]);
            IManagedProject newManagedProject = ManagedBuildManager.createManagedProject(project, type);
            if (newManagedProject != null) {

                ManagedBuildManager.setNewProjectVersion(project);

                ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(project, true);
                cdesc.create(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID, ManagedBuildManager.INTERFACE_IDENTITY);
                IConfiguration configs[] = type.getConfigurations();
                //set binary parser from default config
                if (configs.length > 0) {
                    IToolChain tc = configs[0].getToolChain();
                    ITargetPlatform targetPlatform = tc.getTargetPlatform();
                    for (String id: targetPlatform.getBinaryParserList()) {
                        cdesc.create(CCorePlugin.BINARY_PARSER_UNIQ_ID, id);
                    }
                }
            }
//          Get my initializer to run (copied from NewProject wizard
            IStatus initResult = ManagedBuildManager.initBuildInfoContainer(newProject);
            if (initResult.getCode() != IStatus.OK) {
                // At this point, I can live with a failure
                ImporterPlugin.log(initResult);
            }
            info.setValid(true);
            // No need to save yet; caller will do it after configurations are
            // created.
            //ManagedBuildManager.saveBuildInfo(newProject, true);
            monitor.worked(1);
        }
        
        finally {
            monitor.done();
        }
        return cProject;
    }


    public IConfiguration createConfiguration (
            ICProject cproject,
            String name,
            IProjectType type,
            ICodewrightProject oldProj,
            IConfiguration original) throws CoreException, BuildException {
        if (cproject == null)
            throw new IllegalArgumentException("project is null");
        if (name == null)
            throw new IllegalArgumentException("name is null");
        if (type == null)
            throw new IllegalArgumentException("type is null");
        if (oldProj == null)
            throw new IllegalArgumentException("Codewright project is null");
        if (original == null)
            throw new IllegalArgumentException("original configuration is null");
        IProject project = cproject.getProject();

        String newId = original.getId() + "." + (int) (Math.random() * 1000000);
        IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project.getProject());
        IManagedProject newManagedProject = info.getManagedProject();
        if (newManagedProject == null)
            throw new CoreException(ImporterPlugin.makeErrorStatus("No ManagedProject object"));
        IConfiguration newConfig;

        if (original.isExtensionElement()) {
            newConfig = newManagedProject.createConfiguration(original, newId);
        }
        else {
            newConfig = newManagedProject.createConfigurationClone(original, newId);
        }
        if (info.getDefaultConfiguration() == null ||
            info.getDefaultConfiguration() == original)
            info.setDefaultConfiguration(newConfig);
        newConfig.setName(name);
        // Set the output file name
        if (oldProj.getOutputFile() != null) {
            IPath path = new Path(oldProj.getOutputFile());
            String ext = path.getFileExtension();
            path = path.removeFileExtension();
            newConfig.setArtifactName(path.lastSegment());
            newConfig.setArtifactExtension(ext);
        }
        else
            newConfig.setArtifactName(newManagedProject.getDefaultArtifactName());

        setCompilerIncludes(cproject, newConfig, oldProj.getCompilerIncludes());
        setCompilerDefines(newConfig, oldProj.getCompilerDefines());
        setCompilerUndefs(newConfig, oldProj.getCompilerUndefines());
        setAssemblerIncludes(cproject, newConfig, oldProj.getAssemblerIncludes());
        setAssemblerDefines(newConfig, oldProj.getAssemblerDefines());
        setAssemblerUndefs(newConfig, oldProj.getAssemblerUndefines());
        setCompilerOptions(newConfig, oldProj.getCompilerOptions());
        setAssemblerOptions(newConfig, oldProj.getAssemblerOptions());
        if (!oldProj.isLibrary()) {
            setLinkerOptions(newConfig, oldProj.getLinkerOptions());
            setObjectFilesAndLibraries(cproject, newConfig, oldProj.getObjectFilesAndLibraries());
            setSvr3CommandFiles(cproject, newConfig, oldProj.getSvr3CommandFiles());
            setSvr4CommandFiles(cproject, newConfig, oldProj.getSvr4CommandFiles());
        }
        excludeNonreferencedFiles(project.getProject(), newConfig, oldProj);
        return newConfig;
    }
    
    public void importSourceFile (IProject project, File file, File relativeTo, IProgressMonitor monitor) throws CoreException {
        String fn = relativizeFile(new Path(relativeTo.toString()),file);
        IFile dest = project.getFile(fn);
        mSourceFileMap.put(file,dest);
        try {
            confirmParentFoldersCreated(dest,file,monitor);
            dest.create(new FileInputStream(file),true,monitor);
        }
        catch (FileNotFoundException e) {           
            throw new CoreException(ImporterPlugin.makeErrorStatus("File not found",e));
        }
    }
    
    /**
     * Confirm that intermediate folders of a resource exist and that
     * there is a mapping from the original directory so that we can relative
     * such things as include directories.
     * @param resource the resource whose parent folders we wish to confirm exists.
     * @param original the original file or directory from which this resource corresponds.
     * @param monitor
     * @throws CoreException
     */
    private void confirmParentFoldersCreated(IResource resource, File original, IProgressMonitor monitor) throws CoreException{
        IContainer parent = resource.getParent();
        if (parent instanceof IFolder) {
            File dir = original != null?original.getParentFile():null;
            if (!parent.exists()){
                confirmParentFoldersCreated(parent,dir,monitor);
                ((IFolder)parent).create(true,true,monitor);
            }
            if (dir != null){
                mSourceDirMap.put(dir,(IFolder)parent);
            }
        }
    }
    
    /**
     * Given a path that is (likely) outside of the workspace,
     * find a path variable that can be used from which to reference
     * the path and insert it as a prefix.
     * <P>
     * If there is no path variable, then insert one with a name contrived
     * from the project.
     * 
     * @param path path that is about to be linked to.
     * @param baseName if a name cannot be easily derived for the
     * path variable, use this one as the base to derive a name.
     * @return new path with prefix made to reference path variable.
     * @throws CoreException 
     */
    private  IPath applyPathVariable(IPath path, String baseName) throws CoreException{
        if (mPathVarMap == null) {
            IPathVariableManager mgr = generateVarPathMap();
            mgr.addChangeListener(new IPathVariableChangeListener(){

                public void pathVariableChanged (IPathVariableChangeEvent event) {
                    generateVarPathMap();                  
                }});
        }
        
        int cnt = path.segmentCount();
        String pathVar = null;
        for (int i = 0; i <cnt; i++){
            pathVar = mPathVarMap.get(path.removeLastSegments(i));
            if (pathVar != null) {
                return new Path(pathVar).append(path.removeFirstSegments(cnt-i));
            }
        }
        // No existing path variable that is appropriate.
        // Define one. But we need to figure out which of the enclosing
        // directories to add.
        // "C:/ARC/mqx/source/foo.c" will likely be something like "MQX/source/foo.c"
        IPath varPath = computePathToFormIntoPathVariable(path);
        String varName = computePathVarName(varPath,baseName);
        IPathVariableManager mgr = ResourcesPlugin.getWorkspace().getPathVariableManager();
        mgr.setValue(varName,varPath);
        return new Path(varName).append(path.removeFirstSegments(varPath.segmentCount()));      
    }


    /**
     * Generate a reverse-map of the Path vs Path variable.
     * @return return the path variable manager.
     */
    private IPathVariableManager generateVarPathMap () {
        mPathVarMap = new HashMap<IPath,String>();
        IPathVariableManager mgr = ResourcesPlugin.getWorkspace().getPathVariableManager();
        String[] varNames = mgr.getPathVariableNames();
        for (String n: varNames){
            IPath value = mgr.getValue(n);
            if (value != null){
                mPathVarMap.put(value,n);
            }
        }
        return mgr;
    }
    
    private static String computePathVarName(IPath path, String baseName){
        int segCount = path.segmentCount();
        String lastSeg = path.segment(segCount-1).toLowerCase();
        IPathVariableManager mgr = ResourcesPlugin.getWorkspace().getPathVariableManager();
        if (lastSeg.startsWith("mqx")){
            String[] names = new String[]{"MQX","MQX_INSTALL","MQX_LOCATION",
                        "MQXLIB", "MQX_LIB", "MQXINSTALL", "MQXLOCATION"};
            for (String n: names){
                if (!mgr.isDefined(n)) return n;
            }
        }
        if (!mgr.isDefined(lastSeg.toUpperCase())){
            return lastSeg.toUpperCase();
        }
        if (!mgr.isDefined(lastSeg))
            return lastSeg;
        if (segCount > 2){
            String s = path.segment(segCount-1);
            String[] names = new String[]{ (s + "_" + lastSeg).toUpperCase(),
                            s + "_" + lastSeg };
            for (String n: names){
                if (!mgr.isDefined(n)) return n;
            }
        }
        int link = 0;
        while (true){
            String s = baseName + "_LINK" + link;
            if (!mgr.isDefined(s)) return s;
            link++;
        }
    }
    
    /**
     * Given an absolute path that is being imported as a link,
     * figure out which parent to form into a path variable.
     * @param path the absolute path.
     * @return the parent to form into a path variable.
     */
    private static IPath computePathToFormIntoPathVariable(IPath path){
        if (!path.isAbsolute())
            throw new IllegalArgumentException("Path must be absolute (" + path + ")");
        int segCount = path.segmentCount();
        if (segCount <= 1) return path;
        int segmentIndex = 0;
        // If reference to ARC distribution, check if mqx
        if (path.segment(0).equalsIgnoreCase("arc")){
            segmentIndex++;
        }
        // Reference to MQX distribution (e.g., "C:/ARC/mqx123/..." use
        // that path to it (C:/ARC/mqx123)
        if (path.segment(segmentIndex).toLowerCase().startsWith("mqx")){
            return path.removeLastSegments(segCount-segmentIndex-1);
        }
        //  Look for the name "source" or "include". 
        // We'll return that.
        for (int i = 0; i < segCount-1; i++){
            String segName = path.segment(i).toLowerCase();
            if (segName.indexOf("source") >= 0 ||
                segName.indexOf("src") >= 0 ||
                segName.indexOf("include") >= 0 ||
                segName.indexOf("lib") >= 0)
            {
                return path.removeLastSegments(segCount-i-1);
            }
        }
        // Look for the name "workspace" or "working". We'll go one below
        // that.
        for (int i = 0; i < segCount-2;i++){
            String segName = path.segment(i).toLowerCase();
            if (segName.indexOf("work") >= 0 )
            {
                return path.removeLastSegments(segCount-i-2);
            }
        }
        // If there is just 2 segments, return the parent
        if (segCount == 2) return path.removeLastSegments(1);
        
        //More than 2, return 2 levels up
        return path.removeLastSegments(2);
        
    }

   
    public void linkSourceFile (IProject project, File file, File relativeTo, IProgressMonitor monitor) throws CoreException {
        String fn = relativizeFile(new Path(relativeTo.toString()),file);
        Path pathInProject = new Path(fn);
        IFile dest = project.getFile(pathInProject);
        mSourceFileMap.put(file,dest);
        // Note we can only add links to top most project. If the target is within
        // subdirectory, we just link the directories.
        IContainer parent = dest.getParent();
        if (parent.getType() == IResource.PROJECT) {
            dest.createLink(
                    applyPathVariable(new Path(file.getAbsolutePath()),
                            project.getName().replace(' ','_')
                            ),IResource.FORCE,monitor);
        }
        else {
            IFolder folder = getTopMostFolder(parent);
            if (!folder.exists()){
                Path target = new Path(new File(relativeTo,parent.getProjectRelativePath().segment(0)).getAbsolutePath());
                folder.createLink(applyPathVariable(target,project.getName().replace(' ','_')),IResource.FORCE,monitor);
            }
        }
    }
    
    private static IFolder getTopMostFolder(IResource r){
        if (r instanceof IProject || r.getParent() == null){
            return null;
        }
        if (r.getParent().getType() == IResource.PROJECT){
            if (r instanceof IFolder){
                return (IFolder)r;
            }
            return null;
        }
        return getTopMostFolder(r.getParent());
    }
    
    
    public void useInplaceSourceFile (IProject project, File file, File relativeTo) {
        String fn = relativizeFile(new Path(relativeTo.toString()),file);
        IFile dest = project.getFile(new Path(fn));
        mSourceFileMap.put(file,dest);       
    }
    
    private void setCompilerIncludes(ICProject project, IConfiguration config, File[] includes) throws BuildException, CoreException{
        IOption option = setOption(config,"arc.compiler.options.include_dirs",relativizeFileList(includes,"includes"));
        // Now make sure we materialize the include files to the 
        // C/C++ indexer
        ManagedBuildManager.initializePathEntries(config,option);
    }
    
    private static void setCompilerDefines(IConfiguration config, String[] defines) throws BuildException{
        IOption option = setOption(config,"arc.compiler.options.defines",defines);
        // Now make sure we materialize the symbols to the 
        // C/C++ indexer
        ManagedBuildManager.initializePathEntries(config,option);
    }
    
    private static void setCompilerUndefs(IConfiguration config, String[] undefs) throws BuildException{
        setOption(config,"arc.compiler.options.undefines",undefs);
    }
    
    private void setAssemblerIncludes(ICProject project, IConfiguration config, File[] includes) throws BuildException, CoreException{
        setOption(config,"com.arc.cdt.toolchain.asm.option.includes",relativizeFileList(includes,"includes"));
    }
    
    private static void setAssemblerDefines(IConfiguration config, String[] defines) throws BuildException{
        setOption(config,"com.arc.cdt.toolchain.asm.option.defines",defines);
    }
    
    private static void setAssemblerUndefs(IConfiguration config, String[] undefs) throws BuildException{
        setOption(config,"com.arc.cdt.toolchain.asm.options.undefines",undefs);
    }
    
    private void setObjectFilesAndLibraries(ICProject project, IConfiguration config, File[] files) throws BuildException, CoreException{
        setOption(config,"arc.link.ld.user_objs",relativizeFileList(files,"lib"));
    }
    
    private void setSvr3CommandFiles(ICProject project, IConfiguration config, File[] files) throws BuildException, CoreException{
        setOption(config,"com.arc.cdt.toolchain.option.linker.svr3",relativizeFileList(files,"linker"));
    }
    
    private void setSvr4CommandFiles(ICProject project, IConfiguration config, File[] files) throws BuildException, CoreException{
        setOption(config,"com.arc.cdt.toolchain.option.linker.svr4",relativizeFileList(files,"linker"));
    }
    
//    /**
//     * Given a file within a container, return a relativized path relative
//     * to the container. IF the file is not in the container, return the
//     * full path.
//     * @param home the container.
//     * @param f the file that is presumably in the container.
//     * @return the path of the file relative to the container.
//     */
//    private static String relativizeFile(IContainer home, File f){
//        IPath homePath = home.getFullPath();
//        return relativizeFile(homePath,f);
//    }
    
    /**
     * Given a file within a directory path, return a relativized path relative
     * to the container. IF the file is not in the container, return the
     * full path.
     * @param homePath the directory path relative to what we want the
     * computed path to.
     * @param f the file that is presumably in the container.
     * @return the path of the file relative to the container.
     */
    private static String relativizeFile(IPath homePath, File f){
        IPath filePath = new Path(f.getAbsolutePath());
        String[] homeSegs = homePath.segments();
        String[] fileSegs = filePath.segments();
        if (fileSegs.length > homeSegs.length){
            boolean match = true;
            for (int i = 0; i < homeSegs.length; i++){
                if (!fileSegs[i].equals(homeSegs[i])){
                    match = false;
                    break;
                }
            }
            if (match){
                StringBuilder buf = new StringBuilder();
                for (int i = homeSegs.length; i < fileSegs.length; i++){
                    if (buf.length() > 0)
                        buf.append('/');
                    buf.append(fileSegs[i]);
                }
                return buf.toString();
            }
        }
        return f.getAbsolutePath().replace('\\','/');
    }
    
//    private static String[] relativizeFileList(IContainer home, File files[]){
//        String[] result = new String[files.length];
//        for (int i = 0; i < files.length; i++){
//            result[i] = relativizeFile(home,files[i]);
//        }
//        return result;
//    }
    
//    private static String[] relativizeFileList(IPath homePath, File files[]){
//        String[] result = new String[files.length];
//        for (int i = 0; i < files.length; i++){
//            result[i] = relativizeFile(homePath,files[i]);
//        }
//        return result;
//    }
//    
    private static IOption setOption(IConfiguration config, String id, Object value) throws BuildException{
        ITool tools[] = config.getTools();
        ITool theTool = null;
        IOption theOption = null;
        for (ITool tool: tools){
            IOption opt = tool.getOptionById(id);
            if (opt != null) {
                theOption = opt;
                theTool = tool;
                break;
            }
        }
        if (theOption != null){
            if (value instanceof String[])
                config.setOption(theTool,theOption,(String[])value);
            else if (value instanceof Boolean)
                config.setOption(theTool,theOption,((Boolean)value).booleanValue());
            else if (value instanceof String)
                config.setOption(theTool,theOption,(String)value);
            else throw new BuildException("Don't know type of option " + id +
                    ": " + value);
        }
        else
            throw new BuildException("Can't find option \"" + id + "\"");
        return theOption;
    }
    
    private static void setCompilerOptions(IConfiguration config, String args[]) throws BuildException{
        setToolOptions(config,args,"compiler","arc.compiler.options.misc.additional");
    }
    
    private static void setAssemblerOptions(IConfiguration config, String args[]) throws BuildException{
        setToolOptions(config,args,"assem","com.arc.cdt.toolchain.option.asm.extra");
    }
    
    private static void setLinkerOptions(IConfiguration config, String args[]) throws BuildException{
        setToolOptions(config,args,"linker","arc.link.options.ldflags");
    }
    
    private static void setToolOptions(IConfiguration config, String args[], String embeddedID, String otherOptionName) throws BuildException{
        ITool tool = findTool(config,embeddedID);
        if (tool == null) throw new BuildException("Can't find tool " + embeddedID);
        IOption others = tool.getOptionById(otherOptionName);
        if (others == null)
            throw new BuildException("Can't find additional options for " + embeddedID + " tool");
        setOptions(config,tool,args,others);
    }
    
    private static ITool findTool(IConfiguration config,String embeddedString){
        ITool tools[] = config.getTools();
        embeddedString = embeddedString.toLowerCase();
        for (ITool t: tools){
            if (toolMatches(t,embeddedString)){
                return t;
            }
        }
        return null;
    }
    
    private static boolean toolMatches(ITool tool, String embeddedString){
        if (tool.getId().toLowerCase().indexOf(embeddedString) >= 0){
            return true;
        }
        ITool superClass = tool.getSuperClass();
        if (superClass != null) return toolMatches(superClass,embeddedString);
        return false;
    }
    
    /**
     * Set tool options based on a set of tool flags.
     * @param config the configuration being initialized.
     * @param tool the tool to which the flags apply.
     * @param flags the flags
     * @param others an option for extra flags that are not otherwise
     * categorized.
     * @throws BuildException 
     */
    private static void setOptions(IConfiguration config, ITool tool, String flags[], IOption others) throws BuildException{
        if (others == null || tool == null || flags == null)
            throw new IllegalArgumentException("argument is null");
        if (others.getValueType() != IOption.STRING && others.getValueType() != IOption.STRING_LIST){
            throw new IllegalArgumentException("others option is not string");
        }
        IOption options[] = tool.getOptions();
        List<String> extra = new ArrayList<String>();
        Map<IOption,List<String>>stringListMap = new HashMap<IOption,List<String>>();
        for (String flag: flags){
            OptionSetting setting = findOptionWithFlag(options,flag);
            if (setting != null){
                if (setting.value instanceof Boolean)
                    config.setOption(tool,setting.option,((Boolean)setting.value).booleanValue());
                else if (setting.option.getValueType() == IOption.STRING_LIST){
                    List<String> currentValues = stringListMap.get(setting.option);
                    if (currentValues == null){
                        currentValues = new ArrayList<String>();
                        stringListMap.put(setting.option,currentValues);
                    }
                    currentValues.add((String)setting.value);
                    config.setOption(tool,setting.option,currentValues.toArray(new String[currentValues.size()]));
                }
                else
                    config.setOption(tool,setting.option,((String)setting.value));
            }
            else {
                extra.add(flag);
            }
        }
        if (extra.size() > 0){
            if (others.getValueType() == IOption.STRING)
                config.setOption(tool,others,StringUtil.arrayToArgString(extra.toArray(new String[extra.size()])));
            else 
                config.setOption(tool,others,extra.toArray(new String[extra.size()]));
        }       
    }
    
    /**
     * Search a list of options for one that corresponds to a tool argument.
     * @param options the list of options to search.
     * @param arg the tool argument for which an option is being sought.
     * @return an option and its setting value, or <code>null</code>.
     * @throws BuildException 
     */
    static OptionSetting findOptionWithFlag (IOption options[], String arg) throws BuildException {
        for (IOption opt : options) {
            switch (opt.getValueType()) {
                case IOption.BOOLEAN: {
                    // Toggle specifications (e.g., '-Hon=Foo') are case-insensitive
                    String cmd = opt.getCommand();
                    String cmdFalse = opt.getCommandFalse();
                    String larg = arg;
                    if (cmd != null && cmd.startsWith("-Hon=") || cmdFalse != null && cmdFalse.startsWith("-Hoff=")) {
                        if (cmd != null)
                            cmd = cmd.toLowerCase();
                        if (cmdFalse != null)
                            cmdFalse = cmdFalse.toLowerCase();
                        larg = arg.toLowerCase();
                    }
                    if (larg.equals(cmd)) {
                        return new OptionSetting(opt, true);
                    }
                    else if (larg.equals(cmdFalse)) {
                        return new OptionSetting(opt, false);
                    }
                    break;
                }
                case IOption.STRING:
                case IOption.STRING_LIST:
                    if (opt.getCommand() != null && opt.getCommand().length() > 0 && arg.startsWith(opt.getCommand())) {
                        return new OptionSetting(opt, arg.substring(opt.getCommand().length()));
                    }
                    break;
                case IOption.ENUMERATED: {
                    String[] enums = opt.getApplicableValues();
                    for (String e : enums) {
                        String id = opt.getEnumeratedId(e);
                        if (arg.equals(opt.getEnumCommand(id))) {
                            return new OptionSetting(opt, id);
                        }
                    }
                    break;
                }
            }
        }
        return null;
    }
    
    /**
     * Any source file in the new C project that is not referenced in the
     * configuration, exclude it.
     * @param project the new C project.
     * @param config the configuration being updated.
     * @param oldProj the old codewright project from which we get the list
     * of referenced files.
     * @throws CoreException 
     */
    private void excludeNonreferencedFiles(IProject project, IConfiguration config, ICodewrightProject oldProj) throws CoreException{
        File projectSourceFiles[] = oldProj.getSourceFiles();
        HashSet<IFile> filesToInclude = new HashSet<IFile>();
        for (File f: projectSourceFiles){
            IFile newFileLocation = mSourceFileMap.get(f);
            if (newFileLocation == null)
                throw new CoreException(ImporterPlugin.makeErrorStatus("Missing file " + f + " in new project"));
            filesToInclude.add(newFileLocation);
        }
 
        Collection<IFile> filesToExclude = computeAllFilesWithin(project);
        filesToExclude.removeAll(filesToInclude);
        // Now we have the list of files to exclude.
        for (IFile f: filesToExclude){
            IResourceConfiguration resConfig = config.createResourceConfiguration(f);
            resConfig.setExclude(true);
        }
    }
    
    private static Collection<IFile> computeAllFilesWithin(IContainer folder) throws CoreException{
        List<IFile> files = new ArrayList<IFile>();
        findFilesWithin(files,folder);
        return files;
    }
    /**
     * Transitively find all files within a container and its sub-containers.
     * @param files the list to append to.
     * @param folder the folder to search.
     * @throws CoreException 
     */
    private static void findFilesWithin(List<IFile>files, IContainer folder) throws CoreException{
        IResource resources[] = folder.members();
        for (IResource r: resources){
            if (r instanceof IContainer){
                findFilesWithin(files,(IContainer)r);
            }
            else if (r instanceof IFile){
                files.add((IFile)r);
            }
        }
        
        
    }
    
    private String[] relativizeFileList(File[] files, String pathVarPrefix) throws CoreException{
        String[] result = new String[files.length];
        for (int i = 0; i < result.length; i++){
            result[i] = relativizeFile(files[i],pathVarPrefix);
        }
        return result;
    }
    
    private String relativizeFile(File f, String pathVarPrefix) throws CoreException{
        IFolder folder = mSourceDirMap.get(f);
        
        if (folder != null ){
            String fn = folder.getRawLocation().toString();
            if (mProjectLocation != null && fn.startsWith(mProjectLocation)){
                fn = "${ProjDirPath}" + fn.substring(mProjectLocation.length());
            }
            return fn;     
        }
        if (!f.isDirectory()){
            folder = mSourceDirMap.get(f.getParentFile());
            if (folder != null){
                if (f.isDirectory()){
                    if (!folder.getFolder(f.getName()).exists())
                        folder = null;
                }
                if (!folder.getFile(f.getName()).exists()){
                    folder = null;
                }
            }
            if (folder != null){
                return folder.getRawLocation().toString() + '/' + f.getName();
            }
        }
        //Use path variable if its outside of project
        if (f.isAbsolute()){
            IPath p = new Path(f.toString());
            p = applyPathVariable(p,pathVarPrefix);
            if (!p.isAbsolute()) {
                String s = p.toString();
                Matcher matcher = Pattern.compile("([^/\\\\:]+)(.*)").matcher(s);
                if (matcher.matches()){
                    return "${" + matcher.group(1) + "}" + matcher.group(2);
                }
            }
        }
        return f.getPath().replace(File.separatorChar,'/');
    }
    
    static class OptionSetting {
        public OptionSetting(IOption option, boolean v){
            this(option,Boolean.valueOf(v));
        }
        public OptionSetting(IOption option, Object s){
            this.option = option;
            this.value = s;
        }
        /**
         * The option affected.
         */
        public IOption option;
        /**
         * The value of the option. For a boolean it will be Boolean.TRUE
         * or Boolean.FALSE. For a string it will be string value.
         * For an enumeration, it will be the enumeration id.
         */
        public Object value;
        
    }



   

}
