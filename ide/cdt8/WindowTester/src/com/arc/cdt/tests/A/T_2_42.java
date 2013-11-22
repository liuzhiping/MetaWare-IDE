package com.arc.cdt.tests.A;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;

public class T_2_42 extends UIArcTestCaseSWT {
    public static final String DESCRIPTION = "Test that command-line break command properly infers source file.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
	/**
	 * Main test method.
	 */
	public void testT_2_42() throws Exception {
	    registerPerspectiveConfirmationHandler();
        switchToDebugPerspective(); 
        launchDebugger("Queens_AC.elf",true);
        this.waitUntilDebuggerStops(15000);
        showView(COMMAND_VIEW_ID);
        this.enterDebugCommandLine("break !32");
        this.enterDebugCommandLine("break !36");
        this.clickResumeButton();
        IUIContext ui = getUI();
        ui.find(computeTreeItemLocator("Queens_AC\\.elf.*/MetaWare.*/Thread.*/.*Try\\(\\) queens.c:32 ?.*"));
        this.enterDebugCommandLine("run");
        ui.find(computeTreeItemLocator("Queens_AC\\.elf.*/MetaWare.*/Thread.*/.*Try\\(\\) queens.c:36 ?.*"));
        terminateDebugger();
        switchToCPerspective();
	}

}