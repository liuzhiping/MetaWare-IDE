package com.arc.cdt.tests.A;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;

public class T_2_15 extends UIArcTestCaseSWT {
    
    public static final String DESCRIPTION = "Confirm that arguments are properly passed to an executable.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    private final static String PROJECT_NAME = "Test1";
    /**
     * This test runs a launch that takes arguments. The program displays the arguments on the console.
     */
    public void testT_2_15() throws Exception {
        registerPerspectiveConfirmationHandler();
        switchToCPerspective(); //in case previous test left in wrong perspective
        cleanProject(PROJECT_NAME);
        buildProject(PROJECT_NAME);
        runProject(PROJECT_NAME);
        getUI().wait(milliseconds(3000));
        String console = getApplicationConsoleContent();
        this.writeAndCompareSnapshot("T_2_15.1",console);
        
    }

}