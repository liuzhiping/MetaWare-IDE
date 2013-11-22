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
package com.arc.cdt.importer.internal.ui;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.arc.cdt.importer.core.Factory;
import com.arc.cdt.importer.core.ICodewrightProject;
import com.arc.cdt.importer.core.ICodewrightProjectSpace;
import com.arc.cdt.importer.core.PSPException;


/**
 * The "model" that the project-space import wizard uses to maintain state
 * and fire change events as the user selectes stuff.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
class AccessModel {
    
    private File mPspFile = null;
    private ICodewrightProjectSpace mProjectSpace = null;
    private ICodewrightProject[] mSelectedProjects = new ICodewrightProject[0];
    private String mNewProjectName = null;
    private File mNewProjectLocation = null;
    private boolean mCreateInWorkspace;
    private boolean mCreateInPlace;
    private boolean mLinkToSourceFiles = false;
    private IRunnableContext mContext;
    
    AccessModel(IRunnableContext context){
        if (context == null) throw new IllegalArgumentException("context is null");
        mContext = context;
    }

    public void setPspFile(File pspFile) {
        if (pspFile == null || !pspFile.equals(mPspFile)){
            mPspFile = pspFile;
            mProjectSpace = null; // lazily load it.
            mSelectedProjects = new ICodewrightProject[0];
        }
    }
    
    public ICodewrightProjectSpace getProjectSpace() throws IOException, PSPException{
        if (mProjectSpace == null && mPspFile != null){
            IRunnableWithProgress runnable = new IRunnableWithProgress(){

                public void run (IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try {
                        mProjectSpace = Factory.getProjectSpaceFinder().extractProjectSpace(mPspFile,
                                monitor);
                    }
                    catch (Exception e) {
                        throw new InvocationTargetException(e);
                    }                  
                }};
             try {
                mContext.run(false,false,runnable);
            }
            catch (InvocationTargetException e) {
                Throwable t = e.getTargetException();
                if (t instanceof IOException){
                    throw (IOException)t;
                }
                if (t instanceof PSPException){
                    throw (PSPException)t;
                }
                if (t instanceof RuntimeException)
                    throw (RuntimeException)t;
                throw new Error(t); // shouldn't get here
            }
            catch (InterruptedException e) {
                //shouldn't get here
            }
    
           
        }
        return mProjectSpace;        
    }
    
    public void setSelectedProjects(ICodewrightProject projects[]){
        mSelectedProjects = projects;  
    }
    
    public ICodewrightProject[] getSelectedProjects(){
        return mSelectedProjects;
    }
    
    public void setNewProjectName(String name){
        mNewProjectName = name;
    }
    
    public String getNewProjectName(){
        return mNewProjectName;
    }
    
    public File getNewProjectLocation(){
        if (mCreateInPlace)
            return mProjectSpace.getLocation();
        return mNewProjectLocation;
    }
    
    public void setNewProjectLocation(File path){
        mNewProjectLocation = path;
    }
    
    public boolean isCreateInWorkspace() {
        return mCreateInWorkspace;
    }
    
    public boolean isCreateInPlace(){
        return mCreateInPlace;
    }
    
    public void setCreateInWorkspace(boolean v){
        mCreateInWorkspace = v;
        if (v) {
            setCreateInPlace(false);
        }
    }
    
    public void setCreateInPlace(boolean v){
        mCreateInPlace = v;
        if (v){
            setCreateInWorkspace(false);
        }
        
    }

    /**
     * Indicate whether we're to link to the source files, or import them.
     * @param v
     */
    public void setLinkToSourceFiles (boolean v) {
        mLinkToSourceFiles = v;
        
    }
    
    public boolean isLinkToSourceFiles(){
        return mLinkToSourceFiles;
    }
    private static final String PROJECT_TYPE_PREFIX = "com.arc.cdt.toolchain.";
    private static final String PROJECT_TYPE_EXE_SUFFIX = ".exeProject";
    private static final String PROJECT_TYPE_LIB_SUFFIX = ".libProject";
    
    /**
     * Return the project type corresponding to a target ID (e.g., "ac",
     * "arc", "arm", etc.).
     * @return the project type or <code>null</code> if the target ID is
     * not recognized.
     */
    public static IProjectType getProjectType (String target, boolean isLibrary) {
        if (target == null) return null;
        String t = target.toLowerCase();
        if (t.startsWith("arc ")) t = "arc";
        else
        if (t.equals("ac")) t = "arc";
        else if (t.indexOf("arctan") >= 0) t = "arc";
        else if (t.equals("arc")) t = "arc4";
        String id = PROJECT_TYPE_PREFIX + t + (isLibrary?PROJECT_TYPE_LIB_SUFFIX:PROJECT_TYPE_EXE_SUFFIX);
        return ManagedBuildManager.getProjectType(id);
    }
 
    /**
     * Return the project type corresponding to a codewright project (e.g., "ac",
     * "arc", "arm", etc.).
     * @return the project type or <code>null</code> if the target ID is
     * not recognized.
     */
    public static IProjectType getProjectType(ICodewrightProject project){
        return getProjectType(project.getTarget(), project.isLibrary());
    }
    
    public IProjectType getProjectType(){
        ICodewrightProject[] projects = getSelectedProjects();
        if (projects.length == 0) return null;
        return getProjectType(projects[0]);
    }

}
