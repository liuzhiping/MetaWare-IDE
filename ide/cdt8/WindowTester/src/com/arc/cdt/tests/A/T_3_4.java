package com.arc.cdt.tests.A;
import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.locator.IWidgetReference;
import com.windowtester.runtime.locator.XYLocator;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.CTabItemLocator;
import com.windowtester.runtime.swt.locator.MenuItemLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.TreeItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ContributedToolItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;

public class T_3_4 extends UIArcTestCaseSWT {
    public static final String DESCRIPTION = "Confirm that Expression view properly updates aggregates.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
	/**
	 * Main test method.
	 */
	public void testT_3_4() throws Exception {
		registerPerspectiveConfirmationHandler();
	    switchToCPerspective(); //in case previous test left in wrong perspective
		IUIContext ui = getUI();
		buildProject("Queens_AC");
		invokeDebuggerFor("Queens_AC",true);
		ui.click(new MenuItemLocator("Window/Show View/Other..."));
		ui.wait(new ShellShowingCondition("Show View"));
		ui.click(new TreeItemLocator("Debug/Expressions"));
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Show View"));
		ui.contextClick(new SWTWidgetLocator(Tree.class, new ViewLocator(
			"org.eclipse.debug.ui.ExpressionView")),
			"&Add Watch Expression...");
		// Add an expression so it is safe to "Remove All"
		ui.wait(new ShellShowingCondition("Add Watch Expression"));
		ui.enterText("A[0]");
		ui.click(new ButtonLocator("OK"));
		// Remove all expressions
		ui.contextClick(new SWTWidgetLocator(Tree.class, new ViewLocator(
			"org.eclipse.debug.ui.ExpressionView")), "Remove A&ll");
		ui.wait(new ShellShowingCondition("Remove All Expressions"));
		ui.click(new ButtonLocator("&Yes"));
		ui.wait(new ShellDisposedCondition("Remove All Expressions"));
		// Now add the expressions we want
		ui.contextClick(new SWTWidgetLocator(Tree.class, new ViewLocator(
			"org.eclipse.debug.ui.ExpressionView")),
			"&Add Watch Expression...");
		ui.enterText("A[0]");
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Add Watch Expression"));
		ui.contextClick(new SWTWidgetLocator(Tree.class, new ViewLocator(
			"org.eclipse.debug.ui.ExpressionView")),
			"&Add Watch Expression...");
		ui.wait(new ShellShowingCondition("Add Watch Expression"));
		ui.enterText("B[0]");
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Add Watch Expression"));
		ui.contextClick(new SWTWidgetLocator(Tree.class, new ViewLocator(
			"org.eclipse.debug.ui.ExpressionView")),
			"&Add Watch Expression...");
		ui.wait(new ShellShowingCondition("Add Watch Expression"));
		ui.enterText("A[0]&&B[0]");
		ui.wait(milliseconds(500));
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Add Watch Expression"));
		ui.wait(milliseconds(500));
		ui.click(new ContributedToolItemLocator(
			"org.eclipse.debug.ui.commands.StepInto"));
		ui.click(new XYLocator(new CTabItemLocator("Expressions"), 5, 5));
		
		IWidgetReference e = EclipseUtil.findView("org.eclipse.debug.ui.ExpressionView");
		
		EclipseUtil.computeTreeItemLocator(ui, "\"A[0]\"|0 (False)",(Control)e.getWidget());
	    EclipseUtil.computeTreeItemLocator(ui, "\"B[0]\"|0 (False)",(Control)e.getWidget());
	    EclipseUtil.computeTreeItemLocator(ui, "\"A[0]&&B[0]\"|0",(Control)e.getWidget());

		for (int i = 0; i < 5; i++) {
			this.clickStepIntoButton();
		}
		ui.click(new XYLocator(new CTabItemLocator("Expressions"), 5, 5));
		
		EclipseUtil.computeTreeItemLocator(ui, "\"A[0]\"|1 (True)",(Control)e.getWidget());
        EclipseUtil.computeTreeItemLocator(ui, "\"B[0]\"|0 (False)",(Control)e.getWidget());
        EclipseUtil.computeTreeItemLocator(ui, "\"A[0]&&B[0]\"|0",(Control)e.getWidget());
        
		ui.click(new ContributedToolItemLocator(
			"org.eclipse.debug.ui.commands.StepInto"));
		ui.click(new XYLocator(new CTabItemLocator("Expressions"), 5, 5));
		EclipseUtil.computeTreeItemLocator(ui, "\"A[0]\"|1 (True)",(Control)e.getWidget());
        EclipseUtil.computeTreeItemLocator(ui, "\"B[0]\"|1 (True)",(Control)e.getWidget());
        IWidgetReference w = (IWidgetReference)EclipseUtil.computeTreeItemLocator(ui, "\"A[0]&&B[0]\"|1",(Control)e.getWidget());
		ui.contextClick(w,"Remove All");
		ui.wait(new ShellShowingCondition("Remove All Expressions"));
		ui.click(new ButtonLocator("&Yes"));
		ui.wait(new ShellDisposedCondition("Remove All Expressions"));
		ui.click(new XYLocator(new CTabItemLocator("Expressions"), 93, 9));
		terminateDebugger();
		ui.click(new SWTWidgetLocator(ToolItem.class, "C/C++"));
	}

}