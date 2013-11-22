package com.arc.vdk.tests.VDK_First;

import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.MenuItemLocator;
import com.windowtester.runtime.swt.locator.TreeItemLocator;

public class V_1_1 extends UIArcTestCaseSWT {

	/**
	 * Main test method.
	 */
	public void testV_1_1() throws Exception {
		// create and destroy an empty VDK project
		IUIContext ui = getUI();
		ui.click(new MenuItemLocator("File/New/Project..."));
		ui.wait(new ShellShowingCondition("New Project"));
		ui.click(new TreeItemLocator("VDK/Project"));
		ui.click(new ButtonLocator("&Next >"));
		ui.enterText("VT");
		ui.click(new ButtonLocator("&Next >"));
		ui.click(new ButtonLocator("&Finish"));
		ui.wait(new ShellDisposedCondition("VDK Project"));
		deleteProject("VT");
	}

}