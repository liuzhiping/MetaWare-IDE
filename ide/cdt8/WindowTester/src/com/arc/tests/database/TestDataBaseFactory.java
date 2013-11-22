/*
 * TestDataBaseFactory
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
package com.arc.tests.database;

import java.io.File;
import java.io.IOException;

import com.arc.tests.internal.database.TestDataBase;


public class TestDataBaseFactory {
    public static ITestDataBase read(File dbFile, ITestMetaData md) throws IOException{
        return new TestDataBase(dbFile,md);
    }
}
