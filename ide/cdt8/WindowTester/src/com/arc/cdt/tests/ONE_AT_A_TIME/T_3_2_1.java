package com.arc.cdt.tests.ONE_AT_A_TIME;

import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.locator.MenuItemLocator;
import com.windowtester.runtime.locator.XYLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;
import com.windowtester.runtime.swt.locator.eclipse.ContributedToolItemLocator;

public class T_3_2_1 extends UIArcTestCaseSWT {


    public static final String DESCRIPTION = "Confirm that breakpoints that are not on a statement boundary are remembered between sessions (part 1)";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
	/**
	 * Main test method.
	 */
	public void testT_3_2_1() throws Exception {
		IUIContext ui = getUI();
        switchToCPerspective(); // in case previous test left in wrong perspective
		invokeDebuggerFor("Test1", true);
		ui.click(new MenuItemLocator("Debugger/Disassembly"));
		ui.click(new XYLocator(new NamedWidgetLocator("disasm"), 10, 71));
		ui.click(new ContributedToolItemLocator("disasm.button.breakpoint"));
		ui.click(new MenuItemLocator("Window/Show View/Breakpoints"));
		ui.click(computeTreeItemLocator(".*[\\/\\\\]Test1[\\/\\\\]main.c \\[line: 6\\]\\+0x[46]"));
        clickResumeButton();
        ui.find(computeTreeItemLocator(
        	"Test1\\.elf.*/MetaWare Debugger.*/Thread.*/.*main\\(\\) main.c:6 0x0001011[ce]"));
        terminateDebugger();
	}

}