

package com.arc.cdt.tests.B;


import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.swt.locator.TreeItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ContributedToolItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;


public class T_CR94047 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Test that the MetaWare Watchpoint display works reasonably.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    private static final String LAUNCH_NAME = "Graphics pipeline 2";

    /**
     * Main test method.
     */
    public void testT_CR94047() throws Exception {
       
        switchToDebugPerspective();
        launchDebugger(LAUNCH_NAME,false);
        
        this.waitUntilDebuggerStops(15000);
        
        this.showSeeCodeView("Watchpoints");        
        
        IUIContext ui = getUI();
        ui.click(new TreeItemLocator(LAUNCH_NAME +" \\[C\\/C\\+\\+ Multiprocess Application\\]", new ViewLocator(
        DEBUG_VIEW_ID)));
        
        this.showSeeCodeView("Command-line input");
        this.enterDebugCommandLine("w 0x16000");
        this.pause(ui,2000);
        
        this.showView(BREAKPOINT_VIEW_ID);
        
        this.compareView("T_CR94047.1", BREAKPOINT_VIEW_ID);
        
        doProcess("grmain",2);
        doProcess("displaymain",3);
        doProcess("rendermain0",4);
        this.compareView("T_CR94047.5",BREAKPOINT_VIEW_ID);
        doProcess("rendermain1",6);
        this.compareView("T_CR94047.7",BREAKPOINT_VIEW_ID);
        
        this.terminateDebugger();
       
    }
    
    private void doProcess(String processName, int snapshotIndex) throws WidgetSearchException{
        IUIContext ui = getUI();
        ui.click(this.computeTreeItemLocator(LAUNCH_NAME + ".*/" + processName + ".*\\(Suspended\\)"));
        ui.wait(milliseconds(500));
        this.compareSeeCodeView("T_CR94047." + snapshotIndex+"a", "watch");
        ui.click(new ContributedToolItemLocator("watch.button.remove_all_button"));
        ui.wait(milliseconds(500));
        this.compareSeeCodeView("T_CR94047." + snapshotIndex+"b", "watch");     
    }

}