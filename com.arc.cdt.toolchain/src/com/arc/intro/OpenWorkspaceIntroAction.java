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


import java.util.Properties;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;


/**
 * This class is instantiated when the user clicks the "Open Workspace" link on the Welcome page.
 * @author Hurai Rody
 * @currentOwner <a href="mailto:hurair@arc.com">hurair</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class OpenWorkspaceIntroAction implements IIntroAction {

    public final static String SHOW_WELCOME_PAGE = "SHOW_WELCOME_PAGE";

    public OpenWorkspaceIntroAction() {
    }

    /**
     * Use the ChooseWorkspaceDialog to get the new workspace from the user.
     * @return a string naming the new workspace and null if cancel was selected
     */
    @SuppressWarnings("restriction")
    private String promptForWorkspace () {
        // get the current workspace as the default
        org.eclipse.ui.internal.ide.ChooseWorkspaceData data = new  org.eclipse.ui.internal.ide.ChooseWorkspaceData(Platform.getInstanceLocation().getURL());
        OpenWorkspaceDialog dialog = new OpenWorkspaceDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell(), data, true, false);
        dialog.prompt(true);

        // return null if the user changed their mind
        String selection = data.getSelection();
        if (selection == null)
            return null;

        // otherwise store the new selection and return the selection
        data.writePersistedData();
        return selection;
    }

    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param site
     * @param params
     */
    public void run (IIntroSite site, Properties params) {

        Runnable r = new Runnable() {

            public void run () {
                // This works, but we can do the same thing with the fewer lines of code below...
                String path = promptForWorkspace();
                if (path == null)
                    return;
                WorkspaceRestarter.restartInWorkspace(path);
            }
        };

        Shell currentShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        currentShell.getDisplay().asyncExec(r);
    }
}
