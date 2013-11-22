

package com.arc.cdt.tests.B;


import com.arc.cdt.testutil.UIArcTestCaseSWT;


public class T_CR95766 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Make sure user is prompted to switch to debug perspective when"+
            "\"Resume at startup\" is not checked.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;

    private static final String LAUNCH = "Queens_AC_no_resume";

    /**
     * Main test method.
     */
    public void testT_CR95766 () throws Exception {

        switchToCPerspective();
        
        this.launchDebugger(LAUNCH,true);
        this.terminateDebugger();
       
    }

}