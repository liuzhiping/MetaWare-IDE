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
package com.arc.cdt.debug.seecode.internal.ui;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;


public class LaunchCMPDTabGroup extends AbstractLaunchConfigurationTabGroup {
        
        /* (non-Javadoc)
         * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#createTabs(org.eclipse.debug.ui.ILaunchConfigurationDialog, java.lang.String)
         */
        @Override
        public void createTabs(ILaunchConfigurationDialog dialog, String mode)  {
            ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
                new CMPDDebuggerTab(),
                new CMPDAdditionalSettingsTab(),
                new EnvironmentTab(),
                new SourceLookupTab(),
                new CommonTab() 
            };
            setTabs(tabs);
        }
        
    }

