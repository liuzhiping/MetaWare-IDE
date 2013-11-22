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
package com.arc.cdt.importer;

import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Plugin that is responsible for importing old MetaDeveloer 1
 * projects.
 */
public class ImporterPlugin extends AbstractUIPlugin {
    public static final String PLUGIN_ID = "com.arc.seecode.importer";
	//The shared instance.
	private static ImporterPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	
	/**
	 * The constructor.
	 */
	public ImporterPlugin() {
		super();
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
    @Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
    @Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
		resourceBundle = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static ImporterPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = ImporterPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}
    
    public static IStatus makeErrorStatus(String msg, Throwable t){
        return new Status(IStatus.ERROR,PLUGIN_ID,IStatus.ERROR,msg,t);
    }
    public static IStatus makeErrorStatus(Throwable t){
        return makeErrorStatus(t.getMessage(),t);
    }
    public static IStatus makeErrorStatus(String msg){
        return makeErrorStatus(msg,null);
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
    
    public static void log(IStatus status) {
        getDefault().getLog().log(status);
    }
    

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		try {
			if (resourceBundle == null)
				resourceBundle = ResourceBundle.getBundle("com.arc.cdt.importer.ImporterPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		return resourceBundle;
	}
    
    /**
     * Returns the image descriptor with the given relative path.
     */
    public ImageDescriptor getImageDescriptor (String relativePath) {
        URL url = FileLocator.find(getBundle(),new Path(relativePath),null);
        return url != null?ImageDescriptor.createFromURL(url):null;

    }
}
