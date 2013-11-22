

package com.arc.cdt.tests.B;


import junit.framework.Assert;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Widget;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.arc.cdt.testutil.EclipseUtil.IMatch;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.locator.IWidgetReference;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;


public class T_CR97745 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that there are no unsupported tools appearing in the" +
    " toolchain editor selection dialog.";
    public static final String CATEGORY = PROJECT_MANAGEMENT;

    private static final String PROJECT = "Queens_AC";

    /**
     * Main test method.
     */
    public void testT_CR97745() throws Exception {
        switchToCPerspective();
        this.bringUpPropertiesDialog(PROJECT, new IUIRunnable(){

			@Override
			public void run(IUIContext ui) throws WidgetSearchException {
				IWidgetLocator w = EclipseUtil.computeTreeItemLocator(ui,"C\\/C++ Build/Tool Chain Editor");
                ui.click(w);
                ui.click(new ButtonLocator("Select Tools..."));
                ui.wait(new ShellShowingCondition("Select tools"));
                IWidgetReference tables[] = EclipseUtil.findAllWidgetLocators(new IMatch(){

					@Override
					public boolean matches(Widget widget) {
						return widget instanceof Table;
					}}, (Control)ui.getActiveWindow());
                Assert.assertTrue(tables.length == 2);
                compareWidget("T_CR97745.1", tables[0]);
                ui.click(new ButtonLocator("Cancel"));
                ui.wait(new ShellDisposedCondition("Select tools"));
			}});
         
    }

}