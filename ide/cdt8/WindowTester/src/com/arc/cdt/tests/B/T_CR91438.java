package com.arc.cdt.tests.B;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;

public class T_CR91438 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Test \"Cast To Type\" feature of Variables view";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
	private static final String PROJECT = "CAST2TYPE";

	/**
	 * Main test method.
	 */
	public void testT_CR91438() throws Exception {
		registerPerspectiveConfirmationHandler();
	    switchToCPerspective(); //in case previous test left in wrong perspective
	    deleteProject(PROJECT);
	    createNewProject(PROJECT);
	    createSourceFile(PROJECT,"cast2type.c",
	    		"#include <stdlib.h>\n\n"+
	    		"int main() {\n" + 
	    		"int *a;\n" + 
	    		"a = malloc(40" + ARROW_RIGHT + ";\n" +
	    		"*(a+5) = 0;\n" + 
	    		"return 0;");
	    		
		IUIContext ui = getUI();
		
		buildProject(PROJECT);
		invokeDebuggerFor(PROJECT,true);
		
		this.clickStepIntoButton();
		this.clickStepIntoButton();
		showView(VARIABLE_VIEW_ID);
		
		ui.contextClick(computeTreeItemLocator("a"), "Cast To Type...");
		ui.wait(new ShellShowingCondition("Cast To Type"));
		ui.enterText("char *");
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Cast To Type"));
		terminateDebugger();
	    switchToCPerspective(); //in case previous test left in wrong perspective
	    EclipseUtil.closeAllEditors();
	    deleteProject(PROJECT);
	}

}