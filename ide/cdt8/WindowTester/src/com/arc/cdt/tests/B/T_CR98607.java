

package com.arc.cdt.tests.B;


import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import junit.framework.Assert;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.swt.locator.CTabItemLocator;
import com.windowtester.runtime.swt.locator.ComboItemLocator;
import com.windowtester.runtime.swt.locator.FilteredTreeItemLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;


public class T_CR98607 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that the \"Digilent\" setting works in the Debugger Options Dialog.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;

    private static final String LAUNCH = "Queens_AC.elf";

    /**
     * Main test method.
     */
    public void testT_CR98607() throws Exception {
        switchToCPerspective();
        this.bringUpDebugLaunchDialog(new IUIRunnable(){

			@Override
			public void run(IUIContext ui) throws WidgetSearchException {
				ui.click(new FilteredTreeItemLocator("C\\/C++ Application/"+LAUNCH));
				ui.click(new CTabItemLocator("Debugger"));
				EclipseUtil.setActiveShellSize(ui, 1100, 1000);
				ui.click(EclipseUtil.computeTreeItemLocator(ui,"Target Selection"));
                ui.click(new ComboItemLocator("Hardware",new NamedWidgetLocator("ARC_target")));
                EclipseUtil.setActiveShellSize(ui, 1200, 1000);
                //EclipseUtil.dumpControl((Control)ui.getActiveWindow());
                ui.click(new ComboItemLocator("Digilent JTAG cable",new NamedWidgetLocator("ARC_hardware_selection")));
                IWidgetLocator optionsField = ui.find(new NamedWidgetLocator("debugger_options"));
                ui.wait(milliseconds(400));
                String text = EclipseUtil.getText(optionsField);
                Assert.assertTrue("-digilent setting missing",text.indexOf("-digilent") >= 0);			
			}});        
    }
}