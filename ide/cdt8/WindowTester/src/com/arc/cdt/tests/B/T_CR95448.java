

package com.arc.cdt.tests.B;


import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import junit.framework.Assert;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.condition.IsEnabledCondition;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.CTabItemLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;
import com.windowtester.runtime.swt.locator.TreeItemLocator;


public class T_CR95448 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Test that \"Revert\" works for Launch Configuration dialog.";

    public static final String CATEGORY = DEBUGGER_INTEGRATION;

    private static final String LAUNCH = "Queens_AC.elf";

    /**
     * Main test method.
     */
    public void testT_CR95448 () throws Exception {

        switchToCPerspective();

        this.bringUpDebugLaunchDialog(new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws WidgetSearchException {
                ui.click(new TreeItemLocator("C\\/C\\+\\+ Application/" + LAUNCH));
                ui.click(new CTabItemLocator("Debugger"));
                ui.click(computeTreeItemLocator("Simulator Extensions"));
                ui.click(computeTreeItemLocator("Simulator Extensions/Memory Options"));
                ui.click(new NamedWidgetLocator("ARC_ExtAdrLo1"));
                ui.enterText("0xA000");
                ui.click(new NamedWidgetLocator("ARC_ExtAdrHi1"));
                ui.enterText("0xB000");
                ui.wait(milliseconds(500));
                IWidgetLocator optionsField = ui.find(new NamedWidgetLocator("debugger_options"));
                String text = EclipseUtil.getText(optionsField);
                Assert.assertTrue("Options field contains -memext",text.indexOf("-memext=0xA000,0xB000") >= 0);
                ui.click(computeTreeItemLocator("Simulator Extensions/User Extensions"));
                ui.click(new NamedWidgetLocator("ARC_ExtDLL1"));
                ui.enterText("simext");
                ButtonLocator apply = new ButtonLocator("Appl&y");
                ButtonLocator debug = new ButtonLocator("&Debug");
                ButtonLocator revert = new ButtonLocator("Revert");
                ui.assertThat(new IsEnabledCondition(apply,true));
                ui.assertThat(new IsEnabledCondition(debug,true));
                ui.click(revert);
                ui.wait(milliseconds(500));
                ui.assertThat(new IsEnabledCondition(apply,false));
                ui.assertThat(new IsEnabledCondition(revert,false));
                ui.assertThat(new IsEnabledCondition(debug,true));
                ui.click(new ButtonLocator("Close"));
                
            }});

    }

}