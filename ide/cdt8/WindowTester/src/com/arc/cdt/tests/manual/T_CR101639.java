/*
 * $Revision$
 * $Date$
 *
 * CONFIDENTIAL AND PROPRIETARY INFORMATION.
 * Copyright 2011 ARC International (Unpublished).
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


public class T_CR101639 extends UIArcTestCaseSWT {
    public static final String CATEGORY = INSTALLATION;
    public static final String DESCRIPTION = 
        "Check Classic Update under Preferences->General-Capabilities. The select Help->Software Updates->Manage Configuration."+
        "Confirm that there are no errors in the installation. Also confirm that Subclipse can be installed from " +
        " http://subclipse.tigris.org/update_1.6.x. Don't installation, just see if it can be installed without a conflic error.";
    public void testT_CR1639() throws Exception {
        throw new ManualException();
    }

}
