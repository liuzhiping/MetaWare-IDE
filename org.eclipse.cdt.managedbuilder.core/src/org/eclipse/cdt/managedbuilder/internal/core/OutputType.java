/*******************************************************************************
 * Copyright (c) 2005, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IManagedOutputNameProvider;
import org.eclipse.cdt.managedbuilder.core.IOutputType;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.enablement.OptionEnablementExpression;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;

public class OutputType extends BuildObject implements IOutputType {

	private static final String DEFAULT_SEPARATOR = ","; //$NON-NLS-1$
	private static final String EMPTY_STRING = new String();

	//  Superclass
	private IOutputType superClass;
	private String superClassId;
	//  Parent and children
	private ITool parent;
	//  Managed Build model attributes
	private String outputContentTypeId;
	private IContentType outputContentType;
	private String outputs;
	private String optionId;
	private String buildVariable;
	private Boolean multipleOfType;
	private String primaryInputTypeId;
	private IInputType primaryInputType;
	private Boolean primaryOutput;
	private String outputPrefix;
	private String outputNames;
	private String namePattern;
	private IConfigurationElement nameProviderElement = null;
	private IManagedOutputNameProvider nameProvider = null;
	
	private BooleanExpressionApplicabilityCalculator booleanExpressionCalculator;

	//  Miscellaneous
	private boolean isExtensionOutputType = false;
	private boolean isDirty = false;
	private boolean resolved = true;
	private boolean rebuildState;

	/*
	 *  C O N S T R U C T O R S
	 */
	
	/**
	 * This constructor is called to create an OutputType defined by an extension point in 
	 * a plugin manifest file, or returned by a dynamic element provider
	 * 
	 * @param parent  The ITool parent of this OutputType
	 * @param element The OutputType definition from the manifest file or a dynamic element
	 *                provider
	 */
	public OutputType(ITool parent, IManagedConfigElement element) {
		this.parent = parent;
		isExtensionOutputType = true;
		
		// setup for resolving
		resolved = false;

		loadFromManifest(element);
		
		IManagedConfigElement enablements[] = element.getChildren(OptionEnablementExpression.NAME);
		if(enablements.length > 0)
			booleanExpressionCalculator = new BooleanExpressionApplicabilityCalculator(enablements);

		// Hook me up to the Managed Build Manager
		ManagedBuildManager.addExtensionOutputType(this);
	}

	/**
	 * This constructor is called to create an OutputType whose attributes will be 
	 * set by separate calls.
	 * 
	 * @param Tool The parent of the an OutputType
	 * @param OutputType The superClass, if any
	 * @param String The id for the new OutputType
	 * @param String The name for the new OutputType
	 * @param boolean Indicates whether this is an extension element or a managed project element
	 */
	public OutputType(Tool parent, IOutputType superClass, String Id, String name, boolean isExtensionElement) {
		this.parent = parent;
		this.superClass = superClass;
		if (this.superClass != null) {
			superClassId = this.superClass.getId();
		}
		setId(Id);
		setName(name);
		isExtensionOutputType = isExtensionElement;
		if (isExtensionElement) {
			// Hook me up to the Managed Build Manager
			ManagedBuildManager.addExtensionOutputType(this);
		} else {
			setDirty(true);
			setRebuildState(true);
		}
	}

	/**
	 * Create an <code>OutputType</code> based on the specification stored in the 
	 * project file (.cdtbuild).
	 * 
	 * @param parent The <code>ITool</code> the OutputType will be added to. 
	 * @param element The XML element that contains the OutputType settings.
	 */
	public OutputType(ITool parent, ICStorageElement element) {
		this.parent = parent;
		isExtensionOutputType = false;
		
		// Initialize from the XML attributes
		loadFromProject(element);
	}

	/**
	 * Create an <code>OutputType</code> based upon an existing OutputType.
	 * 
	 * @param parent The <code>ITool</code> the OutputType will be added to.
	 * @param Id The identifier of the new OutputType
	 * @param name The name of the new OutputType
	 * @param outputType The existing OutputType to clone.
	 */
	public OutputType(ITool parent, String Id, String name, OutputType outputType) {
		this.parent = parent;
		superClass = outputType.superClass;
		if (superClass != null) {
			if (outputType.superClassId != null) {
				superClassId = new String(outputType.superClassId);
			}
		}
		setId(Id);
		setName(name);
		isExtensionOutputType = false;
		boolean copyIds = Id.equals(outputType.id);
		
		//  Copy the remaining attributes
		if (outputType.outputContentTypeId != null) {
			outputContentTypeId = new String(outputType.outputContentTypeId);
		}
		outputContentType = outputType.outputContentType;
		if (outputType.outputs != null) {
			outputs = new String(outputType.outputs);
		}
		if (outputType.optionId != null) {
			optionId = new String(outputType.optionId);
		}
		if (outputType.buildVariable != null) {
			buildVariable = new String(outputType.buildVariable);
		}
		if (outputType.multipleOfType != null) {
			multipleOfType = new Boolean(outputType.multipleOfType.booleanValue());
		}
		if (outputType.primaryInputTypeId != null) {
			primaryInputTypeId = new String(outputType.primaryInputTypeId);
		}
		primaryInputType = outputType.primaryInputType;
		if (outputType.primaryOutput != null) {
			primaryOutput = new Boolean(outputType.primaryOutput.booleanValue());
		}
		if (outputType.outputPrefix != null) {
			outputPrefix = new String(outputType.outputPrefix);
		}
		if (outputType.outputNames != null) {
			outputNames = new String(outputType.outputNames);
		}
		if (outputType.namePattern != null) {
			namePattern = new String(outputType.namePattern);
		}

		nameProviderElement = outputType.nameProviderElement; 
		nameProvider = outputType.nameProvider; 
		
		if(copyIds) {
			isDirty = outputType.isDirty;
			rebuildState = outputType.rebuildState;
		} else {
			setDirty(true);
			setRebuildState(true);
		}
	}

	/*
	 *  E L E M E N T   A T T R I B U T E   R E A D E R S   A N D   W R I T E R S
	 */
	
	/* (non-Javadoc)
	 * Loads the OutputType information from the ManagedConfigElement specified in the 
	 * argument.
	 * 
	 * @param element Contains the OutputType information 
	 */
	protected void loadFromManifest(IManagedConfigElement element) {
		ManagedBuildManager.putConfigElement(this, element);
		
		// id
		setId(element.getAttribute(IBuildObject.ID));
		
		// Get the name
		setName(element.getAttribute(IBuildObject.NAME));
		
		// superClass
		superClassId = element.getAttribute(IProjectType.SUPERCLASS);
		
		// outputContentType
		outputContentTypeId = element.getAttribute(IOutputType.OUTPUT_CONTENT_TYPE); 
		
		// outputs
		outputs = element.getAttribute(IOutputType.OUTPUTS);
		
		// option
		optionId = element.getAttribute(IOutputType.OPTION); 
		
		// multipleOfType
        String isMOT = element.getAttribute(IOutputType.MULTIPLE_OF_TYPE);
        if (isMOT != null){
    		multipleOfType = new Boolean("true".equals(isMOT)); //$NON-NLS-1$
        }
		
		// primaryInputType
		primaryInputTypeId = element.getAttribute(IOutputType.PRIMARY_INPUT_TYPE);
		
		// primaryOutput
        String isPO = element.getAttribute(IOutputType.PRIMARY_OUTPUT);
        if (isPO != null){
    		primaryOutput = new Boolean("true".equals(isPO)); //$NON-NLS-1$
        }
		
		// outputPrefix
		outputPrefix = element.getAttribute(IOutputType.OUTPUT_PREFIX);
		
		// outputNames
		outputNames = element.getAttribute(IOutputType.OUTPUT_NAMES);
		
		// namePattern
		namePattern = element.getAttribute(IOutputType.NAME_PATTERN);
		
		// buildVariable
		buildVariable = element.getAttribute(IOutputType.BUILD_VARIABLE); 

		// Store the configuration element IFF there is a name provider defined 
		String nameProvider = element.getAttribute(IOutputType.NAME_PROVIDER); 
		if (nameProvider != null && element instanceof DefaultManagedConfigElement) {
			nameProviderElement = ((DefaultManagedConfigElement)element).getConfigurationElement();			
		}
	}
	
	/* (non-Javadoc)
	 * Initialize the OutputType information from the XML element 
	 * specified in the argument
	 * 
	 * @param element An XML element containing the OutputType information 
	 */
	protected void loadFromProject(ICStorageElement element) {
		
		// id
		setId(element.getAttribute(IBuildObject.ID));

		// name
		if (element.getAttribute(IBuildObject.NAME) != null) {
			setName(element.getAttribute(IBuildObject.NAME));
		}
		
		// superClass
		superClassId = element.getAttribute(IProjectType.SUPERCLASS);
		if (superClassId != null && superClassId.length() > 0) {
			superClass = ManagedBuildManager.getExtensionOutputType(superClassId);
			if (superClass == null) {
				// TODO:  Report error
			}
		}
		
		// outputContentType
		if (element.getAttribute(IOutputType.OUTPUT_CONTENT_TYPE) != null) {
			outputContentTypeId = element.getAttribute(IOutputType.OUTPUT_CONTENT_TYPE); 			
		}
		
		// outputs
		if (element.getAttribute(IOutputType.OUTPUTS) != null) {
			outputs = element.getAttribute(IOutputType.OUTPUTS);			
		}
		
		// option
		if (element.getAttribute(IOutputType.OPTION) != null) {
			optionId = element.getAttribute(IOutputType.OPTION);
		}
		
		// multipleOfType
		if (element.getAttribute(IOutputType.MULTIPLE_OF_TYPE) != null) {
			String isMOT = element.getAttribute(IOutputType.MULTIPLE_OF_TYPE);
			if (isMOT != null){
				multipleOfType = new Boolean("true".equals(isMOT)); //$NON-NLS-1$
			}
		}
		
		// primaryInputType
		if (element.getAttribute(IOutputType.PRIMARY_INPUT_TYPE) != null) {
			primaryInputTypeId = element.getAttribute(IOutputType.PRIMARY_INPUT_TYPE);
			primaryInputType = parent.getInputTypeById(primaryInputTypeId);
		}
		
		// primaryOutput
        if (element.getAttribute(IOutputType.PRIMARY_OUTPUT) != null) {
			String isPO = element.getAttribute(IOutputType.PRIMARY_OUTPUT);
			if (isPO != null){
				primaryOutput = new Boolean("true".equals(isPO)); //$NON-NLS-1$
			}
        }
		
		// outputPrefix
		if (element.getAttribute(IOutputType.OUTPUT_PREFIX) != null) {
			outputPrefix = element.getAttribute(IOutputType.OUTPUT_PREFIX);
		}
		
		// outputNames
		if (element.getAttribute(IOutputType.OUTPUT_NAMES) != null) {
			outputNames = element.getAttribute(IOutputType.OUTPUT_NAMES);
		}
		
		// namePattern
		if (element.getAttribute(IOutputType.NAME_PATTERN) != null) {
			namePattern = element.getAttribute(IOutputType.NAME_PATTERN);
		}
		
		// buildVariable
		if (element.getAttribute(IOutputType.BUILD_VARIABLE) != null) {
			buildVariable = element.getAttribute(IOutputType.BUILD_VARIABLE);
		}
		
		// Note: Name Provider cannot be specified in a project file because
		//       an IConfigurationElement is needed to load it!
		if (element.getAttribute(IOutputType.NAME_PROVIDER) != null) {
			// TODO:  Issue warning?
		}
	}

	/**
	 * Persist the OutputType to the project file.
	 * 
	 * @param doc
	 * @param element
	 */
	public void serialize(ICStorageElement element) {
		if (superClass != null)
			element.setAttribute(IProjectType.SUPERCLASS, superClass.getId());
		
		element.setAttribute(IBuildObject.ID, id);
		
		if (name != null) {
			element.setAttribute(IBuildObject.NAME, name);
		}

		if (outputContentTypeId != null) {
			element.setAttribute(IOutputType.OUTPUT_CONTENT_TYPE, outputContentTypeId);
		}

		if (outputs != null) {
			element.setAttribute(IOutputType.OUTPUTS, outputs);
		}
		
		if (optionId != null) {
			element.setAttribute(IOutputType.OPTION, optionId);
		}
		
		if (multipleOfType != null) {
			element.setAttribute(IOutputType.MULTIPLE_OF_TYPE, multipleOfType.toString());
		}
		
		if (primaryInputTypeId != null) {
			element.setAttribute(IOutputType.PRIMARY_INPUT_TYPE, primaryInputTypeId);
		}
		
		if (primaryOutput != null) {
			element.setAttribute(IOutputType.PRIMARY_OUTPUT, primaryOutput.toString());
		}
		
		if (outputPrefix != null) {
			element.setAttribute(IOutputType.OUTPUT_PREFIX, outputPrefix);
		}
		
		if (outputNames != null) {
			element.setAttribute(IOutputType.OUTPUT_NAMES, outputNames);
		}
		
		if (namePattern != null) {
			element.setAttribute(IOutputType.NAME_PATTERN, namePattern);
		}

		if (buildVariable != null) {
			element.setAttribute(IOutputType.BUILD_VARIABLE, buildVariable);
		}

		// Note: dependency generator cannot be specified in a project file because
		//       an IConfigurationElement is needed to load it!
		if (nameProviderElement != null) {
			//  TODO:  issue warning?
		}
		
		// I am clean now
		isDirty = false;
	}

	/*
	 *  P A R E N T   A N D   C H I L D   H A N D L I N G
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOutputType#getParent()
	 */
	public ITool getParent() {
		return parent;
	}

	/*
	 *  M O D E L   A T T R I B U T E   A C C E S S O R S
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOutputType#getSuperClass()
	 */
	public IOutputType getSuperClass() {
		return superClass;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOutputType#getName()
	 */
	public String getName() {
		return (name == null && superClass != null) ? superClass.getName() : name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOutputType#getBuildVariable()
	 */
	public String getBuildVariable() {
		if (buildVariable == null) {
			// If I have a superClass, ask it
			if (superClass != null) {
				return superClass.getBuildVariable();
			} else {
				//  Use default name
				String name = getName();
				if (name == null || name.length() == 0) {
					name = getId();
				}
				String defaultName = name.toUpperCase();
				defaultName = defaultName.replaceAll("\\W", "_");  //$NON-NLS-1$  //$NON-NLS-2$
				defaultName += "_OUTPUTS";	//$NON-NLS-1$ 
				return defaultName;
			}
		}
		return buildVariable;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOutputType#setBuildVariable()
	 */
	public void setBuildVariable(String variableName) {
		if (variableName == null && buildVariable == null) return;
		if (buildVariable == null || variableName == null || !(variableName.equals(buildVariable))) {
			buildVariable = variableName;
			setDirty(true);
			setRebuildState(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOutputType#getMultipleOfType()
	 */
	public boolean getMultipleOfType() {
		if (multipleOfType == null) {
			if (superClass != null) {
				return superClass.getMultipleOfType();
			} else {
				return false;	// default is false
			}
		}
		return multipleOfType.booleanValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOuputType#setMultipleOfType()
	 */
	public void setMultipleOfType(boolean b) {
		if (multipleOfType == null || !(b == multipleOfType.booleanValue())) {
			multipleOfType = new Boolean(b);
			setDirty(true);
			setRebuildState(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOuputType#getNamePattern()
	 */
	public String getNamePattern() {
		if (namePattern == null) {
			// If I have a superClass, ask it
			if (superClass != null) {
				return superClass.getNamePattern();
			} else {
				return EMPTY_STRING;
			}
		}
		return namePattern;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOuputType#setNamePattern()
	 */
	public void setNamePattern(String pattern) {
		if (pattern == null && namePattern == null) return;
		if (namePattern == null || pattern == null || !(pattern.equals(namePattern))) {
			namePattern = pattern;
			setDirty(true);
			setRebuildState(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOuputType#getNameProviderElement()
	 */
	public IConfigurationElement getNameProviderElement() {
		if (nameProviderElement == null) {
			if (superClass != null) {
				return ((OutputType)superClass).getNameProviderElement();
			}
		}
		return nameProviderElement;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOuputType#setNameProviderElement()
	 */
	public void setNameProviderElement(IConfigurationElement element) {
		nameProviderElement = element;
		setDirty(true);
		setRebuildState(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOuputType#setNameProviderElement()
	 */
	public IManagedOutputNameProvider getNameProvider() {
		if (nameProvider != null) {
			return nameProvider;
		}
		IConfigurationElement element = getNameProviderElement();
		if (element != null) {
			try {
				if (element.getAttribute(NAME_PROVIDER) != null) {
					nameProvider = (IManagedOutputNameProvider) element.createExecutableExtension(NAME_PROVIDER);
					return nameProvider;
				}
			} catch (CoreException e) {}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOuputType#getOptionId()
	 */
	public String getOptionId() {
		if (optionId == null) {
			if (superClass != null) {
				return superClass.getOptionId();
			} else {
				return null;
			}			
		}
		return optionId; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOuputType#setOptionId()
	 */
	public void setOptionId(String id) {
		if (id == null && optionId == null) return;
		if (id == null || optionId == null || !(optionId.equals(id))) {
			optionId = id;
			setDirty(true);
			setRebuildState(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOuputType#getOutputContentType()
	 */
	public IContentType getOutputContentType() {
		if (outputContentType == null) {
			if (superClass != null) {
				return superClass.getOutputContentType();
			} else {
				return null;
			}			
		}
		return outputContentType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOuputType#setOutputContentType()
	 */
	public void setOutputContentType(IContentType type) {
		if (outputContentType != type) {
			outputContentType = type;
			if (outputContentType != null) {
				outputContentTypeId = outputContentType.getId();				
			} else {
				outputContentTypeId = null;
			}
			setDirty(true);
			setRebuildState(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOuputType#getOutputExtensionsAttribute()
	 */
	public String[] getOutputExtensionsAttribute() {
		if (outputs == null) {
			if (superClass != null) {
				return superClass.getOutputExtensionsAttribute();
			} else {
				return null;
			}
		}
		return outputs.split(DEFAULT_SEPARATOR);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOuputType#setOutputExtensionsAttribute()
	 */
	public void setOutputExtensionsAttribute(String exts) {
		if (exts == null && outputs == null) return;
		if (outputs == null || exts == null || !(exts.equals(outputs))) {
			outputs = exts;
			setDirty(true);
			setRebuildState(true);
		}
	}

	public String[] getOutputExtensions(ITool tool, IProject project) {
		//  Use content type if specified and registered with Eclipse
		IContentType type = getOutputContentType();
		if (type != null) {
			return ((Tool)tool).getContentTypeFileSpecs(type, project);
		}
		return getOutputExtensionsAttribute();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOuputType#getOutputExtensions()
	 */
	public String[] getOutputExtensions(ITool tool) {
		return getOutputExtensions(tool, ((Tool)tool).getProject());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOutputType#isOutputExtension()
	 */
	public boolean isOutputExtension(ITool tool, String ext) {
		String[] exts = getOutputExtensions(tool);
		if (exts != null) {
			for (int i=0; i<exts.length; i++) {
				if (ext.equals(exts[i])) return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOuputType#getOutputPrefix()
	 */
	public String getOutputPrefix() {
		if (outputPrefix == null) {
			// If I have a superClass, ask it
			if (superClass != null) {
				return superClass.getOutputPrefix();
			} else {
				return EMPTY_STRING;
			}
		}
		return outputPrefix;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOuputType#setOutputPrefix()
	 */
	public void setOutputPrefix(String prefix) {
		if (prefix == null && outputPrefix == null) return;
		if (outputPrefix == null || prefix == null || !(prefix.equals(outputPrefix))) {
			outputPrefix = prefix;
			setDirty(true);
			setRebuildState(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOuputType#getOutputNames()
	 */
	public String[] getOutputNames() {
		if (outputNames == null) {
			// If I have a superClass, ask it
			if (superClass != null) {
				return superClass.getOutputNames();
			} else {
				return null;
			}
		}
		String[] nameTokens = outputNames.split(";"); //$NON-NLS-1$
		return nameTokens;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOuputType#setOutputNames()
	 */
	public void setOutputNames(String names) {
		if (names == null && outputNames == null) return;
		if (outputNames == null || names == null || !(names.equals(outputNames))) {
			outputNames = names;
			setDirty(true);
			setRebuildState(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOuputType#getPrimaryInputType()
	 */
	public IInputType getPrimaryInputType() {
		IInputType ret = primaryInputType;
		if (ret == null) {
			if (superClass != null) {
				ret = superClass.getPrimaryInputType();
			}
			if (ret == null) {
				ret = getParent().getPrimaryInputType();
			}			
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOuputType#setPrimaryInputType()
	 */
	public void setPrimaryInputType(IInputType type) {
		if (primaryInputType != type) {
			primaryInputType = type;
			if (primaryInputType != null) {
				primaryInputTypeId = primaryInputType.getId();				
			} else {
				primaryInputTypeId = null;
			}
			setDirty(true);
			setRebuildState(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOuputType#getPrimaryOutput()
	 */
	public boolean getPrimaryOutput() {
		if (primaryOutput == null) {
			if (superClass != null) {
				return superClass.getPrimaryOutput();
			} else {
				return false;	// default is false
			}
		}
		return primaryOutput.booleanValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOuputType#setPrimaryOutput()
	 */
	public void setPrimaryOutput(boolean b) {
		if (primaryOutput == null || !(b == primaryOutput.booleanValue())) {
			primaryOutput = new Boolean(b);
			setDirty(true);
			setRebuildState(true);
		}
	}

	/*
	 *  O B J E C T   S T A T E   M A I N T E N A N C E
	 */
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOutputType#isExtensionElement()
	 */
	public boolean isExtensionElement() {
		return isExtensionOutputType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOutputType#isDirty()
	 */
	public boolean isDirty() {
		// This shouldn't be called for an extension OutputType
 		if (isExtensionOutputType) return false;
		return isDirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOutputType#setDirty(boolean)
	 */
	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}
	
	/* (non-Javadoc)
	 *  Resolve the element IDs to interface references
	 */
	public void resolveReferences() {
		if (!resolved) {
			resolved = true;
			// Resolve superClass
			if (superClassId != null && superClassId.length() > 0) {
				superClass = ManagedBuildManager.getExtensionOutputType(superClassId);
				if (superClass == null) {
					// Report error
					ManagedBuildManager.outputResolveError(
							"superClass",	//$NON-NLS-1$
							superClassId,
							"outputType",	//$NON-NLS-1$
							getId());
				}
			}
			
			// Resolve content types
			IContentTypeManager manager = Platform.getContentTypeManager();
			if (outputContentTypeId != null && outputContentTypeId.length() > 0) {
				outputContentType = manager.getContentType(outputContentTypeId);
			}

			// Resolve primary input type
			if (primaryInputTypeId != null && primaryInputTypeId.length() > 0) {
				primaryInputType = parent.getInputTypeById(primaryInputTypeId);
			}
		}
	}
	
	/**
	 * @return Returns the managedBuildRevision.
	 */
	public String getManagedBuildRevision() {
		if ( managedBuildRevision == null) {
			if ( getParent() != null) {
				return getParent().getManagedBuildRevision();
			}
		}
		return managedBuildRevision;
	}

	/**
	 * @return Returns the version.
	 */
	public PluginVersionIdentifier getVersion() {
		if ( version == null) {
			if ( getParent() != null) {
				return getParent().getVersion();
			}
		}
		return version;
	}
	
	public void setVersion(PluginVersionIdentifier version) {
		// Do nothing
	}
	
	public boolean needsRebuild(){
		return rebuildState;
	}
	
	public void setRebuildState(boolean rebuild){
		if(isExtensionElement() && rebuild)
			return;

		rebuildState = rebuild;
	}
	
	public BooleanExpressionApplicabilityCalculator getBooleanExpressionCalculator(){
		if(booleanExpressionCalculator == null){
			if(superClass != null){
				return ((OutputType)superClass).getBooleanExpressionCalculator();
			}
		}
		return booleanExpressionCalculator;
	}
	
	public boolean isEnabled(ITool tool) {
		if(tool.isExtensionElement())
			return true;
		
		BooleanExpressionApplicabilityCalculator calc = getBooleanExpressionCalculator();
		if(calc == null)
			return true;
		
		return calc.isOutputTypeEnabled(tool, this);
	}

	public boolean hasCustomSettings(){
		//TODO:
		return false;
	}
}
