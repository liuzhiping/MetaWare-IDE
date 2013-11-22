

package com.arc.cdt.tests.B;


import junit.framework.Assert;

import com.arc.cdt.testutil.UIArcTestCaseSWT;


public class T_CR96181 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that startup commands for CMPD sessions are executed exactly once.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;

    private static final String LAUNCH = "Graphics pipeline with cmd";

    /**
     * Main test method.
     */
    public void testT_CR96181 () throws Exception {
        this.switchToDebugPerspective();
        
        this.launchDebugger(LAUNCH,false);
        this.waitUntilDebuggerStops(15000);
            
        String s = this.getDebuggerConsoleContent();
        
        int i = s.indexOf("[all] HELLO");
        int j = s.indexOf("HELLO");
        int k = s.lastIndexOf("HELLO");
        if (i < 0 || j != i+6 || k != j){
            this.writeStringToFile("T_CR96181.txt", s);
            Assert.assertTrue("No single evaluation!",false);
        }  
        this.terminateDebugger();
    }

}