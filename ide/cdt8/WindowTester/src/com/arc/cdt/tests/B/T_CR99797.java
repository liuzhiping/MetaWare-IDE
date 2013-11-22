

package com.arc.cdt.tests.B;


import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;
import com.windowtester.runtime.swt.locator.eclipse.WorkbenchLocator;


public class T_CR99797 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that \"Select All/Copy\" works for MetaWare displays.";

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
    public void testT_CR99797 () throws Exception {
        this.launchDebugger("Queens_AC.elf",true);
        this.showSeeCodeView("Global Variables");
        NamedWidgetLocator namedWidgetLocator = new NamedWidgetLocator(GLOBAL_VARS);
        IUIContext ui = getUI();
        ui.click(namedWidgetLocator);
        ui.keyClick(WT.CTRL,'A');
        ui.keyClick(WT.CTRL,'C');
        String clipboard = EclipseUtil.getClipboardText();
        this.writeAndCompareSnapshot("T_CR99797.1",clipboard);
        
        this.showSeeCodeView("Functions");
        namedWidgetLocator = new NamedWidgetLocator(FUNCS);
        ui.click(namedWidgetLocator);
        ui.keyClick(WT.CTRL,'A');
        ui.contextClick(namedWidgetLocator, "Copy");
        clipboard = EclipseUtil.getClipboardText();
        this.writeAndCompareSnapshot("T_CR99797.2",clipboard);

        this.terminateDebugger();
    }

}