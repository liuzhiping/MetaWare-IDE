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


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

import com.arc.cdt.debug.seecode.core.ISeeCodeLaunchConfigurationConstants;
import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.core.launch.ICMPDInfo;
import com.arc.mw.util.StringUtil;
import com.arc.seecode.engine.ProcessIdList;


/**
 * Extracts information from ILaunchConfiguration so that the caller can prepare a debugger session.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class CMPDInfo implements ICMPDInfo {

    @SuppressWarnings("unchecked")
    public static ICMPDInfo extractCMPDInfo (ILaunchConfiguration config) throws CoreException {
        int cmpdProcessCount = getCMPDProcessCount(config);
        List<CMPDProcess> info = new ArrayList<CMPDProcess>();
        int defaultPID = 1;
        for (int i = 0; i < cmpdProcessCount; i++) {
            String projectName = getCMPDProjectName(config, i);
            String processName = getCMPDProcessName(config, i);
            if (processName == null)
                throwCoreException("Missing CMPD process name");
            String encoding = getCMPDProcessIdList(config,i);
            ProcessIdList list;
            if (encoding != null) {
                list = ProcessIdList.create(encoding);
            }
            else {
                // Older project
                int count = getCMPDProcessInstanceCount(config, i);
                if (count == 0)
                    throwCoreException("0 instance count for CMPD process " + processName);
                list = new ProcessIdList();
                list.addRange(defaultPID, defaultPID+count-1);
                defaultPID += count;
            }
            String[] command = getCMPDCommand(config, i);
            IProject project = lookupProject(projectName);
            if (command == null || command.length == 0)
                throwCoreException("Missing exe path for CMPD process " + processName);
//            if (!new File(command[0]).isAbsolute() && project != null) {
//                command[0] = project.getFile(command[0]).getLocation().toOSString();
//            }
            String[] swahili = getCMPDSwahili(config, i);
            if (swahili == null || swahili.length == 0)
                throwCoreException("Missing engine args for CMPD process " + processName);
            Map<String,String>map = config.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_GUIHILI_PROPERTIES + "_" +i, (Map<String,String>)null);
            info.add(new CMPDProcess(command, project, processName, list, swahili,map));
        }
        return new CMPDInfo(info.toArray(new CMPDProcess[info.size()]), getCMPDLaunchArgs(config));
    }

    private CMPDProcess[] fProcesses;
    private String[] fLaunchArgs = new String[0];
    private String[] fStartupCommands = new String[0];
    
    private CMPDInfo(CMPDProcess[] processes, String[] launchArgs){
        fProcesses = processes;
        // Look for "-cmd=..." and break it out. We cannot rely on the debugger engine
        // to process this because, under the IDE, the processes are loaded individually -- not in
        // one swoop. Thus, the IDE must execute the commands after the last CMPD process is loaded.
        if (launchArgs != null) {
            List<String>startupCommands = new ArrayList<String>();
            List<String>otherArgs = new ArrayList<String>();
            for (String s: launchArgs){
                if (s.startsWith("-cmd=")){
                    startupCommands.add(s.substring(5));
                }
                else otherArgs.add(s);
                fLaunchArgs = otherArgs.toArray(new String[otherArgs.size()]);
                fStartupCommands = startupCommands.toArray(new String[startupCommands.size()]);
            }
        }
    }
    
    @Override
    public String[] getLaunchArgs () {
       return fLaunchArgs;
    }

    @Override
    public IProcess[] getProcesses () {
        return fProcesses;
    }
    
    @Override
    public String[] getStartupCommands() {
        return fStartupCommands;
    }

    static class CMPDProcess implements IProcess {

        private String[] fCommand;

        private IProject fProject;

        private String fProcessName;

        private ProcessIdList fList;

        private String[] fSwahiliArgs;
        
        private Map<String,String> fProperties;

        private CMPDProcess(
            String[] command,
            IProject project,
            String processName,
            ProcessIdList list,
            String[] swahiliArgs,
            Map<String,String>properties) {
            fCommand = command;
            fProject = project;
            fProcessName = processName;
            fList = list;
            fSwahiliArgs = swahiliArgs;
            fProperties = properties;
        }

       
        @Override
        public String[] getCommand () {
            return fCommand;
        }

      
        @Override
        public String[] getSwahiliArgs () {
            return fSwahiliArgs;
        }

      
        @Override
        public String getProcessName () {
            return fProcessName;
        }

       
        @Override
        public IProject getProject () {
            return fProject;
        }

      
        @Override
        public int getInstanceCount () {
            return fList.getCount();
        }
        
        @Override
        public ProcessIdList getIDList(){
            return fList;
        }
        
        @Override
        public Map<String,String>getGuihiliProperties(){
            return fProperties;
        }

 
    }
    
    private static int getCMPDProcessCount (ILaunchConfiguration config) throws CoreException {
        return config.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_COUNT, 0);
    }

    private static String getCMPDProcessName (ILaunchConfiguration config, int which) throws CoreException {
        return config.getAttribute(
            ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_PROCESS_NAME + which,
            (String) null);
    }

    private static String getCMPDProjectName (ILaunchConfiguration config, int which) throws CoreException {
        return config.getAttribute(
            ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_PROJECT_NAME + which,
            (String) null);
    }

    private static int getCMPDProcessInstanceCount (ILaunchConfiguration config, int which) throws CoreException {
        return config
            .getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_PROCESS_INSTANCE_COUNT + which, 0);
    }

    private static String getCMPDProcessIdList (ILaunchConfiguration config, int which) throws CoreException {
        return config
            .getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_PROCESS_ID_LIST + which, (String)null);
    }
    @SuppressWarnings("unchecked")
    private static String[] getCMPDLaunchArgs (ILaunchConfiguration config) throws CoreException {
        List<String> s = config.getAttribute(
            ISeeCodeLaunchConfigurationConstants.ATTR_SWAHILI_ARGS,
            (List<String>) null);
        if (s != null) {
            return s.toArray(new String[s.size()]);
        }
        return null;
    }

    private static String[] getCMPDCommand (ILaunchConfiguration config, int which) throws CoreException {
        String s = config.getAttribute(
            ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_PROCESS_PATH + which,
            (String) null);
        if (s != null) {
            // if relative path, then assume relative to project.
            String[]cmd = StringUtil.stringToArray(s);
            // if relative path, then assume relative to project location.
            if (cmd.length > 0 && !new File(cmd[0]).isAbsolute()){
                String projectName = getCMPDProjectName(config,which);
                if (projectName != null){
                    IProject project = lookupProject(projectName);
                    if (project != null) {
                       cmd[0] = new File(project.getLocation().toOSString(),cmd[0]).toString();
                    }
                }                
            }
            return cmd;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static String[] getCMPDSwahili (ILaunchConfiguration config, int which) throws CoreException {
        List<String> s = config.getAttribute(
            ISeeCodeLaunchConfigurationConstants.ATTR_SWAHILI_ARGS + "_" + which,
            (List<String>) null);
        if (s != null) {
            return s.toArray(new String[s.size()]);
        }
        return null;
    }
    
    private static IProject lookupProject (String projectName) {
        if (projectName == null || projectName.trim().length() == 0)
            return null;
        return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
    }

    private static void throwCoreException (String msg) throws CoreException {
        throw new CoreException(SeeCodePlugin.makeErrorStatus(msg));
    }


}
