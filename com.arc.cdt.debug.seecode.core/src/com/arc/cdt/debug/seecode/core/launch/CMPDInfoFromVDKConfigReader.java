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
package com.arc.cdt.debug.seecode.core.launch;


import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.arc.seecode.engine.ProcessIdList;


/**
 * Read a CMPD info from a VDK configuration file.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class CMPDInfoFromVDKConfigReader {

    /**
     * Given a VDK configuration file, extract CMPD information suitable for creating a launch configuration.
     * @param vdkConfig the XML file to read from.
     * @return cmpd description
     * @throws VDKConfigException if an error occurs in reading the config file.
     */
    @SuppressWarnings("unchecked")
    public static ICMPDInfo extractCMPDInfo (File vdkConfig, IProject project) throws VDKConfigException {
        try {
            SAXReader reader = new SAXReader();
            Document doc = reader.read(vdkConfig);
            Element root = doc.getRootElement();
            if (root == null || !root.getName().equalsIgnoreCase("CMPD")) {
                throw new DocumentException("Root element is not \"CMPD\" node");
            }
            if (!"1".equals(root.attributeValue("version"))) {
                throw new DocumentException("VDK config file has unknown version: " + root.attribute("version"));
            }

            List<Element> processes = root.elements("PROCESS");

            final List<ICMPDInfo.IProcess> pList = new ArrayList<ICMPDInfo.IProcess>(processes.size());
            File workingDir = vdkConfig.getParentFile();
            for (Element p : processes) {
                pList.add(formProcess(p, workingDir, project));
            }

            List<Element> launches = root.elements("LAUNCH"); // should be just one
            final List<String> launchSwitches = new ArrayList<String>();
            final List<String> startupCommands = new ArrayList<String>();
            if (launches != null) {
                for (Element e : launches) {
                    appendLaunchSwitches(launchSwitches, startupCommands, e);
                }
            }

            return new ICMPDInfo() {

                @Override
                public String[] getLaunchArgs () {
                    return launchSwitches.toArray(new String[launchSwitches.size()]);
                }

                @Override
                public IProcess[] getProcesses () {
                    return pList.toArray(new IProcess[pList.size()]);
                }
                
                @Override
                public String[] getStartupCommands() {
                    return startupCommands.toArray(new String[startupCommands.size()]);
                }
            };
        }
        catch (MalformedURLException e) {
            throw new VDKConfigException(e.getMessage(),e);
        }
        catch (DocumentException e) {
            throw new VDKConfigException(e.getMessage(),e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void appendLaunchSwitches(List<String> switchList, List<String>startupCommands, Element launch){
        List<Element> switches = launch.elements("SWITCH");
        for (Element e: switches){
            String data = e.getTextTrim();
            if (data.startsWith("-multifiles=")){
                // Do nothing
            }
            else if (data.equals("-OKN")){
                // Do nothing
            }
            else if (data.startsWith("-cmd=")){
                startupCommands.add(data.substring(5));
            }
            else switchList.add(data);
        }
    }

    @SuppressWarnings("unchecked")
    private static ICMPDInfo.IProcess formProcess (Element e, File workingDir, final IProject project) throws DocumentException {
        List<Element> switches = e.elements("SWITCH");
        final String name = e.attributeValue("name");
        final List<String> args = new ArrayList<String>(switches.size());
        ProcessIdList plist = null;
        boolean exeArgsPending = false;
        final List<String> exeCommand = new ArrayList<String>();
        for (Element s : switches) {
            String arg = s.getTextTrim();
            if (arg.startsWith("-pset=")) {
                try{
                    plist = ProcessIdList.create(arg.substring(6));             
                } catch(NumberFormatException x){
                    throw new DocumentException("Bogus -pset value: " + arg.substring(6) + ": "+ x.getMessage());
                }
            }
            else if (arg.startsWith("-psetname=")) {
                // Do nothing; process name already known
            }
            else if (arg.equals("--") && exeCommand.size() == 1){
                exeArgsPending = true;
            }
            else if (arg.startsWith("-") && !exeArgsPending) {
                 args.add(arg);
            }
            else if (exeCommand.size() > 0 && !exeArgsPending) {
                throw new DocumentException("Multiple exe path specified: " + exeCommand.get(0) + " and " + arg);
            }
            else
                exeCommand.add(arg);
        }
        if (exeCommand.size() == 0) {
            throw new DocumentException("exe path missing for process " + name);
        }
        if (!new File(exeCommand.get(0)).isAbsolute() && workingDir != null) {
            String file = new File(workingDir,exeCommand.get(0)).toString();
            exeCommand.set(0, file.replaceAll("\\\\","/"));
        }
        
        if (new File(exeCommand.get(0)).isAbsolute() && project != null){
            // Make absolute paths relative to project if possible.
            IPath exePath = new Path(exeCommand.get(0));
            IPath projectPath = project.getLocation();
            if (projectPath.isPrefixOf(exePath)){
                exePath = exePath.setDevice(null).removeFirstSegments(projectPath.segmentCount());
                exeCommand.set(0,exePath.toString());
            }
        }
        
        List<Element> props = e.elements("property");
        final Map<String,String> properties = new HashMap<String,String>();
        if (props != null){
            for (Element p: props) {
                String key = p.attributeValue("name");
                String value = p.attributeValue("value");
                if (key != null && value != null && key.length() > 0){
                    properties.put(key,value);
                }
            }
        }
        final ProcessIdList plistCopy = plist;
        return new ICMPDInfo.IProcess() {

            @Override
            public String[] getCommand () {
                return exeCommand.toArray(new String[exeCommand.size()]);                
            }

            @Override
            public int getInstanceCount () {
                return plistCopy.getCount();
            }
            
            @Override 
            public ProcessIdList getIDList(){
                return plistCopy;
            }

            @Override
            public String getProcessName () {
                return name;
            }

            @Override
            public IProject getProject () {
                return project;
            }

            @Override
            public String[] getSwahiliArgs () {
                return args.toArray(new String[args.size()]);
            }
            
            @Override
            public Map<String,String> getGuihiliProperties(){
                return properties;
            }
        };
    }
}
