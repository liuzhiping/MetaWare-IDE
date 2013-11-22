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


public class T_MQX_7 extends UIArcTestCaseSWT {
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    public static final String DESCRIPTION = "Source code in libraries: " +
        "This test ensures that the IDE can see the source code of functions that are in libraries. "+ 
        "Run the Demo application for a few seconds and suspend it. " +
        "Set a BP on line 65 of mutexb.c.  (Function MutexB() in MutexB thread). " +
        "Run to BP, and then do a Source Step In to the _mutex_unlock() function. " +
        "IDE should open mu_ulock.c in its source window and you should be able to step through the code.";

    public void testT_MQX_7() throws Exception {
        throw new ManualException();
    }

}
