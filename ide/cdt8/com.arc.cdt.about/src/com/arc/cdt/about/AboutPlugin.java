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
package com.arc.cdt.about;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class AboutPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.arc.cdt.about";

	// The shared instance
	private static AboutPlugin plugin;
	
	/**
	 * The constructor
	 */
	public AboutPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
    public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
    public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static AboutPlugin getDefault() {
		return plugin;
	}
	
    public static void log(IStatus status) {
        getDefault().getLog().log(status);
    }

    public static void log(String msg, Throwable e) {
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

    public static IStatus makeErrorStatus(String msg, Throwable t) {
        return new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, msg, t);
    }



}
