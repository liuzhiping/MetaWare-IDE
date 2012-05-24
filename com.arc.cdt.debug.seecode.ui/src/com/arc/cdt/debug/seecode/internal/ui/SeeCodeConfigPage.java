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
package com.arc.cdt.debug.seecode.internal.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.ui.AbstractCDebuggerPage;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;

import com.arc.cdt.debug.seecode.core.ISeeCodeLaunchConfigurationConstants;
import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.internal.ui.GuihiliPage.IErrorSetter;
import com.arc.cdt.debug.seecode.options.SeeCodeOptions;
import com.arc.cdt.debug.seecode.ui.UISeeCodePlugin;
import com.arc.cdt.debug.seecode.ui.Utilities;
import com.arc.cdt.debug.seecode.ui.views.SeeCodeDisasmView;
import com.arc.seecode.engine.EngineInterface;

/**
 * This class creates the debugger configuration page on the launcher page that
 * is associated with an invocation.
 * <P>
 * The old Guihili processor was ported to SWT. The Guihili properties are saved
 * within the plugin supplied preference page.
 * <P>
 * Guihili has a property named "ARGS_ACTION" that contains a list of
 * command-line arguments to be passed to the "Swahili" script. This script, in
 * turns, creates the arguments to be passed to the seecode engine via its
 * {@link EngineInterface#setEngineArguments(String)}method.
 * 
 * @author David Pickens
 */
@SuppressWarnings({ "restriction", "deprecation" })
public class SeeCodeConfigPage extends AbstractCDebuggerPage implements ILaunchConfigurationListener {


    private GuihiliPage fGuihiliPage;
    
    private boolean fUpdatePending = false;
    
    public SeeCodeConfigPage() {
        fGuihiliPage = new GuihiliPage(new Runnable() {

            @Override
            public void run () {
                SeeCodeConfigPage.this.getLaunchConfigurationDialog().updateButtons();
            }
        }, new IErrorSetter() {

            @Override
            public void setErrorMessage (String msg) {
                
                boolean canSave = msg == null || msg.trim().length() == 0;
                SeeCodeConfigPage.this.setErrorMessage(msg);
                if (!fUpdatePending) {
                    fUpdatePending = true; // Avoid infinite recursion
                    try {
                        SeeCodeConfigPage.this.getLaunchConfigurationDialog().updateMessage();
                        if (canSave != mCanSave) {
                            mCanSave = canSave;
                            SeeCodeConfigPage.this.getLaunchConfigurationDialog().updateButtons();
                        }
                    }
                    finally{
                        fUpdatePending = false;
                    }
                }               
            }
        });
        
        //CR90987: we need to invoke the OK action when the "Apply" button is set
        // so that errors and warnings can be emitted from the "save_on" action.
        // But this method is called anytime the configuration is altered before
        // committing it! So, we must resort to creating a launch listener so
        // that we can actually do a "OK" action when the configuration is saved.
        // Whatever error boxes pop up will have to be serviced after the
        // configuration is saved!
        ILaunchManager mgr = DebugPlugin.getDefault().getLaunchManager();
        mgr.addLaunchConfigurationListener(this);

    }
    
    private boolean mCanSave = true;

    private String mTarget = null;

    private String mProgram = null;

    private String mProject = null;

    private boolean mNoGoIfMainSpecified;

    private String fLastConfigModified;
    
    @Override
    public boolean canSave(){
        return mCanSave;
    }

    /**
     * Here we create the Guihili-based panel.
     */
    @Override
    public void createControl(Composite parent) {
       
       fGuihiliPage.createControl(parent);
       setControl(fGuihiliPage.getControl());
       fGuihiliPage.getControl().addDisposeListener(new DisposeListener(){

		@Override
        public void widgetDisposed(DisposeEvent e) {
			ILaunchManager mgr = DebugPlugin.getDefault().getLaunchManager();
	        mgr.removeLaunchConfigurationListener(SeeCodeConfigPage.this);
			
		}});
    }


    /* override */
    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        fGuihiliPage.setDefaults(new MyGuihiliCallback(configuration));
        setSpecialMetaWareAttributes(configuration);
        recordState(configuration);
    }

    /**
     * Set the process ID factory that corresponds to the
     * <code>org.eclipse.debug.core.processFactories</code> extension point
     * that is it so supply the <code>IProcess</code> object for referencing
     * the SeeCode engine process.
     * <P>
     * Also set the alternate dissassembly display.
     * 
     * @param config
     *            the configuration to be modified.
     */
    public static void setSpecialMetaWareAttributes(ILaunchConfigurationWorkingCopy config) {
        config.setAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID,
                SeeCodePlugin.PROCESS_FACTORY_ID);
        // We've modified CDT so that it looks here for a disassembly view and uses it
        // if not null.
        config.setAttribute(
            ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_DISASSEMBLY_VIEW_ID,
            SeeCodeDisasmView.DISASM_VIEW_ID);
    }

    /**
     * Record the target, program and project so that we can detect a
     * configuration change which would trigger a regeneration of the guihili
     * 
     * @param configuration
     */
    private void recordState(ILaunchConfiguration configuration) {
        // Remember so that we know when to regenerate things
        mProgram = getProgram(configuration);
        String target = getTargetCpuName(configuration);
        // As someone is typing in a program name, don't nullify the target before the path is completed.
        // It causes havoc with regenerating guihili each time.
        if (target != null)
            mTarget = target;

        mProject = Utilities.getProjectName(configuration);

    }


    /**
     * @param configuration
     * @return @throws
     *         CoreException
     */
    private String getProgram(ILaunchConfiguration configuration) {
        try {
            return configuration.getAttribute(
                    ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME,
                    (String) null);
        } catch (CoreException e) {
            return null;
        }
    }


    /**
     * 
     * @param configuration
     */
    private void update(
            ILaunchConfigurationWorkingCopy configuration) {
 
        // See comments on this method:
        setSpecialMetaWareAttributes(configuration);
        
        configuration.setAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_TARGET_CPU, mTarget);
        
        // We've modified CDT so that it looks here for a disassembly view and uses it
        // if not null.
        configuration.setAttribute(
            ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_DISASSEMBLY_VIEW_ID,
            SeeCodeDisasmView.DISASM_VIEW_ID);
        
        // If "-nogoifmain" option set, then turn off "stop in main" box.
        String cmd = fGuihiliPage.getProperty("cmd_line_option");
        if (cmd != null && cmd.indexOf("-nogoifmain") >=0){
            boolean stopAtMain = false;
            try {
                stopAtMain = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN,true);
            } catch (CoreException e) {
            }
            if (stopAtMain){
                mNoGoIfMainSpecified = true;
                configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN,false); 
                
            }
        }
        else if (mNoGoIfMainSpecified){
            //-nogoifmain specified, then unspecified
            mNoGoIfMainSpecified = false;
            configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN,true); 
        }
        recordState(configuration);

    }

    private static boolean compare(String s1, String s2) {
        if (s1 != null)
            return s1.equals(s2);
        return s2 == null;
    }

    private boolean configurationChanged(ILaunchConfiguration config) {
        String target = getTargetCpuName(config);
        String program = getProgram(config);
        String project = Utilities.getProjectName(config);
        return !compare(target, mTarget) || !compare(program, mProgram)
                || !compare(project, mProject);

    }
    
    /* override */
    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        recordState(configuration);
        fGuihiliPage.initializeFrom(new MyGuihiliCallback(configuration));        
        setDirty(fGuihiliPage.isDirty());

        // If target CPU changed, then arrange to update buttons
        try {
            if (this.getControl() != null && (isDirty() || mTarget != null && !mTarget.equals(configuration.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_TARGET_CPU,"")))){
                Display d = this.getControl().getDisplay();
                d.asyncExec(new Runnable() {
                    @Override
                    public void run () {
                        
                        ILaunchConfigurationDialog launchConfigurationDialog = SeeCodeConfigPage.this.getLaunchConfigurationDialog();
                        // WHY DO THIS? IF THE USER WANTS THE DEBUG TAB ACTIVE AFTER A CHANGE, LET HIM DO IT HIMSELF
//                        ILaunchConfigurationTab tabs[] = launchConfigurationDialog.getTabs();
//                        // Find the CDebugTab and make it current; don't know
//                        // how to do this better.
//                        for (ILaunchConfigurationTab tab: tabs){
//                            if (tab instanceof CDebuggerTab){
//                                launchConfigurationDialog.setActiveTab(tab);
//                                break;
//                            }
//                        }
                        launchConfigurationDialog.updateButtons();
                    }
                });
            }
        }
        catch (CoreException e1) {
            //Shouldn't happen
        }
    }

 
    
    /**
     * Returns the current C element context from which to initialize default
     * settings, or <code>null</code> if none. Note, if possible we will
     * return the IBinary based on config entry as this may be more usefull then
     * just the project.
     * 
     * NOTE: this code was copied from AbstractCDebuggerTab that no longer
     * was accessible since the CDT folks refactored things (2/8/06)
     * 
     * @return C element context.
     */
    public static ICElement getContext(ILaunchConfiguration config, String platform) {
        String projectName = null;
        String programName = null;
        IWorkbenchPage page = LaunchUIPlugin.getActivePage();
        Object obj = null;
        try {
            projectName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
            programName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, (String)null);
        } catch (CoreException e) {
        }
        if (projectName != null && !projectName.equals("")) { //$NON-NLS-1$
            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
            ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(project);
            if (cProject != null && cProject.exists()) {
                obj = cProject;
            }
        } else {
            if (page != null) {
                ISelection selection = page.getSelection();
                if (selection instanceof IStructuredSelection) {
                    IStructuredSelection ss = (IStructuredSelection)selection;
                    if (!ss.isEmpty()) {
                        obj = ss.getFirstElement();
                    }
                }
            }
        }
        if (obj instanceof IResource) {
            ICElement ce = CoreModel.getDefault().create((IResource)obj);
            if (ce == null) {
                IProject pro = ((IResource)obj).getProject();
                ce = CoreModel.getDefault().create(pro);
            }
            obj = ce;
        }
        if (obj instanceof ICElement) {
            if (platform != null && !platform.equals("*")) { //$NON-NLS-1$
                ICDescriptor descriptor;
                try {
                    descriptor = CCorePlugin.getDefault().getCProjectDescription( ((ICElement)obj).getCProject().getProject(),
                            false);
                    if (descriptor != null) {
                        String projectPlatform = descriptor.getPlatform();
                        if (!projectPlatform.equals(platform) && !projectPlatform.equals("*")) { //$NON-NLS-1$
                            obj = null;
                        }
                    }
                } catch (CoreException e) {
                }
            }
            if (obj != null) {
                if (programName == null || programName.equals("")) { //$NON-NLS-1$
                    return (ICElement)obj;
                }
                ICElement ce = (ICElement)obj;
                IProject project;
                project = (IProject)ce.getCProject().getResource();
                IPath programFile;
                //<CUSTOMIZATION> handle absolute exe paths
                if (new File(programName).isAbsolute()){
                    programFile = new Path(programName);
                }
                //</CUSTOMIZATION>
                else {
                    programFile = project.getFile(programName).getLocation();
                }
                ce = CCorePlugin.getDefault().getCoreModel().create(programFile);
                if (ce != null && ce.exists()) {
                    return ce;
                }
                return (ICElement)obj;
            }
        }
        if (page != null) {
            IEditorPart part = page.getActiveEditor();
            if (part != null) {
                IEditorInput input = part.getEditorInput();
                return (ICElement) input.getAdapter(ICElement.class);
            }
        }
        return null;
    }
    
 
    /**
     * @param configuration
     * @return the target CPU name, or <code>null</code> if not known.
     */
    private static String getTargetCpuName(ILaunchConfiguration configuration) {
        String configPlatform = SeeCodeOptions.getPlatform(configuration);
        ICElement ce = getContext(configuration, configPlatform);
        return SeeCodeOptions.getTargetCpuName(configuration,ce);
    }

    /* override */
    @Override
    public void performApply (ILaunchConfigurationWorkingCopy configuration) {
        // This method is called as an unfortunate side-effect of the
        // call to "updateButtons" in the propertyChange listener that
        // is created above. We wish to bypass that action in such a case.
        fLastConfigModified = configuration.getName();

        try {
            if (fGuihiliPage.isDirty() ||
                configurationChanged(configuration) ||
                mTarget != null &&
                !mTarget.equals(configuration.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_TARGET_CPU, ""))) {
                this.update(configuration);
                this.fGuihiliPage.performApply(new MyGuihiliCallback(configuration));
            }
        }
        catch (CoreException e) {
            // do nothing
        }
    }

    /* override */
    @Override
    public String getName() {
        return UISeeCodePlugin.getDebuggerName() + " Configuration";
    }
    

    @Override
    public boolean isValid (ILaunchConfiguration launchConfig) {
        if (!mCanSave) return false;
        if (!super.isValid(launchConfig)) return false;
        return mTarget != null;
    }

    class MyGuihiliCallback implements IGuihiliCallback {
        private ILaunchConfiguration fConfig;
        private ILaunchConfigurationWorkingCopy fConfigModify = null;
        private Properties fProps = null;

        MyGuihiliCallback(ILaunchConfiguration config){
            fConfig = config;
        }
        MyGuihiliCallback(ILaunchConfigurationWorkingCopy config){
            fConfig = config;
            fConfigModify = config;
        }

        @Override
        public String getLaunchName () {
            return fConfig.getName();
        }

        @Override
        public IProject getProject () {
            return Utilities.getProject(fConfig);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Properties getProperties () {
            if (fProps == null) {
                fProps = new Properties();
                try {
                    Map<Object,Object> map = fConfig.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_GUIHILI_PROPERTIES,(Map<Object,Object>)null);
                    if (map != null) fProps.putAll(map);
                }
                catch (CoreException e) {
                    //Ignore
                }
            }
            return fProps;
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> getSwahiliArguments () {
            try {
                return fConfig.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_SWAHILI_ARGS, new ArrayList<String>(0));
            }
            catch (CoreException e) {
                return new ArrayList<String>(0);
            }
        }

        @Override
        public String getTargetCPU () {
            return mTarget;
        }

        @Override
        public void setProperties (Properties props) {
            if (fConfigModify == null) throw new IllegalStateException("Can't write");
            fConfigModify.setAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_GUIHILI_PROPERTIES,props);
            
        }

        @Override
        public void setSwahiliArguments (List<String> args) {
            if (fConfigModify == null) throw new IllegalStateException("Can't write");
            fConfigModify.setAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_SWAHILI_ARGS, args);
        }
        @Override
        public String[] getEnvironment () {
            try {
                return DebugPlugin.getDefault().getLaunchManager().getEnvironment(fConfig);
            }
            catch (CoreException e) {
                return null;
            }
        }
        @Override
        public File getWorkingDirectory () {
            IPath path;
            try {
                path = CDebugUtils.getWorkingDirectoryPath(fConfig);
            }
            catch (CoreException e) {
                path = null;
            }
            if (path != null) {
                path.toFile();
            }
            IProject p = Utilities.getProject(fConfig);
            if (p != null && p.getLocation() != null){
                return new File(p.getLocation().toOSString());
            }
            return new File("."); // shouldn't get here.
        }
        
        @Override
        public int getProcessCount(){
            return 1;
        }
    }

    @Override
    public void launchConfigurationAdded (ILaunchConfiguration configuration) {
       
        
    }

    //Called when configuration is actually saved; Need to invoke guihili's
    // "OK" action so that "save_on" actions will be fired.
    // (CR90987)
    @Override
    public void launchConfigurationChanged (ILaunchConfiguration configuration) {
        if (!configuration.isWorkingCopy() && configuration.getName().equals(fLastConfigModified)){
            if (this.fGuihiliPage != null) {
                this.getControl().getDisplay().syncExec(new Runnable(){
                    @Override
                    public void run () {
                        //Must be called in UI thread.
                        fGuihiliPage.performOK();                        
                    }});
                
            }
        }
        
    }

    @Override
    public void launchConfigurationRemoved (ILaunchConfiguration configuration) {
            
    }
}
