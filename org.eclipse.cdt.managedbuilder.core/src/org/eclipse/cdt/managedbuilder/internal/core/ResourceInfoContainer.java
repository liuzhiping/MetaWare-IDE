/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.util.IPathSettingsContainerVisitor;
import org.eclipse.cdt.core.settings.model.util.PathSettingsContainer;
import org.eclipse.cdt.managedbuilder.core.IFileInfo;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.core.runtime.IPath;

public class ResourceInfoContainer {
	private PathSettingsContainer fRcDataContainer;
	private boolean fIncludeCurrent;
	private IResourceInfo fResourceInfosCache[] = null; //CUSTOMIZATION
	private Map<String,IResourceInfo> fResourceInfosMap = null; //CUSTOMIZATION
	
	public ResourceInfoContainer(PathSettingsContainer pathSettings, boolean includeCurrent){
		fRcDataContainer = pathSettings;
		fIncludeCurrent = includeCurrent;
	}
	
	public void changeCurrentPath(IPath path, boolean moveChildren){
		fRcDataContainer.setPath(path, moveChildren);
	}
	
	public IPath getCurrentPath(){
		return fRcDataContainer.getPath();
	}
	
	public IResourceInfo getCurrentResourceInfo(){
		return (IResourceInfo)fRcDataContainer.getValue();
	}
	
	public IResourceInfo getResourceInfo(IPath path, boolean exactPath) {
		PathSettingsContainer cr = fRcDataContainer.getChildContainer(path, false, exactPath);
		if(cr != null)
			return (IResourceInfo)cr.getValue();
		return null;
	}

	public IResourceInfo[] getResourceInfos(Class clazz) {
		return getResourceInfos(ICSettingBase.SETTING_FILE | ICSettingBase.SETTING_FOLDER, clazz);
	}

	public IResourceInfo[] getResourceInfos() {
	    if (fResourceInfosCache == null) {  //CUSTOMIZATION
		     fResourceInfosCache = getResourceInfos(ICSettingBase.SETTING_FILE | ICSettingBase.SETTING_FOLDER);
	    }
	    return fResourceInfosCache;
	}
	
	public IResourceInfo getResourceInfo(String id){ //CUSTOMIZATION
	    if (fResourceInfosMap == null){
	        fResourceInfosMap = new HashMap<String,IResourceInfo>();
	        for (IResourceInfo info: getResourceInfos()){
	            fResourceInfosMap.put(info.getId(),info);
	        }
	    }
	    return fResourceInfosMap.get(id);
	}

	public IResourceInfo[] getResourceInfos(final int kind) {
		return getResourceInfos(kind, IResourceInfo.class);
	}
	
	public IResourceInfo[] getResourceInfos(int kind, Class clazz){
		List<IResourceInfo> list = getRcInfoList(kind);

		IResourceInfo datas[] = (IResourceInfo[])Array.newInstance(clazz, list.size());
		
		return list.toArray(datas);
	}

	public IResourceInfo[] getDirectChildResourceInfos(){
		PathSettingsContainer[] children = fRcDataContainer.getDirectChildren();
		
		IResourceInfo datas[] = new IResourceInfo[children.length];
		
		for(int i = 0; i < datas.length; i++){
			datas[i] = (IResourceInfo)children[i].getValue();
		}
		
		return datas;
	}

	public List<IResourceInfo> getRcInfoList(final int kind){
		return getRcInfoList(kind, fIncludeCurrent);
	}		
	
	public List<IResourceInfo> getRcInfoList(final int kind, final boolean includeCurrent){
		final List<IResourceInfo> list = new ArrayList<IResourceInfo>(); 
		fRcDataContainer.accept(new IPathSettingsContainerVisitor(){

			public boolean visit(PathSettingsContainer container) {
				if(includeCurrent || container != fRcDataContainer){
					IResourceInfo data = (IResourceInfo)container.getValue();
					if((data.getKind() & kind) == data.getKind())
						list.add(data);
				}
				return true;
			}
		});
		
		return list;
	}

	public IResourceInfo getResourceInfo(IPath path, boolean exactPath, int kind){
		IResourceInfo data = getResourceInfo(path, exactPath);
		if(data != null && (data.getKind() & kind) == data.getKind())
			return data;
		return null;
	}

	public void removeResourceInfo(IPath path) {
		fRcDataContainer.removeChildContainer(path);
	    fResourceInfosCache = null; //CUSTOMIZATION
	    fResourceInfosMap = null;
	}
	
	public void addResourceInfo(IResourceInfo data){
		PathSettingsContainer cr = fRcDataContainer.getChildContainer(data.getPath(), true, true);
		cr.setValue(data);
		fResourceInfosCache = null; //CUSTOMIZATION
		if (fResourceInfosMap != null){
		    fResourceInfosMap.put(data.getId(),data);
		}
	}
	
	public IFileInfo getFileInfo(IPath path){
		return (IFileInfo)getResourceInfo(path, true, ICSettingBase.SETTING_FILE);
	}
	
	public IFolderInfo getFolderInfo(IPath path){
		return (IFolderInfo)getResourceInfo(path, true, ICSettingBase.SETTING_FOLDER);
	}
}
