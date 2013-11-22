

package com.arc.cdt.tests.B;


import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.FilteredTreeItemLocator;


public class T_CR96517 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that the MetaWare debugger is the one selected when" +
            "launching directory from the project drop-down menu.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    private static final String PROJECT = "CR96517_project";

    /**
     * Main test method.
     */
    public void testT_CR96517() throws Exception {
        this.switchToCPerspective();
        this.deleteProject(PROJECT);
        this.createNewProject(PROJECT);
        this.createSourceFile(PROJECT, "main.c", "void main(){}");
        this.buildProject(PROJECT);
        
        this.rightClickProjectMenu(PROJECT, "Debug As/&1 Local C\\/C++ Application");
        this.dealWithDebugPerspectiveConfirmation(true);
        
        IUIContext ui = getUI();
        this.waitUntilDebuggerStops(10000);
        ui.click(EclipseUtil.computeTreeItemLocator(ui,PROJECT+".elf.*/MetaWare Debugger.*/Thread.*"));
        this.terminateDebugger();
        
        this.switchToCPerspective();
        EclipseUtil.removeLaunchConfiguration(PROJECT+".elf");
        this.rightClickProjectMenu(PROJECT, "Debug As/De&bug Configurations...");
        ui.wait(new ShellShowingCondition(DEBUG_CONFIG_DIALOG_TITLE));
        
        ui.click(2,new FilteredTreeItemLocator("C\\/C\\+\\+ Application"));
        
        ui.click(new ButtonLocator("&Debug"));
        ui.wait(new ShellDisposedCondition(DEBUG_CONFIG_DIALOG_TITLE));
        dealWithDebugPerspectiveConfirmation(true);
        this.waitUntilDebuggerStops(10000);
        ui.click(EclipseUtil.computeTreeItemLocator(ui,PROJECT+" Debug.*/MetaWare Debugger.*/Thread.*"));
        this.terminateDebugger();
        
        this.deleteProject(PROJECT);
        
    }

}