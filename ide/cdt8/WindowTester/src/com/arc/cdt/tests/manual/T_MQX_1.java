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


public class T_MQX_1 extends UIArcTestCaseSWT {
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    public static final String DESCRIPTION = "Breakpoint in other threads: Use the \"Demo\" example in the examples workspace (C:\\ARC\\mqx_rtos2.51_arc600\\examples) " +
    "Being execution, it will run to Main.  Let it run again for a few seconds then Suspend it. " +
    "Using the call stack in the Debug window, choose the SemB() function in the SemB thread. " +
    "Click on the SemB() function in the list to display the source in the IDE Source window. " +
    "Set a BP on the sem_result = _sem_post(Sem1_handle); line by doubling clicking next to it " +
    "in the source window.  Ensure that the debugger can then Run to the breakpoint with any crashes " +
    " or problems.  -Repeat test but insert BP using MW Source window instead.";

    public void testT_MQX_1() throws Exception {
        throw new ManualException();
    }

}
