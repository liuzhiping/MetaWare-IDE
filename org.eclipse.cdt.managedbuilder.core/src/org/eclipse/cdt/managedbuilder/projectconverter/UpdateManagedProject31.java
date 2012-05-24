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
package org.eclipse.cdt.managedbuilder.projectconverter;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObjectProperties;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class UpdateManagedProject31 {
	private static final String INEXISTEND_PROP_ID = ""; //$NON-NLS-1$
	
	static void doProjectUpdate(IProgressMonitor monitor, final IProject project) throws CoreException {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		((ManagedBuildInfo)info).setVersion(ManagedBuildManager.getBuildInfoVersion().toString());

		info.setValid(true);		
		adjustProperties(info);
	}

	
	private static void adjustProperties(IManagedBuildInfo info){
		IManagedProject mProj = info.getManagedProject();
		IConfiguration[] cfgs = mProj.getConfigurations();
		for(int i = 0; i < cfgs.length; i++){
			adjustProperties(cfgs[i]);
		}
	}

	private static void adjustProperties(IConfiguration cfg){
		IBuildObjectProperties props = cfg.getBuildProperties();
		if(props == null)
			return;
		
		//<CUSTOMIZATION> We got rid of 10 levels of optimizations. Now only 3. Translate the other 7.
		// This is esoteric to ARC toolchain plugins. Need to have a generic way of doing this.
		//CORRECTION: we put the 10 levels back!
//		for (ITool tool: cfg.getTools()){
//		    IOption opt = tool.getOptionBySuperClassId("arc.compiler.options.general.optimization.level");
//		    if (opt != null) {
//		        Object v = opt.getValue();
//		        if (v != null){
//		            String s = v.toString();
//		            try {
//                        if (s.endsWith(".level.O1") || s.endsWith(".level.O2")){
//                            cfg.setOption(tool,opt,"arc.optimization.level.none");
//                        }
//                        else if (s.matches(".*\\.level\\.O[34567]")){
//                            cfg.setOption(tool,opt,"arc.optimization.level.O");
//                        }
//                    }
//                    catch (BuildException e) {
//                        e.printStackTrace();
//                    }
//		        }
//		    }
//		}
		//</CUSTOMIZATION>
		
		//<CUSTOMIZATION cr98675>  hack for MetaWare Toolset in converting from prior to 4.0 to 4.0 or later
        for (ITool tool : cfg.getTools()) {
            IOption opt = tool.getOptionBySuperClassId("arc.arc.compiler.options.nosdata");
            if (opt != null) {
                Object v = opt.getValue();
                Object d = opt.getDefaultValue();
                if (d == v && opt.isExtensionElement()) {
                    // default changed from sdata0 to sdata; so, reset it to sdata0
                    try {
                        cfg.setOption(tool,opt,"sdata0");
                    }
                    catch (BuildException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }               
            }
        }
		//</CUSTOMIZATION>
		
		boolean artefactTypeSupported = props.supportsType(ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_ID);
		boolean buildTypeSupported = props.supportsType(ManagedBuildManager.BUILD_TYPE_PROPERTY_ID);
		if(!artefactTypeSupported && !buildTypeSupported)
			return;
		
		String artefactType = artefactTypeSupported ? null : INEXISTEND_PROP_ID;
		String buildType = buildTypeSupported ? null : INEXISTEND_PROP_ID;
		String artExt = ((Configuration)cfg).getArtifactExtensionAttribute(false);
		String id = cfg.getId();
		if(artefactType == null){
			artefactType = getBuildArtefactTypeFromId(id);
		}
		if(buildType == null){
			buildType = getBuildTypeFromId(id);
		}
		
		if(artefactType == null || buildType == null){
			for(IToolChain tc = cfg.getToolChain(); tc != null && (artefactType == null || buildType == null); tc = tc.getSuperClass()){
				id = tc.getId();
				if(artefactType == null){
					artefactType = getBuildArtefactTypeFromId(id);
				}
				if(buildType == null){
					buildType = getBuildTypeFromId(id);
				}
			}
		}
		
		if(artefactType != null && artefactType != INEXISTEND_PROP_ID){
			try {
				props.setProperty(ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_ID, artefactType);
			} catch (CoreException e) {
				ManagedBuilderCorePlugin.log(e);
			}
		}
		if(buildType != null && buildType != INEXISTEND_PROP_ID){
			try {
				props.setProperty(ManagedBuildManager.BUILD_TYPE_PROPERTY_ID, buildType);
			} catch (CoreException e) {
				ManagedBuilderCorePlugin.log(e);
			}
		}
		
		if(artExt != null)
			cfg.setArtifactExtension(artExt);
	}
	
	private static String getBuildArtefactTypeFromId(String id){
		if(id.indexOf(".exe") != -1) //$NON-NLS-1$
			return ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_EXE;
		if(id.indexOf(".so") != -1) //$NON-NLS-1$
			return ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_SHAREDLIB;
		if(id.indexOf(".lib") != -1) //$NON-NLS-1$
			return ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_STATICLIB;
		return null;
	}

	private static String getBuildTypeFromId(String id){
		if(id.indexOf(".debug") != -1) //$NON-NLS-1$
			return ManagedBuildManager.BUILD_TYPE_PROPERTY_DEBUG;
		if(id.indexOf(".release") != -1) //$NON-NLS-1$
			return ManagedBuildManager.BUILD_TYPE_PROPERTY_RELEASE;
		return null;
	}
}
