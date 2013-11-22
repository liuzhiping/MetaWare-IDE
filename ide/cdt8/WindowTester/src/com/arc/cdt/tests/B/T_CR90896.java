

package com.arc.cdt.tests.B;


import junit.framework.Assert;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.locator.IWidgetReference;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;


public class T_CR90896 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that \"Format\" context menu item"+
       " exists for Expression items and that it works.";

    public static final String CATEGORY = DEBUGGER_INTEGRATION;

    private static final String PROJECT = "Queens_AC";

    private static final String LAUNCH_NAME = PROJECT + ".elf";

    /**
     * Main test method.
     */
    public void testT_CR90896 () throws Exception {

        switchToDebugPerspective();

        this.launchDebugger(LAUNCH_NAME, false);
        this.waitUntilDebuggerStops(20000);
        
        this.showView(EXPRESSION_VIEW_ID);
        
        EclipseUtil.clearExpressionView();
        
        IUIContext ui = getUI();
        
        ui.contextClick(new SWTWidgetLocator(Tree.class, new ViewLocator(EXPRESSION_VIEW_ID)),
		              "&Add Watch Expression...");
        ui.wait(new ShellShowingCondition("Add Watch Expression"));
        ui.enterText("&X");
        ui.click(new ButtonLocator("OK"));
        ui.wait(new ShellDisposedCondition("Add Watch Expression"));
        pause(ui,500);
        IWidgetReference expr = EclipseUtil.findView(EXPRESSION_VIEW_ID);
        IWidgetLocator item = EclipseUtil.computeTreeItemLocator(ui,"\"&X\"\\|0x000.....",(Control)(expr.getWidget()));
        
        String name = EclipseUtil.getText(item);
        
        Assert.assertTrue(name != null);
        if (name == null) return; // keep compiler happy
        
        int i = name.indexOf("0x");
        Assert.assertTrue(i > 0);
        int adr = Integer.parseInt(name.substring(i+2),16);
        ui.contextClick(item,"Format/Decimal");
        name = EclipseUtil.getText(item);
        Assert.assertTrue(Integer.parseInt(name.substring(i)) == adr);
             
        this.terminateDebugger();

    }

}