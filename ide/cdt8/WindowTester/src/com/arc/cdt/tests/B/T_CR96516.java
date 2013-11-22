

package com.arc.cdt.tests.B;


import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import junit.framework.Assert;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.locator.IWidgetReference;
import com.windowtester.runtime.swt.locator.CTabItemLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;


public class T_CR96516 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm workaround for Windows SWT setFocus bug works.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    private static final String PROJECT = "Queens_AC";
    private static final String LAUNCH_NAME = PROJECT+".elf";

    /**
     * Main test method.
     */
    public void testT_CR96516() throws Exception {
       
        this.bringUpDebugLaunchDialog(new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws WidgetSearchException {
                selectLaunchFromLaunchDialog(ui, LAUNCH_NAME);
                ui.click(new CTabItemLocator("Debugger"));
                ui.click(EclipseUtil.computeTreeItemLocator(ui,"Simulator Extensions/DSP Memory"));
                IWidgetLocator XYButton = ui.find(new NamedWidgetLocator("ARC_xy"));
                ui.click(XYButton);
                IWidgetLocator dspButton = ui.find(new NamedWidgetLocator("ARC_nodspmem"));
                ui.mouseMove(dspButton);
                ui.wait(milliseconds(300));
                Assert.assertTrue("XYButton selected",EclipseUtil.isSelected((IWidgetReference)XYButton));
                Assert.assertTrue("DSP Button unselected",!EclipseUtil.isSelected((IWidgetReference)dspButton));
                
                EclipseUtil.setActiveShellSize(ui, 1100, 900); // don't know why dialog resized; undo damage.
                
                // Additional test for CR96958:
                ui.click(new NamedWidgetLocator("x_mem_map"));
                ui.keyClick(WT.CTRL,'A');
                ui.enterText("12345");
                String value = EclipseUtil.getText(ui.find(new NamedWidgetLocator("x_mem_map")));
                Assert.assertTrue("Value in combobox was \"" + value + "\"",value.equals("12345"));
            }});
        
    }

}