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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget3;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IDebugTarget;
import org.osgi.framework.BundleContext;

import com.arc.cdt.debug.seecode.core.cdi.ICDISeeCodeSession;
import com.arc.cdt.debug.seecode.internal.core.cdi.Session;
import com.arc.debugger.IEngineResolver;
import com.arc.mw.util.Cast;
import com.arc.mw.util.StringUtil;
import com.arc.seecode.command.CommandFactory;
import com.arc.seecode.command.ICommandExecutor;
import com.arc.seecode.command.ICommandProcessor;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;
import com.arc.seecode.engine.ICustomDisplayCallback;
import com.arc.seecode.engine.IRunner;
import com.arc.seecode.engine.ITimeoutCallback;

/**
 * The main SeeCode plugin class.
 */
public class SeeCodePlugin extends Plugin {

    public static final String PLUGIN_ID = "com.arc.cdt.debug.seecode.core";

    /**
     * Reference the ID of the process factory that generates the instances of
     * IProcess to represent the seecode engine process. It is required because
     * we extend <code>org.eclipse.debug.core.processFactories</code>
     * extension point.
     */
    public static final String PROCESS_FACTORY_ID = "com.arc.cdt.debug.seecode.processFactory";

    /**
     * A custom DebugEvent (kind=MODEL_SPECIFIC). If the "detail" field is
     * REGEN_DISPLAY_MENU_EVENT_DETAIL, then the engine will be requeried to
     * have the Displays menu regenerated. The source will be the
     * EngineInterface object.
     */
    public static final int REGEN_DISPLAY_MENU_EVENT_DETAIL = 1;
    

    /**
     * The key from which we can retrieve the ICDITarget object from the EngineInterface.getData
     * method.
     */
    private static final String CDITARGET_ENGINE_DATA_KEY = "cditarget";
    
    
    /**
     * The key from which we can retrieve the command processor from the EngineInterface.getData
     * method.
     */
    private static final String COMMAND_PROCESSOR_ENGINE_DATA_KEY = "commandproc";

    // The shared instance.
    private static SeeCodePlugin plugin;

    // Resource bundle.
    private ResourceBundle resourceBundle;

    private ICustomDisplayCallbackCreator mCustomDisplayCallbackCreator = null;

    private ILicenseFailure mLicensingFailure = null;

    private IStatusWriter mStatusWriter = null;

    private IDisplayMessage mDisplayError;

    private IRunner mCallbackRunner;
    
    private ITimeoutCallback mLoadTimeoutCallback = null;

    /**
     * Live sessions.
     */
    private List<Session> mSessions = new ArrayList<Session>();

    private boolean mStopped = false;

    private ILicenseExpirationChecker mLicenseExpirationChecker = null;

    private IDiagnoseProgramLoadTimeout mDiagnoseProgramLoadTimeout = null;

    private ITermSimInstantiator mTermSimInstantiator = null;

    private IEngineResolver fEngineResolver = null;

    private IEclipsePreferences fPreferences = null;

    /**
     * The constructor.
     */
    public SeeCodePlugin() {

        plugin = this;
        try {
            resourceBundle = ResourceBundle
                    .getBundle("com.arc.cdt.seecode.core.SeeCodeResources");
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
    public static SeeCodePlugin getDefault() {
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

    /**
     * Returns the string from the plugin's resource bundle, or 'key' if not
     * found.
     */
    public static String getResourceString(String key) {
        ResourceBundle bundle = SeeCodePlugin.getDefault().getResourceBundle();
        try {
            return (bundle != null) ? bundle.getString(key) : key;
        } catch (MissingResourceException e) {
            return key;
        }
    }

    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public File getStateDirectory() {
        IPath path = getStateLocation();
        return path.toFile();
    }

    /**
     * This class creates the instance of the {@link ICustomDisplayCallback}
     * that is actually defined in our UI package.
     * <P>
     * We can't reference it directly because of circular dependencies...
     */
    public ICustomDisplayCallback createCustomDisplayCallback(ICDITarget target) {
        // Since the CDebugger extension is implemented by the UI plugin (as of MIDE 8.0.2),
        // if our callbacks are not initialized, then something is wrong.
        if (mCustomDisplayCallbackCreator == null) {
            log("Configuraton failure: no callback creator set");
            return null;
        } else
            return mCustomDisplayCallbackCreator.create(target);

    }

    public void reportLicensingFailure(String msg) {
        // Since the CDebugger extension is implemented by the UI plugin (as of MIDE 8.0.2),
        // if our callbacks are not initialized, then something is wrong.
        if (mLicensingFailure == null) {
            log("Configuraton failure: no callback for licensing failure");
        } else
            mLicensingFailure.reportLicenseFailure(msg);
    }
    
    /**
     * Invoke the UI callback to check if the debugger license is near expiration and alert the
     * user appropriately (unless he has set the preference to suppress this alert).
     */
    public void checkLicenseExpirationAlert(int days){
        if (mLicenseExpirationChecker != null && days >= 0)
            mLicenseExpirationChecker.checkLicenseExpiration(days);
    }

    public void setStatus(String msg) {
        // Since the CDebugger extension is implemented by the UI plugin (as of MIDE 8.0.2),
        // if our callbacks are not initialized, then something is wrong.
        if (mStatusWriter == null) {
            log("Configuraton failure: no status writer object");
        } else
            mStatusWriter.setStatus(msg);
    }

    public void displayError(String title, String msg) {
        // Since the CDebugger extension is implemented by the UI plugin (as of MIDE 8.0.2),
        // if our callbacks are not initialized, then something is wrong.
        if (mDisplayError == null) {
            log(msg);
        } else
            mDisplayError.displayError(title, msg);
    }
    
    public void displayNote(String title, String msg) {
        // Since the CDebugger extension is implemented by the UI plugin (as of MIDE 8.0.2),
        // if our callbacks are not initialized, then something is wrong.
        if (mDisplayError == null) {
            log(msg);
        } else
            mDisplayError.displayNote(title, msg);
    }

    /**
     * Return the runner object that will invoke the callback methods from the
     * engine to guaranteed that they are in the UI thread.
     * 
     * @return the runner object for engine callback methods.
     */
    public IRunner getCallbackRunner() {
        // Since the CDebugger extension is implemented by the UI plugin (as of MIDE 8.0.2),
        // if our callbacks are not initialized, then something is wrong.
        if (mCallbackRunner == null) {
            log("Missing callback runner");
        }
        return mCallbackRunner;
    }
    
    /**
     * Return a "callback" that is invoked when a program load operation times out. Its a hook
     * that permits the client to extend the timeout.
     * @return a "callback" that is invoked when a program load operation times out.
     */
    public ITimeoutCallback getLoadTimeoutCallback(){
        return mLoadTimeoutCallback;
    }
    
    public void setLoadTimeoutCallback(ITimeoutCallback loadTimeoutCallback){
        mLoadTimeoutCallback = loadTimeoutCallback;
    }

    /**
     * This is called by the UI SeeCode plugin to set the callback for creating
     * the custom display callback, which is defined in the UI package.
     * 
     * @param c
     *            the callback for creating the {@link ICustomDisplayCallback}
     *            instance.
     */
    public void setCustomDisplayCallbackCreator(ICustomDisplayCallbackCreator c) {
        mCustomDisplayCallbackCreator = c;
    }

    /**
     * This is called by the UI SeeCode plugin to provide a callback for
     * reporting a licensing failure.
     */
    public void setLicensingFailure(ILicenseFailure callback) {
        mLicensingFailure = callback;
    }

    public void setStatusWriter(IStatusWriter w) {
        mStatusWriter = w;
    }

    public void setDisplayError(IDisplayMessage e) {
        mDisplayError = e;
    }
    
    public void setProgramLoadTimeoutCallback(IDiagnoseProgramLoadTimeout callback){
        mDiagnoseProgramLoadTimeout = callback;
    }
    
    public void setTermSimInstantiator(ITermSimInstantiator instantiator){
        mTermSimInstantiator  = instantiator;
    }
    
    /**
     * Create a terminal simulator view in the UI.
     * @param session
     * @param tcpPort
     * @param uartPort
     * @throws Exception
     */
    public void createTermSimView(ICDISeeCodeSession session, int tcpPort, int uartPort) throws Exception{
        mTermSimInstantiator.createTermSimView(session,tcpPort,uartPort);
    }
    
    public void reconnectTermSimViews(ICDISeeCodeSession session) throws Exception{
        mTermSimInstantiator.reconnectTermSimViews(session);
    }
    
    /**
     * Set a callback from the UI that checks if license is near expiration and alerts the
     * user. It is invoked immediately after a successful load of a program.
     * @param r the callback for checking if an alert is required regarding license expiration.
     */
    public void setLicenseExpirationChecker(ILicenseExpirationChecker r) {
        mLicenseExpirationChecker = r;
    }

    /**
     * @param runner
     */
    public void setCallbackRunner(IRunner runner) {
        mCallbackRunner = runner;

    }

    /**
     * Add to our list of live sessions
     * 
     * @param session
     */
    public void addSession(Session session) {
        synchronized (mSessions) {
            mSessions.add(session);
        }
    }

    public void removeSession(Session session) {
        synchronized (mSessions) {
            mSessions.remove(session);
        }
    }
    
    public IEclipsePreferences getPreferences(){
        if (fPreferences == null) {
            fPreferences = InstanceScope.INSTANCE.getNode(this.getBundle().getSymbolicName());
        }
        return fPreferences;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
    	// Must save preferences because they are used to store engine timeouts and next day in which
    	// we alert about license expiration.
    	if (fPreferences != null) fPreferences.flush();
        try {
            shutdownSessions();
        } finally {
            super.stop(context);
            mStopped = true;
        }
    }

    /**
     * Return whether or not this plugin has been shutdown. Dependent plugins
     * may query this to know whether or not to shutdown sessions.
     * 
     * @return whether or not this plugin has been shutdown.
     */
    public boolean isStopped() {
        return mStopped;
    }

    /**
     * Force all active sessions to shutdown, if not already, in preparation of
     * a shutdown of Eclipse.
     */
    public void shutdownSessions() {
        // Make sure all sessions are safely stopped.
        Session sessions[] = null;
        synchronized (mSessions) {
            if (mSessions.size() > 0) {
                sessions = mSessions.toArray(new Session[mSessions.size()]);
            }
        }
        if (sessions != null) {
            // Must do this because mSession can change
            // while iterating!
            for (Session s : sessions) {
                s.forceEmergencyShutdown();
            }
        }
    }
    
    /**
     * A hook for a foreign plugin to insert a SeeCode command-line
     * argument into a Launch Configuration, if it isn't
     * there already. The options string will be tokenized according to the usual conventions.
     * Tokens that contains whites space must be quoted (e.g., "-simextdir=C:/Documents and Settings/...")
     * <P>
     * NOTE: the options will only be added to the debugger command line if they are not
     * already there. Thus, a previously-modified launch configuration can be passed with
     * no affect.
     * <P>
     * @param config the launch configuration that is to be modified.
     * @param options the command-line options to be appended.
     */
    public static void addDebuggerCommandOption (ILaunchConfiguration config, String options) throws CoreException{
        Map<String, String> map = null;
        map = Cast
            .toType(config.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_GUIHILI_PROPERTIES, (Map<String,String>) null));
        if (map == null) {
            map = new HashMap<String, String>();
        }
        String propname = "cmd_line_option"; // See "program2.opt" file.
        String cmds = map.get(propname);
        if (cmds == null) cmds = "";
        if (cmds.indexOf(options) < 0) { // if not already there
            //First set the "Command line" widget
            if (cmds.length() > 0) cmds += ' ';
            cmds += options;
            map.put(propname,cmds);
            ILaunchConfigurationWorkingCopy working = config.getWorkingCopy();
            working.setAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_GUIHILI_PROPERTIES,map);
            //Now actually set the SeeCode arguments. The Command line options are a subset.
            List<String> args = 
                Cast.toType(config.getAttribute(
                    ISeeCodeLaunchConfigurationConstants.ATTR_SWAHILI_ARGS,
                    new ArrayList<String>()));
            List<String> optionsList = StringUtil.stringToList(options);
            ArrayList<String> newArgs = new ArrayList<String>(args.size()+optionsList.size());
            newArgs.addAll(args);
            newArgs.addAll(optionsList);
            working.setAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_SWAHILI_ARGS, newArgs);
            working.doSave();
        }        
    }

    /**
     * Put out a decent message when the engine times out during a program load.
     * This can occur when loading over a slow connection, or doing a blast.
     * The user needs to be told to up the preference setting
     * @param exe
     */
    public void diagnoseProgramLoadTimeout (String exe) {
        if (mDiagnoseProgramLoadTimeout != null){
            mDiagnoseProgramLoadTimeout.diagnoseTimeout(exe,getPreferences().getInt(ISeeCodeConstants.PREF_REQUEST_LOAD_TIMEOUT,
                ISeeCodeConstants.DEF_REQUEST_LOAD_TIMEOUT));
        }       
    }

    @SuppressWarnings({ "unchecked", "restriction" })
    @Override
    public void start (BundleContext context) throws Exception {
        // @todo Auto-generated method stub
        super.start(context);
        IAdapterManager amgr = Platform.getAdapterManager();
        LaunchAdapterFactory f = new LaunchAdapterFactory();
        //<HACK>
        // We need to have an adapter that coerces a ILaunch object into a
        // IStepIntoHandler, etc. by looking at the corresponding Session object.
        // But there is already one registered that
        // takes precedence. Thus, we must go "under the covers" to grab the
        // existing adapter, remove it, and have the new one overriding it.
        // But the new one is to delegate to the old one if it can't do the
        // conversion.
        Map<String,List<IAdapterFactory>> map = ((org.eclipse.core.internal.runtime.AdapterManager)amgr).getFactories();
        List<IAdapterFactory> list = map.get(ILaunch.class.getName());
        if (list != null) {
            Class< ? > ourAdapters[] = f.getAdapterList();
            LOOP: for (IAdapterFactory factory : list) {
                Class< ? >[] classes = factory.getAdapterList();
                if (classes != null) { // Race condition at startup can make this null
                    for (Class< ? > c : classes) {
                        for (Class< ? > cc : ourAdapters) {
                            if (cc == c) {
                                f.setDelegate(factory);
                                amgr.unregisterAdapters(factory, ILaunch.class);
                                break LOOP;
                            }
                        }
                    }
                }
            }
        }
        //</HACK>
        
        amgr.registerAdapters(f, ILaunch.class);
    }

    public void setEngineVersionStrategyCallback (IEngineResolver engineResolver) {
        fEngineResolver  = engineResolver;      
    }
    
    public IEngineResolver getEngineVersionStrategyCallback(){
        return fEngineResolver;
    }
    
    /**
     * Since the engine interface is "below" the CDI layer, we cache the ICDITarget associated
     * with the engine within the engine itself.
     * @param engine the low-level debugger engine interface.
     * @return the associated CDI target.
     */
    public static ICDITarget getEngineTarget(EngineInterface engine){
        return (ICDITarget) engine.getData(CDITARGET_ENGINE_DATA_KEY);
    }
    
    /**
     * Associate an CDI target with the engine that it "wraps".
     * @param engine the debugger engine interface object.
     * @param target the associated target.
     */
    public static void setEngineTarget(EngineInterface engine, ICDITarget target){
       engine.setData(CDITARGET_ENGINE_DATA_KEY, target);
    }
    
    /**
     * Given an engine interface, return a command processor that wraps it, creating it
     * if necessary.
     * @param engine the engine interface.
     * @return associated command processor.
     */
    public static ICommandProcessor getCommandProcessor(final EngineInterface engine){
        ICommandProcessor cp = (ICommandProcessor)engine.getData(COMMAND_PROCESSOR_ENGINE_DATA_KEY);
        if (cp == null) {
            try {
                cp = CommandFactory.createCommandProcessor(engine, engine.getOutputStream(), engine.getErrorStream());
                // Since we want "stop" to stop animation if it is running, we must override it
                cp.addCommandExecutor("stop", new ICommandExecutor() {

                    @Override
                    public void execute (String arguments) throws Exception {
                        ICDITarget target = getEngineTarget(engine);
                      
                        ICDIThread t = target != null?target.getCurrentThread():null;
                        if (t != null) {
                            t.suspend();
                        }
                        else {
                            engine.stop(0); // shouldn't get here
                        }
                    }

                    @Override
                    public boolean repeat () throws Exception {
                        return false;
                    }
                });
                
                cp.addCommandExecutor("restart", new ICommandExecutor() {

                    @Override
                    public void execute (String arguments) throws Exception {
                        ICDITarget target = getEngineTarget(engine);
                        if (target != null) {
                            if (arguments == null || arguments.length() == 0) {
                                // Invoke "restart" from ICDebugTarget so that temp bkpt on main is set.
                                ILaunch launch = (ILaunch) ((IAdaptable) target).getAdapter(ILaunch.class);
                                if (launch != null) {
                                    IDebugTarget targs[] = launch.getDebugTargets();
                                    for (IDebugTarget t : targs) {
                                        if (t.getAdapter(ICDITarget.class) == target) {
                                            ((ICDebugTarget) t).restart();
                                            return;
                                        }
                                    }
                                }
                                // Shouldn't get here.
                                target.restart();
                            }
                            else {
                                // Restart with new arguments won't resume beyond _start.
                                String args[] = StringUtil.stringToArray(arguments);
                                ((ICDITarget3) target).restart(args);
                            }
                        }
                    }

                    @Override
                    public boolean repeat () throws Exception {
                        return false;
                    }
                });
                engine.setData(COMMAND_PROCESSOR_ENGINE_DATA_KEY, cp);
            }
            catch (EngineException e1) {
                SeeCodePlugin.log(e1);
            }
        }
        return cp;
    }

}
