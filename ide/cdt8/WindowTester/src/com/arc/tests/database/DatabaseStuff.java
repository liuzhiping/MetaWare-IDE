/*
 * DatabaseStuff
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestResult;

import org.eclipse.core.runtime.FileLocator;

import com.arc.cdt.tests.plugin.TestsPlugin;


/**
 * The class encapsolates the default database object that is stored and retrieved from a well-known place.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */

public class DatabaseStuff {

    private static final String DEFAULT_METADATA_PATH = "TestMetaData.xml";

    private static final String DEFAULT_DATABASE_PATH = "TestDataBase.xml";

    private static ITestDataBase database;
    static {
        InputStream input = null;
        try {
            if (TestsPlugin.getDefault() != null)
                input = TestsPlugin.getDefault().getInputStream(DEFAULT_METADATA_PATH);
            else 
                input = new FileInputStream(DEFAULT_METADATA_PATH);
        }
        catch (IOException e) {
            // need to generate it.
        }
        if (input == null) {
            try {
                File f;
                if (TestsPlugin.getDefault() != null) {
                    URL url = FileLocator.toFileURL(TestsPlugin.getDefault().getBundle().getEntry("."));
                    f = new File(url.getFile(), DEFAULT_METADATA_PATH);
                }
                else {
                    f = new File(DEFAULT_METADATA_PATH);
                }
                GenerateMetaData.generate(f);
                input = new FileInputStream(f);
            }
            catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
        if (input != null) {
            try {
                ITestMetaData md = TestMetaDataFactory.readMetaDataFromXml(input);
                input.close();
                File f;
                if (TestsPlugin.getDefault() != null) {
                    URL url = FileLocator.toFileURL(TestsPlugin.getDefault().getBundle().getEntry("."));
                    f = new File(url.getFile(), DEFAULT_DATABASE_PATH);
                }
                else {
                    f = new File(DEFAULT_DATABASE_PATH);
                }
                database = TestDataBaseFactory.read(f, md);
            }
            catch (IOException e) {
                // @todo Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static ITestDataBase getDatabase () {
        return database;
    }

    private static Set<TestResult> seenResults = new HashSet<TestResult>();

    private static boolean shutdownHookRegistered = false;
    
    private static long timeOfLastFlush = System.currentTimeMillis();
    
    /**
     * In case of JVM crash (which happens frequently under Linux), flush the
     * data base from time-to-time.
     * @throws IOException
     */
    public static void doPeriodicFlush() throws IOException{
    	if (database != null){
    		if (System.currentTimeMillis() > timeOfLastFlush + 5*60*1000) {
    			database.flush();
    			timeOfLastFlush = System.currentTimeMillis();
    		}
    	}
    }

    public static void addListeners (TestResult results) {
        if (database != null && seenResults.add(results)) {
            results.addListener(database);
            if (!shutdownHookRegistered) {
                shutdownHookRegistered = true;
                Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

                    @Override
                    public void run () {
                        try {
                            database.flush();
                        }
                        catch (IOException e) {
                            // @todo Auto-generated catch block
                            e.printStackTrace();
                        } // flush to a file

                    }
                }));
            }
        }
    }
}
