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
package org.eclipse.cdt.managedbuilder.makegen.gnu;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.makegen.IMakefileAugmenter;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

/**
 * This is a replacement to GnuMakeFileGenerator that adds a little more capability.
 * Specifically, it permits plugins to emit its own macro definitions.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class GnuMakefileGenerator2 extends GnuMakefileGenerator {
    private final String MAKEFILE_AUGMENTER = "MakefileAugmenter";
    public static final String PROJECT_SYMBOL = "PROJECT"; //$NON-NLS-1$
    public GnuMakefileGenerator2() {
        // @todo Auto-generated constructor stub
    }
    private final static String MAIN_LINK = "MAIN_LINK"; // macro name
    private final static String WORKSPACE = "WORKSPACE"; // macro name
    private final static String WORKING = "WORKING"; // macro name

    private IPath fCommonRoot;

    private IProject project;
    private IManagedBuildInfo info;

    private String ext;
    private String workspacePath = null;
    private String workingPath = null;
    private IConfiguration config;
    private String cleanCommand = null;
    private IMakefileAugmenter augmenter = null;
    

    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param project
     * @param info
     * @param monitor
     */
    @Override
    public void initialize(int buildKind, IConfiguration cfg, IBuilder builder,
        IProgressMonitor monitor) {
        super.initialize(buildKind, cfg, builder,monitor);
        this.project = cfg.getOwner().getProject();
        this.info = ManagedBuildManager.getBuildInfo(project);
        this.ext = cfg.getArtifactExtension();
        this.config = cfg;
        this.augmenter = extractAugmenter();
        //NOTE: Can no longer call this method from here because, as of CDT 4.0, the associated
        // ManagedBuildInfo isn't completely constructed. NullPointerExceptions will occur; so we
        // delay it until we need it.
        //initMacroStuff();
        
    }
    
    private IMakefileAugmenter extractAugmenter(){
    	IExtension extensions[] = Platform.getExtensionRegistry().getExtensionPoint(ManagedBuilderCorePlugin.getUniqueIdentifier(), MAKEFILE_AUGMENTER).getExtensions();
        IConfigurationElement e = null;
        IProjectType projectType = getProjectType();
        if (projectType == null) return null; //shouldn't happen
        for (IExtension extension: extensions){
            IConfigurationElement elements[] = extension.getConfigurationElements();
            for (IConfigurationElement element: elements){
                if (matchesProjectType(projectType,element.getAttribute("projectType"))){
                    e = element;
                    break;
                }
            }
        }
        if (e != null){
            try {
                return  (IMakefileAugmenter)e.createExecutableExtension("class");
            }
            catch (CoreException e1) {
                ManagedBuilderCorePlugin.log(e1);
            }       
        }
        return null;
    }

    private void initMacroStuff () {       
        IPath workspace = Platform.getLocation();
        if (true /*isReferencedPath(workspace)*/){
            workspacePath = workspace.toString();
        }
        IPath working = project.getLocation().removeLastSegments(1);
        if (isReferencedPath(working)){
            workingPath = working.toString();
        }
    }
    
    private void addCustomMacros(StringBuffer buffer){

        
        initMacroStuff(); // Moved here from "initialize()" because of ManagedBuildInfo isn't yet fully
                           // constructed when initialize() called, as of CDT 4.0
       
        buffer.append(PROJECT_SYMBOL + " := .." + NEWLINE); //CUSTOMIZATION
        buffer.append(NEWLINE);
        
        IPath workspace = Platform.getLocation();
        if (workspacePath != null){
            if (workspace.isPrefixOf(project.getLocation())) {
                buffer.append(WORKSPACE + " = ../.." + NEWLINE);
            }
            else
                buffer.append(WORKSPACE + " = "
                        + workspacePath
                        + NEWLINE);
        }
        if (workingPath != null){
            buffer.append(WORKING + " = $(PROJECT)/.." + NEWLINE);
        }
        
        applyAugmentation(buffer);
    }
    
    private static boolean matchesProjectType(IProjectType projectType, String id){
        if (projectType.getBaseId().equals(id)) return true;
        if (projectType.getSuperClass() != null)
            return matchesProjectType(projectType.getSuperClass(),id);
        return false;
    }
    
    private IProjectType getProjectType(){
        if (config == null) return null;
        return getProjectType(config);
    }
    
    private static IProjectType getProjectType(IConfiguration config){
        IProjectType p = config.getProjectType();
        if (p != null) return p;
        if (config.getParent() != null){
            return getProjectType(config.getParent());
        }
        return null;
    }
    
    private void applyAugmentation(StringBuffer buffer) {     
        if (this.augmenter != null) {
             String s = augmenter.generateMacroDefinitions(getConfiguration());
             if (s != null) buffer.append(s);
             this.cleanCommand   = augmenter.getCleanCommand(getConfiguration());
        }
    }
      
    private static boolean CASE_INSENSITIVE = new File("/ABC").equals(new File("/abc"));
    
    /**
     * Replace occurrences of the project directory with "$(ROOT)" to
     * make it relative.
     * <P>
     * @param cmd
     * @return the command with "$(ROOT)" substituted.
     */
    private static String relativize(String cmd, String path, String macro){
        if (cmd.indexOf("\\ ") < 0) // No escaped blanks ("\ ")
            cmd = cmd.replace('\\','/');
        else {
            StringBuilder buf = new StringBuilder(cmd.length());
            int i = 0;
            while (i < cmd.length()) {
                char c = cmd.charAt(i);
                switch(c){
                case '\\': 
                    if (i+1 < cmd.length() && cmd.charAt(i+1) == ' ') {
                        i++;
                        buf.append("\\ ");
                    }
                    else buf.append("/");
                    break;
                default:
                    buf.append(c);
                    break;
                }
                i++;
            }
            cmd = buf.toString();
        }
        String lcmd = cmd;
        if (CASE_INSENSITIVE){
            path = path.toLowerCase();
            lcmd = lcmd.toLowerCase();
        }
        int i = lcmd.indexOf(path);
        if (i < 0) {
            if (path.indexOf("\\ ") < 0 && path.indexOf(' ') >= 0){
                path = path.replaceAll("\\s","\\\\ ");
                return relativize(cmd,path,macro);
            }
        }
        while (i >= 0){
            boolean doit = (i == 0 || cmd.charAt(i-1) == ' ') &&
                (i + path.length() >= lcmd.length() ||
                !Character.isJavaIdentifierPart(lcmd.charAt(i+path.length())));
            if (!doit && i > 3 && (cmd.substring(i-2,i).equals("-I") ||
                    cmd.substring(i-3,i).equals("-I\""))){
                doit = true;
            }
            if (doit){
                cmd = cmd.substring(0,i) + "$("+macro+")" + cmd.substring(i+path.length());
                lcmd = cmd;
                if (CASE_INSENSITIVE){
                    lcmd = lcmd.toLowerCase();
                }
            }
            i = lcmd.indexOf(path,i+1);
        }
        return cmd;       
    }
    
    @Override
    protected String canonicalizePaths(String cmd){
        cmd = relativize(cmd,project.getLocation().toString(),PROJECT_SYMBOL);
        Collection<IFolder>linkedFolders = this.getLinkedFolders();
        for (IFolder f: linkedFolders){
        	if (f.getLocation() != null)
                cmd = relativize(cmd,f.getLocation().toString(),getMacroForFolder(f));
        }
        IPath commonRoot = computeCommonRootFor();
        if (commonRoot != null){
            cmd = relativize(cmd,commonRoot.toString(),MAIN_LINK);
        }
        if (workspacePath != null) {
            cmd = relativize(cmd,workspacePath,WORKSPACE);
        }
        if (workingPath != null) {
            cmd = relativize(cmd,workingPath,WORKING);
        }
        return cmd;
    }
    
    @Override
    public String ensurePathIsGNUMakeTargetRuleCompatibleSyntax (String path) {
        if (this.augmenter != null) {
        	String s = this.augmenter.canonicalizePath(path);
        	if (s != null) return s;
        }
        return super.ensurePathIsGNUMakeTargetRuleCompatibleSyntax(path);
    }

    // ASK_DAVID
    private Collection<IFolder> getLinkedFolders() {
       // List<IResource> subdirs = getSubdirList();
    	Collection<IContainer> subdirs = getSubdirList();
        Set<IFolder> folders = new HashSet<IFolder>();
        //for (IResource c: subdirs){
         for(IContainer c:subdirs){
            IFolder top = getTopFolder(c);
            if (top != null && top.isLinked()){
                folders.add(top);
            }
        }
        return folders;
    }
    
    /**
     * Compute the top folder under the project that a folder is within.
     *
     * @param folder
     * @return the top folder under the project that a folder is within.
     */
    private static IFolder getTopFolder(IResource folder){
        if (folder instanceof IFolder && folder.getParent().getType() == IResource.PROJECT){
            return (IFolder)folder;
        }
        if (folder.getParent() != null){
            return getTopFolder(folder.getParent());
        }
        return null;
    }
    /**
     * Return a macro to denote a folder.
     * 
     * @param folder
     * @return a macro to denote a folder.
     */
    private static String getMacroForFolder(IFolder folder){
        return "DIR_" + folder.getProjectRelativePath().toString().toUpperCase();
    }
    
    /**
     * Attempt to relativize the given path relative to the configuration
     * directory we are in.
     * @param path
     * @return relative path, if possible
     */
    private String computeRelativePath(IPath path, IPath relativeTo, String macro) {
        if (path.getDevice() == null && relativeTo.getDevice() == null
                || path.getDevice() != null
                && path.getDevice().equalsIgnoreCase(relativeTo.getDevice())) {
            int cnt = relativeTo.matchingFirstSegments(path);
            StringBuilder buf = new StringBuilder();
            buf.append("$(");
            buf.append(macro);
            buf.append(")");
            for (int i = cnt; i < relativeTo.segmentCount(); i++){
                buf.append("/..");
            }
            String[] segments = path.segments();
            for (int i = cnt; i < segments.length; i++) {
                buf.append("/");
                buf.append(segments[i]);
            }
            return buf.toString();
        }
        return path.toString();
    }
    
    /**
     * Compute common root directory for a series of resources.
     * @param folders
     * @return common root directory for a series of resources.
     */
    private static IPath computeCommonRootFor(Collection<? extends IResource>folders){

        List<IPath> paths = new ArrayList<IPath>(folders.size());
        for (IResource f: folders){
        	if (f.isLinked() && f instanceof IFolder){
        		try {
					for (IResource member: ((IFolder)f).members()) {
						IPath path = member.getRawLocation();
						if (member instanceof IFile){
							path = path.removeLastSegments(1);
						}
						paths.add(path);
					}
				} catch (CoreException e) {
					// what to do?
				}
        	}
        	else {
	            IPath path = f.getLocation();
	            if (f instanceof IFile){
	                path = path.removeLastSegments(1);
	            }
	            paths.add(path);
        	}
        }
        return computeCommonRootForPaths(paths);
    }
    
    /**
     * Compute the common root for a series of paths.
     * 
     * @param paths
     * @return the common root for a series of paths.
     */
    private static IPath computeCommonRootForPaths(Collection<IPath>paths){
        IPath result = null;
        for (IPath path: paths){
            if (result == null){
                result = path;
            }
            else {
                if (path.getDevice() == null && result.getDevice() != null ||
                    path.getDevice() != null && !path.getDevice().equals(result.getDevice()))
                    return null; // not on same volume
                int cnt = result.matchingFirstSegments(path);
                if (cnt == 0) return null; // no common directories.
                result = result.removeLastSegments(result.segmentCount() - cnt);
            }
        }
        return result;
    }
    
    private IPath computeCommonRootFor() {
        if (fCommonRoot == null) {
            Collection<IFolder> linkedDirs = this.getLinkedFolders();
            IPath path = computeCommonRootFor(linkedDirs);

            IResource[] resources;
            try {
                resources = project.members();
            } catch (CoreException e) {
                return path; // shouldn't happend
            }
            List<IResource> linkedFiles = new ArrayList<IResource>();
            for (IResource r : resources) {
                if (r instanceof IFile && ((IFile) r).isLinked()) {
                    linkedFiles.add(r);
                }
            }
            if (linkedFiles.size() > 0) {
                IPath path2 = computeCommonRootFor(linkedFiles);
                if (path != null) {
                    List<IPath> paths = new ArrayList<IPath>();
                    paths.add(path);
                    paths.add(path2);
                    return computeCommonRootForPaths(paths);
                }
                return path2;
            }
            fCommonRoot = path;
        }
        return fCommonRoot;
    }
    
    private boolean isReferencedPath(IPath dir){
        IProject refs[];
        try {
            refs = project.getReferencedProjects();
        } catch (CoreException e) {
            return false;
        }
        if (refs.length > 0) {
            if (hasReferenceTo(refs, dir)) {
                return true;
            }
        }
        String[] userObjs = info.getUserObjectsForConfiguration(ext);
        if (userObjs.length > 0){
            String prefix = dir.toString();
            for (String u: userObjs){
                if (u.replace('\\','/').startsWith(prefix))
                    return true;
                
            }
        }
        return false;
    }

    private static boolean hasReferenceTo(IProject projects[], IPath dir){
        for (IProject p: projects){
            if (p.getLocation() != null && dir.isPrefixOf(p.getLocation())){
                return true;
            }
        }
        return false;
    }

    @Override
    protected StringBuffer addSubdirectories () {
        StringBuffer buffer =  super.addSubdirectories();
        // If directories are links, then we reference them with a macro prefix.
        // Define these macros.
        Collection<IFolder> linkedFolders = getLinkedFolders();
        IPath commonRoot = computeCommonRootFor();
        if (commonRoot != null){
            buffer.append(NEWLINE);
            buffer.append(MAIN_LINK);
            buffer.append(" = ");
            buffer.append(computeRelativePath(commonRoot,project.getLocation(),PROJECT_SYMBOL));
            buffer.append(NEWLINE);
        }
        boolean first = true;
        for (IFolder topFolder : linkedFolders) {
        	if (topFolder.getLocation() == null) continue;
            if (first) {
                buffer.append(NEWLINE);
                first = false;
            }
            buffer.append(getMacroForFolder(topFolder));
            buffer.append(" = ");
            if (commonRoot != null){
                buffer.append(computeRelativePath(topFolder.getLocation(),commonRoot,MAIN_LINK));
            }
            else {
                buffer.append(computeRelativePath(topFolder.getLocation(),project.getLocation(),PROJECT_SYMBOL));
            }
            buffer.append(NEWLINE);
        }

        return buffer;
    }
       
    @Override
    protected StringBuffer addTopHeader () {
        StringBuffer buffer = super.addTopHeader();
        addCustomMacros(buffer);
        return buffer;
    }
    
    @Override
    protected String getCleanCommand () {
        if (cleanCommand != null) return cleanCommand;
        return super.getCleanCommand();
    }
}
