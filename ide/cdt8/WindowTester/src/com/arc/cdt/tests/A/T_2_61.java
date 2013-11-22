package com.arc.cdt.tests.A;
import junit.framework.Assert;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.ComboItemLocator;
import com.windowtester.runtime.swt.locator.TreeItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;

public class T_2_61 extends UIArcTestCaseSWT {
    
    public static final String DESCRIPTION = "Test that user-specified source extensions work as expected.";
    public static final String CATEGORY = PROJECT_MANAGEMENT;

    private static final String PROJECT = "new_extensions";
    private static final String C_SOURCE_CONTENT = 
        "#include <stdio.h>\n" +
        "\n" +
        "int main() {\n" +
        "    printf(\"Hello!\\n\");\n" +
        "return 0;\n}";
    
	/**
	 * Main test method.
	 */
	public void testT_2_61() throws Exception {
		String console;
	    switchToCPerspective(); //in case previous test left in wrong perspective
	    EclipseUtil.closeAllEditors(); // If we run this multiple times, we have issues
	    deleteProject(PROJECT);
		IUIContext ui = getUI();
		// create new project
		createNewProject(PROJECT);
		createTextFile(PROJECT,"main.cj",C_SOURCE_CONTENT,false);
		
		
		// Create new Assembly source file with unspecified extension *.aj
		createTextFile(PROJECT,"foo.aj","nop",false);
		
		// clean, build and verify failure (in multiple ways)
		cleanProject(PROJECT);
		buildProject(PROJECT);
		console = getBuildConsoleContent();
		if(console.indexOf("Nothing to build for project new_extensions")<0) {
		    writeStringToFile("T_2_61a.txt",console);
	        Assert.assertTrue(false);
		}
		// show project fails to run (no binary)
		runProject(PROJECT);
		ui.wait(new ShellShowingCondition("Application Launcher"));
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Application Launcher"));
		
		// Define new "C Source File" extension: *.cj
		this.bringUpPropertiesDialog(PROJECT, new IUIRunnable(){

            @Override
            public void run (IUIContext ui1) throws WidgetSearchException {
                ui1.click(computeTreeItemLocator("C\\/C++ General/File Types"));
                ui1.click(new ButtonLocator("Use project settings"));
                ui1.click(new ButtonLocator("New..."));
                ui1.wait(new ShellShowingCondition("C/C++ File Type"));
                ui1.enterText("*.cj");
                ui1.click(new ComboItemLocator("C Source File"));
                ui1.click(new ButtonLocator("OK"));
                ui1.wait(new ShellDisposedCondition("C/C++ File Type"));
                // Define new "Assembly Source File" extension: *.aj
                ui1.click(new ButtonLocator("New..."));
                //ui.wait(new ShellDisposedCondition("C/C++ File Type")); // WT error!!!
                ui1.wait(new ShellShowingCondition("C/C++ File Type"));
                ui1.enterText("*.aj");
                ui1.click(new ComboItemLocator("Assembly Source File"));
                ui1.click(new ButtonLocator("OK"));
                
            }});

		// clean, build again (succeeds this time)
		cleanProject(PROJECT);
		buildProject(PROJECT);
		console = getBuildConsoleContent();
		// verify foo.aj was assembled
		if(console.indexOf("Finished building: ../foo.a")<0) {
		    writeStringToFile("T_2_61foo.txt",console);
	        Assert.assertTrue(false);
		}
		// verify main.cj was compiled
		if(console.indexOf("Finished building: ../main.cj")<0) {
		    writeStringToFile("T_2_62main.txt",console);
	        Assert.assertTrue(false);
		}
		// run and show run succeeds
		runProject(PROJECT);
		this.waitForLaunchTermination(15000);
		console = getApplicationConsoleContent();
		if(console.indexOf("Hello!")<0) {
		    writeStringToFile("T_2_61hello.txt",console);
	        Assert.assertTrue(false);
		}
		// delete new project using these extensions
		ui.click(new TreeItemLocator(PROJECT, new ViewLocator(
			"org.eclipse.ui.navigator.ProjectExplorer")));
	    deleteProject(PROJECT);
	}

}