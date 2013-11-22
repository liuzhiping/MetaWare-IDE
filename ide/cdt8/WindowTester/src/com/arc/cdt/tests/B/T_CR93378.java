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
import java.io.IOException;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.WidgetSearchException;

/**
 * Tests that all SeeCode displays for a CMPD session come up for a single instance.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class T_CR93378 extends UIArcTestCaseSWT {
    
    public static final String DESCRIPTION = "Test that SeeCode Globals display is properly populated in CMPD";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    private static final String DISPLAY_NAME = "Global Variables";
    private static final String DISPLAY_KIND = "globals";
    private static final String LAUNCH_NAME = "Graphics pipeline 2";
    private int snapShotCount = 0;
    
    public void testT_CR93378() throws Exception {
        setCanonicalSize();
        switchToDebugPerspective();
        this.launchDebugger(LAUNCH_NAME, false);
        this.waitUntilDebuggerStops(30000);
        getUI().wait(milliseconds(500));
        getUI().click(computeTreeItemLocator(LAUNCH_NAME + ".*"));
        this.showSeeCodeView(DISPLAY_NAME);
        
        snapShotEachDisplayInstance();
        
        this.terminateDebugger();
        this.waitForLaunchTermination(15000);
        EclipseUtil.removeTerminatedLaunches();
        
        this.launchDebugger(LAUNCH_NAME,false);
        this.waitUntilDebuggerStops(30000);
        getUI().click(computeTreeItemLocator(LAUNCH_NAME + ".*"));
        
        snapShotEachDisplayInstance();
        
        this.terminateDebugger();
    }
    
    private void snapShotEachDisplayInstance() throws WidgetSearchException, IOException{
        doForProcess("grmain");
        doForProcess("displaymain");
        doForProcess("rendermain0");
        doForProcess("rendermain1");
    }
    
    private void doForProcess(String processName) throws WidgetSearchException, IOException{
        getUI().click(computeTreeItemLocator(LAUNCH_NAME+".*/"+processName + ".*"));
        this.makeSeeCodeDisplayVisible(DISPLAY_KIND);
        getUI().wait(milliseconds(500));
        String s = "";
        if (isLinux()) s = "linux."; // Linux version is different
        this.compareSeecodeDisplay("T_CR93378." + s+(++snapShotCount), DISPLAY_KIND);      
    }

}
