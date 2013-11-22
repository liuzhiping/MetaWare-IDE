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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleContext;

/**
 * The plugin for the toolchain.
 * 
 */
public class ToolchainPlugin extends Plugin {

    private static final String PLUGIN_ID = "com.arc.cdt.toolchain";

    static ToolchainPlugin sInstance = null;

    /**
     * Initialize plugin by setting the default command for discovering includes
     * and defines.
     * 
     */
    public ToolchainPlugin() {
        sInstance = this;
        
    }
    
    // Bundle ID; we access "plugin.properties"
    private static final String BUNDLE_ID = "com.arc.cdt.toolchain.ToolChain"; //$NON-NLS-1$
    //Resource bundle.
    private static ResourceBundle resourceBundle;

    static {
        try {
            resourceBundle = ResourceBundle.getBundle(BUNDLE_ID);
        } catch (MissingResourceException x) {
            resourceBundle = null;
        }
    }

    /**
     * Convenience method which returns the unique identifier of this plugin.
     */
    public static String getUniqueIdentifier() {
        if (getDefault() == null) {
            // If the default instance is not yet initialized,
            // return a static identifier. This identifier must
            // match the plugin id defined in plugin.xml
            return PLUGIN_ID;
        }
        return getDefault().getBundle().getSymbolicName();
    }

    /**
     * Returns the shared instance.
     */
    public static ToolchainPlugin getDefault() {
        return sInstance;
    }
    
    /**
     * Open a file in the "resources" directory of this plugin.
     * <P>
     * The makefile generator gets its template from there.
     * @param path a path relative to the resources directory.
     * @return a reader to the file.
     */
    public static InputStream openResource(String path) throws IOException{
        if (!path.startsWith("/")){
            path = "resources/" + path;
        }
        URL url = getDefault().getBundle().getEntry(path);
        return url.openStream();   
    }
    
    public static String getResourceString(String key) {
        try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException e) {
            return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
        } catch (NullPointerException e) {
            return "#" + key + "#"; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    public static String getProductName() {
        return "MetaWare IDE";
    }


    public static String getTheProductName() {
        return "The MetaWare IDE";
    }

    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param context
     * @throws Exception
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        processArguments();
    }
    
    private static ILaunchConfiguration findLaunchConfig(String name){
        try {
            for (ILaunchConfiguration config: DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations()){
                if (config.getName().equals(name)) return config;
            }
        }
        catch (CoreException e) {
            log(e);
        }
        return null;
    }
    
    private void processArguments(){
        //NOTE: current thread is assumed to be UI thread.
        // Look for special arguments that we support: 
        // -launch "launchName"
        String args[] = Platform.getApplicationArgs();
        for (int i = 0; i < args.length; i++){
            if ("-launch".equals(args[i]) && i+1 < args.length){
                final String launchName = args[i+1];
                ILaunchConfiguration config = findLaunchConfig(launchName);
                final IWorkbench wb = PlatformUI.getWorkbench();
                final IWorkbenchWindow activeWorkbenchWindow = wb.getActiveWorkbenchWindow();
                if (config == null) {                    
                    wb.getDisplay().asyncExec(new Runnable(){
                        @Override
                        public void run () {
                            String name = launchName;
                            if (name.indexOf(' ') >= 0) name = "\"" + launchName + "\"";
                            MessageDialog.openError(activeWorkbenchWindow.getShell(), "Command-line error", 
                                "The command line arguments:\n   -launch " + name + "\nfailed during processing."+
                                " No launch configuration exists with the\nspecified name. The arguments will be ignored.");                            
                        }});
                    
                }
                else {
                    try {
                        IPerspectiveRegistry preg = wb.getPerspectiveRegistry();
                        final IPerspectiveDescriptor p = preg.findPerspectiveWithId("org.eclipse.debug.ui.DebugPerspective");
                        IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
                        if (activePage != null) {
                            activePage.setPerspective(p);
                        } else{
                            activeWorkbenchWindow.addPageListener(new IPageListener(){

                                @Override
                                public void pageActivated (IWorkbenchPage page) {
                                    activeWorkbenchWindow.removePageListener(this);
                                    page.setPerspective(p);                                 
                                }

                                @Override
                                public void pageClosed (IWorkbenchPage page) {
                                }

                                @Override
                                public void pageOpened (IWorkbenchPage page) {}});
                        }
                        config.launch(ILaunchManager.DEBUG_MODE,null);                        
                    }
                    catch (CoreException e) {
                        log(e);
                    }
                    
                }
            }
        }
    }
    
    public static void log(String message, Throwable exception){
        getDefault().getLog().log(new Status(IStatus.ERROR,
            PLUGIN_ID,0,message,exception));
    }
    
    public static void log(Throwable exception){
        getDefault().getLog().log(new Status(IStatus.ERROR,
            PLUGIN_ID,0,exception.getMessage(),exception));
    }

}
