

package com.arc.cdt.tests.B;


import junit.framework.Assert;

import org.eclipse.swt.widgets.Combo;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.ComboItemLocator;
import com.windowtester.runtime.swt.locator.FilteredTreeItemLocator;
import com.windowtester.runtime.swt.locator.LabeledLocator;
import com.windowtester.runtime.swt.locator.LabeledTextLocator;
import com.windowtester.runtime.swt.locator.TableItemLocator;


public class T_CR96150 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that command action points can be set and that they work.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    private static final String LAUNCH = "Queens_AC.elf";

    /**
     * Main test method.
     */
    public void testT_CR96150() throws Exception {
        this.switchToDebugPerspective();
        this.launchDebugger(LAUNCH, false);
        
        // its easier to set breakpoints from command line from GUI tester
        this.showView(COMMAND_VIEW_ID);
        this.enterDebugCommandLine("b !29");
        this.enterDebugCommandLine("b !46");
        this.showView(BREAKPOINT_VIEW_ID);
        
      
        IUIContext ui = getUI();
        IWidgetLocator ref = EclipseUtil.computeTreeItemLocator(ui, ".*queens\\.c \\[line: 29\\]");
        ui.contextClick(ref,"Properties...");
        
        ui.wait(new ShellShowingCondition("Properties for.*"));
        
        ui.click(new FilteredTreeItemLocator("Actions"));
        IWidgetLocator d[] = ui.findAll(new TableItemLocator("Display I"));
        if (d.length > 0) {
            ui.click(d[0]);
            ui.click(new ButtonLocator("Delete"));
        }
        ui.click(new ButtonLocator("New..."));
        ui.wait(new ShellShowingCondition("New Breakpoint Action"));
       
        ui.click(new ComboItemLocator("Debugger Command",new LabeledLocator(Combo.class,"Action type: *")));
        ui.click(new LabeledTextLocator("Action name:"));
        ui.keyClick(WT.CTRL,'A');
        ui.enterText("Display I");
        ui.click(new LabeledTextLocator("Enter debugger commands, one per line"));
        ui.enterText("eval I");
        ui.keyClick(WT.CR);
        ui.enterText("run");
        ui.keyClick(WT.CR);
        ui.click(new ButtonLocator("OK"));
        ui.wait(new ShellDisposedCondition("New Breakpoint Action"));
        ui.click(EclipseUtil.findTableItem(ui,"Display I"));
        ui.click(new ButtonLocator("Attach"));
        ui.click(new ButtonLocator("OK"));
        
        ui.wait(new ShellDisposedCondition("Properties for.*"));     
        
        this.clickResumeButton();
        this.clickResumeButton();
        this.clickResumeButton();
        String console = this.getDebuggerConsoleContent();
        
        int i1 = console.indexOf("I = 1");
        int i2 = console.indexOf("I = 2");
        int i3 = console.indexOf("I = 3");
        Assert.assertTrue(i1 > 0);
        Assert.assertTrue(i2 > i1);
        Assert.assertTrue(i3 > i2);     
       
        this.terminateDebugger();
        
        
    }

}