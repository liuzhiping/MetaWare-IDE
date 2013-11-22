

package com.arc.cdt.tests.deprecated;


import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import junit.framework.Assert;

import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.CTabItemLocator;


public class T_CR93945 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Test that command-line arguments can be passed to CMPD processes"+
    " generated from the VDK config file.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    private static final String LAUNCH_NAME = "TEST1 CMPD";

    /**
     * Main test method.
     */
    public void testT_CR93945 () throws Exception {
       
        switchToDebugPerspective();
        this.bringUpDebugLaunchDialog(new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws WidgetSearchException {
                ui.click(computeTreeItemLocator("C\\/C\\+\\+ [LM].*/"+LAUNCH_NAME));            
                ui.click(new CTabItemLocator("CMPD Debugger Configuration"));
                //ui.click(new ButtonLocator("Reset CMPD Processes"));
                ui.click(new ButtonLocator("&Debug"));
                
            }});
        IUIContext ui = getUI();       
        this.waitUntilDebuggerStops(20000);
        ui.wait(milliseconds(500)); // following tree isn't seen sometimes if we don't delay.
        
        ui.click(this.computeTreeItemLocator(LAUNCH_NAME + " \\[C\\/C\\+\\+ Multiprocess Application\\]/test1 \\(.*\\) ?\\(Suspended\\)"));
        String console = this.getConsoleContent();
       
        this.compareConsole("T_CR93945.1");
        // We make this check so that initial base lines are likely to be correct.
        Assert.assertTrue("Argument count correct",console.startsWith("argc=4"));
        
        ui.click(this.computeTreeItemLocator(LAUNCH_NAME + " \\[C\\/C\\+\\+ Multiprocess Application\\]/test2 \\(.*\\) ?\\(Suspended\\)"));
        console = this.getConsoleContent();       
        this.compareConsole("T_CR93945.2");
        // We make this check so that initial base lines are likely to be correct.
        Assert.assertTrue("Argument count correct",console.startsWith("argc=5"));
        
        this.terminateDebugger();
        
    }

}