

package com.arc.cdt.tests.B;


import junit.framework.Assert;

import org.eclipse.cdt.managedbuilder.ui.properties.StringOrFileListWidget;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.locator.IWidgetReference;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.ComboItemLocator;
import com.windowtester.runtime.swt.locator.LabeledLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.TabItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.WorkbenchLocator;


public class T_CR99966 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that extra dependencies are preserved.";

    public static final String CATEGORY = PROJECT_MANAGEMENT;

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
    public void testT_CR99966 () throws Exception {
    	this.switchToCPerspective();
        this.bringUpPropertiesDialog("Array", new IUIRunnable() {

            @Override
            public void run (IUIContext ui) throws Exception {
                ui.click(EclipseUtil.computeTreeItemLocator(ui,"C\\/C++ Build/Settings"));
                ui.click(new TabItemLocator("Build Steps"));
                ui.click(new ComboItemLocator("String list", new SWTWidgetLocator(Combo.class, new LabeledLocator(
                    StringOrFileListWidget.class,
                    "Specify any additional dependencies that these build steps may require"))));
                ui.click(new SWTWidgetLocator(ToolItem.class, "", 0, new SWTWidgetLocator(ToolBar.class,
                    new LabeledLocator(Composite.class, "  Define dependents list"))));
                ui.wait(new ShellShowingCondition("Enter Value"));
                ui.enterText("one");
                ui.click(new ButtonLocator("OK"));
                ui.wait(new ShellDisposedCondition("Enter Value"));
                ui.click(new SWTWidgetLocator(ToolItem.class, "", 0, new SWTWidgetLocator(ToolBar.class,
                    new LabeledLocator(Composite.class, "  Define dependents list"))));
                ui.wait(new ShellShowingCondition("Enter Value"));
                ui.enterText("two");
                ui.click(new ButtonLocator("OK"));
                ui.wait(new ShellDisposedCondition("Enter Value"));
                // ui.click(new ButtonLocator("&Apply"));
                // ui.click(new ButtonLocator("OK"));

            }
        });

        this.bringUpPropertiesDialog("Array", new IUIRunnable() {

            @Override
            public void run (IUIContext ui) throws Exception {
                ui.click(EclipseUtil.computeTreeItemLocator(ui,"C\\/C++ Build/Settings"));
                ui.click(new TabItemLocator("Build Steps"));
                IWidgetReference listWidget = EclipseUtil.findListLocator((Control) ui.getActiveWindow());
                compareWidget("T_CR99966.1", listWidget);
                ui.click(new ComboItemLocator("None", new SWTWidgetLocator(Combo.class, new LabeledLocator(
                    StringOrFileListWidget.class,
                    "Specify any additional dependencies that these build steps may require"))));
            }
        });

        this.bringUpPropertiesDialog("Array", new IUIRunnable() {

            @Override
            public void run (IUIContext ui) throws Exception {
                ui.click(EclipseUtil.computeTreeItemLocator(ui,"C\\/C++ Build/Settings"));
                ui.click(new TabItemLocator("Build Steps"));
                IWidgetLocator loc = ui.find(new NamedWidgetLocator("StringOrFileList.combo"));
                String text = EclipseUtil.getText(loc);
                if (!"None".equals(text)){
                    System.out.println("text=" + text);
                }
                Assert.assertTrue("None".equals(text));

                ui.click(new ButtonLocator("Cancel"));
                ui.wait(new ShellDisposedCondition("Properties for Array"));
            }
        });
    }

}