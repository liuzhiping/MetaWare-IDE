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
package com.arc.cdt.debug.seecode.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.debug.core.IRunCommand;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

import com.arc.cdt.debug.seecode.options.ConfigurationException;
import com.arc.cdt.debug.seecode.options.SeeCodeOptions;
import com.arc.seecode.engine.config.ArgProcessorFactory;


/**
 * Referenced from extension manifest for this debugger.
 * Called to create a command to invoke an executable that is simulated or
 * needs to run on an external board.
 * <P>
 * Note that the {@link IRunCommand} interface was added to CDT by ARC as a customization so
 * as to be able to invoke executables that require special setup.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class RunCommand implements IRunCommand {

    /**
     * Create the command by which we invoke a particular executable.
     * @param config
     * @param exePath
     * @param arguments
     * @return the command to run an application via the debugger.
     * @throws CoreException
     */
    @Override
    public String[] createCommand (ILaunchConfiguration config, String exePath, String[] arguments) throws CoreException{
        String target = config.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_TARGET_CPU, (String)null);
        if (target == null) {
            target = SeeCodeOptions.getTargetCpuName(config);
            if (target == null)
                throw new CoreException(SeeCodePlugin.makeErrorStatus("CPU not set for launch"));
        }
        String scexe = ArgProcessorFactory.getSCEXE(target);
        if (scexe == null) {
            throw new CoreException(SeeCodePlugin.makeErrorStatus("Can't compute SC command from " + target));
        }
        List<String> list = new ArrayList<String>(arguments.length+20);
        list.add(scexe);
        try {
            list.addAll(Arrays.asList(SeeCodeOptions.computeArguments(config)));
        }
        catch (ConfigurationException e) {
            throw new CoreException(SeeCodePlugin.makeErrorStatus(e.getMessage(),e));
        }
        list.add("-nooptions");
        list.add("-run");
        list.add(exePath);
        if (arguments.length > 0){
            list.add("--");
            list.addAll(Arrays.asList(arguments));
        }
        return list.toArray(new String[list.size()]);         
    }

}
