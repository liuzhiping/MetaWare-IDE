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
package com.arc.debugger;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;

import com.arc.mw.util.Command;
import com.arc.mw.util.StringUtil;
import com.arc.mw.util.ver.FileInfoExtractor;
import com.arc.mw.util.ver.VersionInfo;

/**
 * Methods for locating the debugger engine.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class EngineLocator {
    public static String computeSCCommand (String target, String environment[]) throws EngineLocatorException {
        target = target.toLowerCase();
        String sccmd;
        if (isWindows()) {
            sccmd = "sc" + target + ".exe";
        }
        else {
            sccmd = "sc" + target;
        }
        return computePathFor(sccmd, environment,target);
    }
    
    private static boolean isWindows(){
        return System.getProperty("os.name").indexOf("indow") > 0;
    }
    
    private static String computePathFor(String exeName, String environment[], String cpu) throws EngineLocatorException{
        String paths[] = null;
        if (environment != null){
            paths = Command.extractPathsFromEnvironment(environment);
            if (paths != null){
                File cmd = findPathFromPathStrings(exeName,paths);
                if (cmd != null && cmd.exists()) return cmd.toString();
            }
        }
        
        // Now check in IDE distribution
        String dir = computeToolsetPathFromIDELocation(cpu);
        if (dir != null){
            File bin = new File(dir,"bin");
            if (bin.isDirectory()){
                File cmd = new File(bin,exeName);
                if (cmd.exists() && cmd.isFile()){
                    return cmd.toString();
                }
            }
        }
        if (paths == null) {
            String path = System.getenv("PATH");
            if (path == null) throw new EngineLocatorException("PATH variable not set");
            File result = findPathFromPathStrings(exeName,path);
            if (result != null) return result.toString();
        }
       
        throw new EngineLocatorException(
        "Launch configuration error: cannot locate \"" + exeName + "\".");
    }
    
    public static File findPathFromPathStrings(String fileName, String pathStrings){
        return findPathFromPathStrings(fileName,StringUtil.pathToArray(pathStrings, File.pathSeparator));
    }
    
    public static File findPathFromPathStrings(String filename, String paths[]){
         for (int i = 0; i < paths.length; i++) {
             File dir = new File(paths[i]);
             File cmd = new File(dir,filename);
             if (cmd.exists() && cmd.isFile()){
                 return cmd;
             }
         }
         return null;
    }
    
    /**
     * Given a target processor, compute the location of the SeeCode's "hcXX" directory by searching the search path.
     * @param target target cpu (e.g., "arc")
     * @param env environment strings from which "PATH" can be retreived, or <code>null</code>.
     * @return the directory contain the distribution.
     */
    public static String computeSCDIR(String target, String env[]) {
        // If environment sets "PATH" then use that to override.
        String sccmd = "sc" + target.toLowerCase();
        if (isWindows()) sccmd += ".exe";
        String path;
        try {
            path = computePathFor(sccmd,env,target);
        } catch (EngineLocatorException e) {
            path = null; // what to do here?
        }
        if (path != null){
            File pathFile = new File(path);
            File binFile = pathFile.getParentFile();
            if (binFile != null && binFile.getParent() != null){
                return binFile.getParent();
            }
        }
        
        return "SCDIR"; // Don't know        
    }
    
    /**
     * Return "arc" for "ac"; "VideoCore" for "vc", etc.
     * @param cpu
     * @return the subdirectory under "MetaWare" that a particular toolset resides.
     */
    public static String computeTargetPlatformDirectory(String cpu){
        if ("ac".equals(cpu)) return "arc";
        if ("vc".equals(cpu)) return "VideoCore";
        return cpu; 
    }

    private static String computeToolsetPathFromIDELocation(String target) {
        URL url = Platform.getInstallLocation().getURL();
        try {
            url = FileLocator.toFileURL(url);
        } catch (IOException e) {
        }
        File installDir = new File(url.getFile());
        File f = new File(installDir, "../" + computeTargetPlatformDirectory(target.toLowerCase()));
        if (f.exists() && f.isDirectory()) 
            return f.toString();
        return null;
    }
    
    /**
     * Compute the path to the engine DLL, based on the target CPU designation.
     * @param cpu the target CPU designation (e.g. "ac", "arm", "vc", "mips", etc.)
     * @param environment the environment strings for which <code>PATH</code> is the one of interest.
     * @return the path to the engine DLL.
     * @throws EngineLocatorException 
     */
    public static String computeEnginePath(String cpu, String environment[]) throws EngineLocatorException {
        String target = cpu.toLowerCase();
        String libName;
        String sccmd;
        if (isWindows()){
            libName = "crout.dll";
            sccmd = "sc" + target + ".exe";
        }
        else {
            libName = "libcrout.so";
            sccmd = "sc" + target;
        }
        String p = computePathFor(sccmd,environment,  cpu);
        return new File(new File(p).getParentFile(),libName).getPath();
    }
    
    /**
     * Given a target CPU (e.g., "ac", "vc", etc.), return the path
     * to the debugger engine DLL that needs to be loaded.
     * <P>
     * If there is an ambiguity, the callback method {@link IEngineResolver#useToolSetEngine}
     * method will be invoked to resolve it.
     * @param target the target processor (e.g., "ac", "vc", etc.).
     * @param environment the environment in which the debugger will run.
     * @param callback callback to resolve ambiguity; may pop up a option box. May be <code>null</code>, in which
     * case the bundled DLL will be used unconditionally.
     * @return the absolute path to the engine "crout.dll" (or "libcrout.so").
     */
    public static String computePathToEngineDLL(String target, String[] environment,IEngineResolver callback) throws EngineLocatorException{
        
        Activator plugin = Activator.getDefault();
        URL url = plugin.getBundle().getResource(System.getProperty("osgi.os") + "/" + getEngineName());
        File toolsetEngine = new File(computeEnginePath(target,environment));
        if (url == null){
            return toolsetEngine.getPath();
        }
        try {
            String file = FileLocator.toFileURL(url).getFile();          
            if (callback == null){
                return file;
            }
            File bundledEngine = new File(file);
            int toolsetBuildID = computeBuildId(toolsetEngine,"toolsetEngine_" + target);
            int bundledBuildID = computeBuildId(bundledEngine,"bundledEngine_" + target);
            if (!callback.useToolSetEngine(bundledBuildID,toolsetBuildID,toolsetEngine.getPath())){
                return bundledEngine.getPath();
            }
            return toolsetEngine.getPath();           
        }
        catch (IOException e) {
            // Something went wrong. Use tool set version.
            return toolsetEngine.getPath();
        }
    }
    
    private static int computeBuildId(File exe, String key) throws IOException, EngineLocatorException{
        Activator plugin = Activator.getDefault();
        String timeStampKey = key + "_timeStamp";
        String buildKey = key + "_buildID";
        long timeStamp = plugin.getPreferenceStore().getLong(timeStampKey);
        int buildID = 0;
        if (timeStamp != exe.lastModified()){
            plugin.getPreferenceStore().setValue(timeStampKey,exe.lastModified());
            FileInfoExtractor info = new FileInfoExtractor(exe);              
            VersionInfo verInfo = info.extractVersionInfo();
            if (verInfo == null) 
                throw new EngineLocatorException("Can't get version info about " + exe);
            buildID = verInfo.getMajorVersion() * 100 + verInfo.getMinorVersion();
            // cache build ID in preference store to avoid overhead of re-reading it next time.
            plugin.getPreferenceStore().setValue(buildKey,buildID);
        }
        else {
            buildID = plugin.getPreferenceStore().getInt(buildKey);
        }
        return buildID;
    }
    
    private static String getEngineName(){
        return isWindows()?"crout.dll":"libcrout.so";
    }
    

}
