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
package com.arc.cdt.toolchain.arm;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionApplicability;


public class ArmApplicabilityCalculator implements IOptionApplicability {

    // There is one instance of this class per option. But we want
    // to share the same enablement manager. So make it static.
    private static final ArmOptionEnablementManager EMGR = new ArmOptionEnablementManager();
    private  static IBuildObject lastConfig;
    
    @Override
    public boolean isOptionUsedInCommandLine (IBuildObject configuration, IHoldsOptions holder, IOption option) {
        return isOptionEnabled(configuration,holder,option);       
    }

    @Override
    public boolean isOptionVisible (IBuildObject configuration, IHoldsOptions holder, IOption option) {
        return true;
    }

    @Override
    public boolean isOptionEnabled (IBuildObject configuration, IHoldsOptions holder, IOption option) {
        // Since there are no listeners on option changes,
        // we must resort to reading the states of all options!!!
        if (configuration != lastConfig) {
            lastConfig = configuration;
            EMGR.initialize(configuration);
        }
        return EMGR.isEnabled(option.getBaseId());
    }
    
}
