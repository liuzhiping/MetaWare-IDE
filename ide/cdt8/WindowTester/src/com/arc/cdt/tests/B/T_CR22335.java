package com.arc.cdt.tests.B;

import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;

public class T_CR22335 extends UIArcTestCaseSWT {
    public static final String DESCRIPTION = "Confirm that CDT Memory display updates";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
	/**
	 * Main test method.
	 */
	public void testT_CR22335() throws Exception {
	    setCanonicalSize();
	    switchToCPerspective(); //in case previous test left in wrong perspective
		IUIContext ui = getUI();
		cleanProject("Queens_AC");
		buildProject("Queens_AC");
		launchDebugger("Queens_AC.elf",true);
		ui.wait(milliseconds(1000));
		showView(MEMORY_VIEW_ID);
		ui.wait(milliseconds(1000));
		// make sure there is at least one Memory Monitor
		ui.click(findWidgetWithToolTip("Add Memory Monitor"));
		ui.wait(new ShellShowingCondition("Monitor Memory"));
		ui.enterText("&X");
		ui.click(new ButtonLocator("OK"));
		// Remove all Memory Monitors
		ui.click(findWidgetWithToolTip("Remove All"));
		ui.wait(new ShellShowingCondition("Remove All Memory Monitors"));
		ui.click(new ButtonLocator("&Yes"));
		ui.wait(new ShellDisposedCondition("Remove All Memory Monitors"));
		// Add desired Memory Monitor
		ui.click(findWidgetWithToolTip("Add Memory Monitor"));
		ui.wait(new ShellShowingCondition("Monitor Memory"));
		ui.enterText("&X");
		ui.click(new ButtonLocator("OK"));
		showView(COMMAND_VIEW_ID);
		this.enterDebugCommandLine("break !31");
		// Advance execution till memory view changes 
		this.clickResumeButton();
		this.clickResumeButton();
		this.clickStepIntoButton();
		ui.wait(milliseconds(3000));
		showView(MEMORY_VIEW_ID); // may be under Console tab
		ui.wait(milliseconds(1000)); // required to give display time to populate(?) Has old values otherwise.
		compareView("T_CR22335.1", MEMORY_VIEW_ID);
		terminateDebugger();
		switchToCPerspective();
	}

}