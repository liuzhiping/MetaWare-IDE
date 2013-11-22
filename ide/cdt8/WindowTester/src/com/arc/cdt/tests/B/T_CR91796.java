package com.arc.cdt.tests.B;

import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import org.eclipse.swt.widgets.ToolItem;

import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.arc.widgets.internal.swt.TextColumn;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.locator.XYLocator;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.MenuItemLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.eclipse.ContributedToolItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;

public class T_CR91796 extends UIArcTestCaseSWT {
    public static final String DESCRIPTION = "Test that breakpoints can be set on non-statement boundaries.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
	/**
	 * Main test method.
	 */
	public void testT_CR91796() throws Exception {
		registerPerspectiveConfirmationHandler();
	    switchToCPerspective(); //in case previous test left in wrong perspective
		IUIContext ui = getUI();
		buildProject("Test1");
		invokeDebuggerFor("Test1",true);
		ui.click(new MenuItemLocator("Window/Show View/Breakpoints"));
		// Required to put focus in Disassembly view (for TestSuite)
		ui.click(new MenuItemLocator("Window/Show View/Other..."));
		ui.wait(new ShellShowingCondition("Show View"));
		ui.click(computeTreeItemLocator("Debug/Disassembly (MetaWare)"));
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Show View"));
		//
		ui.click(new XYLocator(new SWTWidgetLocator(TextColumn.class,
				new ViewLocator(DISASM_VIEW_ID)),
				7, 104));
		ui.wait(milliseconds(2000));
		ui.click(new ContributedToolItemLocator("disasm.button.breakpoint"));
		ui.wait(milliseconds(3000));
		ui.contextClick(
			this.computeTreeItemLocator(
				"(.*[\\\\\\/]Test1[\\\\\\/])?main.c \\[line: 6\\]\\+0x[ae]"),
				"Remove All");
		ui.wait(new ShellShowingCondition("Remove All Breakpoints"));
		ui.click(new ButtonLocator("&Yes"));
		ui.wait(new ShellDisposedCondition("Remove All Breakpoints"));
		terminateDebugger();
		ui.click(new SWTWidgetLocator(ToolItem.class, "C/C++"));
	}

}