

package com.arc.cdt.tests.B;


import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.CTabItemLocator;
import com.windowtester.runtime.swt.locator.FilteredTreeItemLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;


public class T_CR97850 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm ARC601 debugger option deals with barrel shifter widget correctly";

    public static final String CATEGORY = DEBUGGER_INTEGRATION;

    private static final String LAUNCH = "uarttest";

    /**
     * Main test method.
     */
    public void testT_CR97850() throws Exception {
        switchToCPerspective();
        this.bringUpDebugLaunchDialog(new IUIRunnable(){

			@Override
			public void run(IUIContext ui) throws WidgetSearchException {
				ui.click(new FilteredTreeItemLocator("C\\/C++ Application/"+LAUNCH));
				ui.click(new CTabItemLocator("Debugger"));
				EclipseUtil.setActiveShellSize(ui, 1100, 1000);
				ui.click(EclipseUtil.computeTreeItemLocator(ui,"Target Selection"));
				ui.click(new ButtonLocator("ARC600"));
				ui.click(EclipseUtil.computeTreeItemLocator(ui,"Simulator Extensions/Instruction Extensions"));
				ui.wait(milliseconds(1000));
				String text = EclipseUtil.getText(ui.find(new NamedWidgetLocator("debugger_options")));
				writeAndCompareSnapshot("T_CR97850.1",text);
				ui.click(new NamedWidgetLocator("ARC_no_bs"));
				ui.wait(milliseconds(1000));
				text = EclipseUtil.getText(ui.find(new NamedWidgetLocator("debugger_options")));
				writeAndCompareSnapshot("T_CR97850.2",text);
				ui.click(EclipseUtil.computeTreeItemLocator(ui,"Target Selection"));
				ui.click(new ButtonLocator("ARC601"));
				ui.click(EclipseUtil.computeTreeItemLocator(ui,"Simulator Extensions/Instruction Extensions"));
				ui.wait(milliseconds(1000));
				text = EclipseUtil.getText(ui.find(new NamedWidgetLocator("debugger_options")));
				writeAndCompareSnapshot("T_CR97850.3",text);
				ui.click(new NamedWidgetLocator("ARC_barrel_shifter"));
				ui.wait(milliseconds(1000));
				text = EclipseUtil.getText(ui.find(new NamedWidgetLocator("debugger_options")));
				writeAndCompareSnapshot("T_CR97850.4",text);								
			}});
         
    }

}