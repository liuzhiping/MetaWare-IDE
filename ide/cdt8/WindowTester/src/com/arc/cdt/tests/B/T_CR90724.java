package com.arc.cdt.tests.B;

import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ToolItem;

import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.locator.XYLocator;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.CTabItemLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;

public class T_CR90724 extends UIArcTestCaseSWT {
	public static final String DESCRIPTION = "Test that debugger does something intelligent when attempting to set a breakpoint on line that has no statement.";
	public static final String CATEGORY = DEBUGGER_INTEGRATION;

	/**
	 * Main test method.
	 */
	public void testT_CR90724() throws Exception {
		switchToCPerspective(); // in case previous test left in wrong
								// perspective
		IUIContext ui = getUI();
		buildProject("BPinWS");
		invokeDebuggerFor("BPinWS", true);
		ui.click(new XYLocator(new CTabItemLocator("bpinws.c"), 32, 18));
		if (isEclipse3_3()) {
			ui
					.click(
							2,
							new XYLocator(
									new SWTWidgetLocator(
											Canvas.class,
											new SWTWidgetLocator(
													Class
															.forName("org.eclipse.jface.text.source.CompositeRuler$CompositeRulerCanvas"))),
									4, 76));
		} else {
			ui.click(2, new XYLocator(new SWTWidgetLocator(Class
					.forName("org.eclipse.jface.text.source.AnnotationRulerColumn$5")), 9, 50));
		}
		this.showView(BREAKPOINT_VIEW_ID);
		IWidgetLocator tree = this
				.computeTreeItemLocator(".*[\\/\\\\]BPinWS[\\/\\\\]bpinws.c \\[line: 7\\]");
		ui.contextClick(tree, "Remove All");
		ui.wait(new ShellShowingCondition("Remove All Breakpoints"));
		ui.click(new ButtonLocator("&Yes"));
		ui.wait(new ShellDisposedCondition("Remove All Breakpoints"));
		terminateDebugger();
		ui.click(new SWTWidgetLocator(ToolItem.class, "C/C++"));
	}

}