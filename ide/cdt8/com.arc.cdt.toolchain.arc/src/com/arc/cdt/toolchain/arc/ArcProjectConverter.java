package com.arc.cdt.toolchain.arc;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IConvertManagedBuildObject;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class ArcProjectConverter implements IConvertManagedBuildObject {

	//@MIDE version I-2013.09
	//for the selected project, for all configurations and correponding toolchain,
	//if it is the ARC Generic tool chain and is also the EM or HS ARC target,
	//it need set the default command as the ccac, mcc -ccac has been deprecated.
	//it also set the Scanner Discovery as the ClangScannerConfigurationDiscoveryProfile to collect predefined symbols and include paths.
	public IBuildObject convert(IBuildObject buildObj, String fromId,
			String toId, boolean isConfirmed) {

		
		//get current selection project
	    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	    if (window != null)
	    {
	        IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
	        Object firstElement = selection.getFirstElement();
	        if (firstElement instanceof IAdaptable)
	        {
	            IProject project = (IProject)((IAdaptable)firstElement).getAdapter(IProject.class);
	            
	            //get all Configurations	            
	            IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
	            for(IConfiguration config : buildInfo.getManagedProject().getConfigurations()) { //for all configurations
	            	IToolChain toolChain = config.getToolChain();
	            	IOption targetOption;
	            	if(toolChain.getId().startsWith("com.arc.cdt.toolchain.arc.")) { //if it's the ARC Generic ToolChain
	            		ITool[] debugCompilers = toolChain.getToolsBySuperClassId("arc.exe.debug.exeCompilerDebug"); //case: debug configuration
	            		if(debugCompilers.length > 0) {
	            			targetOption = debugCompilers[0].getOptionBySuperClassId("arc.compiler.options.target.version");
	            			String target = targetOption.getValue().toString();
	            			if(target.endsWith("arcv2hs") || target.endsWith("arcv2em"))  { //if it's the HS or EM project
	            				//set Clang Scanner
	            				toolChain.setScannerConfigDiscoveryProfileId("com.arc.cdt.toolchain.arc.ClangScannerConfigurationDiscoveryProfile");	            			
	            				
	            				//set ccac for EM or HS target.
	            				ITool[] allTools = toolChain.getTools();
	            				for(ITool tool : allTools)
	            					tool.setToolCommand("ccac");
	            			}
	            		}
	            		ITool[] releaseCompilers = toolChain.getToolsBySuperClassId("com.arc.cdt.toolchain.arc.compiler"); //case: release configuration
	            		if(releaseCompilers.length > 0) {
	            			targetOption = releaseCompilers[0].getOptionBySuperClassId("arc.compiler.options.target.version");
	            			String target = targetOption.getValue().toString();
	            			if(target.endsWith("arcv2hs") || target.endsWith("arcv2em"))  { //if it's the HS or EM project
	            				//set Clang Scanner
	            				toolChain.setScannerConfigDiscoveryProfileId("com.arc.cdt.toolchain.arc.ClangScannerConfigurationDiscoveryProfile");	            			
	            				
	            				//set ccac for EM or HS target.
	            				ITool[] allTools = toolChain.getTools();
	            				for(ITool tool : allTools)
	            					tool.setToolCommand("ccac");
	            			}
	            		}	  
	            	}       	
	            }
	            ManagedBuildManager.saveBuildInfo(project, true);
	        }
	    }
		return buildObj;
	}
}
