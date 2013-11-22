package com.arc.cdt.tests.A;
import org.eclipse.swt.widgets.ToolItem;

import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.MenuItemLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.TableItemLocator;
import com.windowtester.runtime.swt.locator.TreeItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ContributedToolItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;

public class T_2_60 extends UIArcTestCaseSWT {
    public static final String DESCRIPTION = "Confirm that global variables in the Variables display update properly.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
	/**
	 * Main test method.
	 */
	public void testT_2_60() throws Exception {
	    switchToCPerspective(); //in case previous test left in wrong perspective
		IUIContext ui = getUI();
		ui.click(new MenuItemLocator("Window/Open Perspective/Other..."));
		ui.wait(new ShellShowingCondition("Open Perspective"));
		ui.click(new TableItemLocator("C/C++ (default)"));
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Open Perspective"));
		buildProject("Test1");
		launchDebugger("Test1.elf",true);
		ui.click(new MenuItemLocator("Window/Show View/Other..."));
		ui.wait(new ShellShowingCondition("Show View"));
		ui.click(new TreeItemLocator("Debug/Variables"));
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Show View"));
		ui.click(new ContributedToolItemLocator(
				"org.eclipse.debug.ui.commands.StepInto"));
		ui.click(new ContributedToolItemLocator(
				"org.eclipse.debug.ui.commands.StepInto"));
		ui.click(new ContributedToolItemLocator(
				"org.eclipse.debug.ui.commands.StepInto"));
		ui.click(new ContributedToolItemLocator(
				"org.eclipse.debug.ui.commands.StepInto"));
		ui.click(new MenuItemLocator("Window/Show View/Other..."));
		ui.wait(new ShellShowingCondition("Show View"));
		ui.click(new TreeItemLocator("Debug/Variables"));
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Show View"));
		ui.click(new TreeItemLocator("k", new ViewLocator(
				"org.eclipse.debug.ui.VariableView")));
		ui.click(new ContributedToolItemLocator(
				"org.eclipse.debug.ui.commands.StepOver"));
		ui.click(new ContributedToolItemLocator(
				"org.eclipse.debug.ui.commands.StepOver"));
		terminateDebugger();
		ui.click(new SWTWidgetLocator(ToolItem.class, "C/C++"));
	}

}