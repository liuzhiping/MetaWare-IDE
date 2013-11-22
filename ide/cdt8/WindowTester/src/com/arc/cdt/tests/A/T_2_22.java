

package com.arc.cdt.tests.A;


import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.swt.locator.ButtonLocator;


public class T_2_22 extends UIArcTestCaseSWT {
    
    public static final String DESCRIPTION = "Test debugger license failure.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;

    /**
     * Main test method.
     * NOTE: this test must run without "LM_LICENSE_FILE" environment variable set.
     */
    public void testT_2_22 () throws Exception {
        IUIContext ui = getUI();
        this.switchToDebugPerspective();
        this.launchDebugger("Queens_AC NO License",false,false);
        ui.wait(getLicenseFailureDialogCondition(true));
        compareShellContent("T_2_22.1");
        ui.click(new ButtonLocator("OK"));
        ui.wait(getLicenseFailureDialogCondition(false));
    }
}