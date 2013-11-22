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
package com.arc.cdt.debug.seecode.options;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.utils.elf.parser.ElfParser;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;

import com.arc.cdt.debug.seecode.core.ISeeCodeLaunchConfigurationConstants;
import com.arc.cdt.debug.seecode.core.launch.ICMPDInfo;
import com.arc.debugger.EngineLocator;
import com.arc.debugger.EngineLocatorException;
import com.arc.mw.util.Cast;
import com.arc.mw.util.StringUtil;

/**
 * The public method of this class,
 * {@link #computeArguments(ILaunchConfiguration)}compute()} returns the
 * argument to be passed to the SeeCode engine.
 * <p>
 * The arguments are derived from the launch configuration (which is based on
 * the guihili-based dialog) and the per-project build options -- specifically
 * those passed to the compiler.
 * <P>
 * 
 * @author David Pickens
 */
public class SeeCodeOptions {
    private static Element sCached = null;
    
    /**
     * The default TCP/IP port used to communicate with the UART simulator in the debugger
     * engine. It must match the value specified in "vuart.cpp" of termsim.dll, or else
     * relative port numbers ("+0", "+1", etc.) won't work.
     */
    private static final int DEFAULT_TCPIP_PORT = 7000;

    private static String sCachedTarget = null;

    /**
     * Read the XML file that maps compiler options to equivalent seecode
     * arguments that will be passed to the SeeCode swahili processor ("scac").
     * For example, ARC's "-Xswap", "-arc700", etc.
     * <P>
     * If the target does not require any compiler options to be reflected, then
     * this method returns null.
     * <P>
     * The name of the XML file is derived from the target name.
     * 
     * @param target
     *            the name of the target (e.g., "ac", "vc", etc.)
     * @param project
     *            applicable project.
     * @return the option mapping for computing seecode arguments, or
     *         <code>null</code> if there isn't any such mapping.
     */
    public static ISeeCodeOptionsAugmenter readOptionMapping(String target,
            IProject project) throws ConfigurationException {
        // Use cached copy if we've already loaded it.
        try {
            Element root = null;
            // If already parsed, remember it.
            if (target.equals(sCachedTarget))
                root = sCached;
            else {
                String xml = target.toLowerCase() + "_options.xml";
                InputStream input = SeeCodeOptions.class
                        .getResourceAsStream(xml);
                if (input != null) {
                    SAXReader reader = new SAXReader();

                    Document doc = reader.read(input);
                    root = doc.getRootElement();
                    sCached = root;
                    sCachedTarget = target;
                }
            }
            if (root != null) {
                IConfiguration buildConfiguration = getBuildConfiguration(project);
                if (buildConfiguration != null
                        && isConsistentWithTarget(buildConfiguration, target
                                .toLowerCase())) {
                    return new SeeCodeOptionsAugmenter(root, buildConfiguration);
                }
            }
        } catch (DocumentException e) {
            throw new ConfigurationException(e.getMessage(), e);
        } 
        return null;
    }

    /**
     * Given a build configuration, return whether or not it is consistent with
     * the target. E.g., due to some sort of screw up, an ARC 5 configuration
     * could be invoking an ARC 4 exe.
     * 
     * @param config
     * @param target
     * @return true if configuration is consistent with target.
     */
    private static boolean isConsistentWithTarget(IConfiguration config,
            String target) {
        String name = config.getId();
        //CR: Because new toolchain in the format com.arc.cdt.toolchain.arc600 or com.arc.cdt.toolchain.av2hs
        //we can't use the format "toolchain.arc." to compare
        //by failure on this function, we won't be able to launch Debug or Run for EM, HS, etc... new definition toolchain
        //also Guihili gui show ??? on the Target processor:
        if ((name.indexOf("toolchain.arc") >=0) || (name.indexOf("toolchain.av2") >=0))
            return target.equals("ac") || target.equals("ac2");
        
        if (name.indexOf("arc4.") >= 0) {
            return target.equals("arc");
        }
        //That's all we care about
        return false;
    }
    
    /**
     * Compute the path to the engine DLL.
     * @param config the launch configuration from which we derive the target.
     * @param environment the environment strings for which <code>PATH</code> is the one of interest.
     * @return the path to the engine DLL.
     * @throws ConfigurationException
     * @throws CoreException
     */
    public static String computeEnginePath(ILaunchConfiguration config, String environment[]) throws ConfigurationException, CoreException{
        try {
            return EngineLocator.computeEnginePath(computeTarget(config),environment);
        }
        catch (EngineLocatorException e) {
            throw new ConfigurationException(e);
        }
    }
        
    
    /**
     * Given a launch configuration, compoute the target ("ac", "arm", etc.)
     * @param config the launch configuration
     * @return the target string.
     * @throws ConfigurationException
     * @throws CoreException
     */
    public static String computeTarget(ILaunchConfiguration config) throws ConfigurationException, CoreException{
        List<String> swahiliArgsList = Cast.toType(config.getAttribute(
                ISeeCodeLaunchConfigurationConstants.ATTR_SWAHILI_ARGS,
                (List<String>) null));
        if (swahiliArgsList == null) {
            // Dynamically created launch configuration.
            String cpu = getTargetCpuName(config);
            if (cpu == null) {
                if (isCmpdSession(config)){
                    return "ac"; // CMPD
                }
                throw new ConfigurationException(
                    "Launch configuration error: debugger launch configuration not set");
            }
            return cpu;
        }
        
        String target = findTarget(swahiliArgsList.toArray(new String[swahiliArgsList.size()]));

        if (target == null) {
            if (isCmpdSession(config)){
                return "ac"; // CMPD
            }
            throw new ConfigurationException(
                    "Launch configuration error: target unknown in"
                            + StringUtil.listToArgString(swahiliArgsList));
        }
        return target;
        
    }

    private static boolean isCmpdSession (ILaunchConfiguration config) throws CoreException {
        return config.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_COUNT,0) > 0;
    }
    
    public static class TermSimPort {
        public TermSimPort(int tcp, int uart){
            tcpPort  = tcp;
            uartPort = uart;
        }
        public int tcpPort;
        public int uartPort;
    }
    
    /**
     * If the user is invoking one or more terminal simulators, extract the TCP/IP port and
     * the UART port. Otherwise, returns <code>null</code>.
     * @param config
     * @return any terminal simulators that the engine is expected to drive.
     * @throws CoreException 
     */
    @SuppressWarnings("unchecked")
    public static TermSimPort[] extractTermSimPorts(ILaunchConfiguration config) throws CoreException{
        List<String> swahiliArgsList = config.getAttribute(
            ISeeCodeLaunchConfigurationConstants.ATTR_SWAHILI_ARGS,
            (List<String>) null);
        return extractTermSimPorts(swahiliArgsList);
    }
    
    /**
     * If the user is invoking one or more terminal simulators, extract the TCP/IP port and
     * the UART port. Otherwise, returns <code>null</code>.
     * @param swahiliArgsList the list of arguments to be passed to the debugger.
     * @return any terminal simulators that the engine is expected to drive.
     */
    public static TermSimPort[] extractTermSimPorts(List<String>swahiliArgsList){
        List<TermSimPort>list = new ArrayList<TermSimPort>();
        if (swahiliArgsList != null){
            for (String s: swahiliArgsList){
                if (s.indexOf("term_base=") > 0){
                    String[] fields = s.split(",");
                    if (fields[0].startsWith("-simext") && fields[0].toLowerCase().indexOf("ide") >= 0){
                        // OKAY: we're loading a simulator extension that has "ide" in its name and
                        // has parameters "term_base=...". We assume that this extension will
                        // be expecting the IDE to set up a server on the port so as to simulate
                        // a terminal. Get the TCP port and uart port.
                        int tcpPort = -1;
                        int uartPort = 1;
                        for (String f:fields){
                            if (f.startsWith("term_tcpport=")){
                                String portString = f.substring(13);
                                // if starts with "+", it is relative to the default.
                                if (portString.startsWith("+")){
                                    tcpPort = getInt(portString.substring(1),DEFAULT_TCPIP_PORT) + DEFAULT_TCPIP_PORT;
                                }
                                else
                                    tcpPort = getInt(portString,-1);
                            }
                            else if (f.startsWith("term_port=")){
                                uartPort = getInt(f.substring(10),-1);
                            }
                        }
                        if (tcpPort >= 0 && uartPort >= 0)
                            list.add(new TermSimPort(tcpPort,uartPort));
                    }
                }
            }
        }
        return list.toArray(new TermSimPort[list.size()]);
    }
    
    /**
     * If the user is invoking one or more terminal simulators, extract the TCP/IP port and
     * the UART port. Otherwise, returns <code>null</code>.
     * @param info CMPD information.
     * @return any terminal simulators that the engine is expected to drive.
     */
    public static TermSimPort[] extractTermSimPorts(ICMPDInfo info){
        Set<TermSimPort> set = new TreeSet<TermSimPort>(new Comparator<TermSimPort>(){

            @Override
            public int compare (TermSimPort o1, TermSimPort o2) {
                return o1.tcpPort < o2.tcpPort?-1:o1.tcpPort==o2.tcpPort?0:1;
            }

            });
        for (ICMPDInfo.IProcess p: info.getProcesses()){
            TermSimPort[] ts = extractTermSimPorts(Arrays.asList(p.getSwahiliArgs()));
            for (TermSimPort t: ts){
                set.add(t);
            }
        }
        return set.toArray(new TermSimPort[set.size()]);
    }
    
    private static int getInt(String s, int returnIfError){
        try{
            return Integer.parseInt(s);
        }
        catch(NumberFormatException x){
            return returnIfError;
        }
    }

    /**
     * Given a launch configuration, return the list of argument to be passed to
     * the SeeCode swahili processor (scac). These are typically the arguments
     * that were extracted from the Guihili-based dialogs. However, some
     * processors (like ARC), require that the per-project build options be
     * reflected in the argument list (e.g., -Xbarrel_shifter).
     * 
     * @param config
     *            the launch configuration
     * @return array of arguments to be passed to the seecode engine.
     * @throws ConfigurationException
     *             in case something is messed up.
     * @throws CoreException
     */
    public static String[] computeArguments(ILaunchConfiguration config)
            throws ConfigurationException, CoreException {
        // Grab the swahili arguments from the Guihili-based configuration
        // attributes map.
        // This is typically the entire argument list
        // to send to the seecode engine.
        // However, for the case of ARC, they may not
        // be uptodate if the Build options
        // (e.g., -arc700) changed since the
        // launch configuration dialog was set.
        // Therefore, we go through and augment it.
        List<String> swahiliArgsList = Cast.toType(config.getAttribute(
                ISeeCodeLaunchConfigurationConstants.ATTR_SWAHILI_ARGS,
                (List<String>) null));
        if (swahiliArgsList == null) {
            // We're invoking a dynamically-created launch configuration. I.e., 
            // it was not created from a Launch Configuration dialog.
            // Fill it in with appropriate defaults.
            swahiliArgsList = new ArrayList<String>();
            String cpu = getTargetCpuName(config);
            if (cpu == null) cpu = "AC"; //punt
            swahiliArgsList.add("-targs=" + cpu.toUpperCase());
            
//            throw new ConfigurationException(
//                    "Launch configuration error: debugger configuration not set");
        }
        ArrayList<String> newList = new ArrayList<String>(swahiliArgsList);
        newList.add("-nogoifmain"); // sc.cnf defaults this to true! 
        String[] args = newList.toArray(new String[newList.size()]);
        String target = findTarget(args);
        if (target == null) {
            throw new ConfigurationException(
                    "Launch configuration error: target unknown in"
                            + StringUtil.arrayToArgString(args));
        }

        IProject project = getProject(config);
        if (project != null) {
            IConfiguration buildConfiguration = getBuildConfiguration(project);
            if (buildConfiguration != null && buildConfiguration.getBuilder().isManagedBuildOn()) {
                // Now see if there is an XML file for mapping between
                // compiler options and SeeCode arguments (as will be the
                // case for ARC).
                // cr101683: but only for managed make!!! Otherwise there are no project
                // properties to merge with!
                ISeeCodeOptionsAugmenter om = readOptionMapping(target, getProject(config));

                if (om != null) {
                    args = om.augmentArguments(args);
                }
            }
        }
        return args;
    }

    /**
     * Search the tentative SeeCode argument list for "-targs XXX" and return
     * XXX.
     * 
     * @param args
     *            the tentative SeeCode argument list.
     * @return the target.
     */
    private static String findTarget(String args[]) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-targs=")) {
                return args[i].substring(7);
            }
        }
        return null;
    }

    /**
     * Given a launch configuration, return the corresponding build
     * configuration.
     * 
     * @param launchConfig
     *            the launch configuration.
     * @return the corresponding build configuration.
     * @throws ConfigurationException
     */
    private static IProject getProject(
            ILaunchConfiguration launchConfig) throws CoreException {
        // Yes, there is a mapping between compiler options
        // and SeeCode.
        // Compute project configuration from which we
        // can extract compiler options

        String projectName = launchConfig.getAttribute(
                ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,
                (String) null);
        //the project name can be unspecified under subtle circumstances
        // when a new launch configuration is made without a known default project.
        if (projectName == null || projectName.length() == 0)
            return null;
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
                projectName);
//        if (project == null) {
//            throw new ConfigurationException("Can't locate project "
//                    + projectName);
//        }
        return project;
    }
    
    private static IConfiguration getBuildConfiguration (IProject project) {
        if (project == null) return null;
        IManagedBuildInfo mi = ManagedBuildManager.manages(project) ? ManagedBuildManager.getBuildInfo(project) : null;
        return mi == null ? null : mi.getDefaultConfiguration();
    }
    
    public static String getPlatform(ILaunchConfiguration config) {
        String platform = Platform.getOS();
        try {
            return config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PLATFORM, platform);
        } catch (CoreException e) {
            return platform;
        }
    }
    
    /**
     * Return the target cpu name of the executable that is being launched.
     * @param configuration
     * @return the target CPU name, or <code>null</code> if not known.
     */
    public static String getTargetCpuName(ILaunchConfiguration configuration) {
        try {
            String cpu = configuration.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_TARGET_CPU, (String)null);
            if (cpu != null) return cpu;
        }
        catch (CoreException e) {
        }
        // We shouldn't get here, but in case the Launch Configuration is incomplete...
        ICElement ce = getExeElement(configuration);
        //NOTE: ce can be null if executable is outside workspace.
        return SeeCodeOptions.getTargetCpuName(configuration,ce);
    }

    
    /**
     * Return the model element for the executable to be invoked for the associated project. 
     * This is called if we're launching the debugger without having configured it
     * from the Launch Configuration dialog.
     * @param config
     * @return the associated ICProject object.
     */
    public static ICElement getExeElement (ILaunchConfiguration config) {
        String projectName = null;
        ICElement obj = null;
        String programName = null;
        try {
            projectName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
            programName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, (String)null);
        }
        catch (CoreException e) {
        }
        if (projectName != null && !projectName.equals("")) { //$NON-NLS-1$
            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
            if (project != null ){
                if (programName == null || programName.equals("")) { //$NON-NLS-1$
                    return CCorePlugin.getDefault().getCoreModel().create(project);
                }
                IPath programFile;
                if (new File(programName).isAbsolute()){
                    programFile = new Path(programName);
                }
                else {
                    programFile = project.getFile(programName).getLocation();
                }
                ICElement ce = CCorePlugin.getDefault().getCoreModel().create(programFile);
                if (ce != null && ce.exists()) {
                    obj = ce;
                }
            }
        }
        return obj;
    }
    

    /**
     * Given a launch configuration and a model element that corresponds to an EXE file to be
     * invoke, return the CPU name.
     * @param configuration
     * @param selection the exe file model element to be invoked.
     * @return the target CPU name, or <code>null</code> if not known.
     */
    public static String getTargetCpuName (ILaunchConfiguration configuration, ICElement selection) {
        String programCPU = null;
        if (selection instanceof IBinary) {
            IBinary bin = (IBinary) selection;
            programCPU = bin.getCPU();
        }
        else {
            // If program is outside of workspace, then we
            // must resort to brute force work to figure out
            // what its target is. Don't know why CDT doesn't do 
            // this (CDT BUG 39581)
            try {
                String programName = configuration.getAttribute(
                    ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME,
                    (String) null);
                if (programName != null) {
                    IPath path = new Path(programName);
                    // cr97980: permit access to exe's in project that are not in a configuration...
                    if (!path.isAbsolute()){
                    	// Assume to be project relative.
                    	IProject project = getProject(configuration);
                    	if (project != null){
                    		IResource res = project.findMember(path);
                    		if (res != null && res.exists()){
                    			path = res.getLocation();
                    		}
                    	}
                    }
                    if (path.isAbsolute()) {
                        ElfParser parser = new ElfParser();
                        IBinaryParser.IBinaryFile b = parser.getBinary(path);
                        if (b instanceof IBinaryParser.IBinaryExecutable)
                            programCPU = ((IBinaryParser.IBinaryExecutable) b).getCPU();
                    }
                }
            }
            catch (CoreException e) {
                // Ignore exception from getAttribute
            }
            catch (IOException e) {
                // If file is not found, or is not ELF executable, then
                // just do nothin.
            }
        }
        //      We refer to "st-micro" as just "st", not "st100"
        if ("st100".equals(programCPU)) {
            programCPU = "st";
        }
        // We treat vc1, vc2, and vc3 as the same. The debugger
        // will distinguish.
        if ("vc3".equals(programCPU))
            programCPU = "vc";
        
        // Tools see ARCv2 as an ordinary ARC
        if ("ac2".equals(programCPU)) programCPU = "ac";
        
        return programCPU;
    }
    
	/**
	 * Given a target processor, compute the location of
	 * the SeeCode's "hcXX" directory by searching the
	 * search path.
	 * @param config the associated launch configuration.
	 * @return the directory contain the distribution.
	 * @throws CoreException 
	 * @throws ConfigurationException 
	 */
	public static String computeSCDIR(ILaunchConfiguration config) throws ConfigurationException, CoreException {
		String target = computeTarget(config).toLowerCase();
		return EngineLocator.computeSCDIR(target,DebugPlugin.getDefault().getLaunchManager().getEnvironment(config));
	}
	
	/**
	 * Return "arc" for "ac"; "VideoCore" for "vc", etc.
	 * @param cpu
	 * @return the subdirectory under "MetaWare" that a particular toolset resides.
	 */
	public static String computeTargetPlatformDirectory(String cpu){
		if ("ac".equals(cpu)) return "arc";
		if ("ac2".equals(cpu)) return "arc";
		if ("vc".equals(cpu)) return "VideoCore";
		return cpu;	
	}
	
}
