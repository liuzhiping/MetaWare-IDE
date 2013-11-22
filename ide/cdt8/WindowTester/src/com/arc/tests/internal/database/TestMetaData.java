/*
 * TestMetaData
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
package com.arc.tests.internal.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.arc.tests.database.ITestMetaDataWriter;


public class TestMetaData implements ITestMetaDataWriter {
    
    // Category to collection of test names map.
    private Map<String, Set<String>> categories = new TreeMap<String,Set<String>>();
    
    private Map<String,String> testCategoryMap = new TreeMap<String,String>();
    
    // test name to description map.
    private Map<String,String> descriptions = new HashMap<String,String>();
    
    private Set<String> manualSet = new HashSet<String>(); // manual tests.

    public TestMetaData() {}
   
    @Override
    public void enterCategory (String testName, String category) {
        Set<String> tests = categories.get(category);
        if (tests == null){
            tests = new TreeSet<String>();
            categories.put(category,tests);
        }
        testCategoryMap.put(testName,category);
        // Add description so that "getTestNames()" returns all.
        if (descriptions.get(testName) == null){
            descriptions.put(testName,"");
        }
        tests.add(testName);

    }

    @Override
    public void enterDescription (String testName, String description) {
        descriptions.put(testName,description);

    }
    
    @Override
    public boolean isManualTest(String testName){
        return manualSet.contains(testName);
    }
    
    @Override
    public void setManualTest(String testName){
        manualSet.add(testName);
    }

    @Override
    public List<String> getCategories () {
        ArrayList<String> list = new ArrayList<String>();
        list.addAll(categories.keySet());
        return list;
    }

    @Override
    public String getDescription (String testName) {
        return descriptions.get(testName);
    }

    @Override
    public List<String> getTestNames () {
        Set<String> names = descriptions.keySet();
        ArrayList<String> result = new ArrayList<String>(names.size());
        result.addAll(names);
        return result;
    }

    @Override
    public List<String> getTestsForCategory (String category) {
        Set<String> names = categories.get(category);
        if (names != null){
            ArrayList<String> list = new ArrayList<String>(names.size());
            list.addAll(names);
            return list;
        }
        return new ArrayList<String>(0);
    }

    @Override
    public List<String> getUncategorizedTests () {
        ArrayList<String>list = new ArrayList<String>();
        for (String name: getTestNames()){
            if (testCategoryMap.get(name) == null){
                list.add(name);
            }
        }
        return list;
    }
    
    @Override
    public String getCategory(String testName){
        return this.testCategoryMap.get(testName);
    }

}
