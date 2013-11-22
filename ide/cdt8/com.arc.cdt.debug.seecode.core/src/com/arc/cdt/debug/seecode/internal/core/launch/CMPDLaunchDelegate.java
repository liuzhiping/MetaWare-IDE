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
package com.arc.cdt.debug.seecode.internal.core.launch;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISessionConfiguration2;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ISuspendResume;

import com.arc.cdt.debug.seecode.core.ISeeCodeLaunchConfigurationConstants;
import com.arc.cdt.debug.seecode.core.SeeCodeDebugger;
import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.core.cdi.ICMPDTarget;

/**
 * Launch delegate for CMPD.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class CMPDLaunchDelegate extends AbstractCLaunchDelegate  {

    public CMPDLaunchDelegate() {
        // @todo Auto-generated constructor stub
    }

    /**
     * Launch a CMPD session.
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param configuration
     * @param mode
     * @param launch
     * @param monitor
     * @throws CoreException
     */
    @Override
    public void launch (ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
        throws CoreException {
        if ( monitor == null ) {
            monitor = new NullProgressMonitor();
        }
        if ( mode.equals( ILaunchManager.DEBUG_MODE ) ) {
            launchCMPD( configuration, launch, monitor );
        }      
    }
    
    private void launchCMPD(ILaunchConfiguration config, ILaunch launch, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask("CMPD Launch",7);
        setDefaultSourceLocator( launch, config );
        ICDISession session = new SeeCodeDebugger().createSession(launch, null, monitor);
        monitor.worked( 6 );
        try {
            Process debugger = session.getSessionProcess();
            if (debugger != null) {
                IProcess debuggerProcess = DebugPlugin.newProcess(launch, debugger, renderDebuggerProcessLabel(((ICDISessionConfiguration2)session.getConfiguration()).getSessionProcessName()));
                launch.addProcess(debuggerProcess);
            }
        }
        catch (CDIException e) {
            // Presumably this error will be caught later
        }

        //setRuntimeOptions( config, session ); -- information already extracted for CMPD processes
        monitor.worked( 1 );

        try {
            boolean stopInMain = false/* config.getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, false )*/;
            boolean resumeAtStart = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_RESUME_AT_START, true);
            String stopSymbol = null;
            if ( stopInMain )
                stopSymbol = launch.getLaunchConfiguration().getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT );
            ICDITarget[] targets = session.getTargets();
            List<IDebugTarget> cTargets = new ArrayList<IDebugTarget>();
            for( int i = 0; i < targets.length; i++ ) {
                Process process = targets[i].getProcess();
                ICMPDTarget cmpd = (ICMPDTarget)targets[i];
                IProject project = cmpd.getProject();
                
                IPath exePath = cmpd.getExePath();
                ICProject cProject = null;
                if (project != null) {
                    cProject = CCorePlugin.getDefault().getCoreModel().create(project);
                    if (!exePath.isAbsolute()){
                        exePath = project.getFile(exePath).getLocation();
                    }
                }
                IBinaryParser.IBinaryObject exeBin = verifyBinary(cProject,exePath);

                IProcess iprocess = null;
                if ( process != null ) {
                    iprocess = DebugPlugin.newProcess( launch, process, renderProcessLabel( cmpd.getExePath().toOSString() ), getDefaultProcessMap() );
                }
                String processName = cmpd.getProcessName();
                processName += "[" + cmpd.getProcessId() + "]";
                cTargets.add(CDIDebugModel.newDebugTarget( launch, cmpd.getProject(), targets[i], renderProcessLabel( processName ), iprocess, exeBin, true, false, stopSymbol, false));
            }
            // Now that they are created, resume them.
            if (resumeAtStart){
                ((ISuspendResume)session).resume();
            }
        }
        catch (CoreException e) {
            try {
                if ( session != null )
                    session.terminate();
            }
            catch( CDIException e1 ) {
                // ignore
            }
            throw e;
        }
        finally {
            monitor.done();
        }       
    }

    @Override
    protected String getPluginID () {
        return SeeCodePlugin.getUniqueIdentifier();
    }
    
    @Override
    public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {

		workspaceBuildBeforeLaunch = true;
		
		// check the build before launch setting and honor it
		int buildBeforeLaunchValue = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_BUILD_BEFORE_LAUNCH,
				ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_USE_WORKSPACE_SETTING);

		// we shouldn't be getting called if the workspace setting is disabled, so assume we need to
		// build unless the user explicitly disabled it in the main tab of the launch.
		if (buildBeforeLaunchValue == ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_DISABLED) {
			return false;
		}
		
				
		if(monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		int scale = 1000;
		int count = configuration.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_COUNT, 0);
		if(count == 0)
			return false;
		int totalWork = (count + 1) * scale;
		
		try {
			monitor.beginTask(LaunchMessages.AbstractCLaunchDelegate_building_projects, totalWork); 

			try {
				for ( int i = 0; i <count; i ++){
      			  String pName = configuration.getAttribute(
      			            ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_PROJECT_NAME + i,
      			            (String) null);
      			  List projects;
      			  
      			  if(pName != null){

      			      ICProject cProject = CDebugUtils.getCProject(pName);
      			      if (cProject != null) {
      			    	  IProject proj = cProject.getProject();
					
					      monitor.subTask(LaunchMessages.AbstractCLaunchDelegate_building + proj.getName()); 
					      
					      buildProject(proj, monitor,scale);
				        }
      			   }
				}

				
			} catch (CoreException e) {
				// Catch CoreException or OperationCancelledException possibly thrown by the build contract.
				// Still allow the user to continue to the launch
				buildFailed = true;
			}
		} finally {
			monitor.done();
		}

		return false; 
	}

    
}
