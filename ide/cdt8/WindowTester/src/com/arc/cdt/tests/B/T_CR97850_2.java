package com.arc.cdt.tests.B;

import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import junit.framework.Assert;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.condition.IsEnabledCondition;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.CTabItemLocator;
import com.windowtester.runtime.swt.locator.ComboItemLocator;
import com.windowtester.runtime.swt.locator.FilteredTreeItemLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;

public class T_CR97850_2 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm support for ARC 601 propagates to the Launch Configuration.";
    public static final String CATEGORY = PROJECT_MANAGEMENT;
	private static final String PROJECT = "ARC601";
	private static final String LAUNCH = PROJECT + ".elf";

	/**
	 * Main test method.
	 */
	public void testT_CR97850() throws Exception {
	    switchToCPerspective(); //in case previous test left in wrong perspective
	    this.deleteProject(PROJECT);
	    EclipseUtil.deleteLaunch(LAUNCH);
	    this.createNewProject(PROJECT);
	    createSourceFile(PROJECT,"main.c",
	    		"#include <stdio.h>\n\n" +
	    		"int main() { \n\t" +
	    		"printf(\"Hello, world\\n\"" + ARROW_RIGHT+";\n"+
	    		"return 0;");
	    this.buildProject(PROJECT);
		
		this.bringUpPropertiesDialog(PROJECT, new IUIRunnable(){

			@Override
			public void run(IUIContext ui) throws WidgetSearchException {
				
				ui.click(computeTreeItemLocator("C\\/C++ Build/Settings"));
				
				ui.click(computeTreeItemLocator(
					"MetaWare ARC C\\/C++ Compiler/Processor\\\\/Extensions"));
				ui.click(new ComboItemLocator("ARC 601", new NamedWidgetLocator("arc.compiler.options.target.version")));
				NamedWidgetLocator arc6core = new NamedWidgetLocator("arc.compiler.options.arc6core");
				NamedWidgetLocator arc5core = new NamedWidgetLocator("arc.compiler.options.arc5core");
				ui.assertThat(new IsEnabledCondition(arc6core,false));
				ui.assertThat(new IsEnabledCondition(arc5core,false));
				ui.click(computeTreeItemLocator(
				"MetaWare ARC C\\/C++ Compiler/DSP Extensions"));
				NamedWidgetLocator xy = new NamedWidgetLocator("arc.compiler.options.xy");
				ui.assertThat(new IsEnabledCondition(xy,false));  // Make sure -xy is not an option.
				ui.click(new ButtonLocator("OK"));
				
			}});
		
		 this.bringUpDebugLaunchDialog(new IUIRunnable(){
			    private void assertEnabled(IUIContext ui, String prop, boolean enabled){
			    	NamedWidgetLocator w = new NamedWidgetLocator(prop);
			    	ui.assertThat("Property " + prop,new IsEnabledCondition(w,enabled));
			    }

				@Override
				public void run(IUIContext ui) throws WidgetSearchException {
					createNewLaunchFromLaunchDialog(ui, LAUNCH, PROJECT);
					ui.click(new FilteredTreeItemLocator("C\\/C++ Application/"+LAUNCH));
					ui.click(new CTabItemLocator("Debugger"));
					ui.click(EclipseUtil.computeTreeItemLocator(ui,"Target Selection"));
					IWidgetLocator t = ui.find(new NamedWidgetLocator("ARC_target_processor"));
					String text = EclipseUtil.getText(t);
					Assert.assertTrue("ARC 601".equals(text));
					ui.click(EclipseUtil.computeTreeItemLocator(ui,"Simulator Extensions/DSP Instructions"));
					assertEnabled(ui,"ARC_xmac_24",false);
					assertEnabled(ui,"ARC_xmac_d16",false);
					assertEnabled(ui,"ARC_dmulpf",false);
					assertEnabled(ui,"ARC_ea",false);
					
					ui.click(EclipseUtil.computeTreeItemLocator(ui,"Simulator Extensions/DSP Memory"));
					assertEnabled(ui,"ARC_xy",false);
					
					ui.click(EclipseUtil.computeTreeItemLocator(ui,"Simulator Extensions/Floating Point"));
					assertEnabled(ui,"A6_dpfp",false);
					assertEnabled(ui,"A6_spfp",false);
					assertEnabled(ui,"A6_dpfpfast",false);
					assertEnabled(ui,"A6_spfpfast",false);
					
					ui.wait(milliseconds(500));
					
					text = EclipseUtil.getText(ui.find(new NamedWidgetLocator("debugger_options")));
					Assert.assertTrue("-a601 present (" + text + ")",text != null && text.indexOf("-a601")>=0);												
				}});
		 EclipseUtil.deleteLaunch(LAUNCH);
		 deleteProject(PROJECT);
		
	}

}