package com.arc.cdt.tests.A;
import com.arc.cdt.testutil.UIArcTestCaseSWT;

public class T_2_07 extends UIArcTestCaseSWT {

    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    public static final String DESCRIPTION = "Confirm that the debugger can be launched on \"Queens\"; resume and confirm that program completes execution.";
    static final String PROJECT_NAME = "Queens_AC";
    

	/**
	 * Main test method.
	 */
	public void testT_2_07() throws Exception {
	   
	    switchToCPerspective(); //in case previous test left in wrong perspective
	    cleanProject(PROJECT_NAME);
	    buildProject(PROJECT_NAME);
	    // Click "Yes" when prompted to change perspectives.
	    
        //registerPerspectiveConfirmationHandler();     

		invokeDebuggerFor(PROJECT_NAME,true);	 
		
		clickResumeButton();
		this.waitUntilDebuggerStops(10000);
		
		compareApplicationConsole("T_2_07.1");
		
		// terminate debug and return to C/C++ Perpsective
		terminateDebugger();
		switchToCPerspective();		
	}

}