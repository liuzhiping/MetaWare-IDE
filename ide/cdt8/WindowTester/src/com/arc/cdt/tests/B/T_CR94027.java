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

import com.arc.cdt.testutil.UIArcTestCaseSWT;
import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;


public class T_CR94027 extends UIArcTestCaseSWT {
    
    public static final String DESCRIPTION = "Test setting a breakpoint on all CMPD processes "+
            "from the command line gets correctly shown in the CDT breakpoint view.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    
    private static final String LAUNCH_NAME = "Graphics pipeline 2";
    
    public void testT_CR94027() throws Exception {
        setCanonicalSize();
        switchToDebugPerspective();
        this.launchDebugger(LAUNCH_NAME, false);
        this.waitUntilDebuggerStops(30000);
        getUI().wait(milliseconds(500));
        getUI().click(computeTreeItemLocator(LAUNCH_NAME + ".*"));
        this.showView(COMMAND_VIEW_ID);
        this.showView(BREAKPOINT_VIEW_ID);
        this.enterDebugCommandLine("b 0x80c");
        this.compareView("T_CR94027.1", BREAKPOINT_VIEW_ID);
       
        this.terminateDebugger();
    }
    
}
