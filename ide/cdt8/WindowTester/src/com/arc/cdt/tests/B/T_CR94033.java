

package com.arc.cdt.tests.B;


import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import junit.framework.Assert;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.condition.ICondition;
import com.windowtester.runtime.condition.IsEnabledCondition;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.CTabItemLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;


public class T_CR94033 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that a \"-cmd=...\" option can be passed to CMPD.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    private static final String LAUNCH_NAME = "Graphics pipeline cr94033";

    /**
     * Main test method.
     */
    public void testT_CR94033() throws Exception {
       
        switchToDebugPerspective();
        
        this.bringUpDebugLaunchDialog(new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws WidgetSearchException {
                ui.click(computeTreeItemLocator("C\\/C\\+\\+ [LM].*/"+LAUNCH_NAME));
                //CTabItemLocator tabItemLocator1 = new CTabItemLocator("CMPD Debugger Configuration");
                CTabItemLocator tabItemLocator2 = new CTabItemLocator("Additional Settings");
                IWidgetLocator textLocator = new NamedWidgetLocator("cmpd_command_options");
                
                ui.click(tabItemLocator2);
                IWidgetLocator cmdField = ui.find(textLocator);
                String text = EclipseUtil.getText(cmdField);
                if (text.indexOf(" \"-cmd=") >= 0){
                    //ui.click(tabItemLocator1);
                    //ui.click(new ButtonLocator("Reset CMPD Processes"));
                    //EclipseUtil.dumpControl((Control)ui.getActiveWindow());
                	ui.click(textLocator);
                	ui.keyClick(WT.CTRL,'A');
                	ui.enterText(text.substring(0,text.indexOf(" \"-cmd=")));
                	ui.wait(milliseconds(400));
                    ui.click(new ButtonLocator("Appl&y"));
                    //ui.pause(400);
                    //ui.click(tabItemLocator2);
                    ui.wait(milliseconds(300));
                }
                
                text = EclipseUtil.getText(cmdField);
                Assert.assertTrue(text.indexOf("-cmd=") < 0); // should be cleared.
                ui.click(textLocator);
                ui.keyClick(WT.END);
                ui.keyClick(WT.ARROW_DOWN);
                ui.keyClick(WT.ARROW_DOWN);
                ui.keyClick(WT.ARROW_DOWN);
                ui.enterText(" \"-cmd=read input.txt\"");
                ButtonLocator apply = new ButtonLocator("Appl&y");
                ui.mouseMove(apply); // mouse tracker will update Apply buttton
                ICondition enabled = new IsEnabledCondition(apply,true);
                ui.wait(enabled,5000);
                ui.click(new ButtonLocator("Appl&y"));
                ui.click(new ButtonLocator("Debug"));
            }});
        dealWithDebugPerspectiveConfirmation(false);
        EclipseUtil.waitUntilDebuggerStops(getUI(), 30000);
        String console = this.getDebuggerConsoleContent();
        if (console==null || console.length() < 10) { // WindowTester sometimes takes more than one try.
        	getUI().wait(milliseconds(1000));
        	console = this.getDebuggerConsoleContent();
        }
        //int i = console.indexOf("[3] paint_frame() = 0x");
        int j = console.indexOf("[1] _grmain() = 0x");
        int k = console.indexOf("[2] _displaymain() = 0x");
        
        if (j < 0 || k < 0){
            this.writeStringToFile("T_CR94033.txt", console);
        }
        
        //Assert.assertTrue(i >= 0);
        Assert.assertTrue(j > 0);
        Assert.assertTrue(k > 0);
        
        this.terminateDebugger();       
    }
}