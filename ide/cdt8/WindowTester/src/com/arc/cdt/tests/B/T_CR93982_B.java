/*
 * T_CR93378
 * $Revision$
 * $Date$
 *
 * CONFIDENTIAL AND PROPRIETARY INFORMATION.
 * Copyright 2008 ARC International (Unpublished).
 * All Rights Reserved.
 * This document, material and/or software contains confidential and
 * proprietary information of ARC International and is protected by copyright,
 * trade secret and other state, federal, and international laws, and may be
 * embodied in patents issued or pending. Its receipt or possession does not
 * convey any rights to use, reproduce, disclose its contents, or to
 * manufacture, or sell anything it may describe.  Reverse engineering is
 * prohibited, and reproduction, disclosure or use without specific written
 * authorization of ARC International is strictly forbidden.  ARC and the ARC
 * logotype are trademarks of ARC International.
 */
package com.arc.cdt.tests.B;

import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;


public class T_CR93982_B extends UIArcTestCaseSWT {
    // NOTE: we also have a manual test T_CR93982.
    
    public static final String DESCRIPTION = "Confirm that scroller works on Source view when a file is " +
    " shown while the debug context is referencing something without source";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    
    private static final String LAUNCH_NAME = "Graphics pipeline 2";
    
    public void testT_CR93982_B() throws Exception {
        setCanonicalSize();
        switchToDebugPerspective();
        this.launchDebugger(LAUNCH_NAME, false);
        this.waitUntilDebuggerStops(30000);
        IUIContext ui = getUI();
        ui.wait(milliseconds(700));
        ui.click(computeTreeItemLocator(LAUNCH_NAME + ".*/grmain.*"));
        this.showSeeCodeView("Source");
        ui.click(new NamedWidgetLocator("source.combo.source"));
        ui.enterText("grmain.c");
        ui.keyClick(WT.CR);
        ui.wait(milliseconds(500));
        this.compareSeeCodeView("T_CR93982.1", "source");
        
        ui.click(EclipseUtil.findView(SEECODE_VIEW_ID,"source"));
        for (int i = 0; i < 20; i++) {
            ui.keyClick(WT.ARROW_DOWN);
            ui.wait(milliseconds(250));
        }       
        this.compareSeeCodeView("T_CR93982.2", "source");
       
        this.terminateDebugger();
    }
    
}
