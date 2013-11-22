package com.arc.cdt.tests.A;
import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;

import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;

public class T_2_13 extends UIArcTestCaseSWT {
    public static final String DESCRIPTION = "Confirm that \"run\" operation works against ARC executable.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    
    private final static String PROJECT_NAME = "Queens_AC";
	/**
	 * This test simply confirms that "Run" operation works.
	 * But the test is quite redundant because several other tests also perform a Run operation.
	 */
	public void testT_2_13() throws Exception {
	    switchToCPerspective(); //in case previous test left in wrong perspective
		cleanProject(PROJECT_NAME,45000);
		buildProject(PROJECT_NAME);
		runLaunch(PROJECT_NAME+".elf");
		this.waitForLaunchTermination(10000);
		// For some reason, the console delays in getting populated.
		String console = getApplicationConsoleContent();
		IUIContext ui = getUI();
		if (console.length() < 10){
		    ui.wait(milliseconds(1000));

			console = getApplicationConsoleContent();
			if (console.length() < 10) {
	            ui.wait(milliseconds(1000));
				console = getApplicationConsoleContent();
			}
		}
		this.writeAndCompareSnapshot("T_2_13.1",console);
		
	}

}