package com.arc.cdt.tests.B;

import junit.framework.Assert;

import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;

public class T_CR91602 extends UIArcTestCaseSWT {
	 public static final String DESCRIPTION = "Confirm that unresolved reference is diagnosed and that no executable is created.";
	 public static final String CATEGORY = BUILD_MANAGEMENT;
	/**
	 * Main test method.
	 */
	public void testT_CR91602() throws Exception {
		registerPerspectiveConfirmationHandler();
	    switchToCPerspective(); //in case previous test left in wrong perspective
		IUIContext ui = getUI();
		buildProject("NoMain");
		String console = getBuildConsoleContent();
		if(console.indexOf("Unresolved Symbol:")<0) {
		    writeStringToFile("T_CR91602.txt", console);
	        Assert.assertTrue(false);
		}
		ui.contextClick(computeTreeItemLocator("NoMain.*"),
			"Debug As/1 Local C\\/C\\+\\+ Application");
		ui.wait(new ShellShowingCondition("Application Launcher"));
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Application Launcher"));
		ui.contextClick(computeTreeItemLocator("NoMain.*"),
			"Run As/1 Local C\\/C\\+\\+ Application");
		ui.wait(new ShellShowingCondition("Application Launcher"));
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Application Launcher"));
	    switchToCPerspective(); //in case previous test left in wrong perspective
	}

}