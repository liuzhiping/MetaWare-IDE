

package com.arc.cdt.tests.B;


import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import junit.framework.Assert;

import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.locator.IWidgetLocator;


public class T_CR93704 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Test that command line works when multiple processes selected.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    private static final String LAUNCH_NAME = "Graphics pipeline 2";

    /**
     * Main test method.
     */
    public void testT_CR93704() throws Exception {
        IUIContext ui = getUI();
        switchToDebugPerspective();
        launchDebugger(LAUNCH_NAME,false);
        
        this.waitUntilDebuggerStops(15000);
        
        this.showView(COMMAND_VIEW_ID);
        
        ui.wait(milliseconds(500));
        
        String console = this.getDebuggerConsoleContent();
      
        long endTime = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < endTime && console.indexOf("[4]") < 0) {
            ui.wait(milliseconds(500));
            console = this.getDebuggerConsoleContent();
        }
        
        int consoleStartIndex = console.length();
        Assert.assertTrue(console.indexOf("[4]") > 0); // should be significant output
        
        clickProcess("displaymain",false);
        clickProcess("rendermain1",true);
        this.enterDebugCommandLine("reg pc");
        
        ui.wait(milliseconds(500));
        
        console = this.getDebuggerConsoleContent().substring(consoleStartIndex);
        
        String lines[] = console.split("\\n");
        
        if (lines.length != 2) dumpConsole(console);
        Assert.assertTrue(lines.length==2 || lines.length == 3 && lines[2].length() == 0);
        
        if (!lines[0].startsWith("[2]")) {
            dumpConsole(console);
            Assert.fail("missing [2]");
        }
        
        if (!lines[1].startsWith("[4]")) {
            dumpConsole(console);
            Assert.fail("missing [4]");
        }
        if (!lines[0].matches(".*\\spc\\s.*")) {
            dumpConsole(console);
            Assert.fail("Missing pc reference for [2]");
        }
        
        if (!lines[1].matches(".*\\spc\\s.*")) {
            dumpConsole(console);
            Assert.fail("Missing pc reference for [4]");
        }
        
       
        this.terminateDebugger();
       
    }
    
    private void clickProcess(String processName, boolean ctrl) throws WidgetSearchException {
        IWidgetLocator locator = this.computeTreeItemLocator(LAUNCH_NAME + ".*/" + processName + ".*\\(Suspended\\)");
        if (ctrl){
            getUI().click(1,locator,WT.CTRL);
        }
        else {
            getUI().click(locator);
        }
    }
    
    private void dumpConsole(String console){
        this.writeStringToFile("T_CR93704.txt", console);
    }
}