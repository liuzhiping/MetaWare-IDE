

package com.arc.cdt.tests.B;


import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.LabeledLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.TableItemLocator;
import com.windowtester.runtime.swt.locator.TreeItemLocator;


public class T_CR95368 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Test that Build Variables list is not empty.";

    public static final String CATEGORY = DEBUGGER_INTEGRATION;

    private static final String PROJECT = "Queens_AC";

    /**
     * Main test method.
     */
    public void testT_CR95368 () throws Exception {

        switchToCPerspective();

        bringUpPropertiesDialog(PROJECT,new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws WidgetSearchException {
                ui.click(EclipseUtil.computeTreeItemLocator(ui,"C\\/C++ Build/Settings"));
                ui.click(new TreeItemLocator("MetaWare ARC C\\/C++ Compiler/Processor\\\\/Extensions", new SWTWidgetLocator(
                    Tree.class, new SWTWidgetLocator(SashForm.class))));
                ui.click(new TreeItemLocator("MetaWare ARC C\\/C++ Compiler/Include Directories", new SWTWidgetLocator(
                    Tree.class, new SWTWidgetLocator(SashForm.class))));
                ui.click(new SWTWidgetLocator(ToolItem.class, "", 0, new SWTWidgetLocator(ToolBar.class, new LabeledLocator(
                    Composite.class, "  Include Directories (one per line)"))));
                IWidgetLocator cancel = ui.find(new ButtonLocator("Cancel"));
                ui.wait(new ShellShowingCondition("Add directory path"));
                ui.click(new ButtonLocator("Build Variable..."));
                ui.wait(new ShellShowingCondition("Select build variable"));
                ui.click(new TableItemLocator("ProjDirPath"));
                ui.click(new ButtonLocator("OK"));
                ui.wait(new ShellDisposedCondition("Select build variable"));
                ui.click(cancel);
                ui.wait(new ShellDisposedCondition("Add directory path"));
                ui.click(new TreeItemLocator("MetaWare Linker/Search Path", new SWTWidgetLocator(Tree.class,
                    new SWTWidgetLocator(SashForm.class))));
              
                ui.click(new SWTWidgetLocator(ToolItem.class, "", 0, new SWTWidgetLocator(ToolBar.class, new LabeledLocator(
                    Composite.class, "  Library Search Paths (for -lx)"))));
                ui.wait(new ShellShowingCondition("Add directory path"));
                cancel = ui.find(new ButtonLocator("Cancel"));
                ui.click(new ButtonLocator("Build Variable..."));
                ui.wait(new ShellShowingCondition("Select build variable"));
                ui.click(new TableItemLocator("ProjDirPath"));
                ui.click(new ButtonLocator("Cancel"));
                ui.wait(new ShellDisposedCondition("Select build variable"));
                ui.click(cancel);
                ui.wait(new ShellDisposedCondition("Add directory path"));
                ui.click(new TreeItemLocator("MetaWare Linker/Object Files & Libraries", new SWTWidgetLocator(Tree.class,
                    new SWTWidgetLocator(SashForm.class))));
                ui.click(new SWTWidgetLocator(ToolItem.class, "", 0, new SWTWidgetLocator(ToolBar.class, new LabeledLocator(
                    Composite.class, "  Additional Object Files && Libraries"))));
                ui.wait(new ShellShowingCondition("Add file path"));
                cancel = ui.find(new ButtonLocator("Cancel"));
                ui.click(new ButtonLocator("Build Variable..."));
                ui.wait(new ShellShowingCondition("Select build variable"));
                ui.click(new TableItemLocator("ProjDirPath"));
                ui.click(new ButtonLocator("Cancel"));
                ui.wait(new ShellDisposedCondition("Select build variable"));
                ui.click(cancel);
                ui.wait(new ShellDisposedCondition("Add file path"));
                ui.click(new TreeItemLocator("MetaWare Linker/Command files", new SWTWidgetLocator(Tree.class,
                    new SWTWidgetLocator(SashForm.class))));
                ui.click(new SWTWidgetLocator(ToolItem.class, "", 0, new SWTWidgetLocator(ToolBar.class, new LabeledLocator(
                    Composite.class, "  SVR3-style command files"))));
                ui.wait(new ShellShowingCondition("Add file path"));
                cancel = ui.find(new ButtonLocator("Cancel"));
                ui.click(new ButtonLocator("Build Variable..."));
                ui.wait(new ShellShowingCondition("Select build variable"));
                ui.click(new TableItemLocator("ProjDirPath"));
                ui.click(new ButtonLocator("Cancel"));
                ui.wait(new ShellDisposedCondition("Select build variable"));
                ui.click(cancel);
                ui.wait(new ShellDisposedCondition("Add file path"));
                ui.click(new SWTWidgetLocator(ToolItem.class, "", 0, new SWTWidgetLocator(ToolBar.class, new LabeledLocator(
                    Composite.class, "  SVR4-style command files"))));
                ui.wait(new ShellShowingCondition("Add file path"));
                cancel = ui.find(new ButtonLocator("Cancel"));
                ui.click(new ButtonLocator("Build Variable..."));
                ui.wait(new ShellShowingCondition("Select build variable"));
                ui.click(new TableItemLocator("ProjDirPath"));
                ui.click(new ButtonLocator("Cancel"));
                ui.wait(new ShellDisposedCondition("Select build variable"));
                ui.click(cancel);
                ui.wait(new ShellDisposedCondition("Add file path"));
                ui.click(new ButtonLocator("Cancel"));
                
            }});

    }

}