package com.arc.cdt.tests.A;
import org.eclipse.swt.widgets.Control;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.locator.XYLocator;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.CTabItemLocator;
import com.windowtester.runtime.swt.locator.TreeItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;

public class T_2_62 extends UIArcTestCaseSWT {
    public static final String DESCRIPTION = "Test that handling of bogus exe file referenced from Launch Configuration Dialog.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
	/**
	 * Main test method.
	 */
	public void testT_2_62() throws Exception {
	    switchToCPerspective(); //in case previous test left in wrong perspective
		IUIContext ui = getUI();
		ui.contextClick(new TreeItemLocator(computeProjectNameString("Bad_exe"), new ViewLocator(
				PROJECT_VIEW_ID)),
				"Run As/1 Local C\\/C++ Application");
		ui.wait(new ShellShowingCondition("Application Launcher"));
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Application Launcher"));
		ui.contextClick(new TreeItemLocator(computeProjectNameString("Bad_exe"), new ViewLocator(
				PROJECT_VIEW_ID)),
				"Run As/" + RUN_CONFIG_SELECTION_NAME);
		ui.wait(new ShellShowingCondition(RUN_CONFIG_DIALOG_TITLE));
		ui.click(computeTreeItemLocator(
				"C\\/C\\+\\+ .*Application/Bad_exe Debug"));
		
		CTabItemLocator cTabItemLocator = new CTabItemLocator("Debugger");
		IWidgetLocator cTab[] = ui.findAll(cTabItemLocator);
		if (cTab.length != 1){
			EclipseUtil.dumpControl((Control)ui.getActiveWindow());
		}
		else
		    ui.click(new XYLocator(cTab[0], 55, 8));
		ui.click(new ButtonLocator("Close"));
		ui.wait(new ShellDisposedCondition(RUN_CONFIG_DIALOG_TITLE));
	}

}