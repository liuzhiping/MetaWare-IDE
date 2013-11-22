package com.arc.cdt.tests.B;

import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import junit.framework.Assert;

import org.eclipse.swt.custom.StyledText;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;

public class T_CR91888 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Test that standard input can be read from Console.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    static final String PROJECT_NAME = "SCANF";
	/**
	 * Main test method.
	 */
	public void testT_CR91888() throws Exception {
	    switchToCPerspective(); //in case previous test left in wrong perspective
	    deleteProject(PROJECT_NAME);
	    createNewProject(PROJECT_NAME);
	    createSourceFile(PROJECT_NAME,"scanf.c",
	    		"#include <stdio.h>\n\n" +
	    		"int main() { int a;\n\t" +
	    		"scanf(\"%d\",&a" + ARROW_RIGHT + ";\n" +
	    		"printf(\"%d\\n\", a" + ARROW_RIGHT+";\n"+
	    		"return 0;");
		IUIContext ui = getUI();
		
		buildProject(PROJECT_NAME);
		invokeDebuggerFor(PROJECT_NAME,true);
		this.clickResumeButton();
	
		ui.wait(milliseconds(3000)); // allow time for console to switch
	    getApplicationConsoleContent();
		ui.click(new SWTWidgetLocator(StyledText.class, new ViewLocator(
			CONSOLE_VIEW_ID)));
		ui.wait(milliseconds(2000)); // allow time for console to get focus
		ui.enterText("57");
		ui.keyClick(WT.CR);
		ui.wait(milliseconds(2000));
		String console = getApplicationConsoleContent();
		// verify output is correct
		if(!console.equals("57" + NEWLINE + "57\n")) {
			this.writeStringToFile("T_CR91888.txt", console);
			Assert.assertTrue(false);
		}
		terminateDebugger();
	    switchToCPerspective(); //in case previous test left in wrong perspective
	    EclipseUtil.closeAllEditors();
	    deleteProject(PROJECT_NAME);
	}

}