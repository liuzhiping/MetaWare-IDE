package com.arc.cdt.tests.ONE_AT_A_TIME;

import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.locator.MenuItemLocator;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;

public class T_3_2_2 extends UIArcTestCaseSWT {
    public static final String DESCRIPTION = "Confirm that breakpoints that are not on a statement boundary are remembered between sessions (part 2)";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
	/**
	 * Main test method.
	 */
	public void testT_3_2_2() throws Exception {
		IUIContext ui = getUI();
		ui.click(new MenuItemLocator("Window/Show View/Breakpoints"));
		ui.contextClick(computeTreeItemLocator(".*[\\/\\\\]Test1[\\/\\\\]main.c \\[line: 6\\]\\+0x[46]"),
			"Remove All");
		ui.wait(new ShellShowingCondition("Remove All Breakpoints"));
		ui.click(new ButtonLocator("&Yes"));
		ui.wait(new ShellDisposedCondition("Remove All Breakpoints"));
        switchToCPerspective();
	}
	
	@Override
    protected boolean clearBreakpointsAtStartup() {
		return false;
	}

}