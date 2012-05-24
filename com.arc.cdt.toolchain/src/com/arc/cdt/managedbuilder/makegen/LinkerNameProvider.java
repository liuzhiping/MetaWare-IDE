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
package com.arc.cdt.managedbuilder.makegen;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedOutputNameProvider;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages;
import org.eclipse.cdt.managedbuilder.internal.macros.FileContextData;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;


@SuppressWarnings("restriction")
public class LinkerNameProvider implements IManagedOutputNameProvider {
    private final static IPath[] ZERO = new IPath[0];

    public IPath[] getOutputNames (ITool tool, IPath[] primaryInputNames) {
        IBuildObject toolParent = tool.getParent();
        IConfiguration config = null;
        // if the parent is a config then we're done
        if (toolParent instanceof IConfiguration)
            config = (IConfiguration) toolParent;
        else if (toolParent instanceof IToolChain) {
            // must be a toolchain
            config = ((IToolChain) toolParent).getParent();
        }

        else if (toolParent instanceof IResourceConfiguration) {
            config = ((IResourceConfiguration) toolParent).getParent();
        }
        else {
            // bad
            throw new AssertionError(
                    ManagedMakeMessages.getResourceString("GnuLinkOutputNameProvider.0")); //$NON-NLS-1$
        }

        if (config != null) {
            ITool linker = config.getTargetTool();
            if (linker != null) {
                IOption option = linker.getOptionBySuperClassId("com.arc.cdt.toolchain.linker.option.map");
                try {
                    if (option != null && option.getBooleanValue()) {
                        String s;
                        try {
                            s = ManagedBuildManager.getBuildMacroProvider().resolveValue(
                                "${BuildArtifactFileBaseName}.map",
                                ""," ",IBuildMacroProvider.CONTEXT_FILE,
                                    new FileContextData(
                                            null,
                                            null, option, tool));
                        }
                        catch (BuildMacroException e) {
                            return ZERO;
                        }
                        return new IPath[]{new Path(s) };
                    }
                }
                catch (BuildException e) {
                    //???
                }
            }
        }
        return ZERO;
    }
}
