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
package com.arc.cdt.toolchain.arc;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFileInfo;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionApplicability;
import org.eclipse.cdt.managedbuilder.core.IToolChain;


public class ArcApplicabilityCalculator implements IOptionApplicability {
	
    // There is one instance of this class per option. But we want
    // to share the same enablement manager. So make it static.
    private static final ArcOptionEnablementManager EMGR = new ArcOptionEnablementManager();
    private  static IToolChain lastToolChain;
    
    public boolean isOptionUsedInCommandLine (IBuildObject configuration, IHoldsOptions holder, IOption option) {
        return isOptionEnabled(configuration,holder,option);       
    }
    
    private static IToolChain computeToolChain(IBuildObject configuration){
    	IToolChain tc = null;
    	if (configuration instanceof IFolderInfo){
    	    tc = ((IFolderInfo)configuration).getToolChain();
    	}
    	else if (configuration instanceof IConfiguration){
    	    tc = ((IConfiguration)configuration).getToolChain();
    	    
    	}  	
    	else if (configuration instanceof  IFileInfo){
    		//When user selects file, configuration object is ResourceConfiguration
    		//We should handle this case for getting ToolChain object to avoid the below CR
    		//CR:9000672325 Incorrect coreType and coreNum when viewing file properties
     		tc = ((IFileInfo) configuration).getParent().getToolChain();
    	}
    	return tc;
    }

    public boolean isOptionVisible (IBuildObject configuration, IHoldsOptions holder, IOption option) {
    	IToolChain tc = computeToolChain(configuration);
    	if (tc != null) {
    	    if (! ArcOptionEnablementManager.isApplicableToToolChain(option, tc) ) return false;
    	    String id = option.getBaseId();
    	  //   the ".cfg" and ".bcf" are only used to store settings files. Don't make them visible.
    	    if (id.endsWith(".cfg") || id.endsWith(".bcf")) return false;
    	}
        return true;
    }

    public boolean isOptionEnabled (IBuildObject configuration, IHoldsOptions holder, IOption option) {
        // Since there are no listeners on option changes,
        // we must resort to reading the states of all options!!!
    	IToolChain tc = computeToolChain(configuration);
    	if (tc == null) return true; // shouldn't happen
    	
        if (tc != lastToolChain) {
            lastToolChain = tc;
            EMGR.initialize(configuration);            
        }
        return EMGR.isEnabled(option.getBaseId());
    }   
}
