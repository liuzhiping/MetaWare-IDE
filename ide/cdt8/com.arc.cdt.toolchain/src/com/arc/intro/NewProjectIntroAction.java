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
import org.eclipse.ui.actions.NewProjectAction;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;


/**
 * This class opens the "New Project..." wizard dialog that's built into Eclipse.
 * @author hurair
 * @currentOwner <a href="mailto:hurair@arc.com">hurair</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class NewProjectIntroAction implements IIntroAction {

    /**
     * The constructor.
     */
    public NewProjectIntroAction() {
    }

    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param site
     * @param params
     */
    public void run (IIntroSite site, Properties params) {

        Runnable r = new Runnable() {

            public void run () {
                // Close the intro.
                IIntroPart introPart = PlatformUI.getWorkbench().getIntroManager().getIntro();
                if (introPart != null) {
                    PlatformUI.getWorkbench().getIntroManager().closeIntro(introPart);
                }

                // Display the new project wizard dialog.
                NewProjectAction a = new NewProjectAction(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
                a.run();
            }
        };

        Shell currentShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        currentShell.getDisplay().asyncExec(r);
    }
}
