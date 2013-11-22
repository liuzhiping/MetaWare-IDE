package com.arc.cdt.tests.A;
import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import junit.framework.Assert;

import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.locator.XYLocator;
import com.windowtester.runtime.swt.locator.CTabItemLocator;

public class T_2_36 extends UIArcTestCaseSWT {
    public static final String DESCRIPTION = "Test that when a header file is changed, appropriate source files are rebuilt.";
    public static final String CATEGORY = BUILD_MANAGEMENT;
	/**
	 * Main test method.
	 */
	public void testT_2_36() throws Exception {
	    switchToCPerspective(); //in case previous test left in wrong perspective
		String console;
		IUIContext ui = getUI();
		cleanProject("HDR_T2_36");
		buildProject("HDR_T2_36");
		// choose edit view for header file, edit it, then save
		ui.click(2, computeTreeItemLocator("HDR_T2_36.*/T2_36.h.*"));
		ui.wait(milliseconds(1000)); // it may not be there yet
		ui.keyClick(WT.CTRL, 'A');
		ui.enterText("extern int changed;");
		ui.keyClick(WT.CR);
		ui.enterText("extern int all;");
		ui.keyClick(WT.CR);
		ui.keyClick(WT.CTRL, 'S');
		buildProject("HDR_T2_36");
		// Look for build info in console output
		ui.wait(milliseconds(2000));
		console = getBuildConsoleContent();
		if((console.indexOf("Finished building: ../T2_36_chg1.c")<0) ||
		   (console.indexOf("Finished building: ../T2_36_chg2.c")<0) ||
		   (console.indexOf("Finished building: ../main2_36.c")>=0)) {
            writeStringToFile("T_2_36.txt",console);
            Assert.assertTrue(false);
		}
		// choose edit view for header file and edit it to original state then save
		ui.click(2, computeTreeItemLocator("HDR_T2_36.*/T2_36.h.*"));
		ui.keyClick(WT.CTRL, 'A');
		ui.enterText("extern int change;");
		ui.keyClick(WT.CR);
		ui.enterText("extern int all;");
		ui.keyClick(WT.CR);
		ui.keyClick(WT.CTRL, 'S');
		// close view
		ui.click(new XYLocator(new CTabItemLocator("T2_36.h"), 73, 11));
	}

}