/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * Synopsys
 *******************************************************************************/
package org.eclipse.cdt.build.core.scannerconfig;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.core.resources.IProject;

public final class CfgInfoContext{
	private static final String DELIMITER = ";"; //$NON-NLS-1$
	private static final int NULL_OBJ_CODE = 29;
	private Configuration fCfg;
	private IResourceInfo fRcInfo;
	private ITool fTool;
	private IInputType fInType;
	private InfoContext fContext;

	public CfgInfoContext(IResourceInfo rcInfo, ITool tool, IInputType inType){
		this(rcInfo, tool, inType, null);
	}

	private CfgInfoContext(IResourceInfo rcInfo, ITool tool, IInputType inType, InfoContext baseContext){
		this.fRcInfo = rcInfo;
		this.fTool = tool;
		this.fInType = inType;
		this.fCfg = (Configuration)fRcInfo.getParent();
		this.fContext = baseContext;
	}

	public CfgInfoContext(IConfiguration cfg){
		this(cfg, null);
	}

	private CfgInfoContext(IConfiguration cfg, InfoContext baseContext){
		this.fCfg = (Configuration)cfg;
		this.fContext = baseContext;
	}
	
	public IConfiguration getConfiguration(){
		return fCfg;
	}
	
	public IResourceInfo getResourceInfo(){
		return fRcInfo;
	}
	
	public ITool getTool(){
		return fTool;
	}
	
	public IInputType getInputType(){
		return fInType;
	}
	
	public InfoContext toInfoContext(){
		if(fContext == null){
			IProject project = fCfg.isPreference() ? null : fCfg.getOwner().getProject();
			StringBuffer buf = new StringBuffer();
			buf.append(fCfg.getId());
			if(fRcInfo != null){
				buf.append(DELIMITER);
				buf.append(fRcInfo.getId());
			}
			
			if(fTool != null){
				buf.append(DELIMITER);
				buf.append(fTool.getId());
			}
			
			if(fInType != null){
				buf.append(DELIMITER);
				buf.append(fInType.getId());
			}
			
			String instanceId = buf.toString();
			fContext = new InfoContext(project, instanceId);
		}
		return fContext;
	}

	public static CfgInfoContext fromInfoContext(ICProjectDescription des, InfoContext context) {
		IProject project = context.getProject();
		if(project == null)
			return null;
		
		String instanceId = context.getInstanceId();
		if(instanceId.length() == 0)
			return null;
		
		String[] ids = CDataUtil.stringToArray(instanceId, DELIMITER);
		String cfgId = ids[0];

		ICConfigurationDescription cfgDes = des.getConfigurationById(cfgId);
		if(cfgDes == null)
			return null;
		
		IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgDes);
		if(cfg == null)
			return null;
		
		return doCreate(cfg, ids, context);
	}
	
	private static CfgInfoContext doCreate(IConfiguration cfg, String[] ids, InfoContext context){
		String rcInfoId = null, toolId = null, inTypeId = null;
		IResourceInfo rcInfo = null;
		ITool tool = null;
		IInputType inType = null;
		
		switch(ids.length){
		case 4:
			inTypeId = ids[3];
		case 3:
			toolId = ids[2];
		case 2:
			rcInfoId = ids[1];
		}
		
		if(rcInfoId != null){
			// rcInfo = (IResourceInfo)find(cfg.getResourceInfos(), rcInfoId);
			rcInfo = cfg.getResourceInfoById(rcInfoId);
			if(rcInfo == null){
				return null;
			}
		}
		
		if(toolId != null){
			tool = (ITool)find(rcInfo.getTools(), toolId);
			if(tool == null)
				return null;
		}
		
		if(inTypeId != null){
			inType = (IInputType)find(tool.getInputTypes(), inTypeId);
			if(inType == null)
				return null;
		}
		
		if(rcInfo != null)
			return new CfgInfoContext(rcInfo, tool, inType, context);
		return new CfgInfoContext(cfg, context);

	}

	public static CfgInfoContext fromInfoContext(IConfiguration cfg, InfoContext context) {
		IProject project = context.getProject();
		if(project == null)
			return null;
		
		String instanceId = context.getInstanceId();
		if(instanceId.length() == 0)
			return null;
		
		String[] ids = CDataUtil.stringToArray(instanceId, DELIMITER);
		String cfgId = ids[0];
		if(!cfgId.equals(cfg.getId()))
			return null;

		return doCreate(cfg, ids, context);
	}
	
	private static IBuildObject find(IBuildObject objs[], String id){
		for(int i = 0; i < objs.length; i++){
			if(objs[i].getId().equals(id))
				return objs[i];
		}
		return null;
	}
	
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		
		if(!(obj instanceof CfgInfoContext))
			return false;
		
		CfgInfoContext other = (CfgInfoContext)obj;
		if(!checkBuildObjects(other.fCfg, fCfg))
			return false;

		if(!checkBuildObjects(other.fRcInfo, fRcInfo))
			return false;

		if(!checkBuildObjects(other.fTool, fTool))
			return false;

		if(!checkBuildObjects(other.fInType, fInType))
			return false;

		return true;
	}

	public int hashCode() {
		int code = getCode(fCfg);
		code += getCode(fRcInfo);
		code += getCode(fTool);
		code += getCode(fInType);
		return code;
	}
	
	private boolean checkBuildObjects(IBuildObject bo1, IBuildObject bo2){
		if(bo1 == null)
			return bo2 == null;
		if(bo2 == null)
			return false;
		return bo1.getId().equals(bo2.getId());
	}
	
	private int getCode(IBuildObject bo){
		if(bo == null)
			return NULL_OBJ_CODE;
		return bo.getId().hashCode();
	}
}
