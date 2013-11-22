

package com.arc.cdt.tests.B;


import junit.framework.Assert;

import org.eclipse.ui.IViewPart;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.locator.IWidgetReference;


public class T_CR97726 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm \\r does what is expected in Program Output display";

    public static final String CATEGORY = DEBUGGER_INTEGRATION;

    private static final String LAUNCH = "cr97726.elf";

    /**
     * Main test method.
     */
    public void testT_CR97726() throws Exception {
        switchToDebugPerspective();
        this.launchDebugger(LAUNCH,false);
        this.showSeeCodeView("Program [oO]utput");
        this.clickResumeButton();
        this.waitForLaunchTermination(15000);
        
        IViewPart view = EclipseUtil.findOpenView(SEECODE_VIEW_ID, "output");
        IWidgetReference ref = EclipseUtil.getViewReference(view);
        
        String text = EclipseUtil.getText(ref);
        Assert.assertTrue(text.indexOf("2:  100%") >= 0);
        this.terminateDebugger();
         
    }

}