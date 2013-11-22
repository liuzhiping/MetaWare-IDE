package com.arc.cdt.tests.B;

import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import junit.framework.Assert;

import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;

public class T_CR92063 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that internal builder honors dependencies";
    public static final String CATEGORY = BUILD_MANAGEMENT;
	/**
	 * Main test method.
	 */
	public void testT_CR92063() throws Exception {
	    switchToCPerspective(); //in case previous test left in wrong perspective
		IUIContext ui = getUI();
		buildProject("Dependency");
		ui.click(2, computeTreeItemLocator("Dependency.*"));
		ui.click(2, computeTreeItemLocator("Dependency.*/dep2.c.*"));
		ui.keyClick(WT.CTRL, 'A');
		ui.keyClick(WT.BS);
		ui.enterText("int dep(int z");
		ui.keyClick(WT.ARROW_RIGHT);
		ui.enterText(" {");
		ui.keyClick(WT.CR);
		ui.enterText("return z>>1;");
		ui.keyClick(WT.CTRL, 'S');
		ui.wait(milliseconds(3000));
		buildProject("Dependency");
		String console = getBuildConsoleContent();
		if((console.indexOf("Finished building: ../dep1.c")>=0) ||
		   (console.indexOf("Finished building: ../dep2.c")<0) ||
		   (console.indexOf("Finished building target: Dependency.elf")<0)) {
		    writeStringToFile("T_CR92063.txt", console);
	        Assert.assertTrue(false);
		}
	}

}