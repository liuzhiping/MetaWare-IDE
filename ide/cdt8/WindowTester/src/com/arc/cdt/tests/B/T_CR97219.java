

package com.arc.cdt.tests.B;


import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.locator.IWidgetReference;


public class T_CR97219 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that an array element consisting of character \"'\" can be diplayed.";

    public static final String CATEGORY = DEBUGGER_INTEGRATION;

    private static final String LAUNCH = "char39.elf";

    /**
     * Main test method.
     */
    public void testT_CR97219() throws Exception {
        switchToDebugPerspective();
        this.launchDebugger(LAUNCH, false);
        this.showView(VARIABLE_VIEW_ID);
        IUIContext ui = getUI();
        ui.wait(milliseconds(3000));
        //TreeItemLocator ti = new TreeItemLocator("init_array", new ViewLocator("org.eclipse.debug.ui.VariableView"));
        //IWidgetLocator locator = ui.find(ti);
        IWidgetLocator locator = EclipseUtil.computeTreeItemLocator(ui,"init_array");
        EclipseUtil.expandTreeItem(ui,(IWidgetReference)locator);
        ui.wait(milliseconds(1000)); // give it time to settle
        this.compareView("T_CR97219.1", VARIABLE_VIEW_ID);
        this.terminateDebugger();
    }

}