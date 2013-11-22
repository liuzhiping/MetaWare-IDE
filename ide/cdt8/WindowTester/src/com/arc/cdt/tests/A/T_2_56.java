package com.arc.cdt.tests.A;
import com.arc.cdt.testutil.UIArcTestCaseSWT;

public class T_2_56 extends UIArcTestCaseSWT {
    public static final String DESCRIPTION = "Test if we can debug an executable outside of the workspace.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    /**
     * Main test method.
     */
    public void testT_2_56() throws Exception {
	    switchToCPerspective(); //in case previous test left in wrong perspective
        launchDebugger("Queens_AC_external.elf",true);
        this.clickResumeButton();
        this.waitUntilDebuggerStops(15000);
        showView(CONSOLE_VIEW_ID);
        compareApplicationConsole("T_2_56.1");
    }

}