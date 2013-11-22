package com.arc.vdk.tests.VDK_First;

import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import junit.framework.Assert;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.locator.XYLocator;
import com.windowtester.runtime.swt.condition.eclipse.PerspectiveActiveCondition;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.CTabItemLocator;
import com.windowtester.runtime.swt.locator.ComboItemLocator;
import com.windowtester.runtime.swt.locator.FilteredTreeItemLocator;
import com.windowtester.runtime.swt.locator.LabeledLocator;
import com.windowtester.runtime.swt.locator.LabeledTextLocator;
import com.windowtester.runtime.swt.locator.MenuItemLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.TableItemLocator;
import com.windowtester.runtime.swt.locator.TreeItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ContributedToolItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;

public class V_1_2 extends UIArcTestCaseSWT {

    private static final String PROJECT_NAME = "pmp_test";
	/**
	 * Main test method.
	 */
	public void testV_1_2() throws Exception {
		IUIContext ui = getUI();
		ui.assertThat(new MenuItemLocator("Project/Build Automatically").isSelected(true));
		ui.click(new SWTWidgetLocator(ToolItem.class, "C/C++"));
	    //in case previous test left in wrong perspective
        if (!PerspectiveActiveCondition.forName("C/C++").test()) {
            changePerspective("C/C++");
            getUI().wait(milliseconds(1000));
        }
        resetPerspective(); // in case it was left in wrong state.
        EclipseUtil.fixProjectView(getUI()); // In case it is not scrolled to the left
		// Create the new project if it does not exist
		TreeItemLocator pe = new TreeItemLocator(PROJECT_NAME, new ViewLocator(
	      "org.eclipse.ui.navigator.ProjectExplorer"));
		if(!pe.isVisible(ui)) {
		    ui.click(new MenuItemLocator("File/New/Project..."));
		    ui.wait(new ShellShowingCondition("New Project"));
		    ui.click(new TreeItemLocator("VDK/Project"));
		    ui.click(new ButtonLocator("&Next >"));
		    ui.click(new LabeledTextLocator("&Project name:"));
		    ui.enterText(PROJECT_NAME);
		    ui.click(new ButtonLocator("Use &default location"));
		    ui.click(new LabeledTextLocator("&Location:"));
		    ui.enterText("C:/ARC/VDK/examples/" + PROJECT_NAME);
		    ui.click(new ButtonLocator("&Finish"));
		    ui.wait(new ShellDisposedCondition("VDK Project"));
		}
		// Take care of Project Properties
		ui.contextClick(pe, "Properties");
		ui.wait(new ShellShowingCondition("Properties for pmp_test"));
		ui.click(new FilteredTreeItemLocator("C\\/C++ Make Project"));
		// Make sure "Use default" check box is unchecked
		ButtonLocator chk1 = new ButtonLocator("Use default");
		if(chk1.isSelected(ui))
 	        ui.click(chk1); // is checked, clear it
		// Make sure "Build command:" is "make"
		ui.click(new XYLocator(new LabeledTextLocator("Build command:"), 111, 8));
		ui.keyClick(WT.CTRL, 'A');
		ui.enterText("make");
		// Make sure "Build on resource save (Auto Build)" check box is checked
		ButtonLocator chk2 = new ButtonLocator("Build on resource save (Auto Build)");
		if(!chk2.isSelected(ui))
 	        ui.click(chk2); // is not checked, set it
		// save and close Properties dialog
		ui.click(new ButtonLocator("&Apply"));
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Properties for pmp_test"));
		// Do a clean to force a build
		ui.click(new MenuItemLocator("Project/Clean..."));
		ui.wait(new ShellShowingCondition("Clean"));
		ui.click(new ButtonLocator("Clean projects &selected below"));
		ui.click(new TableItemLocator("pmp_test"));
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Clean"));
		ui.wait(new ShellDisposedCondition("Cleaning selected projects"));
		ui.wait(milliseconds(30000)); // wait till build is done
		ui.click(new XYLocator(new CTabItemLocator("Console"), 52, 12));
		// Verify console output shows build succeeded
		String console = getBuildConsoleContent();
		if(console.indexOf("*** Compilation is Done! ***")<0) {
		    writeStringToFile("V_1_2.txt", console);
	        Assert.assertTrue(false);
		}
		// set new internal terminal views for both wp0 and wp1
		ui.click(new MenuItemLocator("Run/Open Run Dialog..."));
		ui.wait(new ShellShowingCondition("Run"));
		// if wp0 exists delete it
		FilteredTreeItemLocator twp0 = 
			new FilteredTreeItemLocator("C\\\\/C++ Local Application/pmp_wp0");
		if(twp0.isVisible(ui)) {
			ui.contextClick(new FilteredTreeItemLocator(
				"C\\/C++ Local Application/pmp_wp0"), "Delete");
			ui.wait(new ShellShowingCondition(
				"Confirm Launch Configuration Deletion"));
			ui.click(new ButtonLocator("&Yes"));
			ui.wait(new ShellDisposedCondition(
				"Confirm Launch Configuration Deletion"));
		}
		// create wp0
		ui.contextClick(new FilteredTreeItemLocator(
			"C\\/C++ Local Application"), "New");
		ui.click(new XYLocator(new LabeledTextLocator("&Name:"), 74, 5));
		ui.keyClick(WT.CTRL, 'A');
		ui.enterText("pmp_wp0");
		ui.click(new LabeledTextLocator("&Project:"));
		ui.keyClick(WT.CTRL, 'A');
		ui.enterText("pmp_test");
		ui.click(new LabeledLocator(Button.class, "C/C++ Application:"));
		ui.wait(new ShellShowingCondition("Program Selection"));
		ui.click(new TableItemLocator("worker_process_0"));
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Program Selection"));
		ui.click(new ButtonLocator("Appl&y"));
		ui.click(new XYLocator(new CTabItemLocator("Debugger"), 41, 11));
		// set wp0 comm
		ui.click(computeTreeItemLocator(
			"Simulator Extensions/Terminal\\/COMM Simulator"));
		ButtonLocator ba0 = new ButtonLocator("None   ");
		if(!ba0.isSelected(ui)) {
			ui.click(ba0);
			ui.click(new ButtonLocator("Appl&y"));
		}
		ui.click(new ButtonLocator("Terminal Simulator   "));
		ui.click(new ComboItemLocator("1", new NamedWidgetLocator(
	    	"term_port")));
		ui.click(new ButtonLocator("Appl&y"));
		ui.wait(new ShellDisposedCondition("ARC600 CAS configuration"));
		ui.wait(new ShellDisposedCondition("ARC600 CAS user extensions"));
		ui.wait(new ShellDisposedCondition("ARC700 CAS configuration"));
		// ~~~ wp1 ~~~
		ui.click(new XYLocator(new CTabItemLocator("Main"), 29, 11));
		// if wp1 exists delete it
		FilteredTreeItemLocator twp1 = 
			new FilteredTreeItemLocator("C\\\\/C++ Local Application/pmp_wp1");
		if(twp1.isVisible(ui)) {
			ui.contextClick(new FilteredTreeItemLocator(
				"C\\/C++ Local Application/pmp_wp1"), "Delete");
			ui.wait(new ShellShowingCondition(
				"Confirm Launch Configuration Deletion"));
			ui.click(new ButtonLocator("&Yes"));
			ui.wait(new ShellDisposedCondition(
				"Confirm Launch Configuration Deletion"));
		}
		ui.contextClick(new FilteredTreeItemLocator(
			"C\\/C++ Local Application"), "New");
		ui.click(new XYLocator(new LabeledTextLocator("&Name:"), 74, 5));
		ui.keyClick(WT.CTRL, 'A');
		ui.enterText("pmp_wp1");
		ui.click(new LabeledTextLocator("&Project:"));
		ui.keyClick(WT.CTRL, 'A');
		ui.enterText("pmp_test");
		ui.click(new LabeledLocator(Button.class, "C/C++ Application:"));
		ui.wait(new ShellShowingCondition("Program Selection"));
		ui.click(new TableItemLocator("worker_process_1"));
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Program Selection"));
		ui.click(new ButtonLocator("Appl&y"));
		ui.click(new XYLocator(new CTabItemLocator("Debugger"), 41, 11));
		// set wp1 comm
		ui.click(computeTreeItemLocator(
			"Simulator Extensions/Terminal\\/COMM Simulator"));
		ButtonLocator ba1 = new ButtonLocator("None   ");
		if(!ba1.isSelected(ui)) {
			ui.click(ba1);
			ui.click(new ButtonLocator("Appl&y"));
		}
		ui.click(new ButtonLocator("Terminal Simulator   "));
		ui.click(new ComboItemLocator("2", new NamedWidgetLocator(
	    	"term_port")));
		ui.click(new ButtonLocator("Appl&y"));
		ui.click(new ButtonLocator("Close"));

		// Make sure Make Targets View is showing
		ui.click(new MenuItemLocator("Window/Show View/Other..."));
		ui.wait(new ShellShowingCondition("Show View"));
		ui.click(new FilteredTreeItemLocator("Make/Make Targets"));
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Show View"));
        // If Make Target "all" exists delete it
	    TreeItemLocator pt_all = new TreeItemLocator("pmp_test/all", 
	    	new ViewLocator("org.eclipse.cdt.make.ui.views.MakeView"));
		try {
			ui.click(pt_all);
		}
        catch (Exception e) {
            //Why is this ignored???
        }
		if(pt_all.isVisible(ui)) {
			ui.contextClick(pt_all, "Delete Make Target");
			ui.wait(new ShellShowingCondition("Confirm Target Deletion"));
			ui.click(new ButtonLocator("&Yes"));
			ui.wait(new ShellDisposedCondition("Confirm Target Deletion"));
		}
        // Create Make Target "all" for project
		ui.contextClick(pe, "Make targets/Create...");
		ui.wait(new ShellShowingCondition("Create a new Make target"));
		ui.click(new XYLocator(new LabeledTextLocator("Target Name:"), 3, 3));
		ui.keyClick(WT.CTRL, 'A');
		ui.enterText("all");
		ui.click(new XYLocator(new LabeledTextLocator("Make Target:"), 3, 3));
		ui.keyClick(WT.CTRL, 'A');
		ui.enterText("all");
		ui.click(new ButtonLocator("Create"));
		ui.wait(new ShellDisposedCondition("Create a new Make target"));

        // If Make Target "clean" exists delete it
	    TreeItemLocator pt_cln = new TreeItemLocator("pmp_test/clean", 
	    	new ViewLocator("org.eclipse.cdt.make.ui.views.MakeView"));
		try {
			ui.click(pt_cln);
		}
        catch (Exception e) {
            //Why is this ignored???
        }

		if(pt_cln.isVisible(ui)) {
			ui.contextClick(pt_cln, "Delete Make Target");
			ui.wait(new ShellShowingCondition("Confirm Target Deletion"));
			ui.click(new ButtonLocator("&Yes"));
			ui.wait(new ShellDisposedCondition("Confirm Target Deletion"));
		}
        // Create Make Target "clean" for project
		ui.contextClick(pe, "Make targets/Create...");
		ui.wait(new ShellShowingCondition("Create a new Make target"));
		ui.click(new XYLocator(new LabeledTextLocator("Target Name:"), 3, 3));
		ui.keyClick(WT.CTRL, 'A');
		ui.enterText("clean");
		ui.click(new XYLocator(new LabeledTextLocator("Make Target:"), 3, 3));
		ui.keyClick(WT.CTRL, 'A');
		ui.enterText("clean");
		ui.click(new ButtonLocator("Create"));
		ui.wait(new ShellDisposedCondition("Create a new Make target"));
		
        ui.contextClick(new TreeItemLocator(computeProjectNameString(PROJECT_NAME), 
            new ViewLocator(PROJECT_VIEW_ID)),"Debug As/Open Debug Dialog...");
		ui.wait(new ShellShowingCondition("Debug"));
		ui.click(new FilteredTreeItemLocator(
			"C\\/C++ Multiprocess Application"));
		ui.click(new SWTWidgetLocator(ToolItem.class, "", 0,
				new SWTWidgetLocator(ToolBar.class)));
		ui.click(new LabeledLocator(Button.class, "VDK Configuration File: "));
		ui.click(new ButtonLocator("OK"));
		ui.click(new ButtonLocator("&Debug"));
		ui.wait(new ShellDisposedCondition("Debug"));
		ui.click(new MenuItemLocator("Window/Open Perspective/Debug"));
        dealWithDebugPerspectiveConfirmation(true);
        waitForDebugPerspective();
		ui.click(new SWTWidgetLocator(ToolItem.class, "Debug"));
		/*
		ButtonLocator chk3 = new ButtonLocator("Start session with processes suspended");
		if(!chk3.isSelected(ui))
 	        ui.click(chk3); // is not checked, set it
		ui.click(new ButtonLocator("&Debug"));
		ui.wait(new ShellDisposedCondition("Debug"));
 	     */
	    ui.wait(milliseconds(7000)); // wait for debug perspective to come up
		ui.click(new SWTWidgetLocator(ToolItem.class, "Debug"));
		ui.click(new SWTWidgetLocator(Sash.class, 0, new SWTWidgetLocator(
			Composite.class)));
		ui.click(new ContributedToolItemLocator(
			"org.eclipse.debug.ui.commands.Resume"));
		ui.click(computeTreeItemLocator(
			".*C\\/C\\+\\+ Multiprocess Application.*/worker_process_0.*"));
		ui.click(computeTreeItemLocator(
			".*C\\/C\\+\\+ Multiprocess Application.*/worker_process_1.*"));
		// Check terminal output for each thread
		// ~~~ TBD ~~~
	}

}