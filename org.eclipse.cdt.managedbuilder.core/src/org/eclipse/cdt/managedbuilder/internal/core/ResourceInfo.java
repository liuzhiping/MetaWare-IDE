/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * Synopsys
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFileInfo;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.core.OptionStringValue;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

public abstract class ResourceInfo extends BuildObject implements IResourceInfo {
	private Configuration config;
	private IPath path;
	boolean isDirty;
	boolean needsRebuild;
	private ResourceInfoContainer rcInfo;
	private CResourceData resourceData;

	ResourceInfo(IConfiguration cfg, IManagedConfigElement element, boolean hasBody){
		config = (Configuration)cfg;
		if(hasBody)
			loadFromManifest(element);
	}

	ResourceInfo(IConfiguration cfg, ResourceInfo base, String id) {
		config = (Configuration)cfg;
		path = normalizePath(base.path);

		setId(id);
		setName(base.getName());

		if(id.equals(base.getId())){
			isDirty = base.isDirty;
			needsRebuild = base.needsRebuild;
		} else {
			needsRebuild = true;
			isDirty = true;
		}
	}
	
	public boolean isRoot(){
		return path.segmentCount() == 0;
	}

	ResourceInfo(IConfiguration cfg, IPath path, String id, String name) {
		config = (Configuration)cfg;
		path = normalizePath(path);
		this.path = path;

		setId(id);
		setName(name);
	}

	ResourceInfo(IFileInfo base, IPath path, String id, String name) {
		config = (Configuration)base.getParent();

		setId(id);
		setName(name);
		path = normalizePath(path);
		
		this.path = path;
		needsRebuild = true;
		isDirty = true;
	}

	ResourceInfo(FolderInfo base, IPath path, String id, String name) {
		config = (Configuration)base.getParent();

		setId(id);
		setName(name);
		path = normalizePath(path);
		
		this.path = path;
		needsRebuild = true;
		isDirty = true;
	}

	ResourceInfo(IConfiguration cfg, ICStorageElement element, boolean hasBody){
		config = (Configuration)cfg;
		if(hasBody)
			loadFromProject(element);
	}
	
	private void loadFromManifest(IManagedConfigElement element) {
	
		// id
		setId(element.getAttribute(ID));
		
		// Get the name
		setName(element.getAttribute(NAME));
		
		// resourcePath
		String tmp = element.getAttribute(RESOURCE_PATH);
		if(tmp != null){
			path = new Path(tmp);
			if(IResourceConfiguration.RESOURCE_CONFIGURATION_ELEMENT_NAME.equals(element.getName())){
				path = path.removeFirstSegments(1);
			}
			path = normalizePath(path);
		} else {
			Status status = new Status(IStatus.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID, "ResourceInfo.loadFromManifest() : resourcePath=NULL", null); //$NON-NLS-1$
			ManagedBuilderCorePlugin.log(status);
		}

		// exclude
        String excludeStr = element.getAttribute(EXCLUDE);
        if (excludeStr != null){
    		config.setExcluded(getPath(), isFolderInfo(), ("true".equals(excludeStr))); //$NON-NLS-1$
        }
	}

	private void loadFromProject(ICStorageElement element) {
		
		// id
		setId(element.getAttribute(ID));

		// name
		if (element.getAttribute(NAME) != null) {
			setName(element.getAttribute(NAME));
		}
		
		// resourcePath
		if (element.getAttribute(RESOURCE_PATH) != null) {
			String tmp = element.getAttribute(RESOURCE_PATH);
			if(tmp != null){
				path = new Path(tmp);
				if(IResourceConfiguration.RESOURCE_CONFIGURATION_ELEMENT_NAME.equals(element.getName())){
					path = path.removeFirstSegments(1);
				}
				path = normalizePath(path);
			} else {
				Status status = new Status(IStatus.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID, "ResourceInfo.loadFromProject() : resourcePath=NULL", null); //$NON-NLS-1$
				ManagedBuilderCorePlugin.log(status);
			}
		}

		// exclude
		if (element.getAttribute(EXCLUDE) != null) {
			String excludeStr = element.getAttribute(EXCLUDE);
			if (excludeStr != null){
	    		config.setExcluded(getPath(), isFolderInfo(), ("true".equals(excludeStr))); //$NON-NLS-1$
			}
		}
	}


	public IConfiguration getParent() {
		return config;
	}

	public IPath getPath() {
		return normalizePath(path);
	}

	public boolean isDirty() {
		return isDirty;
	}

	public boolean isExcluded() {
		return config.isExcluded(getPath());
	}

	public boolean needsRebuild() {
		return needsRebuild;
	}

	public void setDirty(boolean dirty) {
		isDirty = dirty;
	}

	public void setExclude(boolean excluded) {
		if(isExcluded() == excluded)
			return;
		
		config.setExcluded(getPath(), isFolderInfo(), excluded);
		
		setDirty(true);
		setRebuildState(true);
	}
	
	public boolean canExclude(boolean exclude) {
		return config.canExclude(getPath(), isFolderInfo(), exclude);
	}

	public abstract boolean isFolderInfo();
	
	public void setPath(IPath p) {
		p = normalizePath(p);
		if(path == null)
			path = p;
		else if (!p.equals(normalizePath(this.path))) {
			ResourceInfoContainer info = getRcInfo();
			info.changeCurrentPath(p, true);
			this.path = p;
			setDirty(true);
			setRebuildState(true);
		}

	}
	
	private ResourceInfoContainer getRcInfo(){
		if(rcInfo == null)
			rcInfo = (config).getRcInfoContainer(this);
		return rcInfo;
	}

	public void setRebuildState(boolean rebuild) {
		needsRebuild = rebuild;
	}
	
	void serialize(ICStorageElement element){
		element.setAttribute(IBuildObject.ID, id);
		
		if (name != null) {
			element.setAttribute(IBuildObject.NAME, name);
		}
		
		if (path != null) {
			element.setAttribute(IResourceInfo.RESOURCE_PATH, path.toString());
		}
	}

	void resolveReferences() {}
	
	public CResourceData getResourceData(){
		return resourceData;
	}
	
	protected void setResourceData(CResourceData data){
		resourceData = data;
	}
	
	void removed(){
		config = null;
	}
	
	public boolean isValid(){
		return config != null;
	}

	private void propagate(IHoldsOptions parent, IOption option, Object oldValue, Object value) {
		if (! (parent instanceof ITool))
			return;
		ITool tool = (ITool)parent;
		String sup = option.getId();
		IOption op = option;
		while (op.getSuperClass() != null) {
			op = op.getSuperClass();
			sup = op.getId();
		}
		for (IResourceInfo ri : getChildResourceInfos()) {
			for (ITool t : ri.getTools()) {
				if (t.getDefaultInputExtension() != tool.getDefaultInputExtension())
					continue;
				op = t.getOptionBySuperClassId(sup);
				if (op == null) 
					continue;
				try {
					if (value instanceof Boolean) {
						boolean b = ((Boolean)oldValue).booleanValue();
						if (b == op.getBooleanValue() && b != ((Boolean)value).booleanValue())
							ri.setOption(t, op, ((Boolean)value).booleanValue());
					} else if (value instanceof String) {
						String s = (String)oldValue;
						if (s.equals(op.getStringValue()) && ! s.equals(value))
							ri.setOption(t, op, (String)value);
					} else if (value instanceof String[]) {
						String[] s = (String[])oldValue;
						if (Arrays.equals(s, op.getStringListValue()) && 
								! Arrays.equals(s, (String[])value))
							ri.setOption(t, op, (String[])value);
					} else if (value instanceof OptionStringValue[]) {
						OptionStringValue[] s = (OptionStringValue[])oldValue; 
						if (Arrays.equals(s, op.getBasicStringListValueElements()) && 
								! Arrays.equals(s, (OptionStringValue[])value))
							ri.setOption(t, op, (OptionStringValue[])value);
					}
					break;
				} catch (BuildException e) {}
			}
		}
		
	}
	
	
	public IOption setOption(IHoldsOptions parent, IOption option, boolean value) throws BuildException {
		// Is there a change?
		IOption retOpt = option;
		boolean oldVal = option.getBooleanValue();
		if (oldVal != value) {
			retOpt = parent.getOptionToSet(option, false);
			retOpt.setValue(value);
			propagate(parent, option,
					(oldVal ? Boolean.TRUE : Boolean.FALSE), 
					(value  ? Boolean.TRUE : Boolean.FALSE));
			//CUSTOMIZATION: option-->retOpt
			NotificationManager.getInstance().optionChanged(this, parent, retOpt, new Boolean(oldVal));
		}
		return retOpt;
	}

	public IOption setOption(IHoldsOptions parent, IOption option, String value) throws BuildException {
		IOption retOpt = option;
		String oldValue;
		oldValue = option.getStringValue(); 
		if (oldValue != null && !oldValue.equals(value)) {
			retOpt = parent.getOptionToSet(option, false);
			retOpt.setValue(value);
			propagate(parent, option, oldValue, value);
			//CUSTOMIZATION: option-->retOpt
			NotificationManager.getInstance().optionChanged(this, parent, retOpt, oldValue);
		}
		return retOpt;
	}

	public IOption setOption(IHoldsOptions parent, IOption option, String[] value) throws BuildException {
		IOption retOpt = option;
		// Is there a change?
		String[] oldValue;
		switch (option.getBasicValueType()) {
			case IOption.STRING_LIST :
				oldValue = option.getBasicStringListValue();
				break;
			default :
				oldValue = new String[0];
				break;
		}
		if(!Arrays.equals(value, oldValue)) {
			retOpt = parent.getOptionToSet(option, false);
			retOpt.setValue(value);
			propagate(parent, option, oldValue, value);
			//CUSTOMIZATION: option-->retOpt
			NotificationManager.getInstance().optionChanged(this, parent, retOpt, oldValue);
		} 
		return retOpt;
	}
	
	public IOption setOption(IHoldsOptions parent, IOption option, OptionStringValue[] value) throws BuildException {
		IOption retOpt = option;
		// Is there a change?
		OptionStringValue[] oldValue;
		switch (option.getBasicValueType()) {
			case IOption.STRING_LIST :
				oldValue = ((Option)option).getBasicStringListValueElements();
				break;
			default :
				oldValue = new OptionStringValue[0];
				break;
		}
		if(!Arrays.equals(value, oldValue)) {
			retOpt = parent.getOptionToSet(option, false);
			((Option)retOpt).setValue(value);
			propagate(parent, option, oldValue, value);
			//CUSTOMIZATION: option-->retOpt
			NotificationManager.getInstance().optionChanged(this, parent, retOpt, oldValue);
		} 
		return retOpt;
	}

	
	public void propertiesChanged(){
		if(isExtensionElement())
			return;
		
		ITool tools[] = getTools();
		for (ITool tool : tools) {
			((Tool)tool).propertiesChanged();
		}
	}
	
	public abstract boolean isExtensionElement();
	
	public abstract Set<String> contributeErrorParsers(Set<String> set);
	
	protected Set<String> contributeErrorParsers(ITool[] tools, Set<String> set){
		for (ITool tool : tools) {
			set = ((Tool)tool).contributeErrorParsers(set);
		}
		return set;
	}
	
	public abstract void resetErrorParsers();
	
	protected void resetErrorParsers(ITool tools[]){
		for (ITool tool : tools) {
			((Tool)tool).resetErrorParsers();
		}
	}
	
	abstract void removeErrorParsers(Set<String> set);
	
	protected void removeErrorParsers(ITool tools[], Set<String> set){
		for (ITool tool : tools) {
			((Tool)tool).removeErrorParsers(set);
		}
	}
	
	public ITool getToolById(String id) {
		ITool[] tools = getTools();
		for (ITool tool : tools) {
			if(id.equals(tool.getId()))
				return tool;
		}
		return null;
	}
	
	public static IPath normalizePath(IPath path){
		return path.makeRelative();
	}

	public ResourceInfo getParentResourceInfo(){
		if(isRoot())
			return null;
		
		IPath path = getPath();
		path = path.removeLastSegments(1);
		return (ResourceInfo)getParent().getResourceInfo(path, false);
	}

	public IFolderInfo getParentFolderInfo(){
		ResourceInfo parentRc = getParentResourceInfo();
		for(; parentRc != null && !parentRc.isFolderInfo(); parentRc = parentRc.getParentResourceInfo()) {
			// empty body, loop is to find parent only
		}

		return (IFolderInfo)parentRc;
	}

	abstract void resolveProjectReferences(boolean onLoad);

	abstract public boolean hasCustomSettings();
	
	public ToolListModificationInfo getToolListModificationInfo(ITool[] tools) {
		ITool[] curTools = getTools();
		return ToolChainModificationHelper.getModificationInfo(this, curTools, tools);
	}

	static ITool[][] getRealPairs(ITool[] tools){
		ITool[][] pairs = new ITool[tools.length][];
		for(int i = 0; i < tools.length; i++){
			ITool[] pair = new ITool[2];
			pair[0] = ManagedBuildManager.getRealTool(tools[i]);
			if(pair[0] == null)
				pair[0] = tools[i];
			pair[1] = tools[i];
			pairs[i] = pair;
		}
		return pairs;
	}
	
	abstract void applyToolsInternal(ITool[] resultingTools, ToolListModificationInfo info);

	void doApply(ToolListModificationInfo info){
		ITool[] resulting = info.getResultingTools();
		
		ITool[] removed = info.getRemovedTools();
		
		BuildSettingsUtil.disconnectDepentents(getParent(), removed);
		
		applyToolsInternal(resulting, info);

		performPostModificationAdjustments(info);
	}
	
	void performPostModificationAdjustments(ToolListModificationInfo info){
		propertiesChanged();
	}
	
	public IResourceInfo[] getDirectChildResourceInfos(){
		ResourceInfoContainer cr = getRcInfo();
		return cr.getDirectChildResourceInfos();
	}
	
	public IResourceInfo[] getChildResourceInfos(){
		ResourceInfoContainer cr = getRcInfo();
		return cr.getResourceInfos();
	}
	
	public List<IResourceInfo> getChildResourceInfoList(boolean includeCurrent){
		return getRcInfo().getRcInfoList(ICSettingBase.SETTING_FILE | ICSettingBase.SETTING_FOLDER, includeCurrent);
	}
    class OurChangeListener implements ISettingsChangeListener{
        private List<IObserver> observers = new ArrayList<IObserver>();
        public void addObserver(IObserver o) {
            observers.add(o);
        }
        public void removeObserver(IObserver o) {
            observers.remove(o);
        }
        public boolean isEmpty() {
            return observers.isEmpty();
        }
        public void settingsChanged(SettingsChangeEvent event){
            IOption option = event.getOption();
            if (event.getChangeType() == SettingsChangeEvent.REMOVED){
                //Get original one, now that this one has been deleted.
                if (option.getSuperClass() != null){
                    option = option.getSuperClass();
                }
            }
            for( IObserver o: observers){              
                o.optionChanged(ResourceInfo.this, option);
            }         
        }      
    }
    
   
    private OurChangeListener changeListener = null;

    // CUSTOMIZATION
    public void addObserver (IObserver observer) {
        if (changeListener == null) {
            changeListener = new OurChangeListener();
            NotificationManager.getInstance().subscribe(changeListener);
        }
        changeListener.addObserver(observer);
        
    }

    // CUSTOMIZATION
    public void removeObserver (IObserver observer) {
        if (changeListener != null){
            changeListener.removeObserver(observer);
            if (changeListener.isEmpty()){
                NotificationManager.getInstance().unsubscribe(changeListener);
                changeListener = null;
            }
        }      
    }
	

	
}
