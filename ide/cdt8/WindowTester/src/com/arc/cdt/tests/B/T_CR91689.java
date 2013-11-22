package com.arc.cdt.tests.B;

import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;

public class T_CR91689 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Test if \"go !<line>\" works.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
	private static final String PROJECT_NAME = "Queens_AC";

    /**
	 * Tests if "go !<line>" works.
	 */
	public void testT_CR91689() throws Exception {
		registerPerspectiveConfirmationHandler();
	    switchToCPerspective(); //in case previous test left in wrong perspective
		IUIContext ui = getUI();
		this.setDefaultBuildProperties(PROJECT_NAME); // in case "-g" isn't set.
		buildProject(PROJECT_NAME);
		this.launchDebugger(PROJECT_NAME + ".elf",true);
		this.showView(COMMAND_VIEW_ID);
		this.enterDebugCommandLine("go !63");
		ui.wait(milliseconds(1000));
		compareApplicationConsole("T_CR91689.1");
		terminateDebugger();
		switchToCPerspective();
	}

}