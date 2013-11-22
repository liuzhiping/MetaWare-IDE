package com.arc.cdt.tests.A;
import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import org.eclipse.swt.widgets.Button;

import com.arc.cdt.testutil.CDTUtil;
import com.arc.cdt.testutil.IToolOptionSetting;
import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.IUIToolOptionSetter;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.CTabItemLocator;
import com.windowtester.runtime.swt.locator.FilteredTreeItemLocator;
import com.windowtester.runtime.swt.locator.LabeledLocator;
import com.windowtester.runtime.swt.locator.MenuItemLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;
import com.windowtester.runtime.swt.locator.TableItemLocator;

public class T_2_63 extends UIArcTestCaseSWT {
    private static final String PROJECT_NAME = "A_PROJECT";
    public static final String DESCRIPTION = "Launch debugger against ARCompact project and specify \"-Xxy -on=sim_xy_display\" on command line and confirm that there are two new display types: XY Memory and Simulator XY Memory.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
	/**
	 * Main test method.
	 */
	public void testT_2_63() throws Exception {
	    switchToCPerspective(); //in case previous test left in wrong perspective
		deleteProject(PROJECT_NAME);
		createNewProject(PROJECT_NAME);
		this.createSourceFile(PROJECT_NAME,"test.c", 
		    "void main(){\n" +
		    "printf(\"Hello\\n" + ARROW_RIGHT + ARROW_RIGHT + ";\n");
		this.bringUpBuildSettings(PROJECT_NAME, new IUIToolOptionSetter(){

			@Override
			public void run(IUIContext ui, IToolOptionSetting settings)
					throws WidgetSearchException {
				CDTUtil.setCompilerSwitch("-Xxy",settings,true);
				
			}});
		this.buildProject(PROJECT_NAME);
		this.bringUpDebugLaunchDialog(new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws WidgetSearchException {
                ui.contextClick(new FilteredTreeItemLocator("C\\/C\\+\\+ Application"), "New");
                ui.click(new LabeledLocator(Button.class, "&Project:"));
                ui.wait(new ShellShowingCondition("Project Selection"));
                ui.click(new TableItemLocator(PROJECT_NAME));
                ui.click(new ButtonLocator("OK"));
                ui.wait(new ShellDisposedCondition("Project Selection"));
                ui.click(new LabeledLocator(Button.class, "C/C++ Application:"));
                ui.wait(new ShellShowingCondition("Program Selection"));
                ui.click(new TableItemLocator(PROJECT_NAME + ".elf"));
                ui.click(new ButtonLocator("OK"));
                ui.wait(new ShellDisposedCondition("Program Selection"));
                ui.click(new CTabItemLocator("Debugger"));
                ui.click(computeTreeItemLocator("Command-Line Options"));
                ui.click(new NamedWidgetLocator("cmd_line_option"));
                ui.keyClick(WT.CTRL,'A');
                ui.enterText("-Xxy -on=sim_xy_display");
                ui.click(new ButtonLocator("Appl&y"));
                ui.click(new ButtonLocator("&Debug")); 
                T_2_63.this.dealWithDebugPerspectiveConfirmation(true);
                T_2_63.this.waitForDebugPerspective();
            }});
		
		getUI().click(new MenuItemLocator("Debugger/Simulator XY Memory"));
		getUI().wait(milliseconds(500)); // Doesn't work unless we pause (at least on Windows)
		getUI().click(new MenuItemLocator("Debugger/XY Memory"));
		this.terminateDebugger();
		this.resetPerspective();
		deleteProject(PROJECT_NAME);
		
	}

}