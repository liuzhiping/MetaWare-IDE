package com.arc.cdt.toolchain.ui.bcf;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.xml.sax.SAXException;

/**
 * There is an instance of this interface for each "flavor" of toolchain.
 * One for  MetaWare and one for GNU ARC
 * @author pickensd
 *
 */
public interface IToolChainFlavor {
	/**
	 * Create an object that is responsible for updating the option settings for a project or configuration.
	 * @param profile the BCF profile to be applied
	 * @return
	 * @throws BadPropertyException
	 * @throws IOException 
	 * @throws SAXException 
	 */
	IOptionUpdater createOptionUpdator(SettingsFileContent profile) throws BadPropertyException, IOException, SAXException;
	
	/**
	 * Given a configuration, return its target processor family.
	 * @param config
	 * @return target processor family
	 */
	
	ProcessorFamily getProcessorFamily(IToolChain tc);
	
	/**
	 * Return whether or not the given configuration is for the legacy  "generic" ARC.
	 * @param config
	 * @return true if the configuration is for a legacy ARC.
	 */
	boolean isGenericARC(IToolChain tc);
		
	/**
	 * This is called when the user selects "Restore Defaults".
	 * <P>
	 * This function must do whatever is necessary to set the processor family for the given configuration.
	 * This means setting the corresponding compiler, assembler, and linker options that
	 * denote the processor family (e.g, "AV2EM", "AV2HS", "ARC600", etc.
	 * <P>
	 * The reason that these options are not necessarily correct for the MetaWare case is that
	 * each ARC project type is based on the "Generic" ARC project type that has its default
	 * processor family set to ARC600. This function must look at the toolchain name and set
	 * processor family correctly.
	 * <P>
	 * Supposedly there is a way from the plugin.xml to conditionally alter the default for
	 * any option based on the "enablement" subtag, but I haven't been able to get it to 
	 * work. But that would be preferable to having to call this function.
	 * <P>
	 * If the GNU ARC has just one project type for all ARC targets, then this method
	 * need not do anything.
	 * @param config
	 */
	void setProcessorFamily(IConfiguration config);
	
	/**
	 * Hook that can be called from the Debugger Configuration UI to set
	 * BCF properties within a Launch Configuration that is under construction, or
	 * needing defaults applied.
	 *
	 * @param launch the launch configuration.
	 * @return true if change was made.
	 */
	boolean updateLaunchConfiguration(ILaunchConfigurationWorkingCopy launch);
	
//	/**
//	 * Export the settings of a configuration file into a .cfg file.
//	 * @param config
//	 * @param file bcf file or null.
//	 * @throws IOException 
//	 * @throws BuildException 
//	 * @throws BadPropertyException 
//	 */
//	void export(IConfiguration config, File file) throws IOException, BadPropertyException, BuildException;
	
	/**
	 * @param config
	 * @return the TCF path, if any, associated with the given configuration; otherwise null.
	 * @throws BuildException 
	 */
	
	String getTcfPath(IConfiguration config) throws BuildException;
	
//	/**
//	 * Returns the .cfg path, if one is associated with the configuration.
//	 * Note that the ".bcf" path returned from "getBcfPath(config)" should match
//	 * the bcf file path that this .cfg file references.
//	 * @param config
//	 * @return
//	 * @throws BuildException 
//	 */
//	String getCfgPath(IConfiguration config) throws BuildException;
	
	/**
	 * Set the TCF path for the given configurtion.
	 * @param config
	 * @param tcfPath the .tcf path, or null to clear it.
	 * @throws BuildException 
	 */
	void setTcfPath(IConfiguration config, String tcfPath) throws BuildException;
	
//	/**
//	 * Set the .cfg path for the given configuration.
//	 * NOTE: any ".bcf" file shoujld be referenced from this .cfg file.
//	 * @param config
//	 * @param cfgPath the .cfg path, or null to clear it.
//	 * @throws BuildException 
//	 */
//	void setCfgPath(IConfiguration config, String cfgPath) throws BuildException;
	
	
	/**
	 * Return the directory of where the predefined TCFs are located for the particular
	 * target. E.e., ".../arc/metaware/ide/tcf/arcem4"
	 * @param tc the specific toolchain from which the target processor type can be derived.
	 * @return directory of predefined BCFs
	 */
	File[] getTcfFileList(IToolChain tc);

}
