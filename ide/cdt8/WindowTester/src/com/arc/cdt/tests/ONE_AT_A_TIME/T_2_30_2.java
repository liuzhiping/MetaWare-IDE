package com.arc.cdt.tests.ONE_AT_A_TIME;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.locator.XYLocator;
import com.windowtester.runtime.swt.locator.CTabItemLocator;
import com.windowtester.runtime.swt.locator.TreeItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;

public class T_2_30_2 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that command-line input remembers commands between sessions (part 2)";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
	/**
	 * Main test method.
	 */
	public void testT_2_30_2() throws Exception {
		// Do NOT force C perspective, this test expects to be in Debug perspective,
		// and is to be run right after test T_2_30_1 is run.
		IUIContext ui = getUI();
		ui.click(new XYLocator(new CTabItemLocator("Breakpoints"), 64, 8));
		ui.click(new TreeItemLocator(
			".*main.c \\[line: 7\\]",
			new ViewLocator("org.eclipse.debug.ui.BreakpointView")));
		ui.contextClick(new TreeItemLocator(
			".*main.c \\[line: 7\\]",
			new ViewLocator("org.eclipse.debug.ui.BreakpointView")),
			"Remove");
		terminateDebugger();
		switchToCPerspective();
	}
	
	@Override
    protected boolean clearBreakpointsAtStartup() {
		return false;
	}

}