package com.arc.cdt.tests.A;
import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.swt.locator.eclipse.ContributedToolItemLocator;

public class T_2_58 extends UIArcTestCaseSWT {
    public static final String DESCRIPTION = "Confirm that selecting \"instruction step\" mode activates Disassembly view.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
	/**
	 * Main test method.
	 */
	public void testT_2_58() throws Exception {
		registerPerspectiveConfirmationHandler();
	    switchToCPerspective(); //in case previous test left in wrong perspective
		IUIContext ui = getUI();
		launchDebugger("Test1.elf",true);
		ui.click(EclipseUtil.computeTreeItemLocator(ui,"Test.*/MetaWare.*/Thread.*/0.*main.c.*"));
		//ui.click(new MenuItemLocator("Debugger/Disassembly")); // make sure Disassembly view is open
		//ui.click(new XYLocator(new CTabItemLocator("Disassembly"), 103, 11)); // close Disassembly view
		ui.click(new ContributedToolItemLocator(
			"org.eclipse.cdt.debug.internal.ui.actions.ToggleInstructionStepModeActionDelegate"));
		// verify Disassembly view is now present
		ui.click(new ContributedToolItemLocator("disasm.button.breakpoint"));
		terminateDebugger();
	    switchToCPerspective();
	}

}