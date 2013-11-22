package com.arc.cdt.tests.A;
import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import junit.framework.Assert;

import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.swt.locator.TableCellLocator;

public class T_2_25_1 extends UIArcTestCaseSWT {
    public static final String DESCRIPTION = "Test the change of a register value via the Register display.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
	/**
	 * Main test method.
	 */
	public void testT_2_25_1() throws Exception {
	    setCanonicalSize();
	    switchToCPerspective(); //in case previous test left in wrong perspective
		IUIContext ui = getUI();
		buildProject("Test1");
		invokeDebuggerFor("Test1",true);
		showView(REGISTER_VIEW_ID);
		showView(COMMAND_VIEW_ID);
		this.enterDebugCommandLine("eval r10");
		ui.wait(milliseconds(2000));
		String console = getDebuggerConsoleContent();
		// Find "r0 = 1" and verify no "0xfeedbeef"
		if(console.indexOf("0xfeedbeef")>=0) { // Fail
			writeStringToFile("T2_25.1.txt", console);
			Assert.assertTrue(false);
		}
		// Linux requires a single click, then double click
		// Linux location of R10 is in row 3; on Windows it is row 4.
		int row = 4;
		int col = 4;
		ui.click(1,new TableCellLocator(row,col));
		ui.click(2,new TableCellLocator(row,col));
		
		ui.wait(milliseconds(1000));
		// change register value
		ui.keyClick('F');
		ui.keyClick('e');
		ui.keyClick('e');
		ui.keyClick('d');
		ui.keyClick('B');
		ui.keyClick('e');
		ui.keyClick('e');
		ui.keyClick('f');
		ui.keyClick(WT.CR);
		this.enterDebugCommandLine("eval r10");
		ui.wait(milliseconds(1000));
		console = getDebuggerConsoleContent();
		if(console.indexOf("0xfeedbeef")<0) { // Fail if not present
		    writeStringToFile("T_2_25_1.txt",console);
	        Assert.assertTrue(false);
		}
		terminateDebugger();
		switchToCPerspective();
	}

}