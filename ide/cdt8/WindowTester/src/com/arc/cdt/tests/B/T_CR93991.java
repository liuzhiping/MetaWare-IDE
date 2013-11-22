

package com.arc.cdt.tests.B;


import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;


public class T_CR93991 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that when a MetaWare display is closed in one launch and then reopened in another, that it isn't blank.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    private static final String LAUNCH_NAME = "Queens_AC.elf";

    /**
     * Main test method.
     */
    public void testT_CR93991 () throws Exception {
       
        switchToDebugPerspective();
        this.launchDebugger(LAUNCH_NAME,false);
        IUIContext ui = getUI();       
        this.waitUntilDebuggerStops(20000);
        
        this.showSeeCodeView("Source");
        
        ui.wait(milliseconds(500));
        
        this.clearProfilingColumns("source");
        
        ui.wait(milliseconds(500));
        
        this.compareSeeCodeView("T_CR93991.1","source");
               
        this.terminateDebugger();
        
        this.resetPerspective(); // closes Source view
        
        this.launchDebugger(LAUNCH_NAME,false);
      
        this.waitUntilDebuggerStops(20000);
        
        this.showSeeCodeView("Source");
        
        ui.wait(milliseconds(500));
        
        this.compareSeeCodeView("T_CR93991.2","source");       
        this.terminateDebugger();
              
    }

}