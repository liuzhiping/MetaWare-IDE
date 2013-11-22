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
package com.arc.cdt.debug.seecode.core.launch;


import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

import com.arc.cdt.debug.seecode.internal.core.launch.CMPDInfo;
import com.arc.cdt.debug.seecode.internal.core.launch.EngineConfiguration;
import com.arc.cdt.debug.seecode.internal.core.launch.ServerLauncher;
import com.arc.seecode.engine.config.ConfigException;


/**
 * Factory methods for creating objects related to the launching of the MetaWare debugger.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class ServerLauncherFactory {

    /**
     * Return a builder object for launching the MetaWare debugger in its own process.
     * @return a builder object for launching the MetaWare debugger in its own process.
     */
    public static IServerLauncher createServerLauncher () {
        return new ServerLauncher();
    }

    /**
     * Given the launch configuration of a CMPD session, extract the per-process CMPD information.
     * @param config
     * @return the per-process CMPD information of a CMPD debugger session. Or <code>null</code> if this is not a CMPD
     * session.
     * @throws CoreException
     */
    public static ICMPDInfo getCMPDInfo (ILaunchConfiguration config) throws CoreException {
        return CMPDInfo.extractCMPDInfo(config);
    }

    /**
     * Given the so-called "swahili" arguments to be passed to the engine (for a non-CMPD session), create the necessary
     * temp file for configuring the engine. Return a string that is used to read the temp file.
     * <P>
     * The resulting string typically looks something like "@tmpfile".
     * @param swahiliArgs the arguments to be passed to the engine as generated from the Launch Configuration dialog.
     * @param console where to emit messages as the debugger driver is invoked to compute engine configuration.
     * @param env the OS environment; if <code>null</code> the IDE's environment will be used.
     * @return the argument string to be passed to the engine to have it configure itself.
     * @throws CoreException
     */
    public static String createEngineConfiguration (String[] swahiliArgs, IConsole console, String env[])
        throws CoreException, ConfigException {
        return EngineConfiguration.createArgument(swahiliArgs, console, env);
    }

    /**
     * Given a CMPD description of a session, create the necessary temp file(s) be calling into the driver. Return an
     * argument string to be passed to the engine to read the generated temp files. The argument string typically looks
     * something like "@tmpfile".
     * @param cmpdInfo a CMPD session description.
     * @param console where to emit messages as the debugger driver is invoked to compute engine configuration.
     * @param env the OS environment; if <code>null</code> the IDE's environment will be used.
     * @return the argument string to be passed to the engine to have it configure itself.
     */
    public static String createEngineConfiguration (ICMPDInfo cmpdInfo, IConsole console, String env[])
        throws CoreException {
        return EngineConfiguration.createCMPDArgument(cmpdInfo, console, env);
    }
}
