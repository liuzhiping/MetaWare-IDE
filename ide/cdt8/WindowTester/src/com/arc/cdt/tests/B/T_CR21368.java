package com.arc.cdt.tests.B;

import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import junit.framework.Assert;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.locator.IWidgetLocator;

public class T_CR21368 extends UIArcTestCaseSWT {
    public static final String DESCRIPTION = "Confirm that individual source files can be compiled.";
    public static final String CATEGORY = BUILD_MANAGEMENT;
	/**
	 * Main test method.
	 */
	public void testT_CR21368() throws Exception {
		
	    switchToCPerspective(); //in case previous test left in wrong perspective
		IUIContext ui = getUI();
		EclipseUtil.prepareToBuild("queens.c");
		IWidgetLocator treeItem = this.computeTreeItemLocator(computeProjectNameString("Queens_AC") +"/"+
		    "queens.c.*");
		ui.contextClick(treeItem,"Build Selected File(s)");
		EclipseUtil.waitUntilBuildCompleted("queens.c", 20000);
		ui.wait(milliseconds(5000)); //wait for console to finish populating
		String console = getBuildConsoleContent();
		if((console.indexOf("Rebuilding selected file(s)")<0) ||
		   (console.indexOf("queens.c")<0) ||
		   (console.indexOf("Build of selected resources is complete.")<0)) {
			writeStringToFile("cr21368.txt", console);
			Assert.assertTrue(false);
		}
	}

}