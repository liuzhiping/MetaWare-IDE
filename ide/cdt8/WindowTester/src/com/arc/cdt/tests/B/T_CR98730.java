

package com.arc.cdt.tests.B;


import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import junit.framework.Assert;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.CTabItemLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;
import com.windowtester.runtime.swt.locator.TreeItemLocator;


public class T_CR98730 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that timers can be set from Launch Configuration Dialog effectively.";

    public static final String CATEGORY = DEBUGGER_INTEGRATION;

    private static final String PROJECT = "Queens_AC";
    private static final String LAUNCH = "Queens_AC.elf";

    /**
     * Main test method.
     */
    public void testT_CR98730 () throws Exception {

        switchToCPerspective();
        this.setDefaultBuildProperties(PROJECT);

        this.bringUpDebugLaunchDialog(new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws WidgetSearchException {
                ui.click(new TreeItemLocator("C\\/C\\+\\+ Application/" + LAUNCH));
                ui.click(new CTabItemLocator("Debugger"));
                ui.click(computeTreeItemLocator("Simulator Extensions"));
                ui.click(computeTreeItemLocator("Simulator Extensions/Timers"));
                IWidgetLocator optionsField = ui.find(new NamedWidgetLocator("debugger_options"));
                String text = EclipseUtil.getText(optionsField);
                if (text.indexOf("-Xtimer0") >= 0){
                    ui.click(new NamedWidgetLocator("ARC_timer0"));
                    ui.wait(milliseconds(400));
                }
                if (text.indexOf("-Xtimer1") >= 0){
                    ui.click(new NamedWidgetLocator("ARC_timer1"));
                    ui.wait(milliseconds(400));
                }
                text = EclipseUtil.getText(optionsField);
                Assert.assertTrue("timers not initially set",text.indexOf("-Xtimer") < 0);
                
                ui.click(new NamedWidgetLocator("ARC_timer0"));
                ui.wait(milliseconds(400));
                
                text = EclipseUtil.getText(optionsField);
                Assert.assertTrue("-Xtimer0 set", text.indexOf("-Xtimer0") >= 0);
                Assert.assertTrue("-Xtimer1 not set", text.indexOf("-Xtimer1") < 0);
                
                ui.click(new NamedWidgetLocator("ARC_timer1"));
                ui.wait(milliseconds(400));
                
                text = EclipseUtil.getText(optionsField);
                
                Assert.assertTrue("-Xtimer1 set", text.indexOf("-Xtimer1") >= 0);
                
                ui.click(new ButtonLocator("Revert"));
                ui.click(new ButtonLocator("Close"));               
            }});

    }

}