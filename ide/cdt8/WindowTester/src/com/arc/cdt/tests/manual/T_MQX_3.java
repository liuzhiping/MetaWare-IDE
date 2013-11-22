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


public class T_MQX_3 extends UIArcTestCaseSWT {
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    public static final String DESCRIPTION = "Task Aware Debug module integration test: " +
        "Run the demo example " +
        "After a few seconds, Suspend execution " +
        "Choose MQX, Tasks, Task Summary from the menu " + 
        "Ensure that the Task Summary window opens and gets focus " +
        "(i.e. it should not appear behind the IDE window or require a click anywhere to make it appear) " +
        "NOTE:  If this test is repeated with xISS as the execution vehicle, it will fail. " +
        "It appears that the user needs to click on the xISS Terminal window for the MQX window to appear.";


    public void testT_MQX_3() throws Exception {
        throw new ManualException();
    }

}
