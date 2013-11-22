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
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.core.launch.IServerLauncher;
import com.arc.debugger.EngineLocator;
import com.arc.debugger.EngineLocatorException;
import com.arc.mw.util.Log;
import com.arc.mw.util.StringUtil;
import com.arc.mw.util.Toggle;
import com.arc.seecode.connect.SocketTransport;
import com.arc.seecode.plugin.EnginePlugin;
import com.arc.seecode.server.Server;


/**
 * Launches the "server" process that wraps the MetaWare debugger engine.
 * The IDE communicates to it by means of <i>remote method calls</i> from the EngineInterface 
 * object.
 * <P>
 * {@link #makeConnection} is called for each connection. There is one connection per CMPD process.
 * Otherwise, there is just one connection. Then {@link #launch} is invoked to spawn the process.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class ServerLauncher implements IServerLauncher {
    private int fDebugPort = -1;
    private List<String> fToggles = new ArrayList<String>(0);
    private List<SocketTransport>fPorts = new ArrayList<SocketTransport>();
    private int[] pids = new int[]{1};
    
    @Override
    public SocketTransport makeConnection() throws IOException {
        if (fPorts.size() > 0)
            throw new IllegalStateException("More than one connection attempted");
        SocketTransport port = new SocketTransport();
        fPorts.add(port);
        port.listen(0);
        return port;       
    }
    
    @Override
    public void setPids(int pids[]){
        if (pids == null || pids.length == 0) {
            this.pids = new int[]{1};
        }
        else if (pids.length == 1){
            this.pids = new int[]{pids[0]};
        }
        else {
            this.pids = new int[pids.length];
            System.arraycopy(pids,0,this.pids,0,pids.length);
            Arrays.sort(this.pids); // sort so that we can reduce them to ranges
        }
    }
    
    @Override
    public Process launch(String cpu, File workingDirectory, String[] environment) throws IOException, CoreException{
        if (fPorts.size() == 0) throw new IOException("No connections to make");
        String javaCommand = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        // We need to set up the classpath of the Server:
        // com.arc.seecode.engine and com.arc.mw.util

        Bundle enginePlugin = EnginePlugin.getDefault().getBundle();
        Bundle utilPlugin = Platform.getBundle("com.arc.mw.util");
        if (enginePlugin == null || utilPlugin == null)
            throw new CoreException(SeeCodePlugin.makeErrorStatus("can't find plugin locations from which to launch seecode engine"));

        File enginePluginPath = getPluginPath(enginePlugin,"/");
        File utilPluginPath = getPluginPath(utilPlugin,"/");
        if (enginePluginPath == null || utilPluginPath == null)
            throw new CoreException(SeeCodePlugin.makeErrorStatus("can't find classpath for remote engine"));
        String classPath = enginePluginPath.getPath() + File.pathSeparator
                + utilPluginPath.getPath();
        ArrayList<String> cmdList = new ArrayList<String>();
        cmdList.add(javaCommand);
        //TO BEREMOVED
        //cmdList.add("-Xcheck:jni");
        cmdList.add("-classpath");
        cmdList.add(classPath);
        if (fDebugPort >= 0){
            cmdList.add("-Xdebug");
            cmdList.add("-Xrunjdwp:transport=dt_socket,address=" + fDebugPort + ",server=y,suspend=y");
        }
        cmdList.add("-Xss512000"); // Engine needs this much thread stack
        cmdList.add(Server.class.getName());
        cmdList.addAll(fToggles); // -Xserver, etc.
        int lastPid = 0;
        StringBuilder pidBuf = new StringBuilder();
        pidBuf.append("-pid=");
        for (int i = 0; i < pids.length; i++) {
            if (lastPid == 0){
                if (i > 0) pidBuf.append(",");
                pidBuf.append(pids[i]);
                if (i+1 < pids.length && pids[i+1] == pids[i]+1){
                    lastPid = pids[i];
                    pidBuf.append(":");
                }
            }
            else if (i+1 == pids.length || pids[i+1] != pids[i]+1){
                pidBuf.append(pids[i]);
                lastPid = 0;
            }       
        }
        
        cmdList.add(pidBuf.toString());
        
        /*for (SocketTransport transport: fPorts){
           cmdList.add(transport.listeningPort() + "");
        }*/
        if (fPorts.size() == 0) throw new IllegalStateException("No transport establishted");
        cmdList.add("" + fPorts.get(0).listeningPort());
        
        String enginePath;
        try {
            enginePath = EngineLocator.computePathToEngineDLL(cpu,environment,SeeCodePlugin.getDefault().getEngineVersionStrategyCallback());
        } catch (EngineLocatorException e2) {
            throw new CoreException(SeeCodePlugin.makeErrorStatus("Can't locate seecode engine DLL: " + e2.getMessage()));
        }
        cmdList.add(enginePath);
        if (!new File(enginePath).exists()){ // should never fail
            throw new CoreException(SeeCodePlugin.makeErrorStatus("Can't locate seecode engine DLL: " + enginePath));
        }
        String cmd[] = cmdList.toArray(new String[cmdList.size()]);
        Toggle t = Toggle.lookup("CLIENT");
        if (t != null && t.on()){
            Log.log("CLIENT","Launching: " + StringUtil.arrayToArgString(cmd));
        }

        return ProcessFactory.getFactory().exec(cmd, environment, workingDirectory);
       
    }

    @Override
    public void setDebuggerAttachPort (int port) {
        fDebugPort = port;      
    }

    @Override
    public void setEngineToggles (List<String> args) {
        fToggles  = new ArrayList<String>(args);      
    }

    /**
     * Return the File reference to a a subdirectory "subDir"
     * in the plugin location, or, if the sub-directory does not
     * exist, return the jarFile reference.
     * @param bundle
     * @param subDir a subdirectory to reference.
     * @param alternative an alternative if the above doesn't exit.
     * @return the absolute file reference given one that
     * is relative to a plugin.
     * @throws IOException
     */
    private static File getLocationFor(Bundle bundle, String subDir,
            String alternative) throws IOException {
        URL url = bundle.getEntry(subDir);
        File f = null;
        if (url != null) {
            url = FileLocator.toFileURL(url);
            f = new File(url.getFile());

            if (!f.isDirectory()) {
                f = null;
            }
        } 
        if (f == null) {
            url = bundle.getEntry(alternative);
            if (url != null) {
                url = FileLocator.toFileURL(url);
                f = new File(url.getFile());
            }
            if (f == null || !f.exists()) {
                String msg;
                if (f != null) msg = " (" + f.toString() + ")";
                else if (url != null) msg = " (" + url.toString() +")";
                else msg = "";
                    throw new IOException(
                            "Can't locate classpath to plugin "
                                    + bundle.getSymbolicName() + msg);
            }
        }
        return f;
    }
    
    /**
     * Return the plugin class path for a particular plugin.
     * If the plugin has a "bin" directory, then return the
     * path to it as we are presumably in development mode.
     * <P>
     * If there is no "bin" directory, then return the
     * jar file path -- which means we're in deployment mode.
     * @param plugin
     * @param alternative relative path from plugin (e.g. ".", "engine.jar")
     * @return the path to the "bin" directory containing classes, or
     * to the jar file.
     * @throws IOException
     */
    private static File getPluginPath(Bundle plugin, String alternative) throws IOException{
        return getLocationFor(plugin,"bin",alternative);
    }

}
