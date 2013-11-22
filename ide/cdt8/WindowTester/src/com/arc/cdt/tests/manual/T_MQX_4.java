/*
 * T_1_1
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
package com.arc.cdt.tests.manual;

import com.arc.cdt.testutil.ManualException;
import com.arc.cdt.testutil.UIArcTestCaseSWT;


public class T_MQX_4 extends UIArcTestCaseSWT {
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    public static final String DESCRIPTION = "Exit IDE and Restart test: " +
            "Run the Demo application as above.  Terminate it and Exit the IDE " +
            "Restart the IDE, and click on the Debug button to restart the last execution " +
            "Ensure that unchanged application is *not* rebuilt prior to debugger running. " +
            "(I see it rebuild consistently on my machine, not sure why, but this isn't correct.)";

    public void testT_MQX_4() throws Exception {
        throw new ManualException();
    }

}
