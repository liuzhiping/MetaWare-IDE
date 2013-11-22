

package com.arc.cdt.tests.B;


import com.arc.cdt.testutil.UIArcTestCaseSWT;


public class T_CR96226 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that Path Mapping entries get properly passed to the debugger engine.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;

    private static final String LAUNCH = "Remapped with Path Mapping";

    /**
     * Main test method.
     */
    public void testT_CR96226 () throws Exception {
        this.switchToDebugPerspective();
        
        this.launchDebugger(LAUNCH,false);
        this.waitUntilDebuggerStops(15000);
            
        this.showSeeCodeView("Source");
        
        this.compareSeeCodeView("T_CR96226.1", "source");
        this.terminateDebugger();
    }

}