package com.arc.cdt.tests.B;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;

public class T_CR91305 extends UIArcTestCaseSWT {
    public static final String DESCRIPTION = "Confirm that previous CDT editor crash no longer occurs.";
    public static final String CATEGORY = GENERIC_CDT_TESTS;
	/**
	 * Main test method.
	 * Confirms that crash no longer occurs.
	 */
	public void testT_CR91305() throws Exception {
		IUIContext ui = getUI();
		registerPerspectiveConfirmationHandler();
	    switchToCPerspective(); //in case previous test left in wrong perspective
		deleteProject("LB");
		createNewProject("LB");
		this.createSourceFile("LB","lb.c",
				"[" + ARROW_RIGHT + "\b\n");
		ui.contextClick(computeTreeItemLocator("LB/lb.c"), "Delete");
		ui.wait(new ShellShowingCondition(this.isEclipse3_3()?"Confirm Resource Delete":"Delete Resources"));
		ui.click(new ButtonLocator(this.isEclipse3_3()?"&Yes":"OK"));
		ui.wait(new ShellDisposedCondition("Confirm Resource Delete"));
		ui.wait(new ShellDisposedCondition("Progress Information"));
		EclipseUtil.closeAllEditors();
		deleteProject("LB");
	}

}