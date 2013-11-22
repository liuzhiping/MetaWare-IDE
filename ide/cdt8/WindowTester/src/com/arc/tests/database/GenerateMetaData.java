/*
 * GenerateMetaData
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;

import com.arc.cdt.testutil.PackageUtil;
import com.windowtester.runtime.swt.UITestCaseSWT;


public class GenerateMetaData {
    private static final String[] CATEGORIES = {
//        ".*\\.First\\..*", "Installation Tests",
//        ".*\\.A\\..*", "Project management and Debugger Integration",
//        ".*\\.B\\..*", "Regression checks",
        ".*", "Yet Uncategorized"
    };
   
    /**
     * Generate MetaData test file that describes all tests.
     * @param args
     */
    public static void main (String[] args) throws IOException{
        if (args.length != 1){
            System.err.println("Need single argument that is file name for metadata file.");
            System.exit(1);
        }
        File metaFile = new File(args[0]);
        OutputStream output = new FileOutputStream(metaFile);
        try {
            generate(output);
        }finally{
            output.close();
        }
    }
    
    public static void generate(File f){
        OutputStream out = null;
        
        try {
            out = new FileOutputStream(f);
            generate(out);
        }
        catch (FileNotFoundException e) {
            // @todo Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            if (out != null)
                try {
                    out.close();
                }
                catch (IOException e) {
                }
        }
    }
    
    public static void generate(OutputStream out) {
        
        ITestMetaDataWriter md = TestMetaDataFactory.create();
        try {
            Class<?>classes[] = PackageUtil.getClasses("com.arc.cdt.tests",UITestCaseSWT.class,true);
            for (Class<?>klass: classes){
                if (klass.getName().indexOf("deprecated") >= 0)
                	continue;
                String name = klass.getSimpleName();
                try {
                    Field descField = klass.getDeclaredField("DESCRIPTION");
                    String desc = (String)descField.get(null);
                    md.enterDescription(name,desc);
                }catch(Exception x){
                    System.err.println("Warning: no DESCRIPTION field for " + klass.getName());
                }
                String cat = lookupCategory(klass);
                if (cat != null) md.enterCategory(name,cat);
                if (klass.getName().indexOf(".manual.") > 0){
                    md.setManualTest(name);
                }
            }
            TestMetaDataFactory.writeMetaDataFromXml(md, out);
        }catch (IOException x){
            x.printStackTrace(System.err);           
        }

    }
    
    private static String lookupCategory(String name){
        for (int i = 0; i < CATEGORIES.length; i+=2){
            if (name.matches(CATEGORIES[i])){
                return CATEGORIES[i+1];
            }
        }
        return null;
    }
    
    private static String lookupCategory(Class<?> klass){
        try {
            Field catField = klass.getDeclaredField("CATEGORY");
            return (String)catField.get(null);
        }
        catch (Exception e) {
            return lookupCategory(klass.getName());
        }
    }

}
