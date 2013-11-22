package com.arc.cdt.tests.A;
import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import junit.framework.Assert;

import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.swt.locator.ButtonLocator;

public class T_3_1 extends UIArcTestCaseSWT {
    public static final String DESCRIPTION = "Test that individual source files and folders can be excluded from a build.";
    public static final String CATEGORY = PROJECT_MANAGEMENT;
	private static final String PROJECT = "Exclude";
	/**
	 * Main test method.
	 */
	public void testT_3_1() throws Exception {
	    switchToCPerspective(); //in case previous test left in wrong perspective
	    deleteProject(PROJECT);
	    this.createNewProject(PROJECT);
	    this.createSourceFolder(PROJECT,"F_INCLUD");
	    this.createSourceFolder(PROJECT,"F_EXCLUD");
		this.createSourceFile(PROJECT + "/F_INCLUD","include.c",
				"#include <stdio.h>\n" +
				"\n" + 
				"int main() {\n" + 
				"printf(\"Hello!\\n\"" + ARROW_RIGHT + ";\n" +
				"return 0;");
				

		this.createSourceFile(PROJECT +"/F_INCLUD","main2.c",
				"int main() {\n" +
				"return 0;");
		
		this.bringUpPropertiesDialog(PROJECT+"/F_INCLUD/main2.c", new IUIRunnable(){

			@Override
			public void run(IUIContext ui) throws WidgetSearchException {
				ui.click(computeTreeItemLocator("C\\/C++ Build"));
				ui.click(new ButtonLocator("Exclude resource from build"));
				ui.wait(milliseconds(500));
				ui.assertThat(new ButtonLocator("Exclude resource from build").isSelected());
				
			}});

		
		this.createSourceFile(PROJECT + "/F_EXCLUD", "main3.c",
		"int main() {\nreturn 0;");
		
		this.bringUpPropertiesDialog(PROJECT+"/F_EXCLUD", new IUIRunnable(){

			@Override
			public void run(IUIContext ui) throws WidgetSearchException {
				ui.click(new ButtonLocator("Exclude resource from build"));
				ui.wait(milliseconds(500));
				ui.assertThat(new ButtonLocator("Exclude resource from build").isSelected());
				
			}});		
		
		buildProject(PROJECT);
		this.runProject(PROJECT);
		
		this.waitForLaunchTermination(10000);
		
		// Sometimes the console delays in materializing.
		String console = getApplicationConsoleContent();
		if (console.length() < 5){
			for (int i = 0; i < 10; i++){
				getUI().wait(milliseconds(500));
				console = getApplicationConsoleContent();
				if (console.length() >= 5) break;
			}
		}
		
		Assert.assertTrue(getApplicationConsoleContent().indexOf("Hello") >= 0);
		// clean up
	    deleteProject(PROJECT);
	}

}