package com.arc.cdt.toolchain.internal.ui.bcf;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IMultiConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.CDTConfigWizardPage;
import org.eclipse.cdt.managedbuilder.ui.wizards.CfgHolder;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.xml.sax.SAXException;

import com.arc.cdt.toolchain.ui.bcf.Activator;
import com.arc.cdt.toolchain.ui.bcf.BadPropertyException;
import com.arc.cdt.toolchain.ui.bcf.IOptionUpdater;
import com.arc.cdt.toolchain.ui.bcf.IToolChainFlavor;
import com.arc.cdt.toolchain.ui.bcf.SettingsFileContent;
import com.arc.cdt.toolchain.ui.bcf.ToolChainFlavorFactory;

/**
 * Utility functions for applying Configuration information to a project
 * 
 * @author pickensd
 * 
 */
public class BcfUtils {

    private static final ICdtVariableManager vmgr = CCorePlugin.getDefault().getCdtVariableManager();
    //private static final IUserVarSupplier fUserSup = CCorePlugin.getUserVarSupplier();

    /**
     * Build variable that overrides the default BCF directory location
     */
    public static final String SETTINGS_DIR = "SETTINGS_DIR";
    
    public static final String CFG_FILTER = "*.cfg";
    public static final String TCF_FILTER = "*.tcf";
    public static final String CFG_SUFFIX = CFG_FILTER.substring(1);
    public static final String TCF_SUFFIX = TCF_FILTER.substring(1);


    /**
     * Recognized extensions for CAT file
     */
    // CDT requires the filter file in the format of "*.<extension>"
    public final static String EXTENSIONS[] = { TCF_FILTER /*, CFG_FILTER */};

    private BcfUtils() {
        // TODO Auto-generated constructor stub
    }

    public static File getSelectedBcfPath() {
        for (IWizardPage page : MBSCustomPageManager.getCustomPages()) {
            if (page instanceof IBCFLocation) {
                return ((IBCFLocation) page).getBcfLocation();
            }
        }
        return null;
    }

    public static IProject getSelectedProject() {
        for (IWizardPage page : MBSCustomPageManager.getCustomPages()) {
            IWizard wizard = page.getWizard();
            if (wizard instanceof CDTCommonProjectWizard) {
                return ((CDTCommonProjectWizard) wizard).getProject(false);
            }
        }
        return null;
    }
    
    public static IProjectType getSelectedProjectType() {
        for (IWizardPage page : MBSCustomPageManager.getPages()) {
            if (page instanceof CDTConfigWizardPage) {
            	CDTConfigWizardPage h = (CDTConfigWizardPage)page;
            	CfgHolder cfgs[] =  h.getCfgItems(true);           
            	if (cfgs != null && cfgs.length > 0){
            		return cfgs[0].getProjectType();
            	}
            }
        }
        return null;
    }
    
    public static IOption getOption(IConfiguration c, String id) throws BuildException {
        for (ITool tool : c.getTools()) {
            for (IOption option : tool.getOptions()) {
                if (id.equals(option.getBaseId())) {
                    return option;
                }
            }
        }
        return null;
    }

    public static void setOption(IConfiguration c, String id, boolean v) throws BuildException {
        for (ITool tool : c.getTools()) {
            for (IOption option : tool.getOptions()) {
                if (id.equals(option.getBaseId())) {
                    c.setOption((IHoldsOptions) tool, option, v);
                    return;
                }
            }
        }
    }
    
    public static void setOption(IConfiguration c, String id, String[] list) throws BuildException {
        for (ITool tool : c.getTools()) {
            for (IOption option : tool.getOptions()) {
                if (id.equals(option.getBaseId())) {
                    c.setOption((IHoldsOptions) tool, option, list);
                    return;
                }
            }
        }
    }

    public static void setOption(IConfiguration c, String id, String v) throws BuildException {
        for (ITool tool : c.getTools()) {
            for (IOption option : tool.getOptions()) {
                if (id.equals(option.getBaseId())) {
                    c.setOption((IHoldsOptions) tool, option, v);
                    return;
                }
            }
        }
    }

    public static void setOption(IProject p, String id, String v) throws BuildException {
        for (IConfiguration c : getConfigurations(p)) {
            setOption(c, id, v);
        }
    }

  
    public static File computeBcfDirFromBuildVar(IConfiguration config) {
        if (config instanceof IMultiConfiguration){
            config = (IConfiguration)((IMultiConfiguration)config).getItems()[0];
        }
        ICdtVariable v = vmgr.getVariable(SETTINGS_DIR,
                ManagedBuildManager.getDescriptionForConfiguration(config));

        if (v != null) {
            File d;
            try {
                d = new File(v.getStringValue());
                if (d.isDirectory())
                    return d;
            } catch (CdtVariableException e) {
                // Something went wrong. Use Default
            }
        }
        return null;
    }
    

    public static File[] getBcfFileList(File file) {
    	List<File> list = new ArrayList<File>();
    	extractSettingsFiles(file,list);
    	return list.toArray(new File[list.size()]);
    }
    
    private static void extractSettingsFiles(File file, List<File> result){
        if (file.isFile()){
            if (isSettingsFile(file)){
                result.add(file);
            }
        }
        else if (file.isDirectory()){
            for (File kid: file.listFiles()){
                extractSettingsFiles(kid,result);
            }
        }
    }
    
    public static void writeFile(IProject project, String fileName, String content) {
        IPath path = project.getLocation().append(fileName);
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IFile file = root.getFile(path);
        InputStream stream = new ByteArrayInputStream(content.getBytes());
        try {
            file.create(stream, true, new NullProgressMonitor());
        } catch (CoreException e) {
             displayError("Could not write " + path, e, Display.getCurrent().getActiveShell());
        }
    }
    
    private static boolean isSettingsFile(File file) {
        if (!file.isFile())
            return false;
        String n = file.getName().toLowerCase();
        for (String s : EXTENSIONS) {
            int index = s.indexOf("*.");
            if (index >= 0)
                s = s.substring(index + 1);

            if (n.endsWith(s))
                return true;
        }
        return false;
    }

    public static String getSettingsFile(IProject project) throws BadPropertyException, BuildException {
       for (IConfiguration config: getConfigurations(project)){
           return getTcfFilePath(config);
       }
       return null;
    }

    private static void setSettingsFile(IProject project, String file) throws BadPropertyException, IOException, SAXException, BuildException {
        for (IConfiguration config: getConfigurations(project)){
            setSettingsFile(config,file);
        }      
    }

    private static final String[] BUILD_VARS = { SETTINGS_DIR, "workspace_loc", "METAWARE_ROOT", "eclipse_home", "ProjDirPath", "WorkspaceDirPath"};
    /**
     * Make the BCF path "relative" by replacing its prefix with "${SETTINGS_DIR}", etc.
     * @param s
     * @param config
     * @return
     */
    public static String encodeBcfPath(String s, IConfiguration config){
        String ss = s;
        if (PathUtil.isWindowsFileSystem()){
            ss = s.toLowerCase().replaceAll("/", "\\");
        }
        for (String varName: BUILD_VARS){
            ICdtVariable var = vmgr.getVariable(varName,  config != null?ManagedBuildManager.getDescriptionForConfiguration(config):null);
            if (var != null && (var.getValueType() == ICdtVariable.VALUE_PATH_DIR || var.getValueType() == ICdtVariable.VALUE_TEXT)){
                String varVal;
                try {
                    varVal = var.getStringValue();
                    if (PathUtil.isWindowsFileSystem()){
                        varVal = varVal.toLowerCase().replaceAll("/", "\\");
                    }
                    if (ss.startsWith(varVal)){
                        return "${" + varName + "}" + s.substring(varVal.length());
                    }
                } catch (CdtVariableException e) {
                    // Ignore
                }

            }
        }
        return s;
    }

    public static String decodeBcfPath(String s, IConfiguration config){
        try {
            return vmgr.resolveValue(s, null, null, config != null?ManagedBuildManager.getDescriptionForConfiguration(config):null);
        } catch (CdtVariableException e) {
            return s;
        }
    }

//    public static String getBcfFile(IConfiguration config) throws BadPropertyException {
//        IToolChainFlavor flavor = ToolChainFlavorFactory.Get(config.getToolChain());        
//        return flavor.getBcfPath(config);
//    }
//
//    public static void setBcfFile(IConfiguration config, String bcfPath) throws BadPropertyException {
//        IToolChainFlavor flavor = ToolChainFlavorFactory.Get(config.getToolChain());
//        flavor.setBcfPath(config,bcfPath);
//    }
    
    /**
     * Return the .tcf file associated with a particular configuration.
     * @param config
     * @return he .tcf file associated with a particular configuration, or null.
     * @throws BadPropertyException 
     * @throws BuildException 
     */
    public static String getTcfFilePath(IConfiguration config) throws BadPropertyException, BuildException{
        IToolChainFlavor flavor = ToolChainFlavorFactory.Get(config.getToolChain());        
        String f ;// = flavor.getCfgPath(config);
       // if (f == null || f.length() == 0)
            f = flavor.getTcfPath(config);
        return f;
    }
    
    public static void setSettingsFile(IConfiguration config, String fileName) throws BadPropertyException, IOException, SAXException, BuildException{
        IToolChainFlavor flavor = ToolChainFlavorFactory.Get(config.getToolChain());
        if (fileName == null) {
            flavor.setTcfPath(config,null);
            //flavor.setCfgPath(config,null);
        }
        else {
            File file = new File(fileName);
            SettingsFileContent profile = SettingsFileContent.read(file, config);
            if (file.equals(profile.getBcfFile())){
                // There is no .cfg file. The settings file is a direct .bcf reference
                flavor.setTcfPath(config, fileName);
               // flavor.setCfgPath(config, null);
            }
            else {
                //flavor.setCfgPath(config, fileName);
                flavor.setTcfPath(config, profile.getBcfFile() != null? profile.getBcfFile().getPath():null);
            }
        }
    }

    public static IConfiguration[] getConfigurations(IProject project) {
        IManagedBuildInfo mi = ManagedBuildManager.getBuildInfo(project);
        if (mi == null)
            return new IConfiguration[0];
        IManagedProject mp = mi.getManagedProject();
        if (mp == null)
            return new IConfiguration[0];
        return mp.getConfigurations();
    }

    public static IProjectType getProjectType(IProject project) {
        IManagedBuildInfo mi = ManagedBuildManager.getBuildInfo(project);
        if (mi == null)
            return null;
        IManagedProject mp = mi.getManagedProject();
        return mp.getProjectType();
    }

    public static boolean isLibProject(IProject project) {
        IProjectType pt = getProjectType(project);
        return pt.getBaseId().indexOf("lib") > 0;
    }

    public static void flushBuildInfo(IProject project) {
        ManagedBuildManager.saveBuildInfo(project, true);
    }

    public static void setProjectType(IProjectType projectType, IProject project) {

    }

    public static void setToolChain(IConfiguration config, IToolChain toolChain)
            throws BuildException {
        assert (toolChain != null);
        config.getRootFolderInfo().changeToolChain(toolChain, CDataUtil.genId(toolChain.getId()),
                null);
    }

    private static boolean stringsCompare(String a, String b) {
        if (a == null)
            return b == null;
        return a.equals(b);
    }

    public static boolean applySettingsFileToProject(File settingsFile, IProject project, boolean sync) throws BadPropertyException, BuildException, IOException, SAXException {
        IManagedBuildInfo mi = ManagedBuildManager.getBuildInfo(project);
        if (mi == null)
            return false; // not a managed project

        boolean result = false;

        String settingsPath = settingsFile != null ? settingsFile.getPath() : null;

        if (sync && settingsPath != null) {
            SettingsFileContent settings;
            try {
                settings = SettingsFileContent.read(settingsFile,mi.getSelectedConfiguration());
            } catch (SAXException e) {
                throw new IOException(e);
            }
            IConfiguration configs[] = mi.getManagedProject().getConfigurations();
            if (configs.length > 0) {
                IToolChainFlavor tcFlavor = ToolChainFlavorFactory.Get(configs[0].getToolChain());
                IOptionUpdater updater = tcFlavor.createOptionUpdator(settings);
                result |= updater.updateProject(project);
                result |= updater.createLinkerCommandFile(project);
            }
        }
        
        // Associate the settings file with each configuration.
        // NOTE: the configurations may have changed if the "sync" operation altered the
        // tool chain.
        if (!stringsCompare(getSettingsFile(project), settingsPath)) {
            result = true;
            setSettingsFile(project, settingsPath);
        }
        return result;
    }

    public static boolean associateSettingsFile(File settingsFile, IConfiguration config) {

        boolean result = false;

        String settingsPath = settingsFile != null ? settingsFile.getPath() : null;

        try {
            if (!stringsCompare(getTcfFilePath(config), settingsPath)) {
                result = true;
                setSettingsFile(config, settingsPath);
            }
        } catch (BadPropertyException e) {
            Activator.log(e.getMessage(), e);
        } catch (IOException e) {
            Activator.log(e.getMessage(), e);
        } catch (SAXException e) {
            Activator.log(e.getMessage(), e);
        } catch (BuildException e) {
            Activator.log(e.getMessage(), e);
        }
        return result;
    }

    public static boolean applySettingsFileToConfig(File bcfFile, IConfiguration config, boolean updateConfig, boolean updateLaunches) throws IOException,
    BadPropertyException, BuildException, SAXException {
        IProject project = config.getOwner().getProject();
        if (project == null)
            return false;

        boolean result = false;
        String bcfPath = bcfFile != null ? bcfFile.getPath() : null;

        if (bcfPath != null) {
            SettingsFileContent settings;
            try {
                settings = SettingsFileContent.read(bcfFile,config);
            } catch (SAXException e) {
                throw new IOException(e.getMessage(),e);
            }
            IToolChainFlavor tcFlavor = ToolChainFlavorFactory.Get(config.getToolChain());
            IOptionUpdater updater = tcFlavor.createOptionUpdator(settings);
            if (config instanceof IMultiConfiguration) {
                IConfiguration[] cfs = (IConfiguration[]) ((IMultiConfiguration) config).getItems();
                for (int i = 0; i < cfs.length; i++) {
                    if (updateConfig)
                        result |= updater.updateConfiguration(cfs[i]);
                    if (updateLaunches){
                        result |= updater.updateLaunchConfigurations(config);
                    }
                }
            } else {
                if (updateConfig)
                    result |= updater.updateConfiguration(config);
                if (updateLaunches){
                    result |= updater.updateLaunchConfigurations(config);
                }
            }
            // Create the linker command file if launches are being updated, which means the
            // settings are being applied, not just tenatively.
            if (updateLaunches)
                result |= updater.createLinkerCommandFile(project);
        }
        
        associateSettingsFile(bcfFile,config); // in case tool chain changed
        
        return result;

    }
    
    
    private static final boolean FALSE_FALSE[] = { false, false };
    /**
     * 
     * @param config
     * @param content
     * @return 2 element boolean array; first element is true if configuratin is in sync; second is
     * true if associated launch configurations are in sync.
     */
    public static boolean[] isInSync(IConfiguration config, SettingsFileContent content){
    	if (config == null) return FALSE_FALSE; // shouldn't happen
    	if (content == null) return FALSE_FALSE; // shouldn't happen
    	IOptionUpdater updator;
        try {
            IToolChainFlavor flavor = ToolChainFlavorFactory.Get(config.getToolChain());
            String bcfPath = flavor.getTcfPath( config);
            if (bcfPath == null) return FALSE_FALSE;
            if (!new File(bcfPath).equals(content.getBcfFile())) return FALSE_FALSE;

            updator = flavor.createOptionUpdator(content);
        } catch (RuntimeException e) {
              throw e;
        } catch (Exception e) {
            return FALSE_FALSE;
        }
    	return updator.isInSync(config);
    }

    public static void displayBCFError(Exception ex, Composite parent) {
        IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR,
                ex.getMessage(), ex);
        String msg;
        if (ex instanceof IOException) {
            msg = "Error occurred while reading TCF file";
        } else
            msg = "TCF file appears to be invalid or corrupt";
        Activator.log(msg, ex);
        ErrorDialog.openError(parent != null?parent.getShell(): null, "TCF Read Error", msg, status);
    }
    
    public static void displayBCFError(String msg, Composite parent) {
    	IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, msg);
    	ErrorDialog.openError(parent != null?parent.getShell(): null, "TCF Read Error", msg,  status);
    }
    
    public static void displayError(String msg, Throwable t, Composite parent) {
        IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, msg,t);
        ErrorDialog.openError(parent != null?parent.getShell(): null, "BCF Read Error", msg,  status);
    }
    
//    public static void export(IConfiguration config, String fileName) throws BadPropertyException, IOException, BuildException{
//        IToolChainFlavor tcFlavor = ToolChainFlavorFactory.Get(config.getToolChain());
//        tcFlavor.export(config, new File(fileName));
//    }
}
