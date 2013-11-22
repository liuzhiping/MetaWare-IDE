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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;

/**
 * Logically the Launch delegate for CMPD should be in com.arc.cdt.debug.seecode.core
 * plugin. But it requires that the UI register callbacks with it so that the
 * engine can update menus, etc. To make sure the UI is properly started before
 * the "core" plugin, we put the launch delegate here.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */

public class CMPDLaunchDelegate implements ILaunchConfigurationDelegate2 {
    private ILaunchConfigurationDelegate2 delegate = new com.arc.cdt.debug.seecode.internal.core.launch.CMPDLaunchDelegate();

    @Override
    public boolean buildForLaunch (ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
        throws CoreException {
        return delegate.buildForLaunch(configuration, mode, monitor);
    }

    @Override
    public boolean finalLaunchCheck (ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
        throws CoreException {
        return delegate.finalLaunchCheck(configuration, mode, monitor);
    }

    @Override
    public ILaunch getLaunch (ILaunchConfiguration configuration, String mode) throws CoreException {
        return delegate.getLaunch(configuration, mode);
    }

    @Override
    public void launch (ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
        throws CoreException {
        delegate.launch(configuration, mode, launch, monitor);
    }

    @Override
    public boolean preLaunchCheck (ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
        throws CoreException {
        return delegate.preLaunchCheck(configuration, mode, monitor);
    } 

}
