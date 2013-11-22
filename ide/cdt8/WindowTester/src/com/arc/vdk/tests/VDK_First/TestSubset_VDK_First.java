/*
 * TestSubset_VDK_First
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
 * authorization of ARC International is strictly forbidden. 
 * ARC and the ARC logotype are trademarks of ARC International.
 */

package com.arc.vdk.tests.VDK_First;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.arc.cdt.testutil.TestUtil;


public class TestSubset_VDK_First extends TestCase {

    public static TestSuite suite () {
        final TestSuite suite = new TestSuite();
        TestUtil.extractSubsetOfTests(suite,
        	TestSubset_VDK_First.class.getPackage().getName(),true);
        return suite;
    }
}
