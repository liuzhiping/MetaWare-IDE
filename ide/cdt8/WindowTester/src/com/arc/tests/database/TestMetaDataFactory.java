/*
 * TestMetaDataFactory
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.arc.tests.internal.database.TestMetaData;

public class TestMetaDataFactory {
    
    private static final String CATEGORY = "category";
    private static final String DESCRIPTION = "description";
    private static final String TEST = "test";
    private static final String NAME = "name";
    private static final String MANUAL = "manual";
    
    /**
     * Create an empty Meta data object and return it.
     * @return a newly created meta data object.
     */
    public static ITestMetaDataWriter create(){
        return new TestMetaData();
    }

    /**
     * Produce a test metadata object from an XML file.
     * @param xmlFile file from which to read XML data.
     * @return meta data object constructed from XML file.
     * @throws IOException
     */
    public static ITestMetaData readMetaDataFromXml (File xmlFile) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(xmlFile);
        try {
            return readMetaDataFromXml(fileInputStream);
        }
        finally {
            fileInputStream.close();
        }
    }
    
    /**
     * Produce a test metadata object from an XML file stream.
     * @param input XML data stream.
     * @return meta data object constructed from XML file.
     * @throws IOException
     */
    
    public static ITestMetaData readMetaDataFromXml (InputStream input) throws IOException {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(input);
            Element root = doc.getDocumentElement();
            ITestMetaDataWriter md = new TestMetaData();
            if (root == null) throw new IOException("Missing root node");
            if (!"TestMetaData".equals(root.getNodeName())) {
                throw new IOException("Unrecognized root node: " + root.getNodeName());
            }
            NodeList kids = root.getChildNodes();
            for (int i = 0; i < kids.getLength(); i++) {
                Node n = kids.item(i);
                if (n instanceof Element){
                    Element e = (Element)n;
                    if (!TEST.equals(e.getNodeName())){
                        throw new IOException("Unrecognized element: " + e.getNodeName());
                    }
                    String name = e.getAttribute(NAME);
                    String desc = e.getAttribute(DESCRIPTION);
                    String cat = e.getAttribute(CATEGORY);
                    if (cat.length() == 0) cat = null;
                    if (name.length() > 0) {
                        md.enterDescription(name,desc);
                        if (cat != null){
                            md.enterCategory(name,cat);
                        }
                    }
                    if ("1".equals(e.getAttribute(MANUAL))){
                        md.setManualTest(name);
                    }
                }
            }
            return md;
        }
        catch (ParserConfigurationException e) {
            throw new IOException(e.getMessage(), e);
        }
        catch (SAXException e) {
            throw new IOException(e.getMessage(), e);
        }
    }
    
    
    public static void writeMetaDataFromXml (ITestMetaData md, File xmlFile) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(xmlFile);
        try {
            writeMetaDataFromXml(md,fileOutputStream);
        }
        finally {
            fileOutputStream.close();
        }
    }
    
    public static void writeMetaDataFromXml (ITestMetaData md, OutputStream output) throws IOException {
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }
        catch (ParserConfigurationException e) {
            throw new IOException(e.getMessage(),e);
        }
        Document doc = builder.newDocument();
        Element root = doc.createElement("TestMetaData");
        doc.appendChild(root);
        for (String name: md.getTestNames()){
            Element e = doc.createElement(TEST);
            root.appendChild(e);
            String desc = md.getDescription(name);
            String cat = md.getCategory(name);
            e.setAttribute(NAME,name);
            if (desc == null) desc = "";
            e.setAttribute(DESCRIPTION,desc);
            if (cat != null)
                e.setAttribute(CATEGORY,cat);
            if (md.isManualTest(name)){
                e.setAttribute(MANUAL,"1");
            }
        }
        try {
            XMLWriter.serializeDocument(doc,output);
        }
        catch (TransformerException e) {
            throw new IOException(e.getMessage(),e);
        }      
    }
}
