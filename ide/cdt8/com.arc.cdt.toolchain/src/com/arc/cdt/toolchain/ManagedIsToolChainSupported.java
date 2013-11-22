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
package com.arc.cdt.toolchain;


import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IManagedIsToolChainSupported;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;


public class ManagedIsToolChainSupported implements IManagedIsToolChainSupported {

    //8.1_REQUIRED: org.osgi.framework.Version;
    @Override
    public boolean isSupported (IToolChain toolChain, org.osgi.framework.Version version, String instance) {
        ITool[] tools = toolChain.getTools();
        for (ITool tool : tools) {
            String extensions[] = tool.getAllOutputExtensions();
            List<String> extList = Arrays.asList(extensions);
            if (extList.contains("o") || extList.contains("obj")) {
                // We assume this tool is the compiler if its output
                // is .o or .obj file.
                // If the compiler doesn't exist in the search path,
                // then we don't support the tool.
                String cmd = tool.getToolCommand();
                if (cmd != null && cmd.length() > 0) {
                    if (!CommandInfo.commandExists(cmd))
                        return false;
                }
            }
        }
        return true;
    }
    

 
}
