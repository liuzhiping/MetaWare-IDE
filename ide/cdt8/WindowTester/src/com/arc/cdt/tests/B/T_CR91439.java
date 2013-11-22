package com.arc.cdt.tests.B;

import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.locator.XYLocator;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.LabeledTextLocator;

public class T_CR91439 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Test \"Display as Array\" feature of Variables view";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
	private static final String PROJECT = "Array1";

	/**
	 * Main test method.
	 */
	public void testT_CR91439() throws Exception {
	    switchToCPerspective(); //in case previous test left in wrong perspective
	    deleteProject(PROJECT);
	    createNewProject(PROJECT);
		IUIContext ui = getUI();
		createSourceFile(PROJECT,"array.c",
				"#include <stdlib.h>\n" +
				"\nint main() {\n" +
				"int *a, k;\n" +
				"a = malloc(10*sizeof(int" + ARROW_RIGHT + ARROW_RIGHT +";\n" +
				"for (k=0; k < 10; k++" + ARROW_RIGHT +"\n" +
				"a[k] = k;\n" +
				"return 0;");
		
		buildProject(PROJECT);
		invokeDebuggerFor(PROJECT,true);
		this.clickStepIntoButton();
		this.clickStepIntoButton();
		this.showView(VARIABLE_VIEW_ID);
		ui.contextClick(computeTreeItemLocator("a"), "Display As Array...");
		ui.wait(new ShellShowingCondition("Display As Array"));
		ui.click(new XYLocator(new LabeledTextLocator("Length"), 37, 8));
		ui.keyClick(WT.CTRL, 'A');
		ui.enterText("9");
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Display As Array"));
		
		this.clickStepIntoButton();
		this.clickStepIntoButton();
		this.clickStepIntoButton();
		this.clickStepIntoButton();
		this.clickStepIntoButton();
		this.clickStepIntoButton();
		this.clickStepIntoButton();
		this.clickStepIntoButton();
		this.clickStepIntoButton();
		this.clickStepIntoButton();
		this.clickStepIntoButton();
		this.clickStepIntoButton();
		this.clickStepIntoButton();
		this.clickStepIntoButton();
		/* FIXME - click to expand array is not recorded!!!
		 *         need API call to do this.
		 *         For now only testing first entry.
		 */
		ui.click(computeTreeItemLocator("a/.*a\\[0\\]"));
		/* FIXME - remove comment, i.e. make statements active,
		 *         when above FIXME done
		ui.click(computeTreeItemLocator("a/a[1]"));
		ui.click(computeTreeItemLocator("a/a[2]"));
		ui.click(computeTreeItemLocator("a/a[3]"));
		ui.click(computeTreeItemLocator("a/a[4]"));
		ui.click(computeTreeItemLocator("a/a[5]"));
		ui.click(computeTreeItemLocator("a/a[6]"));
		ui.click(computeTreeItemLocator("a/a[7]"));
		 */
		terminateDebugger();
	    switchToCPerspective();
	    //deleteProject("Array");
	}

}