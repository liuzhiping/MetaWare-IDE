package com.arc.cdt.tests.deprecated;

import junit.framework.Assert;

import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.locator.WidgetReference;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.CTabItemLocator;

public class T_CR94034 extends UIArcTestCaseSWT {

	public static final String DESCRIPTION = "Confirm that the Browse for a VDK configuration file uses the correct pattern.";
	public static final String CATEGORY = DEBUGGER_INTEGRATION;
	private static final String LAUNCH_NAME = "Graphics pipeline";

	private static boolean VDK_SUPPORT = false;

	/**
	 * Main test method.
	 */
	public void testT_CR94034() throws Exception {
		if (VDK_SUPPORT) {

			switchToCPerspective();
			this.bringUpDebugLaunchDialog(new IUIRunnable() {

				@Override
				public void run(IUIContext ui) throws WidgetSearchException {
					ui.click(computeTreeItemLocator("C\\/C\\+\\+ [LM].*/"
							+ LAUNCH_NAME));
					ui
							.click(new CTabItemLocator(
									"CMPD Debugger Configuration"));
					ui.click(new ButtonLocator("Search Project..."));
					ui.wait(new ShellShowingCondition("File Selection"));
					compareWidget("T_CR94034.1", new WidgetReference<Object>(ui
							.getActiveWindow()));
					ui.click(new ButtonLocator("Cancel"));
					ui.wait(new ShellDisposedCondition("File Selection"));
					ui.click(new ButtonLocator("Close"));
				}
			});
		}
		else Assert.assertTrue("VDK support suspended",false);

	}

}