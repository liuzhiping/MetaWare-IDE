/*
 * TestDataBase
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.arc.cdt.testutil.FileUtil;
import com.arc.cdt.testutil.ManualException;
import com.arc.tests.database.ITestDataBase;
import com.arc.tests.database.ITestMetaData;
import com.arc.tests.database.XMLWriter;


public class TestDataBase implements ITestDataBase {
    
    private static final String KEY = "key";

    private static final String ENTRY = "entry";

    private static final String PROPERTIES = "properties";

    private static final String IDE_VERSION = "ideVersion";

    private static final String OS = "OS";

    private static final String VERSION_KEY = "version";
    
    private static final String WT = "WT";

    private static final String DATE_END = "dateEnd";

    private static final String DATE_START = "dateStart";

    private static final String TEST_RESULTS = "testResults";

    private static final String VERSION = "1";
    
    private Map<String,String> db = new TreeMap<String,String>();
    private ITestMetaData metaData;
    private File dbFile;
    private boolean dirty = false;
    private String fDateEnd;
    private String fDBVersion;
    private String fIDEVersion;
    private String fDateStart;
    private String fOS;
    private String fWindowTesterVersion;
    
    private String fThisIDE = null;
    
    public TestDataBase(File dbFile, ITestMetaData metaData) throws IOException{
        if (dbFile == null) throw new IllegalArgumentException("dbFile is null");
        if (metaData == null) throw new IllegalArgumentException("metaData is null");
        this.metaData = metaData;
        this.dbFile = dbFile;
        this.fThisIDE = computeIDEVersion();
        boolean existing = false;
        if (dbFile.exists()){
            InputStream input = new FileInputStream(dbFile);
            try {
                loadFromXML(input);
                existing = true;
            } catch(IOException x){
                // File corrupt, just overwrite it.
                db.clear();
                dirty = true;
                
            }
            finally {
                input.close();
            }           
        }
        if (!existing) {
            fOS = getOSIdentity();
            fIDEVersion = computeIDEVersion();
            fWindowTesterVersion = computeWindowTesterVersion();
            fDateStart = decodeDate(Calendar.getInstance());
            fDateEnd = fDateStart;
        }
    }
    
    private static String computeIDEVersion(){
        Bundle bundle = Platform.getBundle("com.arc.cdt.toolchain");
        if (bundle == null) return null;
        return (String)bundle.getHeaders().get(Constants.BUNDLE_VERSION);
    }
    
    private static String computeWindowTesterVersion(){
        Bundle bundle = Platform.getBundle("com.windowtester.runtime");
        if (bundle == null) return null;
        return (String)bundle.getHeaders().get(Constants.BUNDLE_VERSION);
    }
    
    private static void loadProperties(Map<String,String> props, Element root){
        assert root.getNodeName().equals(PROPERTIES);
        NodeList entries = root.getElementsByTagName(ENTRY);
        int len = entries.getLength();
        for (int i = 0; i < len; i++){
            Element entry = (Element)entries.item(i);
            String key = entry.getAttribute(KEY);
            String value = entry.getTextContent();
            if (key.length() > 0){
                if (value == null) value = "";
                props.put(key,value);
            }
        }
    }
    
    /**
     * We must compute the current IDE before the plugin bundle manager shutdown
     * at exit time (which we often write the database).
     */
    private void testIDEVersion() {
        if (fThisIDE == null){
            fThisIDE = computeIDEVersion();
        }
    }
    
    private static String decodeDate(Calendar calendar){
        return calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) + ' '+
        calendar.get(Calendar.DAY_OF_MONTH) + ", " + calendar.get(Calendar.YEAR);
    }
    
    private void loadFromXML(InputStream input) throws IOException{
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(input);
            Element root = doc.getDocumentElement();
            fWindowTesterVersion = computeWindowTesterVersion();
            // if root is "properties", then it is the original format before we included OS and date entries.
            if (PROPERTIES.equals(root.getNodeName())){
                loadProperties(db,root);
                Calendar calendar = Calendar.getInstance();
                fDateStart = decodeDate(calendar);
                fDateEnd = fDateStart;
                fOS = getOSIdentity();
                fIDEVersion = computeIDEVersion();             
            }
            else if (TEST_RESULTS.equals(root.getNodeName())){
                fDateStart = root.getAttribute(DATE_START);
                fDateEnd = root.getAttribute(DATE_END);
                fDBVersion = root.getAttribute(VERSION_KEY);
                if (fWindowTesterVersion == null)
                    fWindowTesterVersion = root.getAttribute(WT);
                fOS = root.getAttribute(OS);
                if (fDBVersion.length() == 0 || fDBVersion.compareTo(VERSION) > 0){
                    throw new IOException("Database has unknown version: '" + fDBVersion + '"');
                }
                fIDEVersion = root.getAttribute(IDE_VERSION);
                if (fIDEVersion.length() == 0) fIDEVersion = computeIDEVersion();
                NodeList nodes = root.getElementsByTagName(PROPERTIES);
                for (int i = 0; i < nodes.getLength(); i++) {
                    loadProperties(db,(Element)nodes.item(i));
                }
            }
        }
        catch (ParserConfigurationException e) {
            // @todo Auto-generated catch block
            e.printStackTrace();
            throw new IOException(e.getMessage(),e);
        }
        catch (SAXException e) {
            // @todo Auto-generated catch block
            e.printStackTrace();
            throw new IOException(e.getMessage(),e);
        }
        
    }
    
    private static String getOSIdentity(){
        String osName =  System.getProperty("os.name");
        if (osName.equals("Linux")){
        	try {
				FileReader reader = new FileReader("/etc/redhat-release");
				BufferedReader input = new BufferedReader(reader);
				osName = input.readLine();
			} catch (IOException e) {
				// Just call it "Linux"
			}
        }
        return osName;
    }

    @Override
    public String getFailureMessage (String name) throws IllegalArgumentException {
        String s = db.get(name);
        if (s == null) throw new IllegalArgumentException("Unrecognized test");
        if (s.length() == 0) return null;
        return s;
    }

    @Override
    public ITestMetaData getMetaData () {
        return metaData;
    }

    @Override
    public List<String> getTests () {
        List<String> list =  new ArrayList<String>();
        for (Object s: db.keySet()){
            list.add((String)s);
        }
        return list;
    }

    @Override
    public boolean isKnown (String testName) {
        return db.get(testName) != null;
    }

    @Override
    public boolean isPassed (String testName) throws IllegalArgumentException {
        String s = db.get(testName);
        if (s == null) throw new IllegalArgumentException("Unknown test: " + testName);
        return s.length() == 0;
    }

    @Override
    public void addFailure (Test test, AssertionFailedError x) {
        dirty = true;
        db.put(getTestName(test),"Failed: " + (x!=null && x.getMessage() != null && x.getMessage().length() > 0?x.getMessage():""));
    }

    @Override
    public void addError (Test test, Throwable x) {
        dirty = true;
        String value;
        if (x instanceof ManualException){
            value = "Manual: " + x.getMessage();
        }
        else {
            value = "Error: " + (x!=null && x.getMessage() != null && x.getMessage().length() > 0?x.getMessage():"");
        }
        db.put(getTestName(test),value);
    }

    @Override
    public void startTest (Test test) {
        testIDEVersion();
        removeTest(getTestName(test));
    }
    
    @Override
    public void endTest(Test test){
        String name = getTestName(test);
        if (db.get(name) == null) {
            dirty = true;
            db.put(name,"");
        }
    }
    
    private static String getTestName(Test test){
        return test.getClass().getSimpleName();
    }
    
    @Override
	public void flush() throws IOException {
		if (dirty) {
			String os = getOSIdentity();
			if (!os.equals(fOS)) {
				System.out.println("Warning: OS mismatch: original was " + fOS
						+ "; current one is " + os);
			}
			// If null, then we were invoked during shutdown.
			if (fIDEVersion == null)
				fIDEVersion = fThisIDE;
			if (fIDEVersion != null && fThisIDE != null
					&& !fIDEVersion.equals(fThisIDE)) {
				throw new IOException("IDE version mismatch: original was "
						+ fIDEVersion + "; current on is " + fThisIDE);
			}
			File tmpFile = File.createTempFile("testDataBase", "xml");
			FileOutputStream output = new FileOutputStream(tmpFile);

			storeToXML(output);
			// We construct in a temp file in case of error. Otherwise, we wipe
			// out our database.
			FileUtil.copyFile(tmpFile, dbFile);
			output.close();
			dirty = false;
		}
	}
    
    private void storeToXML(OutputStream out) throws IOException{
        try {
           
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();
            Element root = doc.createElement(TEST_RESULTS);
            doc.appendChild(root);
            root.setAttribute(VERSION_KEY, VERSION);
            root.setAttribute(DATE_START, getStartDate());
            root.setAttribute(DATE_END, decodeDate(Calendar.getInstance()));
            root.setAttribute(OS,fOS);
            if (fWindowTesterVersion != null)
            	root.setAttribute(WT,fWindowTesterVersion);
            root.setAttribute(IDE_VERSION, fIDEVersion);
            Element properties = doc.createElement(PROPERTIES);
            root.appendChild(properties);
            for (Map.Entry<String,String> entry: db.entrySet()){
                Element elem = doc.createElement(ENTRY);
                elem.setAttribute(KEY,entry.getKey());
                elem.setTextContent(entry.getValue());
                properties.appendChild(elem);
            }
            XMLWriter.serializeDocument(doc,out);
        }
        catch (DOMException e) {
            // @todo Auto-generated catch block
            e.printStackTrace();
            throw new IOException(e.getMessage(),e);
        }
        catch (ParserConfigurationException e) {
            // @todo Auto-generated catch block
            e.printStackTrace();
            throw new IOException(e.getMessage(),e);
        }
        catch (TransformerException e) {
            // @todo Auto-generated catch block
            e.printStackTrace();
            throw new IOException(e.getMessage(),e);
        }
    }

    @Override
    public void removeTest (String name) {
        if (db.remove(name) != null) dirty = true;     
    }

    @Override
    public String getEndDate () {
        return fDateEnd;
    }

    @Override
    public String getIdeVersion () {
        return fIDEVersion;
    }

    @Override
    public String getStartDate () {
        return fDateStart;
    }

    @Override
    public String getOS () {
        return fOS;
    }

	@Override
	public String getWindowTesterVersion() {
		return fWindowTesterVersion;
	}

 
}
