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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.arc.cdt.toolchain.ToolchainPlugin;


/**
 * This class is called when Eclipse is first loaded. We use it to look for a properties file that specifies whether to
 * display the "Welcome Page" and possibly other things in the future.
 * @author Hurai Rody
 * @currentOwner <a href="mailto:hurair@arc.com">hurair</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
@SuppressWarnings("restriction")
public class LoadedAtStartup implements IStartup {

    public final static String SHOW_WELCOME_PAGE_PROPERTY = "SHOW_WELCOME_PAGE";
    
    public final static String INVOKING_SAMPLE_WORKSPACE = "INVOKING_SAMPLE_WORKSPACE";

    public final static String STARTUP_CONFIG_FILE = ".metadata" + File.separator + "config.ini";

    private final static String FIRST_TIME_PROPERTY = "INITIALIZED";

    public final static String SHOW_WORKSPACE_SELECTION_DIALOG_PROPERTY = "SHOW_WORKSPACE_SELECTION_DIALOG";

    /**
     * Here we look for the FIRST_TIME_PROPERTY field in the preferences file and if it's set to "1", we set it to "2"
     * and then we set the SHOW_WORKSPACE_SELECTION_DIALOG property to true. This way all future runs of the IDE will
     * cause the workspace selection dialog to be displayed.
     * <P>
     * This property is initially set to 1 by the customized Eclipse driver.
     * 
     * CORRECTION: as of 8.3.11, the driver no longer modifies the preferences file.
     * Instead, if it detects a first-time user, it invokes the IDE with "-data $USER/mide/workspace".
     * But, in case he is using an older driver (mide.exe), we go ahead and test for
     * it here.
     * 
     * In 8.4 this method can be deleted!
     */
    private void setWorkspacePrefsProperties () {
        Preferences node = new ConfigurationScope().getNode(IDEWorkbenchPlugin.IDE_WORKBENCH);

        String val = node.get(FIRST_TIME_PROPERTY,"0");
        boolean showSelectionDialogNextTime = val.equals("1");
        if (node.getBoolean(INVOKING_SAMPLE_WORKSPACE,false)){
        	node.remove(INVOKING_SAMPLE_WORKSPACE);
        	showSelectionDialogNextTime = true;
        }
        if (showSelectionDialogNextTime) {
            node.putBoolean(IDE.Preferences.SHOW_WORKSPACE_SELECTION_DIALOG, true);
            node.remove(FIRST_TIME_PROPERTY);
            try {
                node.flush();
            }
            catch (BackingStoreException e) {
                // do nothing
            }
        }      
    }
    
    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     */
    public void earlyStartup () {
        // Confirm that JRE is 1.6 or later.
        final String version = System.getProperty("java.specification.version");
        if (version == null || version.compareTo("1.6") < 0) {
            Display d = PlatformUI.getWorkbench().getDisplay();
            d.asyncExec(new Runnable() {

                public void run () {
                    ErrorDialog.openError(null, "Wrong JRE!", null, new Status(IStatus.ERROR, ToolchainPlugin
                        .getUniqueIdentifier(), IStatus.ERROR, "JRE is version " +
                        version +
                        "; it must be version 1.6 or later. Aborting...", null));
                    PlatformUI.getWorkbench().close();
                }
            });
        }
        //
        // If this is a Restart operation, then we passed a secret property
        // via "-Dcom.arc.RESTART=1" to let us know. If the Welcome screen popped
        // up, then dismiss it. CR96249.
        //
        String v = System.getProperty(WorkspaceRestarter.RESTART_ID);
        if ("1".equals(v)) {
            // We just restarted on possibly new Workspace. Don't show Welcome page.
            PlatformUI.getPreferenceStore().setValue(IWorkbenchPreferenceConstants.SHOW_INTRO, false);
            IWorkbench workbench = PlatformUI.getWorkbench();
            if (workbench != null) {
                IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
                for (IWorkbenchWindow w : windows) {
                    // Maybe it is already showing. Dismiss it if it is.
                    for (final IWorkbenchPage page : w.getPages()) {
                        w.getShell().getDisplay().asyncExec(new Runnable() {
                            public void run () {
                                IViewReference vr = page.findViewReference("org.eclipse.ui.internal.introview");
                                if (vr != null)
                                    page.hideView(vr);
                            }
                        });
                    }
                }
            }
        }
        // If we see a property that says it's the first time this workspace is loaded, then
        // make sure the change workspace dialog is displayed in the future.
        // NOTE: see comment above "CORRECTION". The driver no longer modifies
        // the startup preference file. But we check in case the user is using
        // a back-level driver.
        setWorkspacePrefsProperties();
    }
}
