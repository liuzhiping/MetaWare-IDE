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
package com.arc.intro;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

import com.arc.cdt.toolchain.ToolchainPlugin;

/**
 * This action is invoked when the user selects a sample project to import.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class ImportSampleProjectAction implements IIntroAction, IOverwriteQuery {

    private transient Shell fShell;

    public void run (IIntroSite site, Properties params) {
        fShell = site.getWorkbenchWindow().getShell();
        String remotePath = params.getProperty("path");
        if (remotePath == null) {
            error("Missing \"path\" parameter in action link");
            return;
        }
        final File remoteDir = new File(remotePath);
        if (!remoteDir.isDirectory()) {
            error(remotePath + " is not a directory");
            return;
        }
        if (!new File(remoteDir, ".project").exists()) {
            error(remotePath + " is not a project");
            return;
        }
        
        WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
            @Override
            protected void execute(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {
                try {
                    monitor.beginTask("", 2); //$NON-NLS-1$
                    if (monitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }
                    try {
                        importProject(remoteDir, monitor);
                    }
                    catch (CoreException e) {
                        throw new InvocationTargetException(e);
                    }
                } finally {
                    monitor.done();
                }
            }
        };
        // run the new project creation operation
        try {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(true, true, op);
        } catch (InterruptedException e) {
            return;
        } catch (InvocationTargetException e) {
            // one of the steps resulted in a core exception
            Throwable t = e.getTargetException();
            IStatus status;
            if (t instanceof CoreException) {
                status = ((CoreException) t).getStatus();
            } else {
                status = new Status(IStatus.ERROR,
                        ToolchainPlugin.getUniqueIdentifier(), 1, "Internal exception", t);
            }
            ErrorDialog.openError(getShell(), "Internal error", null, status);
            return;
        }
    }

    private Shell getShell () {
        return fShell;
    }
    
    @SuppressWarnings("unchecked")
    private void importProject (File remoteDir, IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProjectDescription sourceProjectDesc = workspace.loadProjectDescription(new Path(remoteDir.getPath() + "/.project"));
        String projectName = sourceProjectDesc.getName();
        IProject project = workspace.getRoot().getProject(projectName);
        if (project.exists()) {
            if (promptForDelete(project)) {
                if (new File(remoteDir,projectName + ".launch").exists()){
                    // Delete any existing launches
                    for (ILaunchConfiguration config: DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations()){
                        if (projectName.equals(config.getName())){
                            config.delete();
                        }
                    }
                }
                project.delete(true, true, new SubProgressMonitor(monitor, 1));
            }
            else
                return;
        }
        else
            monitor.worked(1);
        IProjectDescription desc = workspace.newProjectDescription(projectName);
        desc.setBuildSpec(sourceProjectDesc.getBuildSpec());
        desc.setComment(sourceProjectDesc.getComment());
        desc.setDynamicReferences(sourceProjectDesc.getDynamicReferences());
        desc.setNatureIds(sourceProjectDesc.getNatureIds());
        //desc.setReferencedProjects(sourceProjectDesc.getReferencedProjects());

        try {
            monitor.beginTask("Creating project " + projectName, 100);
            project.create(desc, new SubProgressMonitor(monitor, 30));
            project.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(monitor, 70));

            List<File> filesToImport = FileSystemStructureProvider.INSTANCE.getChildren(remoteDir);
            ImportOperation operation = new ImportOperation(project.getFullPath(), remoteDir,
                FileSystemStructureProvider.INSTANCE, this, filesToImport);
            operation.setContext(getShell());
            operation.setOverwriteResources(true); // need to overwrite
            // .project, .classpath
            // files
            operation.setCreateContainerStructure(false);
            operation.run(monitor);
            // Close the intro.
            getShell().getDisplay().syncExec(new Runnable(){

                public void run () {
                    IIntroPart introPart = PlatformUI.getWorkbench().getIntroManager().getIntro();
                    if (introPart != null) {
                        PlatformUI.getWorkbench().getIntroManager().closeIntro(introPart);
                    }                  
                }});           
        }
        finally {
            monitor.done();
        }
    }
    
    private static void error(String msg){
        ToolchainPlugin.log(new IllegalStateException(msg));
    }
    
    private boolean promptForDelete(final IProject p){
       final  boolean result[] = new boolean[1];
        getShell().getDisplay().syncExec(new Runnable(){

            public void run () {
                result[0] = MessageDialog.openConfirm(getShell(),"Confirmation","Overwrite existing project " + p.getName() +"?");
                
            }});
        return result[0];
    }

    public String queryOverwrite (String pathString) {
        // Shouldn't be called since we prompt to delete the project previously.
        return IOverwriteQuery.NO;
    }

}
