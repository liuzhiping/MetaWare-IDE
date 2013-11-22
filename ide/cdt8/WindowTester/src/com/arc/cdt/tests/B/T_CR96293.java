

package com.arc.cdt.tests.B;


import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import junit.framework.Assert;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.debug.core.model.IBreakpoint;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.TreeItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;


public class T_CR96293 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that a watchpoint can be cleanly set from the Outline view.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    private static final String PROJECT = "Queens_AC";
    private static final String LAUNCH_NAME = PROJECT+".elf";

    /**
     * Main test method.
     */
    public void testT_CR96293() throws Exception {
       
    	// Rebuild in case it was previously built against a different workspace
      //  switchToCPerspective();
      //  cleanProject(PROJECT);
      //  buildProject(PROJECT);
      //  switchToDebugPerspective();
        launchDebugger(LAUNCH_NAME,false);
        
        IUIContext ui = getUI();
        
        this.showView(BREAKPOINT_VIEW_ID);
        
        ui.click(new TreeItemLocator("C : Boolean[]", new ViewLocator("org.eclipse.ui.views.ContentOutline")));
        ui.contextClick(
            new TreeItemLocator("C : Boolean[]", new ViewLocator("org.eclipse.ui.views.ContentOutline")),
            "Toggle Watchpoint");
        ui.wait(new ShellShowingCondition("Add Watchpoint"));
        ui.click(new ButtonLocator("OK"));
        ui.wait(new ShellDisposedCondition("Add Watchpoint"));
        
        ui.wait(milliseconds(1000)); // so we can see it appear
        ui.click(new TreeItemLocator("size : int", new ViewLocator("org.eclipse.ui.views.ContentOutline")));
        ui.contextClick(
            new TreeItemLocator("size : int", new ViewLocator("org.eclipse.ui.views.ContentOutline")),
            "Toggle Watchpoint");
        ui.wait(new ShellShowingCondition("Add Watchpoint"));
        ui.click(new ButtonLocator("OK"));
        ui.wait(new ShellDisposedCondition("Add Watchpoint"));
        
        ui.wait(milliseconds(1000)); // so that we can see it appear
        
        this.showView(BREAKPOINT_VIEW_ID);
        
        IBreakpoint bp[] = EclipseUtil.getBreakpoints();
        Assert.assertTrue("two watchpoints",bp.length == 2);
        for (int i = 0; i < bp.length; i++) {
            Assert.assertTrue("Is C breakpoint",bp[i] instanceof ICBreakpoint);
            Assert.assertTrue("watchpoint #" + i + " enabled ",bp[i].isEnabled() && ((ICBreakpoint)bp[i]).isInstalled());
        }
        this.terminateDebugger();
    }

}