

package com.arc.cdt.tests.A;


import junit.framework.Assert;

import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Tree;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.TreeItemLocator;


public class T_2_05 extends UIArcTestCaseSWT {
    
    public static final String CATEGORY = BUILD_MANAGEMENT;
    public static final String DESCRIPTION = "Confirm that the ARC version settings are propagated to the build command.";

    /**
     * Main test method.
     */
    public void testT_2_05 () throws Exception {
    	switchToCPerspective();
        bringUpPropertiesDialog("Queens_AC", new IUIRunnable() {

            @Override
            public void run (IUIContext uiContext) throws WidgetSearchException {
                uiContext.click(computeTreeItemLocator("C\\/C++ Build/Settings"));
                uiContext.click(new TreeItemLocator("MetaWare ARC C\\/C++ Compiler/General", new SWTWidgetLocator(
                    Tree.class, new SWTWidgetLocator(SashForm.class))));
                uiContext.click(new TreeItemLocator("ARCompact Assembler/ARCompact Specific", new SWTWidgetLocator(
                    Tree.class, new SWTWidgetLocator(SashForm.class))));
                EclipseUtil.setComboBox(uiContext,"ARC 5");
               // uiContext.click(new ComboItemLocator("ARC 5", EclipseUtil.findComboLocator("ARC 5", (Control)uiContext.getActiveWindow())));

            }
        });

        cleanProject("Queens_AC");
        buildProject("Queens_AC");
        String console = getConsoleContent();
        Assert.assertTrue(console.indexOf("-a5") > 0);
        Assert.assertTrue(console.indexOf("-a6") < 0 && console.indexOf("-arc600") < 0);
        bringUpPropertiesDialog("Queens_AC", new IUIRunnable() {

            @Override
            public void run (IUIContext uiContext) throws WidgetSearchException {
                uiContext.click(new TreeItemLocator("ARCompact Assembler/ARCompact Specific", new SWTWidgetLocator(
                    Tree.class, new SWTWidgetLocator(SashForm.class))));
                //uiContext.click(new ComboItemLocator("ARC 600", EclipseUtil.findComboLocator("ARC 5", (Control)uiContext.getActiveWindow())));
                EclipseUtil.setComboBox(uiContext,"ARC 600");
            }
        });
        
        buildProject("Queens_AC");
        console = getConsoleContent();
        Assert.assertTrue(console.indexOf("-a5") < 0);
        Assert.assertTrue(console.indexOf("-arc600") > 0 || console.indexOf("-a6") > 0);
    }

}