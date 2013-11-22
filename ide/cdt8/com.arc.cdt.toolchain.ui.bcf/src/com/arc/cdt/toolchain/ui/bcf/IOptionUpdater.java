package com.arc.cdt.toolchain.ui.bcf;

import java.util.Properties;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;


/**
 * Interface to an object that updates the option settings for a project (which includes all configurations)
 * or to just a single configuration.
 * @author pickensd
 *
 */
public interface IOptionUpdater {
	
	/**
	 * Update the settings of a project.
	 * @param project
	 * @return true if something actually changed
	 * @throws BuildException 
	 */
	boolean updateProject(IProject project) throws BuildException;
	
	/**
	 * Update settings of a single configuration.
	 * @param configuration
	 * @return true if something actually changed.
	 * @throws BuildException 
	 */
	boolean updateConfiguration(IConfiguration configuration) throws BuildException;
	
	/**
	 * Update debugger settings on all Launch Configurations that are dependent on the
	 * given build configuration.
	 * @param configuration the build configuration on which all relavent Launch configurations is based.
	 * @return true if something actually changed.
	 * @throws BuildException 
	 */
	boolean updateLaunchConfigurations(IConfiguration configuration) throws BuildException;
	
	/**
	 * Given a launch configuration under construction, apply BCF properties to it..
	 * @param launch the launch configuration to be synchronized with associated BCF values.
	 * @return true if something changed.
	 * @throws BuildException
	 */
	boolean updateLaunchConfiguration(ILaunchConfigurationWorkingCopy launch) throws BuildException;
	
	/**
	 * Create the default linker command file, if warranted.
	 * @param project
	 * @return true if command file was created; false if already created or not necessary.
	 */
	boolean createLinkerCommandFile(IProject project);
	
	/** 
	 * Return whether or not the configuration and any associated launch configuration
	 * is in sync with the settings.
	 * @param config
	 * @return 2 element array such that the first element is true if config is in sync and 
	 * second element  is true if associated launches are in sync.
	 */
	boolean[] isInSync(IConfiguration config);
	
	/**
	 * update compiler attributes during import TCF File for non project case Launch Configuration GUI
	 * @param launch: the current selected launch configuration.
	 * @return true if something changed.
	 * 
	 */
	boolean updateCompileOptionForLaunchConfiguration(ILaunchConfigurationWorkingCopy launch) ; 
	
           
}
