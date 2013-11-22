package com.arc.cdt.tests.B;

import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.swt.locator.MenuItemLocator;

public class T_CR91129 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm the existence of the \"Refresh displays\" menu item.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
	/**
	 * Main test method.
	 */
	public void testT_CR91129() throws Exception {
	    switchToCPerspective(); //in case previous test left in wrong perspective
		IUIContext ui = getUI();
		buildProject("Test1");
		invokeDebuggerFor("Test1",true);
		ui.click(new MenuItemLocator("Debugger/Refresh displays"));
		terminateDebugger();
	    switchToCPerspective();
	}

}