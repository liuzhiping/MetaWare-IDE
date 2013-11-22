

package com.arc.cdt.tests.B;


import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import org.eclipse.swt.widgets.Control;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.WaitTimedOutException;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.condition.IsEnabledCondition;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.locator.IWidgetReference;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.CTabItemLocator;
import com.windowtester.runtime.swt.locator.ComboItemLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;


public class T_CR96254 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Test that the xISS bridge definition errors are " +
    		"properly dealt with if the target is changed to non-XISS";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    private static final String LAUNCH_NAME = "Queens_AC.elf";

    /**
     * Main test method.
     */
    public void testT_CR96254() throws Exception {
       
        switchToDebugPerspective();
        this.bringUpDebugLaunchDialog(new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws WidgetSearchException {
                ui.click(computeTreeItemLocator("C\\/C\\+\\+ [LMA].*/"+LAUNCH_NAME));            
                ui.click(new CTabItemLocator("Debugger"));
              
                ui.click(EclipseUtil.computeTreeItemLocator(ui,"Target Selection"));
                IWidgetLocator arcTarget[] = ui.findAll(new NamedWidgetLocator("ARC_target"));
                if (arcTarget.length == 0){
                	System.out.println("No ARC_target found!");
                }
                ui.click(new ComboItemLocator("Fast ISS (xISS)",new NamedWidgetLocator("ARC_target")));
               
                
                ui.click(EclipseUtil.computeTreeItemLocator(ui,"Simulator Extensions/xISS Bridge"));
               
                ui.click(new ComboItemLocator("1", new NamedWidgetLocator("XISS_bridge_0_target")));
                ui.click(new NamedWidgetLocator("XISS_bridge_0_length"));
                ui.keyClick(WT.CTRL,'A');
                ui.keyClick('0');
                checkForError("Bridge length must be a page (8K) multiple.");
                NamedWidgetLocator bridge0Start = new NamedWidgetLocator("XISS_bridge_0_start");
                ui.click(bridge0Start);
                
                ui.click(new NamedWidgetLocator("XISS_bridge_0_length"));
                ui.keyClick(WT.CTRL,'A');
                ui.enterText("0x11000");
                checkForError("Bridge length must be a page (8K) multiple.");
              
                ui.click(new ComboItemLocator("1", new NamedWidgetLocator("XISS_bridge_1_target")));
                NamedWidgetLocator bridge1TargetAddress = new NamedWidgetLocator("XISS_bridge_1_target_address");
                ui.click(bridge1TargetAddress);
                ui.keyClick(WT.CTRL,'A');
                ui.enterText("0xFFFF000");
                checkForError("Bridge target address must be a page (8K) multiple.");
                
                ui.click(bridge1TargetAddress);
                ui.keyClick(WT.CTRL,'A');
                ui.enterText("0xFFFE000");
                NamedWidgetLocator bridge1Start = new NamedWidgetLocator("XISS_bridge_1_start");
                ui.click(bridge1Start);
                
                EclipseUtil.selectTreeItem(ui,(IWidgetReference)EclipseUtil.computeTreeItemLocator(ui,"Target Selection"));
                ui.wait(milliseconds(400));
                
                ui.click(new ComboItemLocator("MetaWare ARC simulator",new NamedWidgetLocator("ARC_target")));
                confirmDebugEnablement(true);                  
            }

            });
       
        
    }
    
    private void checkForError (String text) throws WaitTimedOutException, WidgetSearchException {
        IUIContext ui = getUI();
        // Eclipse prefixes with single space for some reason.
        EclipseUtil.findTextLocator(" " + text, (Control)ui.getActiveWindow());
        confirmDebugEnablement(false);
    }
    
    private void confirmDebugEnablement(boolean enabled){
        getUI().assertThat(new IsEnabledCondition(new ButtonLocator("&Debug"),enabled));
    }

}