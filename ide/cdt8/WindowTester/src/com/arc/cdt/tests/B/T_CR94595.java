

package com.arc.cdt.tests.B;


import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import junit.framework.Assert;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Canvas;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;


public class T_CR94595 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Test that the source editor can display source that is outside of the workspace.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    private static final String LAUNCH_NAME1 = "Outside.exe";
    private static final String LAUNCH_NAME2 = "Outside2";

    /**
     * Main test method.
     */
    public void testT_CR94595() throws Exception {
       
        switchToDebugPerspective();
        doLaunch(LAUNCH_NAME1);
        doLaunch(LAUNCH_NAME2);// non-default working directory
       
    }

    private void doLaunch (String launchName) throws WidgetSearchException {
        launchDebugger(launchName,false);
        
        this.waitUntilDebuggerStops(15000);
        
        this.clickStepIntoButton();
        this.clickStepIntoButton();
        
        IUIContext ui = getUI();
        ui.click(new SWTWidgetLocator(StyledText.class, new SWTWidgetLocator(Canvas.class)));
        
        ui.keyClick(WT.CTRL,'A');
        ui.wait(milliseconds(200));
        ui.keyClick(WT.CTRL,'C');
        String s = EclipseUtil.getClipboardText();
        Assert.assertTrue("In source code: ",s.startsWith("void hello()"));
        
        this.terminateDebugger();
    }

}