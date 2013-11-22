package com.arc.cdt.tests.A;
import com.arc.cdt.testutil.CDTUtil;
import com.arc.cdt.testutil.IToolOptionSetting;
import com.arc.cdt.testutil.IUIToolOptionSetter;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;

public class T_2_20 extends UIArcTestCaseSWT {
    
    public static final String DESCRIPTION = "Test if we can debug an application with no debug information.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;

    static final String PROJECT_NAME = "NoDebugInfo";
	/**
	 * Test if we can debug an application with no debug information.
	 */
	public void testT_2_20() throws Exception {
		registerPerspectiveConfirmationHandler();
	    switchToCPerspective(); //in case previous test left in wrong perspective
		this.bringUpBuildSettings(PROJECT_NAME, new IUIToolOptionSetter(){

            @Override
            public void run (IUIContext ui, IToolOptionSetting settings) throws WidgetSearchException {
                CDTUtil.setCompilerSwitch("-g", settings, false);              
            }});
		buildProject(PROJECT_NAME);
		this.invokeDebuggerFor(PROJECT_NAME,true);
		this.showView(DISASM_VIEW_ID);
		this.showView(COMMAND_VIEW_ID);
		this.enterDebugCommandLine("ssi"); // should cause pop up about missing debug info
		IUIContext ui = getUI();
		ui.wait(new ShellShowingCondition("Debugger Error"));
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Debugger Error"));
		this.enterDebugCommandLine("ssi");
		this.enterDebugCommandLine("ssi");
		this.terminateDebugger();
		switchToCPerspective();
		this.setDefaultBuildProperties(PROJECT_NAME);
		
	}

}