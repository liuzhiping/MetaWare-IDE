package com.arc.cdt.tests.A;
import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.locator.XYLocator;
import com.windowtester.runtime.swt.locator.CTabItemLocator;

public class T_4_1 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that input can be read from the console as standard input.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    private static final String PROJECT_NAME = "Read_stdin";
	/**
	 * Main test method.
	 */
	public void testT_4_1() throws Exception {
		registerPerspectiveConfirmationHandler();
	    switchToCPerspective(); //in case previous test left in wrong perspective
		
		IUIContext ui = getUI();
		cleanProject(PROJECT_NAME);
		buildProject(PROJECT_NAME);
		runProject(PROJECT_NAME);
		showView(CONSOLE_VIEW_ID);
		
		ui.click(new XYLocator(new CTabItemLocator("Console"), 44, 12));
		
		//ui.enterText("Howza123"); // this does NOT work and is replaced by:
		ui.wait(milliseconds(500));
		ui.keyClick('H');
		ui.keyClick('o');
		ui.keyClick('w');
		ui.keyClick('z');
		ui.keyClick('a');
		ui.keyClick('1');
		ui.keyClick('2');
		ui.keyClick('3');
		
		ui.keyClick(WT.CR);
		ui.wait(milliseconds(2000));
		compareApplicationConsole("T_4_1.1");
	}

}