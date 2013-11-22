package com.arc.cdt.toolchain.internal.ui.bcf;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

public class ArcBcfWizardAction implements IRunnableWithProgress {

    /**
     * This is called for new project wizard to apply BCF file info:
     */
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException { // TODO Auto-generated method stub
        File path = BcfUtils.getSelectedBcfPath();
        // Properties catProps = readCATProperties(path);
        IProject project = BcfUtils.getSelectedProject();
        if (project == null)
            return; // Shouldn't happen

        try {
            BcfUtils.applySettingsFileToProject(path, project, true);
            ManagedBuildManager.saveBuildInfo(project, true); 
        } catch (Exception ex) {
            BcfUtils.displayBCFError(ex, null);
        }
    }

}
