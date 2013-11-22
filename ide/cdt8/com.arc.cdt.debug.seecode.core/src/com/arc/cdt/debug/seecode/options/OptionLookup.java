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
package com.arc.cdt.debug.seecode.options;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;


/**
 * Utility function that locates the IOption option with a given name.
 * @author pickensd
 *
 */
class OptionLookup {
    /**
     * Lookup a tool option and return it if it exists. Returns
     * <code>null</code. otherwise.
     * @param name id of the option to look up.
     * @param config the configuration from which to extract it.
     * @return the corresponding option or <code>null</code>.
     */
     static IOption lookupOption(String name, IConfiguration config) {
        ITool tools[] = config.getTools();
        for (int i = 0; i < tools.length; i++) {
            IOption o = lookupOption(tools[i],name);
            if (o != null)
                return o;
        }
        return null;
    }
    
    private static IOption lookupOption(ITool tool, String name){
        IOption options[] = tool.getOptions();
        for (IOption opt:options){
            if (optionHasId(opt,name))
                return opt;
        }
        return null;
    }
    
    private static boolean optionHasId(IOption opt, String name){
        String id = opt.getId();
        if (name.equals(id)) return true;
        IOption superClass = opt.getSuperClass();
        if (superClass != null) return optionHasId(superClass,name);
        return false;
    }
}
