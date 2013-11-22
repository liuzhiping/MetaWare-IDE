

package com.arc.cdt.tests.B;


import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import org.eclipse.swt.widgets.Button;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.WaitTimedOutException;
import com.windowtester.runtime.locator.XYLocator;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.FilteredTreeItemLocator;
import com.windowtester.runtime.swt.locator.LabeledLocator;
import com.windowtester.runtime.swt.locator.LabeledTextLocator;
import com.windowtester.runtime.swt.locator.TableItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ContributedToolItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.PullDownMenuItemLocator;


public class T_CR93639 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Test that an executable path can have spaces in CMPD.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    private static final String PROJECT = "project with spaces";
    private static final String LAUNCH_NAME = "CMPD WITH SPACES";

    /**
     * Main test method.
     */
    public void testT_CR93639 () throws Exception {
        IUIContext ui = getUI();
        EclipseUtil.removeLaunchConfiguration(LAUNCH_NAME);
        switchToCPerspective();
        this.buildProject(PROJECT);
        switchToDebugPerspective();
        ui.click(new PullDownMenuItemLocator(DEBUG_CONFIG_SELECTION_NAME, new ContributedToolItemLocator(
            "org.eclipse.debug.internal.ui.actions.DebugDropDownAction")));
        ui.wait(new ShellShowingCondition(DEBUG_CONFIG_DIALOG_TITLE));
        ui.click(new FilteredTreeItemLocator("C\\/C++ Multiprocess Application"));
        ui.contextClick(new FilteredTreeItemLocator("C\\/C++ Multiprocess Application"), "New");
        ui.click(new XYLocator(new LabeledTextLocator("&Name:"), 5, 5));
        ui.keyClick(WT.CTRL,'A');
        ui.enterText(LAUNCH_NAME);
//        ui.click(new LabeledLocator(Button.class, "Select Project: "));
//        ui.wait(new ShellShowingCondition("Project Selection"));
//        EclipseUtil.clickTableItem(ui,1,PROJECT);
//        ui.click(new ButtonLocator("OK"));
//        ui.wait(new ShellDisposedCondition("Project Selection"));
        ui.click(new ButtonLocator("Add process"));
        ui.wait(new ShellShowingCondition("Add Process"));
        ui.click(new XYLocator(new LabeledTextLocator("Process Set Name: "), 2, 5));
        ui.wait(milliseconds(200));
        ui.keyClick(WT.CTRL,'A');
        ui.enterText("name");
        ui.click(new LabeledTextLocator("Process IDs: "));
        if (isLinux()){
        	ui.wait(milliseconds(200)); //Linux has issues
        	ui.click(new LabeledTextLocator("Process IDs: "));
        }
        ui.wait(milliseconds(200));
        ui.keyClick(WT.CTRL,'A');
        ui.enterText("1:2");
        ui.click(new LabeledLocator(Button.class, "Select Project: "));
        ui.wait(new ShellShowingCondition("Project Selection"));
        EclipseUtil.clickTableItem(ui,2,PROJECT);
        //ui.click(new ButtonLocator("OK"));
        ui.wait(new ShellDisposedCondition("Project Selection"));
        ui.click(new LabeledLocator(Button.class, "Path and arguments: "));
        ui.wait(new ShellShowingCondition("File Selection"));
        ui.click(new TableItemLocator("Q U E E N S.elf"));
        ui.click(new ButtonLocator("OK"));
        ui.wait(new ShellDisposedCondition("File Selection"));
        ui.click(new ButtonLocator("OK"));
        ui.wait(new ShellDisposedCondition("Add Process"));
        ui.click(new ButtonLocator("Appl&y"));
        ui.click(new ButtonLocator("&Debug"));
        ui.wait(new ShellDisposedCondition(DEBUG_CONFIG_DIALOG_TITLE));
        // Due to CR93875 in the debugger, the engine will emit "Can't find file..."
        // bogus error message. Dismiss it. Remove when debugger is fixed.
        // <CR93875>
        try{ 
            ui.wait(new ShellShowingCondition("Debugger Error"),5000);
            ui.click(new ButtonLocator("OK"));
            ui.wait(new ShellDisposedCondition("Debugger Error"),1000);
            ui.wait(new ShellShowingCondition("Debugger Error"),5000);
            ui.click(new ButtonLocator("OK"));
        }catch (WaitTimedOutException x){
        	// assume debugger was fixed.
        }
        //  </CR93875>
        
        this.waitUntilDebuggerStops(20000);
        this.compareApplicationConsole("T_CR93639.1");
        this.terminateDebugger();
        EclipseUtil.removeLaunchConfiguration(LAUNCH_NAME);
    }

}