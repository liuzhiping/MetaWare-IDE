

package com.arc.cdt.tests.B;


import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Canvas;

import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.condition.IsEnabledCondition;
import com.windowtester.runtime.locator.XYLocator;
import com.windowtester.runtime.swt.locator.MenuItemLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.eclipse.ContributedToolItemLocator;


public class T_CR98680 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that stepping buttons are enabled after a \"Set PC\" operation.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;

    private static final String LAUNCH = "Queens_AC.elf";

    /**
     * Main test method.
     */
    public void testT_CR98680() throws Exception {
        switchToDebugPerspective();
        this.launchDebugger(LAUNCH, false) ;
        this.waitUntilDebuggerStops(15000);
        IUIContext ui = getUI();
       
        ContributedToolItemLocator step = new ContributedToolItemLocator("org.eclipse.debug.ui.commands.StepInto");
        ui.click(step);
        ui.click(new XYLocator(new SWTWidgetLocator(StyledText.class, new SWTWidgetLocator(Canvas.class)), 99, 203));
        ui.contextClick(new SWTWidgetLocator(StyledText.class, new SWTWidgetLocator(Canvas.class)), "Resume At Li&ne");
        ui.wait(milliseconds(500));        
        ui.assertThat(new IsEnabledCondition(step,true));
        
        ui.click(new MenuItemLocator("Debugger/Source"));
        ui.click(new XYLocator(new NamedWidgetLocator("source"), 120, 110));
        ui.contextClick(new NamedWidgetLocator("source"), "Set PC");
        
        ui.wait(milliseconds(500));        
        ui.assertThat(new IsEnabledCondition(step,true));
        
        this.terminateDebugger();
    }
}