/*******************************************************************************
 * Copyright (c) 2004, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.buildproperties.IBuildProperty;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyType;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.IBuildObjectProperties;
import org.eclipse.cdt.managedbuilder.core.IBuildPropertiesRestriction;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IConfigurationNameProvider;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.envvar.IProjectEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.macros.IProjectBuildMacroSupplier;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PluginVersionIdentifier;


public class ProjectType extends BuildObject implements IProjectType, IBuildPropertiesRestriction, IBuildPropertyChangeListener {
	
	private static final String EMPTY_STRING = new String();
	//private static final IConfiguration[] emptyConfigs = new IConfiguration[0];
	
	//  Superclass
	private IProjectType superClass;
	private String superClassId;
	//  Parent and children
	private List configList;	//  Configurations of this project type
	private Map configMap;
	//  Managed Build model attributes
	private Boolean isAbstract;
	private Boolean isTest;
	private String unusedChildren;
	private String convertToId;

	private IConfigurationElement configurationNameProviderElement = null;
	private IConfigurationNameProvider configurationNameProvider = null;

	private IConfigurationElement environmentVariableSupplierElement = null;
	private IProjectEnvironmentVariableSupplier environmentVariableSupplier = null;
	private IConfigurationElement buildMacroSupplierElement = null;
	private IProjectBuildMacroSupplier buildMacroSupplier = null;
	
	BuildObjectProperties buildProperties;


	//  Miscellaneous
	private boolean resolved = true;
	private IConfigurationElement previousMbsVersionConversionElement;
	private IConfigurationElement currentMbsVersionConversionElement;
	
	/*
	 *  C O N S T R U C T O R S
	 */

	/**
	 * This constructor is called to create a projectType defined by an extension point in 
	 * a plugin manifest file.
	 * 
	 * @param element
	 * @param managedBuildRevision
	 */
	public ProjectType(IManagedConfigElement element, String managedBuildRevision) {
		// setup for resolving
		resolved = false;

		setManagedBuildRevision(managedBuildRevision);
		
		loadFromManifest(element);
		
		// Hook me up to the Managed Build Manager
		ManagedBuildManager.addExtensionProjectType(this);

		// Load the configuration children
		IManagedConfigElement[] configs = element.getChildren(IConfiguration.CONFIGURATION_ELEMENT_NAME);
		
		String [] usedConfigNames = new String[configs.length];
		IConfigurationNameProvider configurationNameProvder = getConfigurationNameProvider();
		
		if (  configurationNameProvder != null ) {
			// Tool Integrator provided 'ConfigurationNameProvider' class
			// to get configuration names dynamically based architecture, os, toolchain version etc.
			for (int n = 0; n < configs.length; ++n) {
				Configuration config = new Configuration(this, configs[n], managedBuildRevision);
				String newConfigName = configurationNameProvder.getNewConfigurationName(config, usedConfigNames);
				config.setName(newConfigName);
				usedConfigNames[n] = newConfigName;
			}
		} else {
			for (int n = 0; n < configs.length; ++n) {
				Configuration config = new Configuration(this, configs[n], managedBuildRevision);
			}
		}
	}

	/**
	 * This constructor is called to create a project type whose attributes and children will be 
	 * added by separate calls.
	 * 
	 * @param ProjectType The superClass, if any
	 * @param String The id for the new project type
	 * @param String The name for the new project type
	 */
	public ProjectType(ProjectType superClass, String Id, String name, String managedBuildRevision) {
		// setup for resolving
		resolved = false;

		this.superClass = superClass;
		if (this.superClass != null) {
			superClassId = this.superClass.getId();
		}
		setId(Id);
		setName(name);
		
		setManagedBuildRevision(managedBuildRevision);
		setVersion(getVersionFromId());
		
		// Hook me up to the Managed Build Manager
		ManagedBuildManager.addExtensionProjectType(this);
	}

	/*
	 *  E L E M E N T   A T T R I B U T E   R E A D E R S   A N D   W R I T E R S
	 */
	
	/* (non-Javadoc)
	 * Load the project-type information from the XML element specified in the 
	 * argument
	 * @param element An XML element containing the project type information 
	 */
	protected void loadFromManifest(IManagedConfigElement element) {
		ManagedBuildManager.putConfigElement(this, element);
		
		// id
		setId(element.getAttribute(ID));
		
		// Get the name
		setName(element.getAttribute(NAME));
		
		// version
		setVersion(getVersionFromId());
		
		// superClass
		superClassId = element.getAttribute(SUPERCLASS);
		
		String props = element.getAttribute(BUILD_PROPERTIES);
		if(props != null)
			buildProperties = new BuildObjectProperties(props, this, this);

		String artType = element.getAttribute(BUILD_ARTEFACT_TYPE);
		if(artType != null){
			if(buildProperties == null)
				buildProperties = new BuildObjectProperties(this, this);
			
			try {
				buildProperties.setProperty(ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_ID, artType, true);
			} catch (CoreException e) {
				ManagedBuilderCorePlugin.log(e);
			}
		}
			

		// Get the unused children, if any
		unusedChildren = element.getAttribute(UNUSED_CHILDREN); 
		
		// isAbstract
        String isAbs = element.getAttribute(IS_ABSTRACT);
        if (isAbs != null){
    		isAbstract = new Boolean("true".equals(isAbs)); //$NON-NLS-1$
        }

		// Is this a test project type
		String isTestStr = element.getAttribute(IS_TEST); //$NON-NLS-1$
        if (isTestStr != null){
    		isTest = new Boolean("true".equals(isTestStr)); //$NON-NLS-1$
        }
		
		// Store the configuration element IFF there is a configuration name provider defined 
		if (element.getAttribute(CONFIGURATION_NAME_PROVIDER) != null && element instanceof DefaultManagedConfigElement) {
			configurationNameProviderElement = ((DefaultManagedConfigElement)element).getConfigurationElement();			
		}
		
		// Get the environmentVariableSupplier configuration element
		String environmentVariableSupplier = element.getAttribute(PROJECT_ENVIRONMENT_SUPPLIER); 
		if(environmentVariableSupplier != null && element instanceof DefaultManagedConfigElement){
			environmentVariableSupplierElement = ((DefaultManagedConfigElement)element).getConfigurationElement();
		}

		// Get the buildMacroSupplier configuration element
		String buildMacroSupplier = element.getAttribute(PROJECT_MACRO_SUPPLIER); 
		if(buildMacroSupplier != null && element instanceof DefaultManagedConfigElement){
			buildMacroSupplierElement = ((DefaultManagedConfigElement)element).getConfigurationElement();
		}

		// Get the 'convertToId' attribute if it is available
		convertToId = element.getAttribute(CONVERT_TO_ID);
	}

	/*
	 *  P A R E N T   A N D   C H I L D   H A N D L I N G
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IProjectType#createConfiguration(org.eclipse.cdt.core.build.managed.IConfiguration)
	 */
	public IConfiguration createConfiguration(IConfiguration parent, String id, String name) {
		Configuration config = new Configuration(this, parent, id, name);
//		ManagedBuildManager.performValueHandlerEvent(config, IManagedOptionValueHandler.EVENT_OPEN);
		return (IConfiguration)config;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IProjectType#getConfiguration()
	 */
	public IConfiguration getConfiguration(String id) {
		return (IConfiguration)getConfigurationMap().get(id);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IProjectType#getConfigurations()
	 */
	public IConfiguration[] getConfigurations() {
		IConfiguration[] configs = new IConfiguration[getConfigurationList().size()];
		Iterator iter = getConfigurationList().listIterator();
		int i = 0;
		while (iter.hasNext()) {
			Configuration config = (Configuration)iter.next();
			configs[i++] = (IConfiguration)config; 
		}
		return configs;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IProjectType#removeConfiguration(java.lang.String)
	 */
	public void removeConfiguration(String id) {
		// Remove the specified configuration from the list and map
		Iterator iter = getConfigurationList().listIterator();
		while (iter.hasNext()) {
			 IConfiguration config = (IConfiguration)iter.next();
			 if (config.getId().equals(id)) {
			 	getConfigurationList().remove(config);
				getConfigurationMap().remove(id);
			 	break;
			 }
		}
	}
	
	/* (non-Javadoc)
	 * Adds the Configuration to the Configuration list and map
	 * 
	 * @param Tool
	 */
	public void addConfiguration(Configuration configuration) {
		if(!configuration.isTemporary()){
			getConfigurationList().add(configuration);
			getConfigurationMap().put(configuration.getId(), configuration);
		}
	}
	
	/* (non-Javadoc)
	 * Safe accessor for the list of configurations.
	 * 
	 * @return List containing the configurations
	 */
	private List getConfigurationList() {
		if (configList == null) {
			configList = new ArrayList();
		}
		return configList;
	}
	
	/* (non-Javadoc)
	 * Safe accessor for the map of configuration ids to configurations
	 * 
	 * @return
	 */
	private Map getConfigurationMap() {
		if (configMap == null) {
			configMap = new HashMap();
		}
		return configMap;
	}

	/*
	 *  M O D E L   A T T R I B U T E   A C C E S S O R S
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuildObject#getName()
	 */
	public String getName() {
		String name = getNameAttribute();
		if(name.length() == 0){
			IBuildObjectProperties props = getBuildProperties();
			IBuildProperty prop = props.getProperty(ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_ID);
			if(prop != null)
				name = prop.getValue().getName();
		}
		return name;
	}

	public String getNameAttribute() {
		// If I am unnamed, see if I can inherit one from my parent
		if (name == null) {
			if (superClass != null) {
				return (superClass).getNameAttribute();
			} else {
				return new String(""); //$NON-NLS-1$
			}
		} else {
			return name;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IProjectType#getSuperClass()
	 */
	public IProjectType getSuperClass() {
		return superClass;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IProjectType#isAbstract()
	 */
	public boolean isAbstract() {
		if (isAbstract != null) {
			return isAbstract.booleanValue();
		} else {
			return false;	// Note: no inheritance from superClass
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IProjectType#unusedChildren()
	 */
	public String getUnusedChildren() {
		if (unusedChildren != null) {
			return unusedChildren;
		} else
			return EMPTY_STRING;	// Note: no inheritance from superClass
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IProjectType#isTestProjectType()
	 */
	public boolean isTestProjectType() {
		if (isTest == null) {
			// If I have a superClass, ask it
			if (superClass != null) {
				return superClass.isTestProjectType();
			} else {
				return false;
			}
		}
		return isTest.booleanValue();
	}

	/* (non-Javadoc)
	 * Sets the isAbstract attribute
	 */
	public void setIsAbstract(boolean b) {
		isAbstract = new Boolean(b);
	}

	/* (non-Javadoc)
	 * Sets the isTest attribute
	 */
	public void setIsTest(boolean b) {
		isTest = new Boolean(b);
	}

	/*
	 *  O B J E C T   S T A T E   M A I N T E N A N C E
	 */
	
	/* (non-Javadoc)
	 *  Resolve the element IDs to interface references
	 */
	public void resolveReferences() {
		if (!resolved) {
			resolved = true;
			// Resolve superClass
			if (superClassId != null && superClassId.length() > 0) {
				superClass = ManagedBuildManager.getExtensionProjectType(superClassId);
				if (superClass == null) {
					// Report error
					ManagedBuildManager.outputResolveError(
							"superClass",	//$NON-NLS-1$
							superClassId,
							"projectType",	//$NON-NLS-1$
							getId());
				}
			}
			
			// Add configurations from our superClass that are not overridden here
			if (superClass != null) {
			    ((ProjectType)superClass).resolveReferences();
			    IConfiguration[] superConfigs = superClass.getConfigurations();
			    for (int i = 0; i < superConfigs.length; i++) {
			        String superId = superConfigs[i].getId();
				    
				    check: { 
					    IConfiguration[] currentConfigs = getConfigurations();
				        for (int j = 0; j < currentConfigs.length; j++) {
					        IConfiguration config = currentConfigs[j];
					        while (config.getParent() != null) {
					            if (config.getParent().getId().equals(superId)) break check;
					            config = config.getParent();
					        }
				        }
				        addConfiguration((Configuration)superConfigs[i]);
				    } // end check
				    
			    }
			}

			// Call resolve references on any children
			Iterator configIter = getConfigurationList().iterator();
			while (configIter.hasNext()) {
				Configuration current = (Configuration)configIter.next();
				current.resolveReferences();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IProjectType#isSupported()
	 */
	public boolean isSupported(){
		Iterator configIter = getConfigurationList().iterator();
		while (configIter.hasNext()) {
			Configuration current = (Configuration)configIter.next();
			if(current.isSupported())
				return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IProjectType#getConfigurationNameProviderElement()
	 */
	public IConfigurationElement getConfigurationNameProviderElement() {
		if(configurationNameProviderElement == null){
			if(superClass != null) {
				ProjectType tmpSuperClass = (ProjectType)superClass;
				return tmpSuperClass.getConfigurationNameProviderElement();
			}
		}
		return configurationNameProviderElement;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IProjectType#setConfigurationNameProviderElement(IConfigurationElement)
	 */
	
	public void setConfigurationNameProviderElement(IConfigurationElement configurationElement) {
		configurationNameProviderElement = configurationElement;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IProjectType#getConfigurationNameProvider()
	 */
	public IConfigurationNameProvider getConfigurationNameProvider() {
		
		if (configurationNameProvider != null) {
			return configurationNameProvider;
		}
		
		IConfigurationElement element = getConfigurationNameProviderElement();
		if (element != null) {
			try {
				if (element.getAttribute(CONFIGURATION_NAME_PROVIDER) != null) {
					configurationNameProvider = (IConfigurationNameProvider) element.createExecutableExtension(CONFIGURATION_NAME_PROVIDER);
					return configurationNameProvider;
				}
			} catch (CoreException e) {}
		}
		return null;
	}
	
	/**
	 * Returns the plugin.xml element of the projectEnvironmentSupplier extension or <code>null</code> if none. 
	 *  
	 * @return IConfigurationElement
	 */
	public IConfigurationElement getEnvironmentVariableSupplierElement(){
		if (environmentVariableSupplierElement == null) {
			if (superClass != null && superClass instanceof ProjectType) {
				return ((ProjectType)superClass).getEnvironmentVariableSupplierElement();
			}
		}
		return environmentVariableSupplierElement;
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IProjectType#getEnvironmentVariableSupplier()
	 */
	public IProjectEnvironmentVariableSupplier getEnvironmentVariableSupplier(){
		if (environmentVariableSupplier != null) {
			return environmentVariableSupplier;
		}
		IConfigurationElement element = getEnvironmentVariableSupplierElement();
		if (element != null) {
			try {
				if (element.getAttribute(PROJECT_ENVIRONMENT_SUPPLIER) != null) {
					environmentVariableSupplier = (IProjectEnvironmentVariableSupplier) element.createExecutableExtension(PROJECT_ENVIRONMENT_SUPPLIER);
					return environmentVariableSupplier;
				}
			} catch (CoreException e) {}
		}
		return null;
	}

	/**
	 * Returns the plugin.xml element of the projectMacroSupplier extension or <code>null</code> if none. 
	 *  
	 * @return IConfigurationElement
	 */
	public IConfigurationElement getBuildMacroSupplierElement(){
		if (buildMacroSupplierElement == null) {
			if (superClass != null && superClass instanceof ProjectType) {
				return ((ProjectType)superClass).getBuildMacroSupplierElement();
			}
		}
		return buildMacroSupplierElement;
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IProjectType#getBuildMacroSupplier()
	 */
	public IProjectBuildMacroSupplier getBuildMacroSupplier(){
		if (buildMacroSupplier != null) {
			return buildMacroSupplier;
		}
		IConfigurationElement element = getBuildMacroSupplierElement();
		if (element != null) {
			try {
				if (element.getAttribute(PROJECT_MACRO_SUPPLIER) != null) {
					buildMacroSupplier = (IProjectBuildMacroSupplier) element.createExecutableExtension(PROJECT_MACRO_SUPPLIER);
					return buildMacroSupplier;
				}
			} catch (CoreException e) {}
		}
		return null;
	}
	
	
	public String getConvertToId() {
		if (convertToId == null) {
			// If I have a superClass, ask it
			if (getSuperClass() != null) {
				return getSuperClass().getConvertToId();
			} else {
				return EMPTY_STRING;
			}
		}
		return convertToId;
	}

	
	public void setConvertToId(String convertToId) {
		if (convertToId == null && this.convertToId == null) return;
		if (convertToId == null || this.convertToId == null || !convertToId.equals(this.convertToId)) {
			this.convertToId = convertToId;			
		}
		return;
	}
	
	/*
	 * This function checks for migration support for the projectType while
	 * loading the project. If migration support is needed, looks for the available
	 * converters and adds them to the list.
	 */

	public boolean checkForMigrationSupport() {
	
		String convertToId = getConvertToId();
		if ((convertToId == null) || (convertToId.equals(""))) { //$NON-NLS-1$
				// It means there is no 'convertToId' attribute available and
				// the project type is still actively
				// supported by the tool integrator. So do nothing, just return
				return true;
		} else {
				// In case the 'convertToId' attribute is available,
				// it means that Tool integrator currently does not support this
				// project type.
				// Look for the converters available for this project type.

				return getConverter(convertToId);
		}

	}

	private boolean getConverter(String convertToId) {

		String fromId = null;
		String toId = null;

		// Get the Converter Extension Point
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
				.getExtensionPoint("org.eclipse.cdt.managedbuilder.core", //$NON-NLS-1$
						"projectConverter"); //$NON-NLS-1$
		if (extensionPoint != null) {
			// Get the extensions
			IExtension[] extensions = extensionPoint.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				// Get the configuration elements of each extension
				IConfigurationElement[] configElements = extensions[i]
						.getConfigurationElements();
				for (int j = 0; j < configElements.length; j++) {

					IConfigurationElement element = configElements[j];

					if (element.getName().equals("converter")) { //$NON-NLS-1$

						fromId = element.getAttribute("fromId"); //$NON-NLS-1$
						toId = element.getAttribute("toId"); //$NON-NLS-1$
						// Check whether the current converter can be used for
						// the selected project type

						if (fromId.equals(getId())
								&& toId.equals(convertToId)) {
							// If it matches
							String mbsVersion = element
									.getAttribute("mbsVersion"); //$NON-NLS-1$
							PluginVersionIdentifier currentMbsVersion = ManagedBuildManager
									.getBuildInfoVersion();

							// set the converter element based on the MbsVersion
							if (currentMbsVersion
									.isGreaterThan(new PluginVersionIdentifier(
											mbsVersion))) {
								previousMbsVersionConversionElement = element;
							} else {
								currentMbsVersionConversionElement = element;
							}
							return true;
						}
					}
				}
			}
		}

		// If control comes here, it means 'Tool Integrator' specified
		// 'convertToId' attribute in toolchain definition file, but
		// has not provided any converter. So, make the project is invalid
		
		return false;
	}


	public IConfigurationElement getPreviousMbsVersionConversionElement() {
		return previousMbsVersionConversionElement;
	}

	public IConfigurationElement getCurrentMbsVersionConversionElement() {
		return currentMbsVersionConversionElement;
	}
	
	public IBuildObjectProperties getBuildProperties() {
		if(buildProperties == null){
			BuildObjectProperties parentProps = findBuildProperties();
			if(parentProps != null)
				buildProperties = new BuildObjectProperties(parentProps, this, this);
			else
				buildProperties = new BuildObjectProperties(this, this);
		}
		return buildProperties;
	}
	
	BuildObjectProperties findBuildProperties(){
		if(buildProperties == null){
			if(superClass != null){
				return ((ProjectType)superClass).findBuildProperties();
			}
			return null;
		}
		return buildProperties;
	}

	public void propertiesChanged() {
		List list = getConfigurationList();
		for(int i = 0; i < list.size(); i++){
			((Configuration)list.get(i)).propertiesChanged();
		}
	}

	public boolean supportsType(IBuildPropertyType type) {
		return supportsType(type.getId());
	}

	public boolean supportsValue(IBuildPropertyType type,
			IBuildPropertyValue value) {
		return supportsValue(type.getId(), value.getId());
	}

	public boolean supportsType(String typeId) {
		List list = getConfigurationList();
		for(int i = 0; i < list.size(); i++){
			if(((Configuration)list.get(i)).supportsType(typeId))
				return true;
		}
		return false;
	}

	public boolean supportsValue(String typeId, String valueId) {
		List list = getConfigurationList();
		for(int i = 0; i < list.size(); i++){
			if(((Configuration)list.get(i)).supportsValue(typeId, valueId))
				return true;
		}
		return false;
	}

	public String[] getRequiredTypeIds() {
		List result = new ArrayList();
		List list = getConfigurationList();
		for(int i = 0; i < list.size(); i++){
			result.addAll(Arrays.asList(((Configuration)list.get(i)).getRequiredTypeIds()));
		}
		return (String[])result.toArray(new String[result.size()]);
	}

	public String[] getSupportedTypeIds() {
		List result = new ArrayList();
		List list = getConfigurationList();
		for(int i = 0; i < list.size(); i++){
			result.addAll(Arrays.asList(((Configuration)list.get(i)).getSupportedTypeIds()));
		}
		return (String[])result.toArray(new String[result.size()]);
	}

	public String[] getSupportedValueIds(String typeId) {
		List result = new ArrayList();
		List list = getConfigurationList();
		for(int i = 0; i < list.size(); i++){
			result.addAll(Arrays.asList(((Configuration)list.get(i)).getSupportedValueIds(typeId)));
		}
		return (String[])result.toArray(new String[result.size()]);
	}

	public boolean requiresType(String typeId) {
		List list = getConfigurationList();
		for(int i = 0; i < list.size(); i++){
			if(((Configuration)list.get(i)).requiresType(typeId))
				return true;
		}
		return false;
	}

	public IBuildPropertyValue getBuildArtefactType() {
		IBuildObjectProperties props = findBuildProperties();
		if(props != null){
			IBuildProperty prop = props.getProperty(ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_ID);
			if(prop != null)
				return prop.getValue();
		}
		return null;
	}

	public boolean isSystemObject() {
		return isTestProjectType() || getConvertToId().length() != 0;
	}
}
