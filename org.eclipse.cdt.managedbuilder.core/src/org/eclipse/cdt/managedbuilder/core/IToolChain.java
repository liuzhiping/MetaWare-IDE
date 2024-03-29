/*******************************************************************************
 * Copyright (c) 2004, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;

import org.eclipse.cdt.core.settings.model.extension.CTargetPlatformData;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.macros.IConfigurationBuildMacroSupplier;


/**
 * This interface represents a tool-integrator-defined, ordered set of tools 
 * that transform the project's input into the project's outputs.  A 
 * tool-chain can be defined as part of a configuration, or as an 
 * independent specification that is referenced in a separate configuration
 * via the toolChain superclass attribute.
 * <p>
 * The toolChain contains one or more children of type tool.  These define
 * the tools used in the tool-chain.  The toolChain contains one child of 
 * type targetPlatform.  This defines the architecture/os combination where
 * the outputs of the project can be deployed.  The toolChain contains one 
 * child of type builder.  This defines the "build" or "make" utility that
 * is used to drive the transformation of the inputs into outputs.
 * 
 * @since 2.1
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IToolChain extends IBuildObject, IHoldsOptions {
	public static final String TOOL_CHAIN_ELEMENT_NAME = "toolChain";	//$NON-NLS-1$
	public static final String OS_LIST = "osList";						//$NON-NLS-1$
	public static final String ARCH_LIST = "archList";					//$NON-NLS-1$
	public static final String ERROR_PARSERS = "errorParsers";			//$NON-NLS-1$
	public static final String VERSIONS_SUPPORTED = "versionsSupported";	//$NON-NLS-1$
	public static final String CONVERT_TO_ID = "convertToId";			//$NON-NLS-1$
	public static final String TARGET_TOOL = "targetTool";				//$NON-NLS-1$
	public static final String SECONDARY_OUTPUTS = "secondaryOutputs";	//$NON-NLS-1$
	public static final String IS_TOOL_CHAIN_SUPPORTED = "isToolChainSupported";			//$NON-NLS-1$
	public static final String CONFIGURATION_ENVIRONMENT_SUPPLIER = "configurationEnvironmentSupplier";			//$NON-NLS-1$
	public static final String CONFIGURATION_MACRO_SUPPLIER = "configurationMacroSupplier";			//$NON-NLS-1$
	public static final String SUPPORTS_MANAGED_BUILD = "supportsManagedBuild"; //$NON-NLS-1$
	public static final String IS_SYSTEM= "isSystem";							//$NON-NLS-1$
	public static final String NON_INTERNAL_BUILDER_ID = "nonInternalBuilderId";	//$NON-NLS-1$
	public static final String RESOURCE_TYPE_BASED_DISCOVERY = "resourceTypeBasedDiscovery";	//$NON-NLS-1$
	
	// The attribute name for the scanner info collector
	public static final String SCANNER_CONFIG_PROFILE_ID = "scannerConfigDiscoveryProfileId"; //$NON-NLS-1$

	/**
	 * Returns the configuration that is the parent of this tool-chain.
	 * 
	 * @return IConfiguration
	 */
	public IConfiguration getParent();

	/**
	 * Creates the <code>TargetPlatform</code> child of this tool-chain.
	 *
	 * @param ITargetPlatform The superClass, if any
	 * @param String The id for the new tool chain
	 * @param String The name for the new tool chain
	 * @param boolean Indicates whether this is an extension element or a managed project element
	 * 
	 * @return ITargetPlatform
	 */
	public ITargetPlatform createTargetPlatform(ITargetPlatform superClass, String Id, String name, boolean isExtensionElement);

	/**
	 * Returns the target-platform child of this tool-chain
	 * 
	 * @return ITargetPlatform
	 */
	public ITargetPlatform getTargetPlatform();

	/**
	 * If the tool chain is not an extension element, and it has its own TargetPlatform child,
	 * remove the TargetPlatform so that the tool chain uses its superclass' TargetPlatform
	 */
	public void removeLocalTargetPlatform();

	/**
	 * Returns the 'versionsSupported' of this tool-chain
	 * 
	 * @return String
	 */

	public String getVersionsSupported();
	/**
	 * Returns the 'convertToId' of this tool-chain
	 * 
	 * @return String
	 */

	public String getConvertToId();
	
	/**
	 * Sets the 'versionsSupported' attribute of the tool-chain. 
	 * 
	 * @param versionsSupported
	 */
	
	public void setVersionsSupported(String versionsSupported);
	
	/**
	 * Sets the 'convertToId' attribute of the tool-chain. 
	 * 
	 * @param convertToId
	 */
	public void setConvertToId(String convertToId);
	
	/**
	 * Creates the <code>Builder</code> child of this tool-chain.
	 *
	 * @param IBuilder The superClass, if any
	 * @param String The id for the new tool chain
	 * @param String The name for the new tool chain
	 * @param boolean Indicates whether this is an extension element or a managed project element
	 * 
	 * @return IBuilder
	 */
	public IBuilder createBuilder(IBuilder superClass, String Id, String name, boolean isExtensionElement);

	/**
	 * If the tool chain is not an extension element, and it has its own Builder child,
	 * remove the builder so that the tool chain uses its superclass' Builder
	 */
	public void removeLocalBuilder();

	/**
	 * Returns the builder child of this tool-chain.
	 * 
	 * @return IBuilder
	 */
	public IBuilder getBuilder();

	/**
	 * Creates a <code>Tool</code> child of this tool-chain.
	 *
	 * @param ITool The superClass, if any
	 * @param String The id for the new tool chain
	 * @param String The name for the new tool chain
	 * @param boolean Indicates whether this is an extension element or a managed project element
	 * 
	 * @return ITool
	 */
	public ITool createTool(ITool superClass, String Id, String name, boolean isExtensionElement);

	/**
	 * Returns an array of tool children of this tool-chain
	 * 
	 * @return ITool[]
	 */
	public ITool[] getTools();

	/**
	 * Returns the tool in this tool-chain with the ID specified in the argument, 
	 * or <code>null</code> 
	 * 
	 * @param id The ID of the requested tool
	 * @return ITool
	 */
	public ITool getTool(String id);

	/**
	 * Returns the <code>ITool</code> in the tool-chain with the specified 
	 * ID, or the tool(s) with a superclass with this id. 
	 * 
	 * <p>If the tool-chain does not have a tool with that ID, the method 
	 * returns an empty array. It is the responsibility of the caller to 
	 * verify the return value.  
	 * 
	 * @param id unique identifier of the tool to search for
	 * @return <code>ITool[]</code>
	 * @since 3.0.2
	 */
	public ITool[] getToolsBySuperClassId(String id);

	/**
	 * Returns the <code>IToolChain</code> that is the superclass of this
	 * tool-chain, or <code>null</code> if the attribute was not specified.
	 * 
	 * @return IToolChain
	 */
	public IToolChain getSuperClass();
	
	/**
	 * Returns whether this element is abstract.  Returns <code>false</code>
	 * if the attribute was not specified.
	 * @return boolean 
	 */
	public boolean isAbstract();

	/**
	 * Sets the isAbstract attribute of the tool-chain. 
	 * 
	 * @param b
	 */
	public void setIsAbstract(boolean b);
	
	/**
	 * Returns a semi-colon delimited list of child Ids of the superclass'
	 * children that should not be automatically inherited by this element.
	 * Returns an empty string if the attribute was not specified. 
	 * @return String 
	 */
	public String getUnusedChildren();
	
	/**
	 * Returns an array of operating systems the tool-chain outputs can run on.
	 * 
	 * @return String[]
	 */
	public String[] getOSList();

	/**
	 * Sets the OS list.
	 * 
	 * @param String[] The list of OS names
	 */
	public void setOSList(String[] OSs);
		 
	/**
	 * Returns an array of architectures the tool-chain outputs can run on.
	 * 
	 * @return String[]
	 */
	public String[] getArchList();
	
	/**
	 * Sets the architecture list.
	 * 
	 * @param String[] The list of architecture names
	 */
	public void setArchList(String[] archs);

	/**
	 * Returns the semicolon separated list of unique IDs of the error parsers associated
	 * with the tool-chain.
	 * 
	 * @return String
	 */
	public String getErrorParserIds();

	/**
	 * Returns the semicolon separated list of unique IDs of the error parsers associated
	 * with the tool-chain, filtered for the specified configuration.
	 * 
	 * @param config
	 * @return String
	 */
	public String getErrorParserIds(IConfiguration config);

	/**
	 * Returns the ordered list of unique IDs of the error parsers associated with the 
	 * tool-chain.
	 * 
	 * @return String[]
	 */
	public String[] getErrorParserList();

	/**
	 * Sets the semicolon separated list of error parser ids.
	 * 
	 * @param ids
	 */
	public void setErrorParserIds(String ids);

	/**
	 * Returns the scanner config discovery profile id or <code>null</code> if none. 
	 *  
	 * @return String
	 */
	public String getScannerConfigDiscoveryProfileId();

	/**
	 * Sets the scanner config discovery profile id.
	 * 
	 * @param profileId
	 */
	public void setScannerConfigDiscoveryProfileId(String profileId);

	/**
	 * Returns the sem-colon separated list of Tool ids containing each 
	 * tool that can create the final build artifact (the end target of 
	 * the build).  MBS will use the first ID in the list that matches 
	 * a Tool in the ToolChain.  One reason for specifying a list, is 
	 * that different versions of a tool can be selected based upon the 
	 * project nature (e.g. different tool definitions for a linker for C vs. C++).
	 * 
	 * @return String
	 */
	public String getTargetToolIds();

	/**
	 * Sets the sem-colon separated list of Tool ids containing each 
	 * tool that can create the final build artifact (the end target of 
	 * the build).  
	 * 
	 * @param targetToolIds
	 */
	public void setTargetToolIds(String targetToolIds);

	/**
	 * Returns the list of Tool ids containing each 
	 * tool that can create the final build artifact (the end target of 
	 * the build).  MBS will use the first ID in the list that matches 
	 * a Tool in the ToolChain.  One reason for specifying a list, is 
	 * that different versions of a tool can be selected based upon the 
	 * project nature (e.g. different tool definitions for a linker for C vs. C++).
	 * 
	 * @return String[]
	 */
	public String[] getTargetToolList();
	
	/**
	 * Returns the OutputTypes in this tool-chain, besides the primary 
	 * output of the targetTool, that are also considered to be build 
	 * artifacts.  
	 * 
	 * @return IOutputType[]
	 */
	public IOutputType[] getSecondaryOutputs();
	
	/**
	 * Sets the semicolon separated list of OutputType identifiers in 
	 * this tool-chain, besides the primary output of the targetTool,
	 * that are also considered to be build artifacts.  
	 * 
	 * @param ids
	 */
	public void setSecondaryOutputs(String ids);

	/**
	 * Returns <code>true</code> if this tool-chain has changes that need to 
	 * be saved in the project file, else <code>false</code>.
	 * 
	 * @return boolean 
	 */
	public boolean isDirty();
	
	/**
	 * Sets the element's "dirty" (have I been modified?) flag.
	 * 
	 * @param isDirty
	 */
	public void setDirty(boolean isDirty);
	
	/**
	 * Returns <code>true</code> if this tool-chain was loaded from a manifest file,
	 * and <code>false</code> if it was loaded from a project (.cdtbuild) file.
	 * 
	 * @return boolean 
	 */
	public boolean isExtensionElement();
	
	/**
	 * Returns <code>true</code> if the tool-chain support is installed on the system
	 * otherwise returns <code>false</code>
	 * 
	 * @return boolean 
	 */	
	public boolean isSupported();
	
	/**
	 * Returns the tool-integrator provided implementation of the configuration environment variable supplier
	 * or <code>null</code> if none. 
	 *  
	 * @return IConfigurationEnvironmentVariableSupplier
	 */
	public IConfigurationEnvironmentVariableSupplier getEnvironmentVariableSupplier();
		
	/**
	 * Returns the tool-integrator provided implementation of the configuration build macro supplier
	 * or <code>null</code> if none. 
	 *  
	 * @return IConfigurationBuildMacroSupplier
	 */
	public IConfigurationBuildMacroSupplier getBuildMacroSupplier();

	/**
	 * Returns an IOptionPathConverter implementation for this toolchain
	 * or null, if no conversion is required
	 */
	public IOptionPathConverter getOptionPathConverter() ;
	
	IFolderInfo getParentFolderInfo();
	
	CTargetPlatformData getTargetPlatformData();
	
	boolean supportsBuild(boolean managed);
	
	boolean isSystemObject();
	
	boolean matches(IToolChain tc);
	
	String getUniqueRealName();
}
