package com.arc.cdt.tests.ONE_AT_A_TIME;
import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.swt.locator.MenuItemLocator;

public class T_2_30_1 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that command-line input remembers commands between sessions (part 1)";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
	/**
	 * Main test method.
	 */
	public void testT_2_30_1() throws Exception {
	    setCanonicalSize();
	    switchToCPerspective(); //in case previous test left in wrong perspective
		IUIContext ui = getUI();
		invokeDebuggerFor("Test1",true);
		ui.wait(milliseconds(1000));
		ui.click(new MenuItemLocator("Debugger/Command-line input"));
		ui.enterText("break !7");
		ui.keyClick(WT.CR);
		ui.keyClick(WT.CR);
	}

}