package com.arc.cdt.tests.B;

import junit.framework.Assert;

import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.TableItemLocator;

public class T_CR91914 extends UIArcTestCaseSWT {
	 public static final String DESCRIPTION = "Confirm that there is no 'Unknown' entries in the Discovery Profile table.";
	 public static final String CATEGORY = PROJECT_MANAGEMENT;
	/**
	 * Main test method.
	 */
	public void testT_CR91914() throws Exception {
		registerPerspectiveConfirmationHandler();
	    switchToCPerspective(); //in case previous test left in wrong perspective
		IUIContext ui = getUI();
		this.rightClickProjectMenu("Test1","Properties");
		ui.wait(new ShellShowingCondition("Properties for Test1"));
		ui.click(computeTreeItemLocator("C\\/C++ Build/Discovery [Oo]ptions"));
		IWidgetLocator refs[] = ui.findAll(new TableItemLocator("Unknown"));
		Assert.assertTrue(refs.length==0);
		ui.click(new ButtonLocator("Cancel"));
		ui.wait(new ShellDisposedCondition("Properties for Test1"));
	}

}