package com.arc.cdt.tests.B;
import com.arc.cdt.testutil.UIArcTestCaseSWT;

public class T_CR101683 extends UIArcTestCaseSWT {

    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    public static final String DESCRIPTION = "Confirm that the debugger can be launched for a \"makefile\" project that is targeted to ARC700 with -Xmpy enabled";
    static final String PROJECT_NAME = "Queens_STD_make";
    static final String LAUNCH_NAME = "Queens_STD_make";

    

	/**
	 * Main test method.
	 */
	public void testT_101683() throws Exception {
	   
	    switchToCPerspective(); //in case previous test left in wrong perspective
	    cleanProject(PROJECT_NAME);
	    buildProject(PROJECT_NAME);
	    // Click "Yes" when prompted to change perspectives.
	    
        //registerPerspectiveConfirmationHandler();     

		launchDebugger(LAUNCH_NAME,true);	 
		
		clickResumeButton();
		this.waitUntilDebuggerStops(10000);
		
		compareApplicationConsole("T_2_07.1");
		
		// terminate debug and return to C/C++ Perpsective
		terminateDebugger();
		switchToCPerspective();		
	}

}