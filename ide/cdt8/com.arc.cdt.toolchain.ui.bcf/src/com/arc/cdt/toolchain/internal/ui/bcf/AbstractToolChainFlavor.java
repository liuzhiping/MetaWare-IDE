package com.arc.cdt.toolchain.internal.ui.bcf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IMultiConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.xml.sax.SAXException;

import com.arc.cdt.toolchain.ui.bcf.Activator;
import com.arc.cdt.toolchain.ui.bcf.BadPropertyException;
import com.arc.cdt.toolchain.ui.bcf.IToolChainFlavor;
import com.arc.cdt.toolchain.ui.bcf.SettingsFileContent;

public abstract class AbstractToolChainFlavor implements IToolChainFlavor {
    
    private static final String BCF_DIR_PATTERN = "${TCF_DIR}";

    public AbstractToolChainFlavor() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean updateLaunchConfiguration(ILaunchConfigurationWorkingCopy launch) {
        try {
            String projectName = launch.getAttribute(
                    ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
            if (projectName == null||projectName.length() == 0)
                return false;
            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
            if (project == null)
                return false;
            String configName = launch.getAttribute(
                    ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID, (String) null);
            if (configName == null)
                return false;
            IManagedBuildInfo mi = ManagedBuildManager.getBuildInfo(project);
            if (mi == null)
                return false;
            IManagedProject mp = mi.getManagedProject();
            if (mp == null)
                return false;
            IConfiguration config = mp.getConfiguration(configName);
            if (config == null)
                return false;
            
            String file = BcfUtils.getTcfFilePath(config);
            if (file == null || file.length() == 0) return false;            
            SettingsFileContent prof = SettingsFileContent.read(new File(file), config);
            return this.createOptionUpdator(prof).updateLaunchConfiguration(launch);

        } catch (CoreException e) {
            Activator.log(e.getMessage(), e);
        } catch (IOException e) {
            Activator.log(e.getMessage(), e);
        } catch (BuildException e) {
            Activator.log(e.getMessage(), e);
        } catch (BadPropertyException e) {
            Activator.log(e.getMessage(), e);
        } catch (SAXException e) {
            Activator.log(e.getMessage(), e);
        }
        return false;
    }
    
//    private static String stringifyList(List<String> list){
//        StringBuilder buf = new StringBuilder();
//        for (String s: list){
//            if (buf.length() > 0) buf.append('\n');
//            if (s.indexOf(' ') >= 0 || s.indexOf('"') >= 0){
//                buf.append('"');
//                for (int i = 0, e = s.length(); i != e; ++i){
//                    char c = s.charAt(i);
//                    if (c == '"') buf.append('\\');
//                    buf.append(c);
//                }
//                buf.append('"');
//            }
//            else buf.append(s);
//        }
//        return buf.toString();
//    }
    
    /**
     * Does the given tool chain generate an executable or shared library? That is, does it
     * require the linker?
     * @param tc
     * @return
     */
    protected abstract boolean isExeToolChain(IToolChain tc);

//    /**
//     * NOTE: this code is currently not executed.
//     */
//    @Override
//    public void export(IConfiguration config, File file) throws IOException, BadPropertyException, BuildException {
//        String bcfFile = BcfUtils.getSettingsFile(config);
//        DocumentFactory factory = DocumentFactory.getInstance();
//        Element root = factory.createElement(SettingsFileContent.PROFILE_TAG);
//        if (bcfFile != null){
//            if (bcfFile.endsWith(".bcf"/*BcfUtils.CFG_SUFFIX*/)){
//                SettingsFileContent profile;
//                try {
//                    profile = SettingsFileContent.read(new File(bcfFile),config);
//                    File f = profile.getBcfFile();
//                    if (f != null) bcfFile = f.getPath();
//                } catch (SAXException e) {
//                    // Shouldn't be possible since we are not reading .bcf file
//                    Activator.log(e.getMessage(), e);
//                }
//            }
//            Element bcf = factory.createElement(SettingsFileContent.BCF_TAG);
//            root.add(bcf);
//            bcf.addAttribute(SettingsFileContent.BCF_PATH_ATTR, BcfUtils.encodeBcfPath(bcfFile, null));
//        }
//        
//        IToolChain tc = config.getToolChain();
//        doTool(factory,root,config,getCompileTool(tc),SettingsFileContent.COMPILER_TAG,bcfFile != null);
//        if (isExeToolChain(tc))
//            doTool(factory,root,config,getLinkerTool(tc),SettingsFileContent.LINKER_TAG,bcfFile != null);
//        doTool(factory,root,config,getAssemblerTool(tc),SettingsFileContent.ASM_TAG,bcfFile != null);
//
//        Document doc = factory.createDocument();
//        doc.setRootElement(root);
//        FileWriter fout = new FileWriter(file);
//        XMLWriter writer = new XMLWriter(fout, OutputFormat.createPrettyPrint());
//        writer.write(doc);
//        fout.close();
//    }
    
    
//    private void doTool(DocumentFactory factory, Element root, IConfiguration config, ITool tool, String tag, boolean hasBCF) {
//        Element element = factory.createElement(tag);
//        List<String> args;
//        try {
//            args = getArgListFromTool(factory,config, tool, hasBCF, element);
//        } catch (BuildException e) {
//            Activator.log(e.getMessage(), e);
//            return;
//        }
//        if (args.size() > 0) {
//            String argString = stringifyList(args);
//            element.setText(argString);
//        }
//        root.add(element);
//    }
    
    
    protected abstract ITool getCompileTool(IToolChain tc);
    protected abstract ITool getAssemblerTool(IToolChain tc);
    protected abstract ITool getLinkerTool(IToolChain tc);

    
    protected abstract boolean isBCFOption(IOption opt);
    
    
//    private static boolean objectsMatch(Object a, Object b){
//        if (a == b) return true;
//        if (a == null) return false;
//        if (a.equals(b)) return true;
//        if (a instanceof String[]) {
//            if (!(b instanceof String[])){
//                return false;
//            }
//            String A[] = (String[]) a;
//            String B[] = (String[]) b;
//            if (A.length != B.length) return false;
//            for (int i = 0; i < A.length; i++){
//                if (!objectsMatch(A[i],B[i])) return false;
//            }
//            return true;
//        }
//        return false;
//    }
//    private static boolean isDefault(IOption opt){
//        Object value = opt.getValue();
//        if (value == null) return true; // not set means use default.
//        Object defaultValue = opt.getDefaultValue();
//        // A null value for  default boolean option means "false"
//        if (defaultValue == null){
//            if (value instanceof Boolean)
//                defaultValue = Boolean.FALSE;
//            else if (value instanceof String)
//                defaultValue = ""; // empty string
//        }
//        return objectsMatch(value,defaultValue);
//    }
//    
//    private static boolean isEnabled(IConfiguration config, ITool tool, IOption opt){
//        IOptionApplicability c = opt.getApplicabilityCalculator();
//        if (c == null) return true;
//        return c.isOptionEnabled(config, tool, opt);
//    }
    
//    private List<String> getArgListFromTool(DocumentFactory factory, IConfiguration config, ITool tool, boolean hasBCF, Element parent) throws BuildException {
//        List<String> list = new ArrayList<String>();
//        for (IOption opt: tool.getOptions()){
//            if (!(hasBCF && isBCFOption(opt)) && isEnabled(config,tool,opt) && !isDefault(opt)){
//                switch(opt.getBasicValueType()){
//                case IOption.BOOLEAN: {
//                    String cmd = null;
//                    if (opt.getBooleanValue()) 
//                        cmd = opt.getCommand();
//                    else
//                        cmd = opt.getCommandFalse();
//                    if (cmd != null && cmd.length() > 0)
//                        list.add(cmd);
//                    break;
//                }
//                case IOption.STRING:{
//                    String s = opt.getCommand();
//                    if (s != null && s.length() > 0) {
//                        list.add(s + opt.getStringValue());
//                    }
//                    break;
//                }
//                case IOption.ENUMERATED:{
//                    String id = opt.getStringValue();
//                    String cmd = opt.getEnumCommand(id);
//                    if (cmd != null && cmd.length() > 0){
//                        list.add(cmd);
//                    }
//                    break;
//                }
//                case IOption.STRING_LIST:
//                    switch(opt.getValueType()){
//                    case IOption.LIBRARY_FILES:
//                    case IOption.OBJECTS:{
//                           String tag = opt.getBaseId().indexOf("svr3") >= 0?SettingsFileContent.LINKER_COMMAND_TAG:SettingsFileContent.LINKER_OBJECT_TAG;
//                           for (String o: opt.getBasicStringListValue()) {
//                               Element objFile = factory.createElement(tag);
//                               parent.add(objFile);
//                               objFile.setText(o);
//                           }
//                           break;
//                    }
//                    
//                    default: {                 
//                        String cmd = opt.getCommand();
//                        if (cmd != null && cmd.length() > 0) {
//                            String[] value = opt.getBasicStringListValue();
//                            for (String s: value){
//                                list.add(cmd + s);
//                            }
//                        }
//                    break;
//                    }
//                    }                  
//                }
//            }
//        }
//        return list;
//    }
    
    /**
     * The option ID of the  option that holds the BCF path.
     * @return
     */
    protected abstract String getBcfPathOptionId();
    
    /**
     * The option ID of the option that holds the CFG path.
     * @return
     */
    protected abstract String getCfgPathOptionId();
    
    private String getXXXPath(IConfiguration config, String optionId) throws BuildException{
        if (config instanceof IMultiConfiguration){
            IMultiConfiguration mc = (IMultiConfiguration) config;
            if (mc.getItems().length > 0){
                return getXXXPath((IConfiguration)mc.getItems()[0], optionId);
            }
            return null; //shouldn't get here.
        }
        IOption opt = BcfUtils.getOption(config,optionId);
        if (opt != null) {
            String s = opt.getStringValue();
            if (s != null && s.length() == 0) s = null;
            return s;
        }
        return null;       
    }

    @Override
    public String getTcfPath(IConfiguration config) throws BuildException {
        String s =  getXXXPath(config,getBcfPathOptionId());
        // To keep .cproject file portable, we reference BCF files fvrm "BCF_DIR"
        if (s != null && s.startsWith(BCF_DIR_PATTERN)){
            File f = this.getBcfRootDirectory();
            if (f != null)
                s = f.getPath() + s.substring(BCF_DIR_PATTERN.length());
            else s = null;
        }
        return s;
    }
       

//    @Override
//    public String getCfgPath(IConfiguration config) throws BuildException {
//        return getXXXPath(config,getCfgPathOptionId());
//    }
    
    protected void setXXXPath(IConfiguration config, String optionId, String path) throws BuildException {
        if (config instanceof IMultiConfiguration){
            IMultiConfiguration mc = (IMultiConfiguration) config;
            for (IConfiguration c: (IConfiguration[])mc.getItems()){
                setXXXPath(c,optionId,path);
            }
        }
        else {
            BcfUtils.setOption(config, optionId, path);
        }      
    }

    @Override
    public void setTcfPath(IConfiguration config, String bcfPath) throws BuildException {
        if (bcfPath != null) {
            bcfPath = new File(bcfPath).getPath(); // "Canonicalize"
            File bcfDir = this.getBcfRootDirectory();
            if (bcfDir != null && bcfPath.startsWith(bcfDir.getPath())){
                bcfPath = BCF_DIR_PATTERN + bcfPath.substring(bcfDir.getPath().length());
            }
            // Store in Unix format so as to be portable
            if (File.separator.charAt(0) == '\\'){
                bcfPath = bcfPath.replace('\\', '/');
            }
        }
        setXXXPath(config,getBcfPathOptionId(),bcfPath);        
    }

//    @Override
//    public void setCfgPath(IConfiguration config, String cfgPath) throws BuildException {
//        setXXXPath(config,getCfgPathOptionId(),cfgPath);        
//    }
    
    @Override
    public File[] getTcfFileList(IToolChain tc) {
        File f = getBcfRootDirectory();
        if (f == null || !f.isDirectory()) return new File[0]; // shouldn't happen
        List<File> list = new ArrayList<File>();
        if (isGenericARC(tc)) tc = null;
        extractBcfFiles(list,f,tc);
        return list.toArray(new File[list.size()]);
    }

    private void extractBcfFiles(List<File> list, File dir, IToolChain tc) {
        File[] kids = dir.listFiles();
        if (kids != null) {
            for (File file : kids) {
                if (file.isDirectory()) {
                    extractBcfFiles(list, file, tc);
                } else if (isBcfApplicable(tc, file)) {
                    list.add(file);
                }
            }
        }
    }

    protected boolean isBcfApplicable(IToolChain tc, File f) {
        if (!f.getName().toLowerCase().endsWith(BcfUtils.TCF_SUFFIX))
            return false;
        if (tc == null) return true; // generic toolchain
        try {
            SettingsFileContent content = SettingsFileContent.read(f, null);
            String processorString = content.getProperties().getProperty("processor.family",null);
            if (processorString == null) return false;
            String id = tc.getBaseId();
            switch (Integer.parseInt(processorString)){
            case 0:  // ARC 4
                return (id.indexOf("a4") >= 0 || id.indexOf("arc4") >= 0);
            case 1:   // A5
                return (id.indexOf("a5") >= 0 || id.indexOf("arc4") >= 0);
            case 2: { // ARC601
                String core = content.getProperties().getProperty("processor.core","");
                
                if (core.length() == 1
                && core.charAt(0) >= '5'
                && "0".equals(content.getProperties().getProperty(
                        "processor.Xbarrel_shifter", "0"))) 
                    return id.indexOf("601") >= 0;
                return id.indexOf("600") >= 0 || id.indexOf("a6") >= 0;
            }
            case 3: // ARC 700
                return id.indexOf("700") >= 0 || id.indexOf("a7") >= 0;

            case 4: // AV2 EM
                if (id.indexOf("em4") >= 0) 
            	    return f.getPath().indexOf("em4") >= 0;
                return id.indexOf("em") >= 0;
            case 5: return id.indexOf("hs") >= 0;

            }
        } catch (RuntimeException e){
            throw e;
        } catch (SAXException e) {
            Activator.log("From file " + f.getPath() +": " + e.getMessage(), e);
        } catch (Exception e) {
            Activator.log(e.getMessage(), e);
        }
        return false;
    }
    
    protected abstract String getCompilerVersionOptionID();
    
    /**
     * 
     * @return the root of the directory containing the predefined BCFs 
     */
    protected abstract File getBcfRootDirectory();
}
