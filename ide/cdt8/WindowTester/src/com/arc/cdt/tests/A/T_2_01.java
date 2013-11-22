package com.arc.cdt.tests.A;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.MenuItemLocator;

public class T_2_01 extends UIArcTestCaseSWT {
    public static final String CATEGORY = PROJECT_MANAGEMENT;
    public static final String DESCRIPTION = "Test that a C project can be created";

	/**
	 * Create a ARCompact project and confirm that Wizard pages match
	 * base line. (TO DO: check wizard pages)
	 */
	public void testT_2_01() throws Exception {
	    switchToCPerspective(); //in case previous test left in wrong perspective
		IUIContext ui = getUI();
		deleteProject("new_proj_T_2_1");
		ui.click(new MenuItemLocator("File/New/Project..."));
		ui.wait(new ShellShowingCondition("New Project"));
		ui.click(computeTreeItemLocator("C\\/C\\+\\+/C Project"));
		ui.click(new ButtonLocator("&Next >"));
		ui.enterText("new_proj_T_2_1");
		ui.click(new ButtonLocator("&Finish"));
		dealWithSVNConfirmation();
		deleteProject("new_proj_T_2_1");
	}

}