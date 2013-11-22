package com.arc.cdt.tests.A;

import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.swt.locator.CComboItemLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;


public class T_4_4 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Exhaustively test the Register view modes.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    /**
     * Main test method.
     */
    public void testT_4_4 () throws Exception {
        switchToCPerspective(); // in case previous test left in wrong perspective
        setCanonicalSize();
        IUIContext ui = getUI();
        this.setDefaultBuildProperties("Queens_AC");
        cleanProject("Queens_AC");
        buildProject("Queens_AC");
        invokeDebuggerFor("Queens_AC", true);
        for (int i = 0; i < 6; i++)
            this.clickStepIntoButton();
        showView(VARIABLE_VIEW_ID);
        compareView("T_4_4.0", VARIABLE_VIEW_ID);
        showView(REGISTER_VIEW_ID);
        ui.click(new NamedWidgetLocator("filter"));
        ui.keyClick('r');
        ui.keyClick('?');
        ui.keyClick('2');
        ui.keyClick(WT.CR);
        compareView("T_4_4.1",REGISTER_VIEW_ID);
        ui.keyClick(WT.DEL);
        ui.keyClick(WT.CR);
        compareView("T_4_4.2",REGISTER_VIEW_ID);
        ui.click(new CComboItemLocator("Signed", new NamedWidgetLocator("format")));
        compareView("T_4_4.3",REGISTER_VIEW_ID);
        ui.click(new CComboItemLocator("Unsigned", new NamedWidgetLocator("format")));
        compareView("T_4_4.4",REGISTER_VIEW_ID);
        ui.click(new CComboItemLocator("Float", new NamedWidgetLocator("format")));
        compareView("T_4_4.5",REGISTER_VIEW_ID);
        ui.click(new CComboItemLocator("Binary", new NamedWidgetLocator("format")));
        compareView("T_4_4.6",REGISTER_VIEW_ID);
        ui.click(new CComboItemLocator("Fraction", new NamedWidgetLocator("format")));
        compareView("T_4_4.7",REGISTER_VIEW_ID);
        ui.click(new CComboItemLocator("Octal", new NamedWidgetLocator("format")));
        compareView("T_4_4.8",REGISTER_VIEW_ID);
        ui.click(new CComboItemLocator("Signed", new NamedWidgetLocator("format")));
        compareView("T_4_4.9",REGISTER_VIEW_ID);
        ui.click(new CComboItemLocator("Hex", new NamedWidgetLocator("format")));
        ui.click(new CComboItemLocator("core", new NamedWidgetLocator("banks")));
        compareView("T_4_4.10",REGISTER_VIEW_ID);
        ui.click(new CComboItemLocator("aux", new NamedWidgetLocator("banks")));
        compareView("T_4_4.11",REGISTER_VIEW_ID);
        ui.click(new CComboItemLocator("sim", new NamedWidgetLocator("banks")));
        compareView("T_4_4.12",REGISTER_VIEW_ID);
        terminateDebugger();
        switchToCPerspective();
    }

}