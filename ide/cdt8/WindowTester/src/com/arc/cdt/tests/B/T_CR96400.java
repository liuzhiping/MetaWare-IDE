

package com.arc.cdt.tests.B;


import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;


public class T_CR96400 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Conform that note boxes pop up to display notes from the debugger";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    private static final String LAUNCH = "Queens_AC.elf XISS";

    /**
     * Main test method.
     */
    public void testT_CR96400() throws Exception {
        this.switchToDebugPerspective();
        this.launchDebugger(LAUNCH,false);
        
        this.showView(COMMAND_VIEW_ID);
        
        this.enterDebugCommandLine("watchreg r1");
        
        this.getUI().wait(new ShellShowingCondition("Debugger Note"));
        
        this.getUI().click(new ButtonLocator("OK"));
        
        this.getUI().wait(new ShellDisposedCondition("Debugger Note"));
        
      
        this.terminateDebugger();

        
    }

}