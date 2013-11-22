package com.arc.cdt.tests.B;

import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import junit.framework.Assert;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.locator.IWidgetReference;

public class T_CR96189 extends UIArcTestCaseSWT {
    public static final String DESCRIPTION = "Confirm that things look okay when launching a project without \"Resume at startup\" checked.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
	/**
	 * Main test method.
	 */
	public void testCR96189() throws Exception {
		IUIContext ui = getUI();
		
		this.switchToDebugPerspective();
		
		launchDebugger("Queens_AC_no_resume",false);
		
		this.waitUntilDebuggerStops(10000);
		IWidgetReference stepInto = (IWidgetReference)ui.find(this.getStepIntoLocator());
		IWidgetReference stepOver = (IWidgetReference)ui.find(this.getStepOverLocator());
		IWidgetReference restart = (IWidgetReference)ui.find(this.getRestartLocator());
		
		Assert.assertTrue("StepInto enabled",EclipseUtil.isEnabled(stepInto));
        Assert.assertTrue("StepOver enabled",EclipseUtil.isEnabled(stepOver));

        Assert.assertTrue("Relaunch enabled",EclipseUtil.isEnabled(restart));
		
		this.clickRestartButton();
		ui.wait(milliseconds(300));
		ui.click(EclipseUtil.computeTreeItemLocator(ui,
						"Queens_AC_no_resume ?\\[C\\/C\\+\\+ .*Application\\]/MetaWare Debugger \\([^)]*\\) \\(Suspended\\)/Thread \\[main thread\\] \\(Suspended\\)"
						));
		ui.wait(milliseconds(300));
		Assert.assertTrue("StepInto enabled",EclipseUtil.isEnabled(stepInto));
		Assert.assertTrue("StepOver enabled",EclipseUtil.isEnabled(stepOver));

		Assert.assertTrue("Relaunch enabled",EclipseUtil.isEnabled(restart));
		
		this.terminateDebugger();
	}

}