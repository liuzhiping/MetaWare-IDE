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
package com.arc.seecode.plugin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

/**
 * We materialize a trivial plugin class for the engine because under 3.0, the
 * Platform.getPlugin(name) method has been deprecated. Therefore, we have no
 * way to retreive it programmatically unless we have a concrete class.
 * 
 * @author David Pickens
 */
public class EnginePlugin extends Plugin {

    public static final String PLUGIN_ID = "com.arc.seecode.engine";

    private static EnginePlugin sInstance = null;

    public static EnginePlugin getDefault() {
        return sInstance;
    }

    public EnginePlugin() {
        sInstance = this;
    }

    public static void log(IStatus status) {
        getDefault().getLog().log(status);
    }

    public static void log(String msg, Exception e) {
        log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, msg, e));
    }

    public static void log(String msg) {
        log(makeErrorStatus(msg));
    }

    public static void log(Throwable e) {
        log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, "Error", e)); //$NON-NLS-1$
    }

    public static IStatus makeErrorStatus(String msg) {
        return new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, msg, null);
    }

    // We now access the engine path by loooking for "scac" in the
    // search path. See SeeCodeOptions in com.arc.cdt.debug.seecode.core plugin
//    public String getEnginePath() {
//        String lib;
//        if (isWindows())
//            lib = "crout.dll";
//        else
//            lib = "libcrout.so";
//        try {
//            URL url = Platform.find(getBundle(),new Path(lib));
//            if (url == null){
//                log("Can't find fragment with SeeCode engine");
//                return lib;
//            }
//            return Platform.asLocalURL(url).getFile();
//        } catch (Exception e) {
//            log(e);
//            return lib;
//        }
//    }
}
