

package com.arc.cdt.tests.B;


import com.arc.cdt.testutil.UIArcTestCaseSWT;


public class T_CR95366 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that \"restart\" command works.";

    public static final String CATEGORY = DEBUGGER_INTEGRATION;

    private static final String PROJECT = "Queens_AC";

    private static final String LAUNCH_NAME = PROJECT + ".elf";

    /**
     * Main test method.
     */
    public void testT_CR95366 () throws Exception {

        switchToDebugPerspective();

        this.launchDebugger(LAUNCH_NAME, false);
        this.waitUntilDebuggerStops(20000);
        
        this.showSeeCodeView("Command-line input");
        
        this.enterDebugCommandLine("run");
        
        this.waitUntilDebuggerStops(5000);
        
        this.compareApplicationConsole("T_CR95366.1");
        
        this.enterDebugCommandLine("restart");
        
        this.waitUntilDebuggerStops(5000);
        
        getUI().find(this.computeTreeItemLocator(LAUNCH_NAME + 
            " \\[C\\/C\\+\\+ .*Application\\]/MetaWare Debugger \\(.*\\) \\(Suspended\\)/"+
            "Thread \\[main thread\\] \\(Suspended(: Breakpoint hit.)?\\)/"+
            "0 main\\(\\) queens.c:48 0x........"));
        
        this.enterDebugCommandLine("run");
        
        this.waitUntilDebuggerStops(5000);
      
        this.compareApplicationConsole("T_CR95366.2");
             
        this.terminateDebugger();

    }

}