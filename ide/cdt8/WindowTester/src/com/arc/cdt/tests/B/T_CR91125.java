package com.arc.cdt.tests.B;

import junit.framework.Assert;

import com.arc.cdt.testutil.CDTUtil;
import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IToolOptionSetting;
import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.IUIToolOptionSetter;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.swt.locator.CTabItemLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;
import com.windowtester.runtime.swt.locator.TreeItemLocator;

public class T_CR91125 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm rf16 switch is propagated to the debugger.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    
    private static String PROJECT = "Queens_AC";
    private static String LAUNCH="Queens_AC.elf";
	/**
	 * Main test method.
	 */
	public void testT_CR91125() throws Exception {
	    switchToCPerspective(); //in case previous test left in wrong perspective

		this.bringUpBuildSettings(PROJECT, new IUIToolOptionSetter(){

            @Override
            public void run (IUIContext ui, IToolOptionSetting settings) throws WidgetSearchException {
                CDTUtil.setCompilerSwitch("-arc600", settings, true);
                CDTUtil.setCompilerSwitch("-rf16",settings,true);
                
            }});
		
		this.bringUpDebugLaunchDialog(new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws WidgetSearchException {
                ui.click(new TreeItemLocator("C\\/C\\+\\+ Application/" + LAUNCH));
                ui.click(new CTabItemLocator("Debugger"));
                IWidgetLocator optionsField = ui.find(new NamedWidgetLocator("debugger_options"));
                String text = EclipseUtil.getText(optionsField);
                Assert.assertTrue("-on=rf16 should be in debugger options",text.indexOf("-on=rf16") >= 0);
            }});
		
		this.setDefaultBuildProperties(PROJECT);
		this.bringUpDebugLaunchDialog(new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws WidgetSearchException {
                ui.click(new TreeItemLocator("C\\/C\\+\\+ Application/" + LAUNCH));
                ui.click(new CTabItemLocator("Debugger"));
                IWidgetLocator optionsField = ui.find(new NamedWidgetLocator("debugger_options"));
                String text = EclipseUtil.getText(optionsField);
                Assert.assertTrue("-on=rf16 should not be in debugger options",text.indexOf("-on=rf16") < 0);             
            }});
	    
	}

}