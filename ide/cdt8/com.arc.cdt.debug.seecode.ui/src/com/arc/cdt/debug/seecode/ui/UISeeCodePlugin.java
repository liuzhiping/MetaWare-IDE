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
package com.arc.cdt.debug.seecode.ui;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.arc.cdt.debug.seecode.core.ICustomDisplayCallbackCreator;
import com.arc.cdt.debug.seecode.core.IDisplayMessage;
import com.arc.cdt.debug.seecode.core.ILicenseExpirationChecker;
import com.arc.cdt.debug.seecode.core.ILicenseFailure;
import com.arc.cdt.debug.seecode.core.ISeeCodeConstants;
import com.arc.cdt.debug.seecode.core.IStatusWriter;
import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.internal.ui.PromptForEngineSelectionDialog;
import com.arc.cdt.debug.seecode.internal.ui.SeeCodeAdapterFactory;
import com.arc.cdt.debug.seecode.internal.ui.SeeCodeMenuBarManager;
import com.arc.cdt.debug.seecode.internal.ui.action.AnimateToolBarManager;
import com.arc.cdt.debug.seecode.ui.display.CustomDisplayCallback;
import com.arc.cdt.debug.seecode.ui.termsim.TermSimInstantiator;
import com.arc.cdt.debug.seecode.ui.views.AbstractEngineBasedView;
import com.arc.cdt.debug.seecode.ui.views.SeeCodeCustomView;
import com.arc.cdt.debug.seecode.ui.views.SeeCodeDisasmView;
import com.arc.debugger.IEngineResolver;
import com.arc.seecode.display.IColorPreferences;
import com.arc.seecode.engine.EngineInterface;
import com.arc.seecode.engine.ICustomDisplayCallback;
import com.arc.seecode.engine.IRunner;
import com.arc.seecode.engine.ITimeoutCallback;
import com.arc.widgets.IColor;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.WidgetsFactory;

/**
 * The main plugin class to be used in the desktop.
 */
public class UISeeCodePlugin extends AbstractUIPlugin {
    
    public static final String PLUGIN_ID = "com.arc.cdt.debug.seecode.ui";
    //The shared instance.
    private static UISeeCodePlugin plugin;

    //Resource bundle.
    private ResourceBundle resourceBundle;

    private static IComponentFactory sWidgetFactory = null;

    private SeeCodeMenuBarManager mMenuBarManager;
    
    private transient int mAlertDays; // days remaining to alert
    private AnimateToolBarManager fAnimateToolBarMgr;

//    /**
//     * The number of SeeCode processes running
//     */
//    private static int sSeeCodeProcesses = 0;
//


    /**
     * The constructor.
     */
    public UISeeCodePlugin() {
        super();
        plugin = this;
        try {
            resourceBundle = ResourceBundle.getBundle("com.arc.cdt.debug.seecode.ui.SeeCode");
        }
        catch (MissingResourceException x) {
            resourceBundle = null;
        }

//        // Create the object that intercepts "create-display"
//        // events from the core plugin so as to create
//        // the Custom seecode displays.
//        new DisplayCreatorDelegate();

        // The core plugin doesn't "see" us to avoid
        // circular dependencies. But it needs to
        // instantiate the CustomDisplayCallback class
        // that is defined in this package.
        // We use a callback to do that:
        SeeCodePlugin.getDefault().setCustomDisplayCallbackCreator(new ICustomDisplayCallbackCreator() {

            @Override
            public ICustomDisplayCallback create (ICDITarget target) {              
                return new CustomDisplayCallback(target);
            }
        });
        
        SeeCodePlugin.getDefault().setLicenseExpirationChecker(new ILicenseExpirationChecker() {

            @Override
            public void checkLicenseExpiration (int days) {              
                 UISeeCodePlugin.this.checkLicensingAlert(days);
            }
        });

        SeeCodePlugin.getDefault().setLicensingFailure(new ILicenseFailure() {

            @Override
            public void reportLicenseFailure (final String msg) {
                PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

                    @Override
                    public void run () {
                        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                        String fullMsg =  
                          "A valid license for " + UISeeCodePlugin.getTheDebuggerName() + " was not found.\n\n" +  msg;
                        IStatus status = makeErrorStatus(fullMsg);
                        ErrorDialog.openError(shell, UISeeCodePlugin.getDebuggerName() + " Licensing Failure", null, status);

                    }
                });

            }
        });

        SeeCodePlugin.getDefault().setStatusWriter(new IStatusWriter() {

            @Override
            public void setStatus (final String msg) {
                // We need to get to the status line manager, but we can only get
                // access to from a viewsite. Unfortunately, we have
                // no direct reference to such. Therefore, look for
                // the debug view whose ID is "org.eclipse.debug.ui.DebugView"
                PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

                    @Override
                    public void run () {
                        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                        if (window != null) {
                            IWorkbenchPage page = window.getActivePage();
                            if (page != null) {
                                IViewPart viewPart = page.findView(IDebugUIConstants.ID_DEBUG_VIEW);
                                if (viewPart != null) {
                                    IStatusLineManager statusLine = viewPart.getViewSite().getActionBars()
                                            .getStatusLineManager();
                                    statusLine.setMessage(msg);
                                }
                            }
                        }
                    }
                });

            }
        });
        
        SeeCodePlugin.getDefault().setTermSimInstantiator(new TermSimInstantiator());

        SeeCodePlugin.getDefault().setDisplayError(new IDisplayMessage() {

            @Override
            public void displayError (final String title, final String msg) {
                PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

                    @Override
                    public void run () {
                        showError(title,msg);
                    }
                });

            }

            @Override
            public void displayNote (final String title, final String msg) {
                PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

                    @Override
                    public void run () {
                        showNote(title,msg);
                    }
                });
                
            }
        });
        
        SeeCodePlugin.getDefault().setEngineVersionStrategyCallback(new IEngineResolver(){

            @Override
            public boolean useToolSetEngine (final int bundledEngineId, final int toolsetEngineId, final String toolsetPath) {
                switch (SeeCodePlugin.getDefault().getPreferences().getInt(ISeeCodeConstants.PREF_ENGINE_VERSION_MANAGEMENT,
                            ISeeCodeConstants.ENGINE_VERSION_USE_TOOLSET)){
                    case ISeeCodeConstants.ENGINE_VERSION_PROMPT:
                        if (bundledEngineId != toolsetEngineId) {
                            final boolean results[] = new boolean[1];
                            PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable(){

                                @Override
                                public void run () {
                                    results[0] = new PromptForEngineSelectionDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                                        bundledEngineId,toolsetEngineId,toolsetPath).open();
                                    
                                }});
                            return results[0];
                        }
                        return false;
                    case ISeeCodeConstants.ENGINE_VERSION_USE_BUNDLED:
                        return false;
                    case ISeeCodeConstants.ENGINE_VERSION_USE_TOOLSET:
                        return true;
                    case ISeeCodeConstants.ENGINE_VERSION_USE_LATEST:
                        return toolsetEngineId > bundledEngineId;
                }
                return false; // shouldn't get here
            }});
        
/*        SeeCodePlugin.getDefault().setProgramLoadTimeoutCallback(new IDiagnoseProgramLoadTimeout(){

            public void diagnoseTimeout (final String exeName, final int timeout) {
                PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

                    public void run () {
                        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                        String fullMsg =  
                          "The debugger engine timed out while loading\n\"" +
                          exeName + "\".\n" +
                          "The current timeout for loading a program is " + timeout + " milliseconds.\n" +
                          "If you have a slow target connection, or if you are doing a long-running\n"+
                          "blast operation, you may need to increase the timeout value. Go to the\n" +
                          "preference page: \"Windows->Preferences->C/C++->Debugger->MetaWare Debugger\".\n";
                        
                        IStatus status = SeeCodePlugin.makeErrorStatus(fullMsg);
                        ErrorDialog.openError(shell, "Program load timeout failure", null, status);

                    }
                });
                
            }});*/
        
        SeeCodePlugin.getDefault().setLoadTimeoutCallback(new ITimeoutCallback(){
            private int _newTimeout = 0;
            @Override
            public int getNewTimeout(final int timeout) {
                PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
                    @Override
                    public void run () {
                        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                        String fullMsg =  
                          "The debugger engine is attempting to load a program and it has not\n"+
                          "completed after \"" + (timeout+500)/1000 + "\" seconds (as specified in the "+
                          "MetaWare Debugger\npreferences page).\n\n" +
                          "If you are loading over a slow connnection, or if you are doing a blast\n"+
                          "operation, the engine may need more time.\n\n"+
                          "What do you want the IDE to do?\n";
                        
        
                        
                          MessageDialog dialog = new MessageDialog(shell, "Engine Timeout Alert", null, // accept
                              fullMsg,
                              MessageDialog.WARNING,
                              new String[]{IDialogConstants.OK_LABEL } ,
                              0) {

                             
                              @Override
                              protected Control createCustomArea (Composite parent) {
                                  Composite container = new Group(parent,0);
                                  container.setLayout(new GridLayout(4,false));
                                  GridData gd = new GridData();
                                  gd.horizontalSpan = 1;
                                  gd.grabExcessHorizontalSpace = false;
                                  final Button b1 = new Button(container,SWT.RADIO);
                                  b1.setLayoutData(gd);
                                  b1.setText("Wait no longer;");
                                  // Label on button appears to be limited in size; add additional
                                  // as a label
                                  Label label1 = new Label(container,SWT.LEFT);
                                  label1.setText("abort if load is not yet complete.");
                                  GridData gd1 = new GridData();
                                  gd1.horizontalSpan = 3;
                                  gd1.grabExcessHorizontalSpace = true;
                                  label1.setLayoutData(gd1);
                                  
                                  gd = new GridData();
                                  gd.horizontalSpan = 4;
                                  gd.grabExcessHorizontalSpace = true;
                                  final Button b2 = new Button(container,SWT.RADIO);
                                  b2.setLayoutData(gd);
                                  b2.setText("Continue waiting indefinitely.");
                                  final Button b3 = new Button(container,SWT.RADIO);
                                  b3.setText("Wait for an additional number of seconds");
                                  b3.setLayoutData(gd);
                                  final Label label = new Label(container,SWT.LEFT);
                                  label.setText("    Number of additional seconds to wait: ");
                                  gd = new GridData();
                                  gd.horizontalSpan = 2;
                                  label.setLayoutData(gd);
                                  final Text field = new Text(container,SWT.SINGLE);
                                  field.setText("" + (timeout+500)/1000);
                                  gd = new GridData();
                                  gd.horizontalSpan = 2;
                                  gd.grabExcessHorizontalSpace = true;
                                  gd.minimumWidth = 80;
                                  field.setLayoutData(gd);
                                  SelectionListener listener = new SelectionListener(){

                                    @Override
                                    public void widgetDefaultSelected (SelectionEvent e) {                                 
                                    }

                                    @Override
                                    public void widgetSelected (SelectionEvent e) {
                                        if (e.widget == b1) {
                                            _newTimeout = 0;
                                            field.setEnabled(false);
                                            label.setEnabled(false);
                                        }
                                        else if (e.widget == b2){
                                            _newTimeout = -1;
                                            field.setEnabled(false);
                                            label.setEnabled(false);
                                        }
                                        else {
                                            field.setEnabled(true);
                                            label.setEnabled(true);
                                            try {
                                                _newTimeout = Integer.parseInt(field.getText())*1000;
                                            }
                                            catch (NumberFormatException e1) {
                                                field.setText("0");
                                                _newTimeout = 0;
                                            }
                                       }                                      
                                    }};
                                    b1.addSelectionListener(listener);
                                    b2.addSelectionListener(listener);
                                    b3.addSelectionListener(listener);
                                    field.addModifyListener(new ModifyListener(){

                                        @Override
                                        public void modifyText (ModifyEvent e) {
                                        try {
                                            if (b3.getSelection()) {
                                                _newTimeout = Integer.parseInt(field.getText())*1000;
                                                if (_newTimeout < 0) {
                                                    field.setText("0");
                                                    _newTimeout = 0;
                                                }
                                            }
                                        }
                                        catch (NumberFormatException e1) {
                                            field.setText("0");
                                            _newTimeout = 0;
                                        }

                                    }});
                                    return container;
                              }
                          };
                          if (dialog.open() != Window.OK){
                              _newTimeout = 0; // terminate immediately.
                          }
                      }

                    }
                );
                return _newTimeout;
                
            }});

        /*
         * Set the Run wrapper for all engine callbacks so that they are in the UI thread.
         */
        SeeCodePlugin.getDefault().setCallbackRunner(new IRunner() {

            @Override
            public void invoke (Runnable run, boolean async) throws Throwable {
                Display display = PlatformUI.getWorkbench().getDisplay();
                try {
                    // Run asynchronously to avoid deadlock of UI thread is
                    // waiting for the engine to return.
                    // display is null or disposed after workbench has shutdown,
                    // but the engine may still be sending stuff...
                    if (display != null && !display.isDisposed()) {
                        if (async) {
                            display.asyncExec(run);
                        }
                        else
                            display.syncExec(run);
                    }
                }
                catch (SWTException e) {
                    if (e.throwable != null) {
                        // If the display is disposed of after the above
                        // check, but before the "run" is invoked, then
                        // we can get an exception. Ignore such cases.
                        if (display != null && !display.isDisposed())
                            throw e.throwable;
                    }
                    throw e;
                }
            }

        });

    }
    
    
    private void checkLicensingAlert (final int daysRemaining) {
        final int alertDays = SeeCodePlugin.getDefault().getPreferences().getInt(
            ISeeCodeConstants.PREF_LICENSE_EXPIRATION_DAYS, ISeeCodeConstants.DEF_PREF_LICENSE_EXPIRATION_DAYS);
        if (daysRemaining >= 0) {
            boolean alert = SeeCodePlugin.getDefault().getPreferences().getBoolean(
                ISeeCodeConstants.PREF_LICENSE_EXPIRATION_ALERT, true);         
            if (alertDays >= daysRemaining) {
                if (alert) {
                    PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

                        @Override
                        public void run () {
                            Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                            String s;
                            if (daysRemaining == 0)
                                s = "after today";
                            else if (daysRemaining == 1)
                                s = "after tomorrow";
                            else
                                s = "in " + daysRemaining + " days";
                            MessageDialog dialog = new MessageDialog(shell, "License Expiration Alert", null, // accept
                                "Debugger license will expire " + s,
                                MessageDialog.WARNING,
                                new String[] { IDialogConstants.OK_LABEL },
                                0) {

                                @Override
                                protected Control createCustomArea (Composite parent) {
                                    if (daysRemaining > 0 && daysRemaining <= 2) {
                                        Label label = new Label(parent, SWT.RIGHT);
                                        GridData data = new GridData();
                                        data.horizontalAlignment = GridData.END;
                                        data.grabExcessHorizontalSpace = true;
                                        label.setLayoutData(data);
                                        label.setText("You will be reminded again tomorrow.");
                                        setAlertDays(daysRemaining - 1);
                                        return label;
                                    }
                                    if (daysRemaining > 0) {
                                        Composite container = new Composite(parent, 0);
                                        GridData data = new GridData();
                                        data.horizontalAlignment = GridData.END;
                                        data.grabExcessHorizontalSpace = true;
                                        container.setLayoutData(data);
                                        container.setLayout(new GridLayout(3, false));
                                        Label label1 = new Label(container, SWT.LEFT);
                                        label1.setText("Remind me again when ");
                                        final Combo combo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
                                        Label label2 = new Label(container, SWT.LEFT);
                                        label2.setText(" days remain.");
                                        label2.setLayoutData(data);
                                        for (int i = Math.min(15,daysRemaining - 1); i >= 0; i--) {
                                            combo.add("" + i);
                                        }
                                        combo.select(0);
                                        setAlertDays(Integer.parseInt(combo.getItem(0)));
                                        combo.addSelectionListener(new SelectionListener() {

                                            @Override
                                            public void widgetDefaultSelected (SelectionEvent e) {
                                            }

                                            @Override
                                            public void widgetSelected (SelectionEvent e) {
                                                setAlertDays(Integer.parseInt(combo.getText()));
                                            }
                                        });
                                        return container;
                                    }
                                    return null;
                                }
                            };
                            if (dialog.open() == Window.OK){
                                SeeCodePlugin.getDefault().getPreferences().putInt(ISeeCodeConstants.PREF_LICENSE_EXPIRATION_DAYS,mAlertDays);            
                            }
                        }
                    });

                }
            }
            else if (daysRemaining > ISeeCodeConstants.DEF_PREF_LICENSE_EXPIRATION_DAYS &&
                     alertDays != ISeeCodeConstants.DEF_PREF_LICENSE_EXPIRATION_DAYS){
                // License has been renewed. Reset things to default.
                restoreDefaultAlertDays();
            }
        }
        else if (alertDays != ISeeCodeConstants.DEF_PREF_LICENSE_EXPIRATION_DAYS){
            // Evidently a permanent license; make sure we reset alert if he previously
            // had an expired one.
            restoreDefaultAlertDays();
        }
    }

    private void restoreDefaultAlertDays () {
        // If license has been refreshed, then reset counter to default             
        SeeCodePlugin.getDefault().getPreferences().putInt(
            ISeeCodeConstants.PREF_LICENSE_EXPIRATION_DAYS,
            ISeeCodeConstants.DEF_PREF_LICENSE_EXPIRATION_DAYS);
    }
    
    private void setAlertDays(int days){
        mAlertDays = days;
    }


    /**
     * Returns the shared instance.
     */
    public static UISeeCodePlugin getDefault() {
        return plugin;
    }

    /**
     * Returns the string from the plugin's resource bundle, or 'key' if not found.
     */
    public static String getResourceString(String key) {
        ResourceBundle bundle = UISeeCodePlugin.getDefault()
                .getResourceBundle();
        try {
            return (bundle != null) ? bundle.getString(key) : key;
        } catch (MissingResourceException e) {
            return key;
        }
    }
    
    /**
     * Return whether or not this IDE is named "TopSide".
     * @return whether or not this IDE is named "TopSide".
     */
    private static boolean isTopSide(){
        return System.getProperty("TOPSIDE") != null;
    }
    
    /**
     * 
     * @return either "SeeCode" or "MetaWare Debugger"
     */
    public static String getDebuggerName(){
        if (isTopSide()){
            return "SeeCode";
        }
        return "MetaWare Debugger";
    }
    
    /**
     * 
     * @return either "SeeCode" or "the MetaWare Debugger"
     */
    public static String getTheDebuggerName(){
        return isTopSide()?"SeeCode":"the MetaWare Debugger";
    }
    
    /**
     * Factory for creating our GUI-independent widgets
     * that our custom display framework uses.
     * @return the widget factory.
     */
    public static IComponentFactory getWidgetFactory(){
        if (sWidgetFactory == null){
            sWidgetFactory  = WidgetsFactory.createSWT(PlatformUI.getWorkbench().getDisplay());
        }
        return sWidgetFactory;
    }


    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }
    
    public IMenuBarUpdater getMenuBarUpdater(){
        //      Create object to control the seecode menu
        // in the main menu bar.
        // Must be created lazily, because the workbench may not be up
        // when this plugin is started.
        if (mMenuBarManager == null)
            mMenuBarManager = new SeeCodeMenuBarManager();
        return mMenuBarManager;
    }
    
    /**
     * Map a SeeCode display "kind" to a CDT view that we have userped.
     */
    private static final Map<String,String> sUserpedDisplayMap = new HashMap<String,String>();
    static {
        sUserpedDisplayMap.put("reg","org.eclipse.cdt.debug.ui.RegisterView");
        sUserpedDisplayMap.put("disasm",SeeCodeDisasmView.DISASM_VIEW_ID);
    }
    /**
     * The set of display kinds that we permit more than one instance of.
     * E.g., Memory display.
     */
    private static final Set<String> sRepeatableDisplays = new HashSet<String>();
    static {
        sRepeatableDisplays.add("mem");
    }
    
    /**
     * Given a display kind that is repeatable (e.g., "mem" for Memory display),
     * compute the ordinal number for the next occurence. If there is an instance
     * that is not visible, then use that one. Otherwise, it is one greater than
     * the last one.
     * @param kind the SeeCode display kind (e.g., "mem")
     * @return the ordinal for the next instance of this display.
     */
    private String computeOrdinalFor(String kind){
        IViewReference refs[] = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
        String prefix = kind + SeeCodeCustomView.INSTANCE_SEPARATOR;
        Set<String> inUse = new HashSet<String>();
        for (IViewReference v: refs){
       	
        	//CR:9000652829
            //Eclipse current version plugin 4.2.2 made WorkbenchPage view ID to be viewID + ':' + secondaryID  
            if ((v.getId().indexOf(SeeCodeCustomView.VIEW_ID) >=0) &&
                v.getSecondaryId().startsWith(prefix)){
                String ord = v.getSecondaryId().substring(prefix.length());
                if (v.getView(false) == null){
                    return ord; // Re use it.
                }
                else inUse.add(ord);
            }
        }
        for (int i = 1; ; i++){
            if (!inUse.contains(""+i)){
                return ""+i;
            }
        }
    }
    
    public IViewPart createDisplay(EngineInterface engine, String kind) {
        try {
            // See if it is one of those that we have userped
            String viewID = sUserpedDisplayMap.get(kind);
            if (viewID != null){
                kind = null; 
            }
            else {
                viewID = SeeCodeCustomView.VIEW_ID;
                if (sRepeatableDisplays.contains(kind)){
                    // We have a display for which we permit multiple instances.
                    // We want to create a new instance, regardless if there is one already
                    // existing.
                    kind = kind + SeeCodeCustomView.INSTANCE_SEPARATOR + computeOrdinalFor(kind);
                }
            }
            IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .showView(viewID, kind,
                            IWorkbenchPage.VIEW_ACTIVATE);
            if (view instanceof AbstractEngineBasedView){
                ((AbstractEngineBasedView)view).setEngineSource(engine);
            }
            // Else SeeCode display has been aliased to a CDT display...
            return view;
        } catch (PartInitException e) {
            SeeCodePlugin.log(e);
        }
        return null;
    }
    
    /**
     * The font registry key that is maintained by the preference page.
     * It is defined as a "theme" extension.
     */
    public static final String SEECODE_FONT = "com.arc.cdt.debug.seecode.ui.SeeCodeFont";
    
    /**
     * The font registry key that is maintained by the preference page.
     * It is defined as a "theme" extension.
     */
    private static final String SEECODE_FOREGROUND = "com.arc.cdt.debug.seecode.ui.SeeCodeColor";
    private static final String SEECODE_BACKGROUND = "com.arc.cdt.debug.seecode.ui.color.background";
    private static final String SEECODE_ERRORLOG_BACKGROUND = "com.arc.cdt.debug.seecode.ui.errorlog.background";
    private static final String SEECODE_ERRORLOG_FOREGROUND = "com.arc.cdt.debug.seecode.ui.errorlog.foreground";
    private static final String SEECODE_OVERLAY_BACKGROUND = "com.arc.cdt.debug.seecode.ui.color.overlay";
    private static final String SEECODE_READONLY_BACKGROUND = "com.arc.cdt.debug.seecode.ui.color.readonly";
    private static final String SEECODE_MISALIGNED_BACKGROUND = "com.arc.cdt.debug.seecode.ui.color.misaligned";



    public static boolean isKnownColorProperty(String prop){
        return SEECODE_FOREGROUND.equals(prop) ||
               SEECODE_BACKGROUND.equals(prop) ||
               SEECODE_OVERLAY_BACKGROUND.equals(prop) ||
               SEECODE_READONLY_BACKGROUND.equals(prop) ||
               SEECODE_MISALIGNED_BACKGROUND.equals(prop) ||
               SEECODE_ERRORLOG_BACKGROUND.equals(prop) ||
               SEECODE_ERRORLOG_FOREGROUND.equals(prop);
    }

    
    /**
     * Return the font that is to apply to each SeeCode display, and
     * to any editable comboboxes. Its is configurable from the Preferences
     * dialog.
     * @return the seecode display font.
     */
    public static Font getSeeCodeFont(){
        return JFaceResources.getFont( SEECODE_FONT);
    }
    
    private static IColorPreferences COLOR_PREFERENCES = null;
    
    /**
     * Return preference colors for MetaWare debugger displays.
     * @return the seecode display color.
     */
    public static IColorPreferences getColorPreferences(){
        if (COLOR_PREFERENCES == null) {
            COLOR_PREFERENCES = new IColorPreferences() {
            private IColor getColor(String prefID){
                RGB rgb = JFaceResources.getColorRegistry().getRGB(prefID);
                return rgb != null?getWidgetFactory().makeColor(rgb.red,rgb.green,rgb.blue):null;
            }
            @Override
            public IColor getForegroundColor () {
                return getColor(SEECODE_FOREGROUND);
            }

            @Override
            public IColor getBackgroundColor () {
                return getColor(SEECODE_BACKGROUND);
            }

            @Override
            public IColor getOverlayBackgroundColor () {
                return getColor(SEECODE_OVERLAY_BACKGROUND);
            }

            @Override
            public IColor getReadonlyBackgroundColor () {
               return getColor(SEECODE_READONLY_BACKGROUND);
            }

            @Override
            public IColor getMisalignedBackgroundColor () {
                return getColor(SEECODE_MISALIGNED_BACKGROUND);
            }
            @Override
            public IColor getErrorLogBackgroundColor () {
                return getColor(SEECODE_ERRORLOG_BACKGROUND);
            }
            @Override
            public IColor getErrorLogForegroundColor () {
                return getColor(SEECODE_ERRORLOG_FOREGROUND);

            }};
        }
        return COLOR_PREFERENCES;
    }
    
    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param context
     * @throws Exception
     */
    @Override
    public void start (BundleContext context) throws Exception {
        super.start(context);
        fAnimateToolBarMgr = new AnimateToolBarManager();
        IAdapterManager manager= Platform.getAdapterManager();
		SeeCodeAdapterFactory f = new SeeCodeAdapterFactory();
		manager.registerAdapters( f, ILaunch.class );
    }
    
    public AnimateToolBarManager getAnimateToolBarManager(){
        return fAnimateToolBarMgr;
    }


    @Override
    public void stop(BundleContext context) throws Exception {
        // If SeeCode core plugin hasn't yet stopped, then
        // close all active sessions. Otherwise, the engine may
        // be sending updates to dead plugins while Eclipse is
        // shutting down. This causes all sorts of errors to
        // appear in the error log.
        if (!SeeCodePlugin.getDefault().isStopped()){
            SeeCodePlugin.getDefault().shutdownSessions();
        }
        super.stop(context);
    }
    
    /**
     * The default seecode options for any new configurations.
     */
    private static QualifiedName GENERIC = computeSeeCodeOptionName("*");
    
    /**
     * The old MetaDeveloper importer will have retrieved SeeCode options that need
     * to be stored someplace, so that they can be used as initial values when
     * a new Launch configuration is built.
     * <P>
     * We base them on the project and a configuration ID, since mult-project project
     * spaces can be imported into the same project in different configurations.
     * @param project the project
     * @param configID the configuration ID
     * @param options the initial seecode options for any launch configuration based on
     * this project.
     */
    public void setDefaultSeeCodeOptions(IProject project, String configID, Map<String,String> options){
        QualifiedName name = computeSeeCodeOptionName(configID);
        StringBuilder encoding = new StringBuilder(100);
   
        //Sort the keys so that we can compare below to storing identical maps for
        // each configuration.
        List<String> sortedKeys = new ArrayList<String>(options.keySet());
        Collections.sort(sortedKeys);
        for (String key: sortedKeys){
            encoding.append(key);
            encoding.append('=');
            encoding.append(options.get(key));
            encoding.append('\n');
        }
        // Avoid storing options for each configuration if they are all identical.
        try {
            String encoded = encoding.toString();
            String prev = project.getPersistentProperty(GENERIC);
            if (prev == null){
                project.setPersistentProperty(GENERIC,encoded);
            }
            else if (!prev.equals(encoded))
                project.setPersistentProperty(name,encoded);         
        } catch (CoreException e) {
            SeeCodePlugin.log(e);
        }
    }


    /**
     * @param configID the configuration ID.
     * @return seecode option name for the persistent storage, given a configuration ID.
     */
    private static QualifiedName computeSeeCodeOptionName(String configID) {
        return new QualifiedName("SeeCodeOptions",configID);
    }
    
    /**
     * Retrieve any initial seecode options that may have been imported from
     * an old MetaDeveloper project.
     * @return any initial seecode options imported from old MetaDeveloper, or <code>null</code>.
     */
    public Map<String,String> getDefaultSeeCodeOptions(IProject project, String configID){
        try {
            String encoding = project.getPersistentProperty(computeSeeCodeOptionName(configID));
            if (encoding == null) {
                encoding = project.getPersistentProperty(GENERIC);
                if (encoding == null) return null;
            }
            HashMap<String,String> map = new HashMap<String,String>();
            String keyValue[] = encoding.split("\\n");
            for (String kv: keyValue){
                int index = kv.indexOf('=');
                String key = kv.substring(0,index);
                String value = kv.substring(index+1);
                map.put(key,value);
            }
            return map;
        } catch (CoreException e) {
            SeeCodePlugin.log(e);
            return null;
        } catch (RuntimeException e){
            SeeCodePlugin.log(e);
            return null;
        }
    }
    
    /**
     * Returns the image descriptor with the given relative path.
     */
    public ImageDescriptor getImageDescriptor (String relativePath) {
        URL url = FileLocator.find(getBundle(),new Path(relativePath),null);
        return url != null?ImageDescriptor.createFromURL(url):null;
    }
    
    public Image getImage(String relativePath){
        ImageRegistry registry = getImageRegistry();
        Image image = registry.get(relativePath);
        if (image == null){
            ImageDescriptor desc = getImageDescriptor(relativePath);
            if (desc != null) {
                registry.put(relativePath,desc); 
                image = registry.get(relativePath);
            }
        }
        return image;
    }

    public Image getDebuggerIcon(){
        return getImage("icons/small_meta.gif");
    }
    
    public static IStatus makeErrorStatus(String msg) {
        return new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, msg, null);
    }
    
    public static IStatus makeErrorStatus(String msg, Throwable t) {
        return new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, msg, t);
    }
    
    /**
     * Show an error box.
     * (We use JFace ErrorDialog instead of MessageBox. The latter is done outside
     * of Java and the WindowTester framework loses control).
     * @param title
     * @param message
     */
    public static void showError(String title, String message){
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        ErrorDialog.openError(shell, title, null, makeErrorStatus(message));
    }
    
    private static final int NOTE_TIME_OUT = 12000;
    
    /**
     * Show an note box.
     * (We use JFace ErrorDialog instead of MessageBox. The latter is done outside
     * of Java and the WindowTester framework loses control).
     * @param title
     * @param message
     */
    public static void showNote(String title, String message){
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        MessageDialog dialog = new MessageDialog(shell, title, null, // accept
            // the
            // default
            // window
            // icon
            message, MessageDialog.INFORMATION,
            new String[] { IDialogConstants.OK_LABEL }, 0);
        dialog.setBlockOnOpen(false); // we want to be able to make box expire
        // ok is the default
        dialog.open();
        long expireTime = System.currentTimeMillis() + NOTE_TIME_OUT;
        Display display = shell.getDisplay();
        try {
            while (dialog.getShell() != null && !dialog.getShell().isDisposed() && System.currentTimeMillis() < expireTime) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
        }
        finally {
            if (dialog.getShell() != null && !dialog.getShell().isDisposed()) {
                dialog.close();
            }
        }
    }
    
    /**
     * Show an error box.
     * (We use JFace ErrorDialog instead of MessageBox. The latter is done outside
     * of Java and the WindowTester framework loses control).
     * @param title
     * @param msg
     */
    public static void showError(String title, String msg, Throwable e){
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        ErrorDialog.openError(shell, title, null, makeErrorStatus(msg,e));
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
}
