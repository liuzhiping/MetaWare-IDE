package com.arc.cdt.toolchain.internal.ui.bcf.properties;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.widgets.Display;
import org.xml.sax.SAXException;

import com.arc.cdt.toolchain.internal.ui.bcf.BcfUtils;
import com.arc.cdt.toolchain.ui.bcf.Activator;
import com.arc.cdt.toolchain.ui.bcf.BadPropertyException;
import com.arc.cdt.toolchain.ui.bcf.IOptionUpdater;
import com.arc.cdt.toolchain.ui.bcf.ProcessorFamily;
import com.arc.cdt.toolchain.ui.bcf.SettingsFileContent;
import com.arc.cdt.toolchain.ui.bcf.ToolChainFlavorFactory;

public abstract class AbstractOptionUpdater implements IOptionUpdater {

    protected ProcessorFamily family;
    private Map<String, Object> optionValues = new HashMap<String, Object>();
    protected SettingsFileContent settings;
    
    private static Map<String,ProcessorFamily> versionArgMap = new HashMap<String,ProcessorFamily>();
    static {
        for (ProcessorFamily f: ProcessorFamily.values()){
            versionArgMap.put(f.getDebuggerArg(), f);
        }
        versionArgMap.put("-a6", ProcessorFamily.ARC600);
        versionArgMap.put("-arc600", ProcessorFamily.ARC600);
        versionArgMap.put("-arc601", ProcessorFamily.ARC601);
        versionArgMap.put("-a601", ProcessorFamily.ARC601);
        versionArgMap.put("-a7", ProcessorFamily.ARC700);
        versionArgMap.put("-arc700", ProcessorFamily.ARC700);
        versionArgMap.put("-av2em", ProcessorFamily.ARCEM);
        versionArgMap.put("-av2hs", ProcessorFamily.ARCHS);
    }

    public AbstractOptionUpdater(SettingsFileContent settings)
            throws BadPropertyException, IOException, SAXException  {
        this.settings = settings;
        // Read processor from BCF
        if (settings.getProperties().size() > 0) {
            family = settings.getProcessorFamily();
            optionValues = computeOptionValues(settings.getProperties());
        }
        else {
            // No BCF, read it from compiler command-line options
            List<String> args = settings.getCompilerOptions();
            for (String s: args){
                if (s.startsWith("-a")){
                    this.family = versionArgMap.get(s);
                    if (this.family != null) break;
                }
            }
        }
        if (family == null)
            throw new IllegalStateException("Can't identify processor version");
    }


    protected abstract IProjectType getProjectTypeFor(SettingsFileContent settings, boolean lib);

    protected final IProjectType getNewProjectType(boolean lib) {
        return getProjectTypeFor(settings,lib);
    }

    /**
     * Compute option ID to value map from the BCF properties. Value can be
     * String or boolean.
     * 
     * @param catProperties
     * @return map of option ID's to value.
     */
    protected abstract Map<String, Object> computeOptionValues(
            Properties catProperties);

    @Override
    public boolean updateProject(IProject project) throws BuildException {
        boolean result = false;
        boolean isLib = BcfUtils.isLibProject(project);
        IProjectType newProjectType = getNewProjectType(isLib);
        if (newProjectType != BcfUtils.getProjectType(project)) {
            updateProjectType(project, newProjectType);
            result = true;
        }
        IManagedBuildInfo mi = ManagedBuildManager.getBuildInfo(project);
        for (IConfiguration config : mi.getManagedProject().getConfigurations()) {
            result |= updateConfiguration(config);
        }
        return result;
    }

    private static boolean isLibraryProject(IProjectType pt){
        return pt.getBaseId().toLowerCase().indexOf("lib") >= 0;
    }

    static IConfiguration findConfig(IProjectType pt, String name){
        for (IConfiguration c: pt.getConfigurations()){
            if (name.equals(c.getName()))
                return c;
        }
        return null;
    }
    
    /**
     * Return the option ID for the string list of compiler options that are not otherwise categorized.
     * @return the option ID for the string list of compiler options that are not otherwise categorized.
     */
    protected abstract String getCompilerAdditionalOptionsID();
    
    /**
     * Return the option ID for the string list of assembler options that are not otherwise categorized.
     * @return the option ID for the string list of assembler options that are not otherwise categorized.
     */
    protected abstract String getAsmAdditionalOptionsID();
    
    /**
     * Return the option ID for the string list of linker options that are not otherwise categorized.
     * @return the option ID for the string list of assembler options that are not otherwise categorized.
     */
    protected abstract String getLinkerAdditionalOptionsID();
    
    protected ProcessorFamily getProcessorFamily(IToolChain tc){
        try {
            return ToolChainFlavorFactory.Get(tc).getProcessorFamily(tc);
        } catch (BadPropertyException e) {
            return null;
        }
    }

    @Override
    public boolean updateConfiguration(IConfiguration configuration) throws BuildException{
        if (getProcessorFamily(configuration.getToolChain()) != family) {
            IProjectType pt = getProjectTypeFor(settings, isLibraryProject(configuration.getManagedProject().getProjectType()));
            if (configuration.getProjectType() != pt) {
                IConfiguration newCfg = findConfig(pt,configuration.getName());
                IToolChain tc = null;
                if (newCfg != null) tc = newCfg.getToolChain();
                else tc = pt.getConfigurations()[0].getToolChain();
                BcfUtils.setToolChain(configuration, tc);
            }
        }
        boolean result = updateOptions(configuration,"compiler",settings.getCompilerOptions(), getCompilerAdditionalOptionsID());
        result |= updateOptions(configuration,"asm", settings.getAssemblerOptions(),getAsmAdditionalOptionsID());
        result |= updateOptions(configuration,"linker",canonicalizeLinkerArgs(settings.getLinkerOptions()), getLinkerAdditionalOptionsID());
        result |= addLinkerCommandFileRef(configuration);
//        result |= updateLinkerStuff(configuration,"svr3",settings.getLinkerCommandFiles());
//        result |= updateLinkerStuff(configuration,"user_objs",settings.getLinkerObjectFiles());
        return updateConfiguration(configuration,optionValues, true) | result;
    }
    
    private static boolean updateOptions(IConfiguration configuration, String toolNameFragment,List<String> options, String idOfUnclassifiedOptions) throws BuildException{
        IToolChain tc = configuration.getToolChain();
        boolean result = false;
        for (ITool tool: tc.getTools()){
            if (tool.getBaseId().toLowerCase().indexOf(toolNameFragment) >= 0){
                result |= updateOptionsInTool(configuration,tool,options, idOfUnclassifiedOptions);
            }
        }
        return result;
    }
    
    /**
     * Hook for subclass to munge the linker arguments into something recognizable.
     * @param args
     * @return modified args
     */
    protected List<String> canonicalizeLinkerArgs(List<String>args){
    	return args;
    }
    
    static class IntString{
        IntString(int index, String value){
            this.index = index;
            this.value = value;
        }
        int index;
        String value;
    }
    
    private static boolean updateOptionsInTool(IConfiguration config, ITool tool, List<String> args, String idOfUnclassifiedOptions) throws BuildException {
        boolean result = false;
        Map<String,IntString> optionSet = new HashMap<String,IntString>();
        BitSet unprocessedOptionSet = new BitSet(args.size());
        int index = 0;
        for (String arg: args) {
            unprocessedOptionSet.set(index);
            optionSet.put(arg, new IntString(index,"1"));

            int i = arg.indexOf('=');
            if (i > 0) {
                optionSet.put(arg.substring(0,i+1), new IntString(index,arg.substring(i+1))); // "-foo=" "value"
                optionSet.put(arg.substring(0,i), new IntString(index,arg.substring(i+1)));  // "-foo" "value"
            }
            index++;
        }
        
        IOption additionalOption = null;
        
        for (IOption opt: tool.getOptions()){
            if (opt.getBaseId().equals(idOfUnclassifiedOptions)){
                additionalOption = opt;
                continue;
            }
            String s = opt.getCommand();
            switch(opt.getValueType()){
            case IOption.STRING:
                if (optionSet.containsKey(s)){
                    IntString value = optionSet.get(s);
                    unprocessedOptionSet.clear(value.index);
                    //System.out.println("Option " + opt.getBaseId() + " set to string " + value.value);
                    if (!value.equals(opt.getStringValue())) {
                        config.setOption(tool, opt, value.value);
                        result = true;
                    }
                }
                break;
            case IOption.BOOLEAN:{
                boolean value = true;
                IntString v = optionSet.get(s);
                if (v == null) {
                	v = optionSet.get(opt.getCommandFalse());
                	value = false;
                }
                else v = optionSet.get(s);

                if (v != null) {
                    unprocessedOptionSet.clear(v.index);

                    boolean f = "0".equals(v.value)?!value:value;
                    if (opt.getBooleanValue() != f) {
                        //System.out.println("Option " + opt.getBaseId() + " set to boolean " + v.value);
                        config.setOption(tool, opt, f);
                        result = true;
                    }
                }
                break;
            }
            case IOption.PREPROCESSOR_SYMBOLS:{
                doPreproc(config, "-D", tool, args, opt, unprocessedOptionSet);
                result = true;
                break;
            }
            case IOption.UNDEF_PREPROCESSOR_SYMBOLS:{
                doPreproc(config, "-U", tool, args, opt, unprocessedOptionSet);
                result = true;
                break;               
            }
            case IOption.ENUMERATED:{
                String enumNames[] = opt.getApplicableValues();
                for (int i = 0; i < enumNames.length; i++){
                    //RANT: this is really stupid what we must go through
                    String id = opt.getId(enumNames[i]);
                    if (id == null) {
                        Activator.log("Option " + opt.getBaseId() + " enum " + enumNames[i] + " has no ID!!!!",null);
                    }
                    else {
                        String cmdArg = opt.getCommand(id);
                        if (cmdArg != null && cmdArg.length() > 0 && optionSet.containsKey(cmdArg)){
                            IntString v = optionSet.get(cmdArg);
                            unprocessedOptionSet.clear(v.index);
                            if ("0".equals(v.value)){
                                // Option is being turned off! (E.g, "-g=0")
                                id = (String)opt.getDefaultValue();
                            }
                            if (!id.equals(opt.getStringValue())){
                                config.setOption(tool, opt, id);
                               // System.out.println("Option " + opt.getBaseId() + " set to enum id " + id);
                                result = true;
                            }
                        }
                    }
                }
                break;
            }
            default:
                break; // Do we need to handle others?
            }
        }
        
        if (unprocessedOptionSet.size() > 0 && additionalOption != null) {
            List<String> list = new ArrayList<String>();
            for (int i = 0; i < args.size(); ++i){
                if (unprocessedOptionSet.get(i)) {
                    list.add(args.get(i));
                }
            }
            config.setOption(tool, additionalOption,  list.toArray(new String[list.size()]));
        }
        return result;
    }


    private static void doPreproc(IConfiguration config, String argPrefix, ITool tool, List<String> options,
            IOption opt, BitSet unprocessedOptionSet) throws BuildException {
        List<String> list = null;
        int i = 0;
        for (String os: options){
            if (os.startsWith(argPrefix)){
                if (list == null) list = new ArrayList<String>();
                list.add(os.substring(2));
                unprocessedOptionSet.clear(i);
                //System.out.println("Option " + opt.getBaseId() + " appends " + argPrefix + " " + os.substring(2));
            }
            i++;
        }
        if (list != null){
            config.setOption(tool, opt, list.toArray(new String[list.size()]));
        }
    }

    protected boolean updateConfiguration(IConfiguration configuration, Map<String,Object> map, boolean verify)
            throws BuildException {
        boolean result = false;
        Set<String> unrecognized = new HashSet<String>();
        unrecognized.addAll(map.keySet());
        boolean recognized[] = new boolean[1];
        for (ITool tool : configuration.getTools()) {
            for (IOption option : tool.getOptions()) {
                result |= updateOption(configuration, tool, option,map, recognized);
                if (recognized[0])
                    unrecognized.remove(option.getBaseId());
            }
        }
        if (verify && unrecognized.size() > 0){
            StringBuilder buf = new StringBuilder();
            for (String s: unrecognized){
                buf.append(' ');
                buf.append(s);
            }
            Activator.log("BCF refs unrecognized options: " + buf.toString(), null);
        }
        return result;
    }

    private static boolean isTrue(Object v) throws IllegalArgumentException{
        if (v instanceof Boolean)
            return ((Boolean)v).booleanValue();
        if (v == null) return false;
        String s = v.toString();
        if (s.equals("0")) return false;
        if (s.equals("1")) return true;
        s = s.toLowerCase();
        if (s.equals("true")) return true;
        if (s.equals("false")) return false;
        throw new IllegalArgumentException("Not a valid boolean value: " + s);
    }

    private static boolean arrayEqual(String a[], String b[]){
        if (a == null) return b == null;
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++){
            if (a[i] == null) {
                if (b[i] != null) return false;
            }
            else if (!a[i].equals(b[i]))
                return false;
        }
        return true;
    }

    protected boolean updateOption(IConfiguration config, ITool tool, IOption option, Map<String,Object> map, boolean recognized[]) throws BuildException{
        //System.out.println("\"" + option.getBaseId() + "\" = \""+ option.getValue() + "\"");
        boolean result = false;
        recognized[0] = false;
        Object value = map.get(option.getBaseId());
        if (value != null) {
            recognized[0] = true;
            if (option.getBasicValueType() == IOption.BOOLEAN)
                try {
                    boolean newValue = isTrue(value);
                    boolean oldValue = option.getBooleanValue();
                    if (oldValue != newValue){
                        config.setOption(tool, option, newValue);
                        result = true;
                    }
                } catch (IllegalArgumentException e) {
                    throw new BuildException("Option " + option.getBaseId() + ": " + e.getMessage());
                }
            else if (value instanceof String[]) {
                if (!arrayEqual((String[])value, option.getStringListValue())){
                    result = true;
                    config.setOption(tool, option,(String[]) value);
                }
            }
            else 
                if (!value.equals(option.getValue())){
                    config.setOption(tool, option, value.toString());
                    result = true;
                }
        }
        return result;

    }

    protected void updateProjectType(IProject project,
            IProjectType newProjectType) throws BuildException {
        IProjectType oldProjectType = BcfUtils.getProjectType(project);
        if (oldProjectType == newProjectType)
            return; // no change.

        Map<String, Map<String,Object>> optionStates = new HashMap<String,Map<String,Object>>();
        IManagedBuildInfo mi = ManagedBuildManager.getBuildInfo(project);
        for (IConfiguration config : mi.getManagedProject().getConfigurations()) {
            Map<String,Object>map = optionStates.get(config.getName());
            if (map == null){
                map = new HashMap<String,Object>();
                optionStates.put(config.getName(), map);
            }
            for (ITool tool : config.getTools()){				
                for (IOption option: tool.getOptions()){
                    Object v = option.getValue();
                    map.put(option.getBaseId(),v);
                }
            }
        }

        // Now restore the config values for the new project.
        for (IConfiguration config : mi.getManagedProject().getConfigurations()) {
            Map<String,Object>map = optionStates.get(config.getName());
            if (map != null) 
                updateConfiguration(config,map,false);
        }
    }


    @Override
    public boolean updateLaunchConfigurations(IConfiguration configuration)
            throws BuildException {
        if (configuration == null) return false; // shouldn't happen
        boolean result = false;
        IProject project = configuration.getOwner().getProject();
        if (project == null) return false; // should not happen
        String projectName = project.getName();
        String configId = configuration.getId();
        if (configId == null) return false; // Should not happen
        try {
            for (ILaunchConfiguration launch: DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations()){
                String p = launch.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
                if (p == null) continue; // not a CDT debugger launch
                if (!p.equals(projectName)) continue; // Launch associated with a different project.
                String c = launch.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID, (String)null);
                if (!configId.equals(c)) continue; // Launch associated with different build configuration
                
                // At this point, we know that the Launch is associated with the configuration that we're updating.
                // Now, update its launch attributes.
                
                ILaunchConfigurationWorkingCopy working = launch.getWorkingCopy();
                
                if (updateLaunchConfiguration(working,settings.getProperties())){
                    working.doSave();
                }
              
            }
        } catch (RuntimeException x){
            throw x;
        } catch (CoreException e) {
            Activator.log(e.getMessage(), e);
        } catch (Exception e) {
            BcfUtils.displayBCFError(e, Display.getDefault().getActiveShell());
        }
        return result;
    }
    
    
    /**
     * Apply the relevant properties from the BCF to the given Launch. Returns true if a change was made. The caller is responsible for
     * saving the changes.
     * @param launch The launch configuration being updated.
     * @param bcfProperties BCF properties to be applied to the launch configuration.
     * @return true if something changes
     */
    protected abstract boolean updateLaunchConfiguration(ILaunchConfigurationWorkingCopy launch, Properties bcfProperties)
            throws CoreException;

    public abstract boolean updateCompileOptionForLaunchConfiguration(ILaunchConfigurationWorkingCopy launch) ;

    @Override
    public boolean updateLaunchConfiguration(ILaunchConfigurationWorkingCopy launch)
            throws BuildException {
        try {
            return updateLaunchConfiguration(launch,settings.getProperties());
        } catch (RuntimeException x){
            throw x;
        } catch (CoreException e) {
            Activator.log(e.getMessage(), e);
            return false;
        } catch (Exception e) {
            BcfUtils.displayBCFError(e, Display.getDefault().getActiveShell());
            return false;
        }
    }
    
    private Set<IFile> linkerCache = new HashSet<IFile>();
    
    /**
     * Return the option ID of the linker command file list.
     * @return
     */
    protected abstract String getLinkerCommandFileID();
    
    
    /**
     * Create linker command file, if necessary, and arrange for it to be referenced by the linker.
     * @param config
     */
    private boolean addLinkerCommandFileRef(IConfiguration config){
        boolean result = false;

        try {
            IOption lcfOpt = BcfUtils.getOption(config, getLinkerCommandFileID());
            if (lcfOpt == null) return false; // no linker option
            
            String lcf = settings.getLinkerCommandFileContent();
            if (lcf != null && lcf.length() > 0){
                String lcfName = settings.getLinkerCommandFileName();
                String fixedName = "${ProjDirPath}/" + lcfName;
                String[] list = lcfOpt.getBasicStringListValue();
                if (list == null || list.length != 1 || !fixedName.equals(list[0])){
                    result = true;
                    list = new String[]{fixedName};
                    BcfUtils.setOption(config, getLinkerCommandFileID(), list);
                }
            }
        } catch (RuntimeException e){
            throw e;
        } catch (Exception e) {
            BcfUtils.displayBCFError(e, Display.getDefault().getActiveShell());
        }
        return result;
    }
    
    /**
     * Create linker command file, if necessary, and arrange for it to be referenced by the linker.
     * @param config
     */
    @Override
    public boolean createLinkerCommandFile(IProject project){
        boolean result = false;

        try {
            
            String lcf = settings.getLinkerCommandFileContent();
            if (lcf != null && lcf.length() > 0){
                String lcfName = settings.getLinkerCommandFileName();
                IFile file = project.getFile(new Path(lcfName));
                if (linkerCache.add(file)){
                    InputStream istream  = new ByteArrayInputStream(lcf.getBytes());
                    if (!file.exists())
                        file.create(istream, true, new NullProgressMonitor());
                    else
                        file.setContents(istream, true, false, new NullProgressMonitor());
                    result = true;
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            BcfUtils.displayBCFError(e, Display.getDefault().getActiveShell());
        } 
        return result;
    }

    static boolean hasOptionValue(IOption option, Object value){
        try {
            switch(option.getBasicValueType()){
            case IOption.BOOLEAN:
                return isTrue(value) == option.getBooleanValue();
            case IOption.STRING:
                if (value == null) value = "";
                return value.equals(option.getStringValue());
            case IOption.STRING_LIST:
                if (value == null) value = new String[0];
                return arrayEqual((String[])value, option.getBasicStringListValue());
            case IOption.ENUMERATED:
                if (value == null) return false;
                return value.equals(option.getValue());
            }
        } catch (IllegalArgumentException e) {
            
        } catch (BuildException e) {
           
        }
        return false;
    }

    @Override
    public boolean[] isInSync(IConfiguration config) {
        boolean result[] = new boolean[]{ false, false };
        try {
            for (Map.Entry<String, Object> value : optionValues.entrySet()) {
                IOption opt = BcfUtils.getOption(config, value.getKey());
                if (opt != null && !hasOptionValue(opt, value.getValue())) {
                    return result;
                }
            }

            result[0] = true;
            // Check launches
            IProject project = config.getOwner().getProject();
            if (project == null)
                return result; // should not happen
            String projectName = project.getName();
            String configId = config.getId();
            if (configId == null)
                return result; // Should not happen
            for (ILaunchConfiguration launch : DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations()) {
                String p = launch.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
                if (p == null)
                    continue; // not a CDT debugger launch
                if (!p.equals(projectName))
                    continue; // Launch associated with a different project.
                String c = launch.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID, (String) null);
                if (!configId.equals(c))
                    continue; // Launch associated with different build
                              // configuration

                ILaunchConfigurationWorkingCopy working = launch.getWorkingCopy();

                if (updateLaunchConfiguration(working, settings.getProperties()))
                    return result;
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            return result;
        }
        result[1] = true;
        return result;
    }

}
