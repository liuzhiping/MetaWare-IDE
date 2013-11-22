package com.arc.cdt.tests.B;

import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.CTabItemLocator;
import com.windowtester.runtime.swt.locator.MenuItemLocator;

public class T_CR91975 extends UIArcTestCaseSWT {
	 public static final String DESCRIPTION = "Confirm the Cheat Sheets for C/C++ project development.";
	 public static final String CATEGORY = GENERIC_CDT_TESTS;
	/**
	 * Main test method.
	 */
	public void testT_CR91975() throws Exception {
		IUIContext ui = getUI();
		ui.click(new MenuItemLocator("Help/Cheat Sheets..."));
		ui.wait(new ShellShowingCondition("Cheat Sheet Selection"));
		ui.click(computeTreeItemLocator("C\\/C++ Development"));
		ui.click(computeTreeItemLocator(
			"C\\/C++ Development/Creating C\\/C++ Projects"));
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Cheat Sheet Selection"));
		ui.click(new MenuItemLocator("Help/Cheat Sheets..."));
		ui.wait(new ShellShowingCondition("Cheat Sheet Selection"));
		ui.click(computeTreeItemLocator("C\\/C++ Development"));
		ui.click(computeTreeItemLocator(
			"C\\/C++ Development/Importing C\\/C++ Projects"));
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Cheat Sheet Selection"));
		ui.click(new MenuItemLocator("Help/Cheat Sheets..."));
		ui.wait(new ShellShowingCondition("Cheat Sheet Selection"));
		ui.click(computeTreeItemLocator("C\\/C++ Development"));
		ui.click(new ButtonLocator("Cancel"));
		ui.wait(new ShellDisposedCondition("Cheat Sheet Selection"));
		ui.contextClick(new CTabItemLocator("Cheat Sheets"), "Close");
	}

}