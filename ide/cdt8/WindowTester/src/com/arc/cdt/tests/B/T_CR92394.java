package com.arc.cdt.tests.B;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolItem;

import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.swt.locator.MenuItemLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;

public class T_CR92394 extends UIArcTestCaseSWT {
    public static final String DESCRIPTION = "Confirm that the Command-line view can be dismissed.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
	/**
	 * Main test method.
	 */
	public void testT_CR92394() throws Exception {
		registerPerspectiveConfirmationHandler();
	    switchToCPerspective(); //in case previous test left in wrong perspective
		IUIContext ui = getUI();
		buildProject("Test1");
		invokeDebuggerFor("Test1",true);
		ui.click(new MenuItemLocator("Debugger/Command-line input"));
		ui.click(new SWTWidgetLocator(Label.class, "", new ViewLocator(
				COMMAND_VIEW_ID)));
		terminateDebugger();
		ui.click(new SWTWidgetLocator(ToolItem.class, "C/C++"));
	}

}