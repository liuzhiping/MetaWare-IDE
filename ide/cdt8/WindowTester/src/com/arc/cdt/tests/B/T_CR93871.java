

package com.arc.cdt.tests.B;


import junit.framework.Assert;

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
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.CTabItemLocator;
import com.windowtester.runtime.swt.locator.ComboItemLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;
import com.windowtester.runtime.swt.locator.TableItemLocator;
import com.windowtester.runtime.swt.locator.TreeItemLocator;


public class T_CR93871 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Test that the xISS bridge definitions complain about "+
    "addresses or lengths not being on page boundary";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    private static final String LAUNCH_NAME = "Graphics pipeline";

    /**
     * Main test method.
     */
    public void testT_CR93871 () throws Exception {
       
        switchToDebugPerspective();
        this.bringUpDebugLaunchDialog(new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws WidgetSearchException {
                ui.click(computeTreeItemLocator("C\\/C\\+\\+ [LM].*/"+LAUNCH_NAME));            
                ui.click(new CTabItemLocator("CMPD Debugger Configuration"));
                ui.click(new TableItemLocator("displaymain"));
                ui.click(new ButtonLocator("Edit"));
                ui.wait(new ShellShowingCondition("Edit Process"));
                ui.click(new ButtonLocator("Configure..."));
                ui.wait(new ShellShowingCondition("Configure process"));
                ui.click(new TreeItemLocator("Target Selection"));
                IWidgetLocator all[] = ui.findAll(new NamedWidgetLocator("ARC_target"));
                if (all.length == 0){
                	System.out.println("No ARC_target found!");
                }
                ui.click(new ComboItemLocator("Fast ISS (xISS)",new NamedWidgetLocator("ARC_target")));
                ui.click(new NamedWidgetLocator("XISS_ARC6"));
                ui.assertThat(new IsEnabledCondition(new NamedWidgetLocator("XISS_MMU"),false));
                ui.click(new NamedWidgetLocator("XISS_ARC7"));
                ui.assertThat(new IsEnabledCondition(new NamedWidgetLocator("XISS_MMU"),true));
                confirmWidget("xiss_jit",true);
                confirmWidget("xiss_ulibc",true);
                confirmWidget("xiss_trans_cache_size",true);
                confirmWidget("xiss_trans_cache_ways",true);
                confirmWidget("xiss_trans_time_slice",true);
                confirmWidget("xiss_trans_threshold",true);
                
                ui.click(new TreeItemLocator("Simulator Extensions/DSP Memory"));
                confirmWidget("ARC_nodspmem",true);
                confirmWidget("ARC_scratchram",false);
                confirmWidget("ARC_xy",true);
                
                ui.click(new TreeItemLocator("Simulator Extensions/xISS Bridge"));
               
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
                ui.keyClick(WT.CTRL,'A');
                ui.enterText("0x2000");
                
                ui.click(bridge0Start);
                ui.keyClick(WT.CTRL,'A');
                ui.enterText("garbage");
                checkForError("Bridge start address must be an integer multiple of 8192");
                NamedWidgetLocator bridge0TargetAddress = new NamedWidgetLocator("XISS_bridge_0_target_address");
                ui.click(bridge0TargetAddress);      
                
                ui.click(bridge0Start);
                ui.keyClick(WT.CTRL,'A');
                ui.enterText("0xF001000");
                checkForError("Bridge start address must be a page (8K) multiple.");
                ui.click(bridge0TargetAddress);

                ui.click(bridge0Start);
                ui.keyClick(WT.CTRL,'A');
                ui.enterText("0xF002000");
                ui.click(bridge0TargetAddress);
                String  text = EclipseUtil.getText(ui.find(bridge0TargetAddress));
                Assert.assertTrue(text.equals("0xF002000"));
                
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
                
                text = EclipseUtil.getText(ui.find(bridge1Start));
                Assert.assertTrue(text.equals("0xFFFE000"));
                
                //<SWTBUG: focus is lost>
                EclipseUtil.setFocus(EclipseUtil.findShell("Configure process"));
                //</SWTBUG>
                ui.click(new ButtonLocator("Cancel"));
                ui.wait(new ShellDisposedCondition("Configure process"));
               //<SWTBUG: focus is lost>
                EclipseUtil.setFocus(EclipseUtil.findShell("Edit Process"));
                //</SWTBUG>
                ui.click(new ButtonLocator("Cancel"));
                ui.wait(new ShellDisposedCondition("Edit Process"));
                ui.click(new ButtonLocator("Close"));
                
            }

            });
       
        
    }
    
    private void checkForError (String text) throws WaitTimedOutException, WidgetSearchException {
        IUIContext ui = getUI();
        EclipseUtil.findLabelLocator(text, (Control)ui.getActiveWindow());
    }
    
    private void confirmWidget(String name, boolean enabled){
        getUI().assertThat(new IsEnabledCondition(new NamedWidgetLocator(name),enabled));
    }

}