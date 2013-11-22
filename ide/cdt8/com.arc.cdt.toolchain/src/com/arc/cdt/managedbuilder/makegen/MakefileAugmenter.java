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
package com.arc.cdt.managedbuilder.makegen;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IContributedEnvironment;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.makegen.IMakefileAugmenter;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;


/**
 * Insert the code for finding a Shell under Windows within the auto-generated makefile.
 * This is what is invoked for the MakefileAugmenter extension in managed make package.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class MakefileAugmenter implements IMakefileAugmenter {
    
    private static final String DELETE_EXE = "rm.exe";
	private static final String NEWLINE = IManagedBuilderMakefileGenerator.NEWLINE;
    private IConfiguration config;
    private String rmPath = null;
    private File shellPath;

    public String generateMacroDefinitions (IConfiguration configuration) {
        this.config = configuration;
        // If under Windows, we must guarantee access to "sh.exe". If it isn't on the search
        // path, then we force it to reference the one in the MetaWare toolset.
        if (isWindows()){
            StringBuilder buffer = new StringBuilder(300);
            handleWindowsShellIssues(buffer,configuration); 
            return buffer.toString();
        }
        return null;
    }
    
    private static String getEnvironmentSetting(String name, IConfiguration configuration){
        ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
        ICProjectDescription pd = mngr.getProjectDescription(configuration.getOwner().getProject(), false);
        if (pd != null) {
            ICConfigurationDescription des = pd.getConfigurationByName(configuration.getName());
            if (des != null) {
                IContributedEnvironment ice = CCorePlugin.getDefault().getBuildEnvironmentManager()
                    .getContributedEnvironment();
                IEnvironmentVariable envVar = ice.getVariable(name, des);
                if (envVar != null)
                    return envVar.getValue();                 
            }
        }
        return System.getenv(name);
    }
    
    private void handleWindowsShellIssues (StringBuilder buffer, IConfiguration configuration) {
        shellPath = findPathTo("sh.exe");
        if (shellPath == null) {
            // No "sh.exe"; we define our own
            buffer.append("# \"sh.exe\" was not found in search path; using Windows cmd.exe" + NEWLINE + NEWLINE);
        }
        else {
            buffer.append("#SHELL=" + shellPath + NEWLINE+NEWLINE);
            // The latest Cygwin shell requires that nodosfilewarning get set in the CYGWIN
            // environment variable.
            String cygwin = getEnvironmentSetting("CYGWIN",configuration);
            String shellopts = getEnvironmentSetting("SHELLOPTS",configuration);
            if (cygwin != null) {
                buffer.append("# CYGWIN=" + cygwin + NEWLINE);
            }
            else {
                buffer.append("# This setting prevents the \"MS-DOS style path detected\" warning from being" + NEWLINE);
                buffer.append("# generated from the Cygwin shell. This definition is not emitted if CYGWIN" + NEWLINE);
                buffer.append("# is already defined in the environment." + NEWLINE);
                buffer.append("export CYGWIN=nodosfilewarning" + NEWLINE + NEWLINE);
            }
            if (shellopts != null) {
                buffer.append("# SHELLOPTS=" + shellopts + NEWLINE);
            }
            else {
                buffer.append("# This setting is required for the Cygwin shell to recognize the DOS cr/lf" + NEWLINE);
                buffer.append("# line-termination convention. This definition is not emitted if SHELLOPTS" + NEWLINE);
                buffer.append("# is already defined in the environment." + NEWLINE);
                buffer.append("export SHELLOPTS=igncr" + NEWLINE + NEWLINE);     
            }
        }
        File rmExe = findPathTo("rm.exe");
        if (rmExe == null) {
            String installURI = System.getProperty("osgi.install.area");
            if (installURI != null) {
                try {
                    File ideDir = new File(new URI(installURI).toURL().getFile());
                    if (ideDir.isDirectory()) {
                        File installDir = new File(ideDir.getParentFile(), "utils");
                        rmExe = new File(installDir, DELETE_EXE);
                        if (rmExe.exists())
                            rmPath = rmExe.toString();
                        else {
                            rmExe = new File(installDir, "rm.exe"); // Old name
                            if (rmExe.exists()) {
                                rmPath = rmExe.toString();
                            }
                            else
                                rmExe = null;
                        }
                    }
                }
                catch (MalformedURLException e) {
                    // Ignore
                }
                catch (URISyntaxException e) {
                    // Ignore
                }
            }
            // We're Desperate. The IDE must be in a different location than
            // usual.

            if (rmExe == null) {
                File f = new File("C:\\ARC\\MetaWare\\ide\\utils\\" + DELETE_EXE);
                if (f.exists())
                    rmPath = f.getPath();
            }
            if (rmPath != null) {
                buffer.append("# Could not find \"rm.exe\" on the search path;" + NEWLINE);
                buffer.append("# the command \"" + rmPath + "\" will be used instead." + NEWLINE+NEWLINE);
            }
            if (shellPath != null && rmPath != null) {
                rmPath = rmPath.replaceAll("\\\\", "/"); // Assume Unix shell needs to see forward slashes
            }
        }
    }
    
    private File findPathTo(String cmd, String pathString){
        String paths[] = pathString.split(";");
        return findPathTo(cmd, paths);
    }

    /**
     * Return full path to command, given search path strings.
     * @param cmd the command without any directory qualification.
     * @param paths paths to searcyh.
     * @return the File object that references the command.
     */
    private File findPathTo (String cmd, String[] paths) {
        for (String path: paths){
            File f = new File(path,cmd);
            if (f.exists()) return f;
        }
        return null;
    }

    private static boolean isWindows(){
        return System.getProperty("os.name").indexOf("indow") > 0;
    }
    
    /**
     * Return the value of an environment variable by first looking in the user-defined
     * environment addition, and then the system environment.
     * @param key the environment symbol whose value we want.
     * @return the value of an environment symbol.
     */
    private String getEnv(String key){
        if (config != null) {
            IEnvironmentVariable envPath = ManagedBuildManager.getEnvironmentVariableProvider().getVariable(key, config, false);
            if (envPath != null) {
                return envPath.getValue();
            }
        }
        return System.getenv(key);
    }
    
    private  File findPathTo(String cmd){      
        String pathString = getEnv("PATH");
        if (pathString != null){
            return findPathTo(cmd,pathString);
        }
        return null;
    }

    public String getCleanCommand (IConfiguration configuration) {
        if (rmPath != null){
        	String s;
        	if (rmPath.indexOf(' ') > 0)
        		s = "\"" + rmPath + "\"";
        	else s = rmPath;
        	if (rmPath.endsWith("rm.exe")) s += " -f";
            return s;
        }
        return null;
    }

    public String canonicalizePath (String path) {
        // If Unix-like shell being used, then default behavior is acceptable.
        if (shellPath != null)
            return null;
        // Fix things so that cmd.exe works.
        if (path.indexOf(' ') >= 0 && config != null){
            if (path.startsWith("\"") && path.endsWith("\"") && path.length() > 1){
                path = path.substring(1,path.length()-1);
            }
            IProject project = config.getOwner().getProject();
            if (project != null) {
                if (project.getLocation().isPrefixOf(new Path(path))){
                    String projString = project.getLocation().toString();
                    String path_ = new Path(path).toString();
                    return "$(PROJECT)" + path_.substring(projString.length());
                }
            }   
            IPath ws = Platform.getLocation();
            if (ws.isPrefixOf(new Path(path))){
                String wsString = ws.toString();
                String path_ = new Path(path).toString();
                return "$(WORKSPACE)" + path_.substring(wsString.length());
            }
        }
        return null;
    }
}
