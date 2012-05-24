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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;

import com.arc.cdt.debug.seecode.ui.UISeeCodePlugin;
import com.arc.cdt.importer.ImporterPlugin;
import com.arc.cdt.importer.core.Factory;
import com.arc.cdt.importer.core.ICodewrightProject;
import com.arc.cdt.importer.core.IProjectCreator;
import com.arc.cdt.importer.core.PSPException;


/**
 * The wizard for importing old CodeWright-based MetaDeveloper 1 projects.
 * It is invoked as an extension to "org.eclipse.ui.importWizards".
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class CodeWrightImportWizard extends Wizard implements IImportWizard {


    //private IWorkbench mWorkbench = null;
    private SelectProjectSpaceWizardPage mSelectProjectSpaceWizardPage = null;
    private ProjectSelectionWizardPage mProjectTransferWizardPage;
    private AccessModel mAccess;
    private NewProjectCreatorPage mNewProjectCreatorPage;
    private Map<ICodewrightProject,List<String>> mConfigIDForProject = 
        new HashMap<ICodewrightProject,List<String>>();

    public CodeWrightImportWizard() {
        super();
        setNeedsProgressMonitor(true);
    }
   
  
    AccessModel getAccessModel(){
        if (mAccess == null){
            mAccess = new AccessModel(getContainer());
        }
        return mAccess;
    }
    
    private static boolean hasCPlusPlusProject(ICodewrightProject projects[]){
        for (ICodewrightProject p: projects){
            if (p.isCPlusPlus()) return true;
        }
        return false;
    }

    private Collection<File> computeSourceFiles(ICodewrightProject projects[]){
        Set<File> result = new HashSet<File>();
        for (ICodewrightProject p: projects){
            File[] files = p.getSourceFiles();
            for (File f: files){
                result.add(f);
            }          
        }
        return result;
    }
    
    @Override
    public boolean performFinish () {
        IRunnableWithProgress runnable = new IRunnableWithProgress(){

            public void run (IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                IProjectCreator creator = Factory.createProjectCreator();
 
                if (monitor == null){
                    monitor = new NullProgressMonitor();
                }

                try {
                    monitor.beginTask("Importing project space",3);
                    boolean isCplusplus = hasCPlusPlusProject(mAccess.getSelectedProjects());
                    IProjectType type = mAccess.getProjectType();
                    ICProject cproject = creator.createProject(mAccess.getNewProjectLocation(),isCplusplus,
                            mAccess.getNewProjectName(),type,monitor);
                    importOrLinkSourceFiles(creator,cproject,monitor);
                    createConfigurations(creator,cproject,monitor); 
                    ManagedBuildManager.saveBuildInfo(cproject.getProject(), true);
                    exportSeeCodeOptions(cproject.getProject());
                }
                catch (Exception e) {
                    throw new InvocationTargetException(e);
                }  
                finally {
                    monitor.done();
                }
            }
        };
        runnable = new WorkspaceModifyDelegatingOperation(runnable);
            
        try {
            this.getContainer().run(false,false,runnable);
        }
        catch (InvocationTargetException e) {
            final Throwable t = e.getTargetException();
            ImporterPlugin.log(t);
            getShell().getDisplay().syncExec(new Runnable() {

                public void run () {
                    MessageBox box = new MessageBox(getShell(), SWT.OK | SWT.ICON_ERROR);
                    box.setMessage("Exception occurred during operation: \n"
                            + t.getMessage()
                            + "\nSee the error log for details."
                            + "\nThe construction of the new project did not complete.");
                    box.setText("Error");
                    box.open();
                }
            });
            
        }
        catch (InterruptedException e) {
            //Shouldn't get here.
        }
        
        return true;
    }
    
    /**
     * Export the seecode options in a persistent way so that
     * we can initialize new launch configurations appropriately.
     */
    private void exportSeeCodeOptions(IProject project){
        ICodewrightProject projects[] = mAccess.getSelectedProjects();
        for (ICodewrightProject p: projects){
            Map<String,String> seeCodeOptions = p.getSeeCodeOptions();
            if (seeCodeOptions.size() > 0){
                List<String> ids = this.mConfigIDForProject.get(p);
                for (String id: ids){
                   UISeeCodePlugin.getDefault().setDefaultSeeCodeOptions(project,id,seeCodeOptions);
                }
            }
        }
    }
    
    private void importOrLinkSourceFiles (IProjectCreator creator, ICProject cproject, IProgressMonitor monitor)
            throws IOException, PSPException, CoreException {
        ICodewrightProject projects[] = mAccess.getSelectedProjects();
        // If one project, then dedicate the new project to that project.
        // If multiple projects, then create a configuration for each project.
        if (projects.length == 0)
            return; // Shouldn't happen

        // For the benefit of the progress monitor, use the total number
        // of source files as the total progress count.
        Collection<File> sourceFiles = computeSourceFiles(projects);
        int workCount = sourceFiles.size();
        monitor = new SubProgressMonitor(monitor, 1);
        monitor.beginTask("Importing old project space", workCount);
        try {

            File originalRoot = mAccess.getProjectSpace().getLocation();

            IProject project = cproject.getProject();
            if (!mAccess.isCreateInPlace()) {
                for (File f : sourceFiles) {
                    File relativeTo = computeRelativeDirectory(originalRoot, f);
                    if (!mAccess.isLinkToSourceFiles()) {

                        monitor.subTask("Importing " + f.toString());
                        creator.importSourceFile(project, f, relativeTo, monitor);
                    }
                    else {
                        monitor.subTask("Linking to " + f.toString());
                        creator.linkSourceFile(project, f, relativeTo, monitor);
                    }
                }
            }
            else {
                // If there are any source that are not in the root,
                // then create links.
                monitor.subTask("Checking for files outside of project space");
                for (File f : sourceFiles) {
                    File relativeTo = computeRelativeDirectory(originalRoot, f);
                    if (relativeTo != originalRoot) {
                        monitor.subTask("Linking " + f.toString());
                        creator.linkSourceFile(project, f, relativeTo, monitor);
                    }
                    else {
                        creator.useInplaceSourceFile(project,f,relativeTo);
                        monitor.worked(1);
                    }
                }
                monitor.subTask("Refreshing inplace project");
                project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
            }
        }
        finally {
            monitor.done();
        }
    }
    
    /**
     * Create the configurations in the new project. If there is just
     * one original project in the old project space, then create the
     * traditional "Debug" and "Release" configurations.
     * <P>
     * If there are multiple projects, then create "Foo_Debug" and "Foo_Release"
     * projects for each Codewright project Foo.
     * @param creator the conveniencs object for creating the configurations.
     * @param monitor progress monitor.
     * @throws CoreException 
     * @throws BuildException 
     */
    private void createConfigurations(IProjectCreator creator, ICProject cproject, IProgressMonitor monitor) throws CoreException, BuildException{
        monitor.subTask("Creating configurations");
        ICodewrightProject projects[] = mAccess.getSelectedProjects();
        if (projects.length == 0) 
            return; // shouldn't happen
        IProjectType type = AccessModel.getProjectType(projects[0]);
        if (type == null)
            throw new CoreException(ImporterPlugin.makeErrorStatus("Unknown project target type: " + projects[0].getTarget()));
        IConfiguration configs[] = type.getConfigurations();
        for (IConfiguration config: configs){
            for (ICodewrightProject p: projects){
                String name = projects.length == 1? config.getName():
                    p.getName() + "_" + config.getName();
                creator.createConfiguration(cproject,name,type,p,config);
                List<String> list = mConfigIDForProject.get(p);
                if (list == null){
                    list = new ArrayList<String>(2);
                    mConfigIDForProject.put(p,list);
                }
                list.add(name);
            }
        }
        monitor.worked(1);
    }
    
    /**
     * Compute the directory that a file in the project space is to be
     * considered "relative to". This determines the relative path
     * that will be copied into the new project space.
     * @param originalRoot
     * @param f
     */
    private static File computeRelativeDirectory (File originalRoot, File f) {
        File relativeTo = originalRoot;
        if (!isWithinDirectory(f,originalRoot)){
            relativeTo = f.getParentFile();
            if (relativeTo != null && relativeTo.getParentFile() != null)
                relativeTo = relativeTo.getParentFile();
        }
        return relativeTo;
    }
    /**
     * Return true if a file is transitively located within the given
     * directory.
     * @param f the file to test.
     * @param directory the directory.
     * @return true if the file is within the directory or subdirectory.
     */
    private static boolean isWithinDirectory(File f, File directory){
        File parent = f.getParentFile();
        while (parent != null && !parent.equals(directory)){
            parent = parent.getParentFile();
        }
        return parent != null;
    }

   
    public void init (IWorkbench workbench, IStructuredSelection selection) {
        //this.mWorkbench = workbench;

        setWindowTitle("Old MetaDeveloper Project-space Importer"); //$NON-NLS-1$
        setDefaultPageImageDescriptor(ImporterPlugin.getDefault().getImageDescriptor("icons/arc.gif"));//$NON-NLS-1$
        setNeedsProgressMonitor(true);
    }
    
    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     */
    @Override
    public void addPages () {
        super.addPages();
        mSelectProjectSpaceWizardPage = new SelectProjectSpaceWizardPage();
        mProjectTransferWizardPage = new ProjectSelectionWizardPage();
        mNewProjectCreatorPage = new NewProjectCreatorPage();
        addPage(mSelectProjectSpaceWizardPage);
        addPage(mProjectTransferWizardPage);
        addPage(mNewProjectCreatorPage);
    }

}
