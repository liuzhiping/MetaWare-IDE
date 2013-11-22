package com.arc.cdt.tests.B;

import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.TreeItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;

public class T_CR92972 extends UIArcTestCaseSWT {
    private static final String TEST = "T_CR92972";

    public static final String DESCRIPTION = "Test \"soft\" vs. \"hard\" breakpoint support.";   
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    /**
     * Tests hardware breakpoints
     */
    public void testT_CR92972() throws Exception {
        registerPerspectiveConfirmationHandler();
        switchToDebugPerspective(); 
        launchDebugger("Queens_AC.elf",true);
        this.waitUntilDebuggerStops(15000);
        showView(COMMAND_VIEW_ID);
        this.enterDebugCommandLine("break !32,hard");
        this.enterDebugCommandLine("break !36,soft");
        this.showView(BREAKPOINT_VIEW_ID);
        this.clickResumeButton();
        IUIContext ui = getUI();
        ui.find(computeTreeItemLocator("Queens_AC\\.elf.*/MetaWare.*/Thread.*/.*Try\\(\\) queens.c:32 ?.*"));
        this.enterDebugCommandLine("run");
        ui.find(computeTreeItemLocator("Queens_AC\\.elf.*/MetaWare.*/Thread.*/.*Try\\(\\) queens.c:36 ?.*"));
        compareBreakpointList("1");
        
        this.compareView(TEST + ".2", BREAKPOINT_VIEW_ID);
        
        ui.click(new TreeItemLocator(WT.CHECK, ".*.Queens_AC.queens.c \\[line: 32\\].*",
            new ViewLocator(BREAKPOINT_VIEW_ID)));
        ui.wait(milliseconds(500));
        compareBreakpointList("3");
        this.compareView(TEST + ".4", BREAKPOINT_VIEW_ID);
        this.enterDebugCommandLine("enable 1");
        ui.wait(milliseconds(300));
        this.compareView(TEST + ".5", BREAKPOINT_VIEW_ID);
        compareBreakpointList("6");
        
        this.clickRemoveAllBreakpoints();
        compareBreakpointList("7");
        
        ui.contextClick(new TreeItemLocator("Try(Integer, Boolean*) : void", new ViewLocator(
        OUTLINE_VIEW_ID)), "Toggle Hardware Breakpoint");
        ui.contextClick(new TreeItemLocator("main(int, char**) : void", new ViewLocator(
        OUTLINE_VIEW_ID)), "Toggle Breakpoint");
    
        compareBreakpointList("8");
        this.compareView(TEST + ".9", BREAKPOINT_VIEW_ID);
        enterDebugCommandLine("disable 2");
        this.compareView(TEST + ".10", BREAKPOINT_VIEW_ID);
        
        // Can't control where on the ruler it is clicked. But just confirm that hardware
        // breakpoint is there.
        ui.contextClick(new SWTWidgetLocator(Class.forName("org.eclipse.jface.text.source.AnnotationRulerColumn$5")), "Toggle Hardware Breakpoint");
        
        terminateDebugger();
        switchToCPerspective();
    }

    private void compareBreakpointList (String snapshot) throws WidgetSearchException {
        String prevConsole = this.getDebuggerConsoleContent();
        this.enterDebugCommandLine("b");
        getUI().wait(milliseconds(1000));
        String console = this.getDebuggerConsoleContent();
        String output = console.substring(prevConsole.length());
        this.writeAndCompareSnapshot(TEST + "."+snapshot, EclipseUtil.xlate(output));
    }

}