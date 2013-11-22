

package com.arc.cdt.tests.B;


import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.swt.locator.TreeItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;


public class T_CR94146 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Test that the debugger displays union fields correctly.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    private static final String PROJECT = "jumpingunion";
    private static final String LAUNCH_NAME = PROJECT+".elf";

    /**
     * Main test method.
     */
    public void testT_CR94146() throws Exception {
       
        switchToDebugPerspective();
        launchDebugger(LAUNCH_NAME,false);
        this.waitUntilDebuggerStops(20000);
        
        IUIContext ui = getUI();
        this.clickStepOverButton();
        this.clickStepOverButton();
        ui.click(new TreeItemLocator("person/person.thisnumber", new ViewLocator(
        "org.eclipse.debug.ui.VariableView")));
        
        this.clickStepOverButton();
            
        ui.click(new TreeItemLocator("person/person\\.namelast/person\\.namelast\\[0\\]", new ViewLocator(
            "org.eclipse.debug.ui.VariableView")));
        ui.click(new TreeItemLocator("person/person\\.namefirst/person\\.namefirst\\[0\\]", new ViewLocator(
            "org.eclipse.debug.ui.VariableView")));
        
       
        this.terminateDebugger();
       
    }
    
}