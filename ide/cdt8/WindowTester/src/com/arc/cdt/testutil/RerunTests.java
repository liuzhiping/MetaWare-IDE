/*
 * TestAll
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
package com.arc.cdt.testutil;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.arc.tests.database.DatabaseStuff;
import com.arc.tests.database.ITestDataBase;
import com.arc.tests.database.ITestMetaData;
import com.windowtester.runtime.swt.UITestCaseSWT;

/**
 * Run all tests that do not have "Passed" status in the database.
 * 
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */

public class RerunTests extends TestCase {

	@SuppressWarnings("unchecked")
    public static TestSuite suite() {
		TestSuite suite = new TestSuiteWithDatabaseUpdate();
		EclipseUtil.closeAllEditors();

		Class<? extends TestCase> classes[];
		try {
			classes = (Class< ? extends TestCase>[]) PackageUtil.getClasses("com.arc.cdt.tests",
					UITestCaseSWT.class, true);
			Arrays.sort(classes, new Comparator<Class<? extends TestCase>>() {

				@Override
                public int compare(Class<? extends TestCase> o1, Class<? extends TestCase> o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
		} catch (IOException e) {
			classes = (Class< ? extends TestCase>[]) new Class<?>[0];
			e.printStackTrace();
		}
		ITestDataBase db = DatabaseStuff.getDatabase();
		ITestMetaData mdb = db.getMetaData();
		for (Class<? extends TestCase> c : classes) {
			if (c.getName().indexOf("ONE_AT_A_TIME") < 0 &&
			    c.getName().indexOf("deprecated") < 0) {
				String name = c.getSimpleName();
				if ((!db.isKnown(name) || !db.isPassed(name))
						&& !mdb.isManualTest(name)) {
					suite.addTestSuite(c);
				}
			}
		}
		return suite;
	}
}
