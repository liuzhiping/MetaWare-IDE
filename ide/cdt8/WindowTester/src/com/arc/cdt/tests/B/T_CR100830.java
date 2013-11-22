

package com.arc.cdt.tests.B;


import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IViewPart;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.condition.IsSelectedCondition;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.locator.IWidgetReference;
import com.windowtester.runtime.locator.XYLocator;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.ComboItemLocator;
import com.windowtester.runtime.swt.locator.FilteredTreeItemLocator;
import com.windowtester.runtime.swt.locator.LabeledLocator;
import com.windowtester.runtime.swt.locator.LabeledTextLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;
import com.windowtester.runtime.swt.locator.TreeItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ContributedToolItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;
import com.windowtester.runtime.swt.locator.eclipse.WorkbenchLocator;


public class T_CR100830 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that watchpoints can be set and edited from the MetaWare watchpoint display. (Test cr100921 handles the breakpoint display.)";

    public static final String CATEGORY = DEBUGGER_INTEGRATION;

    /*
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp () throws Exception {
        super.setUp();
        IUIContext ui = getUI();
        ui.ensureThat(new WorkbenchLocator().hasFocus());
    }

    /**
     * Main test method.
     */
    public void testT_CR100830 () throws Exception {
        IUIContext ui = getUI();
        this.switchToDebugPerspective();
        this.launchDebugger("uarttest", false);
        pause(ui, 3000);
        this.clickStopButton();
        this.waitUntilDebuggerStops(10000);

        this.showView(COMMAND_VIEW_ID);


        this.enterDebugCommandLine("b serl_pol.c!281");
        this.clickResumeButton();
        this.waitUntilDebuggerStops(2000);
        
        this.showSeeCodeView("Watchpoints");

        ui.click(new ContributedToolItemLocator("watchdisp.button.id4"));
        ui.wait(new ShellShowingCondition("Create watchpoint"));
        setWpFromDisplay(false, "0x80800", null, null);
        ui.wait(new ShellDisposedCondition("Create watchpoint"));

        String s = getWatchpointList("command w");

        boolean condition = s.indexOf("H 0x00080800") >= 0 && s.indexOf("data-write") >= 0;
        if (!condition) {
            writeStringToFile("T_100830_1.txt",s);
            assertTrue(condition);
        }

        s = getWatchpointList("w");

        condition = s.indexOf("H watch 0x00080800, length 4") >= 0;
        if (!condition) {
            writeStringToFile("T_100830_1.txt",s);
            assertTrue(condition);
        }

        ui.click(new XYLocator(new NamedWidgetLocator("watchdisp"), 273, 14));
        ui.click(new ContributedToolItemLocator("watchdisp.button.id5"));
        ui.wait(new ShellShowingCondition("Edit watchpoint"));
        setWpFromDisplay(false, null, "i > 0", "_mqx_idle_task[0x10001]");
        ui.wait(new ShellDisposedCondition("Edit watchpoint"));

        confirmWpThreadProperties("0x00080800", true, false, "i > 0");
        confirmWatchpointSetting("0x00080800", "_mqx_idle_task", "0x10001", 1, "i > 0",4);

        ui.contextClick(new TreeItemLocator("uarttest.elf.*80800.*", new ViewLocator(
            "org.eclipse.debug.ui.BreakpointView")), "Breakpoint Properties...");
        ui.wait(new ShellShowingCondition("Properties for "));
        ui.click(new FilteredTreeItemLocator("Filtering"));
        TreeItemLocator tree = new TreeItemLocator("MetaWare Debugger \\([^)]*\\) \\(Suspended\\)", new LabeledLocator(
            Tree.class, "&Restrict to Selected Targets and Threads:"));
        final IWidgetLocator treeRef = ui.find(tree);
        EclipseUtil.expandTreeItem(ui, (IWidgetReference) treeRef);

        TreeItemLocator t = new TreeItemLocator(
            "MetaWare Debugger \\([^)]*\\) \\(Suspended\\)/Thread \\[_mqx_idle_task\\] \\(Suspended\\)",
            new LabeledLocator(Tree.class, "&Restrict to Selected Targets and Threads:"));

        ui.assertThat(new IsSelectedCondition(t, true));
        // ui.click(t);
        IWidgetLocator treeItem = ui.find(t);

        EclipseUtil.clickTreeItem(ui, (IWidgetReference) treeItem);

        ui.assertThat(new IsSelectedCondition(t, false));

        t = new TreeItemLocator(
            "MetaWare Debugger \\([^)]*\\) \\(Suspended\\)/Thread \\[\\*?hello\\] \\(Suspended[^)]*\\)",
            new LabeledLocator(Tree.class, "&Restrict to Selected Targets and Threads:"));
        ui.assertThat(new IsSelectedCondition(t, false));

        treeItem = ui.find(t);

        EclipseUtil.clickTreeItem(ui, (IWidgetReference) treeItem);
        ui.assertThat(new IsSelectedCondition(t, true));
        ui.click(new ButtonLocator("OK"));
        ui.wait(new ShellDisposedCondition("Properties for "));

        confirmWpThreadProperties("0x00080800", false, true, "i > 0");
        confirmWatchpointSetting("0x00080800", "hello", "0x10002", 2, "i > 0",4);
        

        this.enterDebugCommandLine("w 0x80820, len 2, eval i == -1, thread 2");
        confirmWatchpointSetting("0x80820", "hello", "0x10002", 2, "i == -1", 2);
        
        ui.click(new ContributedToolItemLocator("watchdisp.button.id4"));
        ui.wait(new ShellShowingCondition("Create watchpoint"));
        setWpFromDisplay(true, "io_dev_ptr", null, null);
        ui.wait(new ShellDisposedCondition("Create watchpoint"));
        
        ui.wait(new ShellShowingCondition("Debugger Note"));
        ui.click(new ButtonLocator("OK"));
        ui.wait(new ShellDisposedCondition("Debugger Note"));
        s = getWatchpointList("command w");

        condition = s.indexOf("io_dev_ptr data-write") >= 0;
        if (!condition) {
            writeStringToFile("T_100830_1.txt",s);
            assertTrue(condition);
        }

        s = getWatchpointList("w");

        condition = s.indexOf("watch io_dev_ptr, length 4") >= 0;
        if (!condition) {
            writeStringToFile("T_100830_1.txt",s);
            assertTrue(condition);
        }
        this.terminateDebugger();
    }

    private void setWpFromDisplay (boolean expr, String addr, String condition, String thread)
        throws WidgetSearchException {
        IUIContext ui = getUI();
        if (addr != null) {
            ui.click(new ButtonLocator(expr ? "Expression" : "Address"));
            this.enterTextWithinNamedWidget(expr ? "EXPRESSION" : "ADDRESS", addr, false);
        }

        this.setCheckBox("CONDITION_ENABLED", condition != null);
        if (condition != null)
            this.enterTextWithinNamedWidget("CONDITION", condition, false);

        this.setCheckBox("THREAD_SPECIFIC", thread != null);

        if (thread != null)
            ui.click(new ComboItemLocator(thread, new NamedWidgetLocator("THREAD")));
        ui.click(new ButtonLocator("OK"));
    }

    private void confirmWpThreadProperties (String expr, boolean first, boolean second, String condition)
        throws WidgetSearchException {
        IUIContext ui = getUI();
        String loc = "uarttest.elf [expression: '" + expr + "']";
        if (condition != null)
            loc += " [condition: " + condition + "]";
        this.showView("org.eclipse.debug.ui.BreakpointView");
        ui.click(new TreeItemLocator(loc, new ViewLocator("org.eclipse.debug.ui.BreakpointView")));
        ui.contextClick(
            new TreeItemLocator(loc, new ViewLocator("org.eclipse.debug.ui.BreakpointView")),
            "Breakpoint Properties...");
        ui.wait(new ShellShowingCondition("Properties for "));
        ui.click(new FilteredTreeItemLocator("Common"));
        if (condition == null)
            condition = "";
        LabeledTextLocator condText = new LabeledTextLocator("Condition:");
        ui.assertThat(condText.hasText(condition));

        ui.click(new FilteredTreeItemLocator("Filtering"));

        TreeItemLocator treeItemLocator = new TreeItemLocator("MetaWare Debugger \\([^)]*\\) \\(Suspended\\)",
            new LabeledLocator(Tree.class, "&Restrict to Selected Targets and Threads:"));
        IWidgetLocator treeItemRef = ui.find(treeItemLocator);
        EclipseUtil.expandTreeItem(ui, (IWidgetReference) treeItemRef);
        TreeItemLocator t = new TreeItemLocator(
            "MetaWare Debugger \\([^)]*\\) \\(Suspended\\).Thread \\[\\*?_mqx_idle_task\\] \\(Suspended\\)",
            new LabeledLocator(Tree.class, "&Restrict to Selected Targets and Threads:"));
        ui.assertThat(new IsSelectedCondition(t, first));
        t = new TreeItemLocator(
            "MetaWare Debugger \\([^)]*\\) \\(Suspended\\).Thread \\[\\*?hello\\] \\(Suspended[^)]*\\)",
            new LabeledLocator(Tree.class, "&Restrict to Selected Targets and Threads:"));
        ui.assertThat(new IsSelectedCondition(t, second));
        ui.click(new ButtonLocator("Cancel"));
        ui.wait(new ShellDisposedCondition("Properties for "));
    }

    private String getWatchpointList (String cmd) throws WidgetSearchException {
        String consoleString = this.getDebuggerConsoleContent();
        this.enterDebugCommandLine(cmd);
        return this.getDebuggerConsoleContent().substring(consoleString.length());
    }

    private void confirmWatchpointSetting (
        String location,
        String taskName,
        String taskID,
        int threadNumber,
        String cond, int len) throws WidgetSearchException {
        String consoleString = this.getDebuggerConsoleContent();
        this.enterDebugCommandLine("command w");
        String consoleString2 = this.getDebuggerConsoleContent().substring(consoleString.length());
        String[] substrings = {
                "condition:[task:" + taskID + " " + taskName,
                (cond != null ? "expn:" + cond : "") + "]",
                "data-write" };
        for (String s : substrings) {
            if (consoleString2.indexOf(s) < 0) {
                this.writeStringToFile("T_CR100830_1.txt", consoleString2);
                assertTrue("Missing console output: " + s, false);
            }
        }

        IViewPart bpview = EclipseUtil.makeViewVisible(SEECODE_VIEW_ID, "watchdisp");
        assertTrue("Watchpoint view visibility", bpview != null);

        IWidgetReference bpviewref = EclipseUtil.getViewReference(bpview);

        String text = EclipseUtil.getText(bpviewref);
        assertTrue("Content from watchpoint view", text != null);
        for (String s : substrings) {
            if (text == null || text.indexOf(s) < 0) {
                this.writeStringToFile("T_CR100830_2.txt", text);
                assertTrue("Missing bp display string: " + s, false);
            }
        }

        String cons = this.getDebuggerConsoleContent();
        this.enterDebugCommandLine("w");
        cons = this.getDebuggerConsoleContent().substring(cons.length());
        String s = "watch " + location + ", length " + len +", " + (cond != null ? "eval " + cond : "") + ", thread " + threadNumber;
        if (cons.indexOf(s) < 0) {
            this.writeStringToFile("T_CR100830_3.txt", cons);
            assertTrue("Missing w console output: " + s, false);
        }
    }

}