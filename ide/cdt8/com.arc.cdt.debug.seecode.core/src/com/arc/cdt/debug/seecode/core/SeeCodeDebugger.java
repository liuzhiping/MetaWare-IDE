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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.debug.core.ICDIDebugger2;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

import com.arc.cdt.debug.seecode.internal.core.cdi.Session;
import com.arc.mw.util.Toggle;
import com.arc.seecode.plugin.EnginePlugin;

/**
 * Factory for creating SeeCode debug sessions.
 * <P>
 * Logially, this should be the class implements the CDebugger extension, but
 * due to problems that we documented in {@link com.arc.cdt.debug.seecode.ui.SeeCodeDebugger},
 * we have the UI plugin instantiate this class.
 * <P>
 * TODO: how do we model and accomodate CMPD? It appears that the infrastructure
 * assumes one process per "session".
 * 
 * @author David Pickens
 */
public class SeeCodeDebugger implements ICDIDebugger2 {

    public static final String ENGINE_PLUGIN_ID = "com.arc.seecode.engine";
    
    public SeeCodeDebugger(){
    }
 
    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.ICDIDebugger#createDebuggerSession(org.eclipse.debug.core.ILaunch, org.eclipse.cdt.core.IBinaryParser.IBinaryExecutable, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public ICDISession createSession(ILaunch launch, File exe, IProgressMonitor monitor) throws CoreException {
        // Set the toggles from .options file, as
        // cached by PDE. Also return options to be sent
        // to engine via an array of "-X..." arguments.
        List<String> serverOptions = new ArrayList<String>();
        ILaunchConfiguration config = launch.getLaunchConfiguration();
        setToggles(config, serverOptions);
        return new Session(launch, exe, serverOptions,monitor);
    }

//    /**
//     * This is the key under which PDE stores the ".options". It is not part of
//     * a public API, so if it ever changes, we'll need to fix it.
//     */
//    private static final String TRACING_OPTIONS = "tracingOptions";

    /**
     * Read the .options file and extract toggle settings. Also, each toggle,
     * foo, that is prefixed with "/server/" are to be passed to the engine
     * process as "-Xfoo".
     * 
     * @param config the associated launch configuration.
     * @param serverOptions list of server argument strings to be filled in.
     *  
     */
    private static void setToggles(ILaunchConfiguration config,
            List<String> serverOptions) {
        Plugin enginePlugin = EnginePlugin.getDefault();
        if (enginePlugin == null) {
            SeeCodePlugin.log("Can't find engine plugin");
            return;
        }
        // The API provides no way to enumerate all options.
        // So, we resort to re-reading the .options files
        // to get the keys, and then call Platform.getDebugOption(key)
        // to get the latest value.
        Properties props = null;

        // need some sort of message here.
        try {
            InputStream input = FileLocator.openStream(enginePlugin.getBundle(),new Path(".options"),false);
            props = new Properties();
            props.load(input);
            input.close();

        } catch (IOException e) {
            SeeCodePlugin.log("Can't read engine's .options", e);
        }
        if (props != null) {
            Iterator<Object> eachOption = props.keySet().iterator();
            while (eachOption.hasNext()) {
                String key = (String) eachOption.next();
                if (key.startsWith(ENGINE_PLUGIN_ID + "/")) {
                    String toggle = key
                            .substring(ENGINE_PLUGIN_ID.length() + 1);
                    if (toggle.startsWith("server/")) {
                        if (isTrue(Platform.getDebugOption(key))) {
                            serverOptions.add("-X" + toggle.substring(7));                           
                        }
                    } else {
                        if (toggle.startsWith("client/"))
                                toggle = toggle.substring(7);
                        Toggle
                                .set(toggle, isTrue(Platform
                                        .getDebugOption(key)));
                    }
                }
            }
        }
    }

    private static boolean isTrue(String value) {
        return value != null
                && (value.equalsIgnoreCase("true") || value.equals("1"));
    }


    @Override
    public ICDISession createDebuggerSession (ILaunch launch, IBinaryObject exe, IProgressMonitor monitor) throws CoreException {
        throw new IllegalStateException("Obsolete method called");
    }


}
