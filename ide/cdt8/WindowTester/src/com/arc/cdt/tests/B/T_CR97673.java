

package com.arc.cdt.tests.B;


import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.locator.IWidgetReference;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.TableItemLocator;


public class T_CR97673 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that we can replace a tool in a toolchain";

    public static final String CATEGORY = PROJECT_MANAGEMENT;

    private static final String PROJECT_NAME = "TEMP";

    /**
     * Main test method.
     */
    public void testT_CR97673() throws Exception {
        switchToCPerspective();
        this.deleteProject(PROJECT_NAME);
        this.createNewProject(PROJECT_NAME,true);
        
        this.bringUpPropertiesDialog(PROJECT_NAME, new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws WidgetSearchException {
                IWidgetLocator w = EclipseUtil.computeTreeItemLocator(ui,"C\\/C++ Build/Tool Chain Editor");
                ui.click(w);
                w = ui.find(new SWTWidgetLocator(Text.class, new SWTWidgetLocator(Group.class, "Used tools")));
                compareWidget("T_CR97673.1",(IWidgetReference)w,"Used tools list");
                ui.click(new ButtonLocator("Select Tools..."));
                ui.wait(new ShellShowingCondition("Select tools"));
                ui.click(new TableItemLocator("MetaWare Archiver for ARCompact"));
                ui.click(new TableItemLocator("GCC Archiver"));
                ui.click(new ButtonLocator("<<- Replace ->>"));
                ui.click(new ButtonLocator("OK"));
                ui.wait(new ShellDisposedCondition("Select tools"));
                compareWidget("T_CR97673.2",(IWidgetReference)w,"Used tools list");
                
            }});
         this.deleteProject(PROJECT_NAME);
         
    }

}