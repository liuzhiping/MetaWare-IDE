

package com.arc.cdt.tests.B;


import org.junit.Assert;

import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.locator.XYLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.eclipse.WorkbenchLocator;


public class T_CR99887 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that we can set a breakpoint on a source file in a separate project.";

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
    public void testT_CR99887() throws Exception {
        IUIContext ui = getUI();
        this.launchDebugger("application",true);
        this.waitUntilDebuggerStops(30000);
        this.clickStepIntoButton();
        this.pause(ui,500);
        this.clickStepIntoButton();
        this.pause(ui,500);
        this.clickStepIntoButton();
        this.pause(ui,500);
        String console = this.getDebuggerConsoleContent();
        ui.click(2, new XYLocator(new SWTWidgetLocator(Class.forName("org.eclipse.jface.text.source.AnnotationRulerColumn$5")),5, 76));
        this.showView(COMMAND_VIEW_ID);
        this.enterDebugCommandLine("b");
        this.pause(ui,1000);
        console  = this.getDebuggerConsoleContent().substring(console.length());
        System.out.println("Console:");
        System.out.println(console);
        Assert.assertTrue(console.indexOf("/library/library.c!") > 0);
        this.terminateDebugger();
    }

}