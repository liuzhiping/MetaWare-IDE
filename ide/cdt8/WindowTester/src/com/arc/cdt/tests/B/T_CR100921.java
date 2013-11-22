

package com.arc.cdt.tests.B;


import junit.framework.Assert;

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
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;
import com.windowtester.runtime.swt.locator.TreeItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ContributedToolItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;
import com.windowtester.runtime.swt.locator.eclipse.WorkbenchLocator;


public class T_CR100921 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that thread-specific breakpoints can be set from command line and MetaWare breakpoint display. Also tests cr100830.";

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
    public void testT_CR100921() throws Exception {
        IUIContext ui = getUI();
        this.switchToDebugPerspective();
        this.launchDebugger("uarttest", false);
        pause(ui,5000);
        this.clickStopButton();
        //HACK: Windows ignores the first press of the "Stop" button as of CDT 7. Doesn't appear to happen
        // when done manually.
        pause(ui,500);
        this.clickStopButton();
        Assert.assertTrue("Suspend attempt",this.waitUntilDebuggerStops(10000));
        
        this.showView(COMMAND_VIEW_ID);

        this.showSeeCodeView("Breakpoints");
        ui.click(new ContributedToolItemLocator("break.button.id4"));
        ui.wait(new ShellShowingCondition("Create breakpoint"));
        setBpFromDisplay("10", "i > j", "hello[0x10002]");
        ui.wait(new ShellDisposedCondition("Create breakpoint"));

        confirmBpThreadProperties(51,false,true,"10","i > j");        
        confirmBreakpointSetting(51,"hello","0x10002",2,"i > j");
        
        ui.click(new XYLocator(new NamedWidgetLocator("break"), 273, 30));
        ui.click(new ContributedToolItemLocator("break.button.id5"));
        ui.wait(new ShellShowingCondition("Edit breakpoint"));
        setBpFromDisplay("10",null,"_mqx_idle_task[0x10001]");
        ui.wait(new ShellDisposedCondition("Edit breakpoint"));
        confirmBpThreadProperties(51,true,false,"10",null);
        confirmBreakpointSetting(51,"_mqx_idle_task","0x10001",1,null);
       

        ui.contextClick(new TreeItemLocator("uarttest.c [line: 51] [ignore count: 10]", new ViewLocator(
            "org.eclipse.debug.ui.BreakpointView")), "Breakpoint Properties...");
        ui.wait(new ShellShowingCondition("Properties for "));
        ui.click(new FilteredTreeItemLocator("Filtering"));
        TreeItemLocator tree = new TreeItemLocator(
            "MetaWare Debugger \\([^)]*\\) \\(Suspended\\)",
            new LabeledLocator(Tree.class, "&Restrict to Selected Targets and Threads:"));
        final IWidgetLocator treeRef = ui.find(tree);
        EclipseUtil.expandTreeItem(ui,(IWidgetReference)treeRef);

        TreeItemLocator t = new TreeItemLocator(
            "MetaWare Debugger \\([^)]*\\) \\(Suspended\\)/Thread \\[_mqx_idle_task\\] \\(Suspended\\)",
            new LabeledLocator(Tree.class, "&Restrict to Selected Targets and Threads:"));
      
        ui.assertThat(new IsSelectedCondition(t,true));
       // ui.click(t);
        IWidgetLocator treeItem = ui.find(t);
        
        EclipseUtil.clickTreeItem(ui,(IWidgetReference)treeItem);             
          
        ui.assertThat(new IsSelectedCondition(t,false));

        t = new TreeItemLocator(
            "MetaWare Debugger \\([^)]*\\) \\(Suspended\\)/Thread \\[\\*?hello\\] \\(Suspended[^)]*\\)",
            new LabeledLocator(Tree.class, "&Restrict to Selected Targets and Threads:"));
        ui.assertThat(new IsSelectedCondition(t,false));

        treeItem = ui.find(t);
        
        EclipseUtil.clickTreeItem(ui,(IWidgetReference)treeItem);    
        ui.assertThat(new IsSelectedCondition(t,true));
        ui.click(new ButtonLocator("OK"));
        ui.wait(new ShellDisposedCondition("Properties for "));
        
        confirmBpThreadProperties(51,false,true,"10", null);        
        confirmBreakpointSetting(51,"hello","0x10002",2,null);
        
        this.enterDebugCommandLine("b uarttest.c!50, thread 2, count 10");
        confirmBreakpointSetting(50,"hello","0x10002",2,null);


        
        this.terminateDebugger();
    }

    private void setBpFromDisplay (String count, String condition, String thread) throws WidgetSearchException {
        IUIContext ui = getUI();
             
        this.enterTextWithinNamedWidget("LOCATION","uarttest.c!51",false);
        this.enterTextWithinNamedWidget("COUNT",count != null?count:"",false);
     
        this.setCheckBox("CONDITION_ENABLED",condition != null);
        if (condition != null)
            this.enterTextWithinNamedWidget("CONDITION",condition,false);

        this.setCheckBox("THREAD_SPECIFIC",thread != null);
        this.setCheckBox("TEMPORARY",false);
        
        if (thread != null)
            ui.click(new ComboItemLocator(thread, new NamedWidgetLocator("THREAD")));
        ui.click(new ButtonLocator("OK"));
    }

    private void confirmBpThreadProperties (int line, boolean first, boolean second, String count, String condition) throws WidgetSearchException {
        IUIContext ui = getUI();
        String loc = "uarttest.c [line: " + line + "]";
        if (count != null) loc += " [ignore count: " + count + "]";
        if (condition != null) loc += " [condition: " + condition + "]";
        this.showView("org.eclipse.debug.ui.BreakpointView");
        ui.click(new TreeItemLocator(loc, new ViewLocator(
            "org.eclipse.debug.ui.BreakpointView")));
        ui.contextClick(new TreeItemLocator(loc,
            new ViewLocator("org.eclipse.debug.ui.BreakpointView")), "Breakpoint Properties...");
        ui.wait(new ShellShowingCondition("Properties for "));
        ui.click(new FilteredTreeItemLocator("Filtering"));

        TreeItemLocator treeItemLocator = new TreeItemLocator("MetaWare Debugger \\([^)]*\\) \\(Suspended\\)", new LabeledLocator(
            Tree.class, "&Restrict to Selected Targets and Threads:"));
        IWidgetLocator treeItemRef = ui.find(treeItemLocator);
        EclipseUtil.expandTreeItem(ui, (IWidgetReference)treeItemRef);
        TreeItemLocator t = new TreeItemLocator(
            "MetaWare Debugger \\([^)]*\\) \\(Suspended\\).Thread \\[\\*?_mqx_idle_task\\] \\(Suspended\\)",
            new LabeledLocator(Tree.class, "&Restrict to Selected Targets and Threads:"));
        ui.assertThat(new IsSelectedCondition(t,first));
        t = new TreeItemLocator(
            "MetaWare Debugger \\([^)]*\\) \\(Suspended\\).Thread \\[\\*?hello\\] \\(Suspended[^)]*\\)",
            new LabeledLocator(Tree.class, "&Restrict to Selected Targets and Threads:"));
        ui.assertThat(new IsSelectedCondition(t,second));
        ui.click(new ButtonLocator("Cancel"));
        ui.wait(new ShellDisposedCondition("Properties for "));
    }

    private void confirmBreakpointSetting (int line, String taskName, String taskID, int threadNumber, String cond) throws WidgetSearchException {
        String consoleString = this.getDebuggerConsoleContent();
        this.enterDebugCommandLine("command b");
        String consoleString2 = this.getDebuggerConsoleContent().substring(consoleString.length());
        String[] substrings = { "condition:[task:" + taskID + " " + taskName + ", count:10",(cond != null?"expn:"+cond:"")+"] uarttest.c!" + line};
        for (String s: substrings) {
            if (consoleString2.indexOf(s) < 0) {
                this.writeStringToFile("T_CR100921_1.txt",consoleString2);
                assertTrue("Missing console output: " + s,false);
            }
        }
        
        IViewPart bpview = EclipseUtil.makeViewVisible(SEECODE_VIEW_ID,"break");
        assertTrue("Breakpoint view visibility",bpview != null);
        
        IWidgetReference bpviewref = EclipseUtil.getViewReference(bpview);
        
        String text = EclipseUtil.getText(bpviewref);
        assertTrue("Content from breakpoint view",text != null);
        for (String s: substrings) {
            if (text == null || text.indexOf(s) < 0) {
                this.writeStringToFile("T_CR100921_2.txt",text);
                assertTrue("Missing bp display string: " + s,false);
            }
        }
        
        String cons = this.getDebuggerConsoleContent();
        this.enterDebugCommandLine("b");
        cons = this.getDebuggerConsoleContent().substring(cons.length());
        substrings = new String[]{ "uarttest.c!"+line, (cond != null?"eval " + cond +", ":"") +"count 10, thread " + threadNumber };
        for (String s: substrings) {
            if (cons.indexOf(s) < 0) {
                this.writeStringToFile("T_CR100921_3.txt",cons);
                assertTrue("Missing b console output: " + s,false);
            }
        }
    }

}