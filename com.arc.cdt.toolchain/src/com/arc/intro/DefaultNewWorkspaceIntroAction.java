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

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;


/**
 * This class is instantiated when the user clicks the "New Workspace" or "Open Workspace" link on the Welcome page.
 * @author Hurai Rody
 * @currentOwner <a href="mailto:hurair@arc.com">hurair</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class DefaultNewWorkspaceIntroAction implements IIntroAction {

    public DefaultNewWorkspaceIntroAction() {
    }

    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param site
     * @param params
     */
    public void run (IIntroSite site, Properties params) {

        Runnable r = new Runnable() {

            public void run () {
                IIntroPart introPart = PlatformUI.getWorkbench().getIntroManager().getIntro();
                PlatformUI.getWorkbench().getIntroManager().closeIntro(introPart);

                IWorkbenchAction a = IDEActionFactory.OPEN_WORKSPACE.create(PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow());
                a.run();
            }
        };

        Shell currentShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        currentShell.getDisplay().asyncExec(r);
    }
}
