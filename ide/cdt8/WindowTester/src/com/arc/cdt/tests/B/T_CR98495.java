

package com.arc.cdt.tests.B;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.LabeledLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.TableItemLocator;


public class T_CR98495 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that Build Variables button shows up in \"Additional Compiler Flags\" dialog.";

    public static final String CATEGORY = PROJECT_MANAGEMENT;

    private static final String PROJECT = "Queens_AC";

    /**
     * Main test method.
     */
    public void testT_CR98495 () throws Exception {
        switchToCPerspective(); // in case previous test left in wrong perspective

        this.bringUpPropertiesDialog(PROJECT, new IUIRunnable() {

            @Override
            public void run (IUIContext ui) throws WidgetSearchException {

                ui.click(computeTreeItemLocator("C\\/C++ Build/Settings"));

                ui.click(computeTreeItemLocator("MetaWare ARC C\\/C++ Compiler/General"));
              
                ui.click(new SWTWidgetLocator(ToolItem.class, "", 0, new SWTWidgetLocator(ToolBar.class,
                    new LabeledLocator(Composite.class, "  Additional Compiler Flags"))));
                ui.wait(new ShellShowingCondition("Enter Value"));
                Control enterShell = (Control)ui.getActiveWindow();
                ui.click(new ButtonLocator("Build Variable..."));
                ui.wait(new ShellShowingCondition("Select build variable"));
                ui.click(new TableItemLocator("eclipse_home"));
                ui.click(new TableItemLocator("ProjDirPath"));
                ui.click(new ButtonLocator("Cancel"));            
                ui.wait(new ShellDisposedCondition("Select build variable"));
                // Bug in WindowTester: this selects wrong Cancel button
                //ui.click(new ButtonLocator("Cancel"));
                ui.click(EclipseUtil.findButtonLocator("Cancel", enterShell));
                ui.wait(new ShellDisposedCondition("Enter Value"));
                ui.click(new ButtonLocator("Cancel"));
                ui.wait(new ShellDisposedCondition("Properties for Queens_AC"));
            }
        });

    }

}