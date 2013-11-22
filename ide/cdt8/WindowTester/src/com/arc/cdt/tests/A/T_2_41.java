package com.arc.cdt.tests.A;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.locator.WidgetReference;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;

public class T_2_41 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Test that bogus thread reference in command-line \"break\" command is diagnosed.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
	/**
	 * Main test method.
	 */
	public void testT_2_41() throws Exception {
		registerPerspectiveConfirmationHandler();
	    switchToDebugPerspective(); 
	    launchDebugger("Test1.elf",true);
	    showView(COMMAND_VIEW_ID);
	    this.enterDebugCommandLine("break !10, thread 1000");
	    IUIContext ui = getUI();
	    ui.wait(new ShellShowingCondition("Debugger Error"));
	    this.compareWidget("T_2_41.1",new WidgetReference<Object>(ui.getActiveWindow()));
        ui.click(new ButtonLocator("OK"));
        ui.wait(new ShellDisposedCondition("Debugger Error"));
        terminateDebugger();
        switchToCPerspective();
   }
}