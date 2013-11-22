/*
 * ITestMetaData
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

import java.util.List;


public interface ITestMetaData {
    
    /**
     * Given a test name, returns its description, or <code>null</code> if no
     * description has been registered.
     * @param testName name of test.
     * @return description of test, or <code>null</code>.
     */
    String getDescription(String testName);
    
    /**
     * Get name of all tests.
     * @return immutable list of names of all tests.
     */
    List<String> getTestNames();
    
    /**
     * Associate a name with a category. If this is the first time the category is seen, it will
     * be added to the list of known categories.
     * @param testName name of test.
     * @param category category of the test.
     */
    void enterCategory(String testName, String category);
    
    /**
     * Return list of categories.
     * @return list of categories as immutable list.
     */
    List<String> getCategories();
    
    /**
     * Return all tests within the given category.
     * @param category name of category.
     * @return all tests within the given category as an immutable list.
     */
    List<String> getTestsForCategory(String category);
    
    /**
     * Return the list of all tests that don't have a category.
     * @return an immutable list of all tests that don't have a category.
     */
    List<String> getUncategorizedTests();
    
    /**
     * Return category for a test.
     * @param testName name of test.
     * @return category of test, or <code>null</code> if the test is non-existent or doesn't have a category.
     */
    String getCategory(String testName);
    
    /**
     * Return whether or not the given test must be run manually.
     * @return whether or not the given test must be run manually.
     */
    boolean isManualTest(String testName);
    
}
