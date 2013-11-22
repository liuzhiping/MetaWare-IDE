/*
 * ITestDataBase
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

import java.io.IOException;
import java.util.List;

import junit.framework.TestListener;

/**
 * Interface to track state of testing.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface ITestDataBase extends TestListener{
    
    /**
     * Return the metadata for this database
     * @return the metadata for this database.
     */
    ITestMetaData getMetaData();
    
    /**
     * Remove test results for test with the given name.
     * @param name name of test to remove.
     */
    void removeTest(String name);
    
    /**
     * Return names of all tests for which we have published results.
     * @return inmutable list of names.
     */
    List<String> getTests();
    
    /**
     * Indicate whether or not the given test is recognized.
     * @param testName name of test.
     * @return true if test has been registered with the database.
     */
    boolean isKnown(String testName);
    
    /**
     * Return whether or not the given test passed.
     * @param name of test.
     * @throws IllegalArgumentException if name is not known.
     * @return true if the test passed.
     */
    boolean isPassed(String name) throws IllegalArgumentException;
    
    /**
     * Return failure message for given test, or <code>null</code> if the test did not fail.
     * @param name name of test.
     * @return failure message for the test, or <code>null</code> if the test did not fail.
     * @throws IllegalArgumentException if name is not known.
     */
    String getFailureMessage(String name) throws IllegalArgumentException;
    
    /**
     * Return the version of the IDE for which the tests were run (e.g., "8.3.5").
     */
    String getIdeVersion();
    
    /**
     * Return the earliest date from which the tests were run.
     */
    String getStartDate();
    
    /**
     * Return the end date from which the tests were run.
     */
    String getEndDate();
    
    /**
     * Return a string that describes the OS environment.
     * @return a string that describes the OS environment (e.g, "Windows/XP").
     */
    String getOS();
    
    /**
     * Called to flush the database to persistent storage. 
     * @throws IOException 
     */
    void flush() throws IOException;
    
    /**
     * Return the version of WindowTester.
     * @return the version of WindowTester.
     */
    String getWindowTesterVersion();
    
}
