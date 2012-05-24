/*******************************************************************************
 * Copyright (c) 2005-2012 Synopsys, Incorporated
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Synopsys, Inc - Initial implementation 
 *******************************************************************************/
package com.arc.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * This class can serve as a base class for any XML-based object. NOTE: all
 * subclasses must invoke
 * <code>{@link #init(URL,IBuilderInstantiator,String) init()}</code> from
 * within the constructor. We couldn't do this from the constructor of
 * XmlBasedObject because its argument are not necessarily available
 * immediately.
 * <P>
 * As the XML is being read, this object is presumably initialized by
 * side-effects of the instantiating the {@link IBuilder builder}classes.
 * 
 * @author J. David Pickens
 * @version 7/9/99
 */
public abstract class XmlBasedObject {

    /**
     * Instantiate with the URL of the "meta" XML definition. That is, the XML
     * that describes the XML that we will be reading.
     * 
     * @param meta_xml
     *            URL of meta xml.
     * @param instantiator
     *            instantiates builder classes.
     * @param rootPackage
     *            package prefix for all class references, or null.
     */
    protected void init(URL meta_xml, IBuilderInstantiator instantiator,
            String rootPackage) {
        if (instantiator == null)
                throw new IllegalArgumentException("Instantiator is null");

        /*
         * Read in the meta-XML for the xml processor.
         */
        if (meta_xml == null) throw new Error("Can't find meta.xml resource!");
        mProcessor = sProcessorMap.get(meta_xml);
        mInstantiator = instantiator;
        mRootPackage = rootPackage;
        if (mProcessor == null) {
            try {
                mProcessor = new XmlProcessor(meta_xml);
                // Cache it in case we read it again.
                sProcessorMap.put(meta_xml, mProcessor);
            } catch (Exception x) {
                System.err.println("Error in meta-XML file: meta.xml!");
                System.err.println(x);
                if (x instanceof SAXException) {
                    Exception e = ((SAXException) x).getException();
                    if (e != null) System.err.println("(" + e + ")");
                } else
                    x.printStackTrace();
                throw new Error("meta-XML error");
            }
        }
    }

    public void setXMLReader(XMLReader reader) {
        mProcessor.setXMLReader(reader);
    }

    /**
     * Processes an XML file by name.
     */
    public void readXML(String xmlPath) throws IOException, SAXException,
            SAXParseException {
        readXML(new File(xmlPath));
    }

    /**
     * Processes an XML file by name.
     */
    public void readXML(File xmlPath) throws IOException, SAXException,
            SAXParseException {
        InputSource input = Resolver.createInputSource(xmlPath);
        readXML(input);
    }

    /**
     * Processes an XML file from URL.
     */
    public void readXML(URL xml) throws IOException, SAXException,
            SAXParseException {
        InputSource input = Resolver.createInputSource(xml);
        readXML(input);
    }

    /**
     * Process XML from an input stream.
     */
    public void readXML(InputStream stream) throws SAXException,
            SAXParseException {
        InputSource input = new InputSource(stream);
        readXML(input);
    }

    /**
     * Process the XML file. The side-effects of processing it should be calling
     * back into this object.
     */
    public void readXML(InputSource input) throws SAXException,
            SAXParseException {
        startReading();

        /* Document doc = */
        mProcessor.read(input, mInstantiator, mRootPackage, getErrorHandler());

        endReading();
    }

    /**
     * Hook for any action that needs to be done immediately prior to reading
     * XML
     */
    protected void startReading() {
    }

    /**
     * Hook for any action that needs to be done immediately after reading XML
     */
    protected void endReading() {
    }

    /**
     * Return the error Handler. This is a default that can be overridden.
     */
    protected ErrorHandler getErrorHandler() {
        return new OurErrorHandler();
    }

    private XmlProcessor mProcessor;

    private IBuilderInstantiator mInstantiator;

    private String mRootPackage;

    private static HashMap<URL,XmlProcessor> sProcessorMap = new HashMap<URL,XmlProcessor>();
}

class OurErrorHandler implements org.xml.sax.ErrorHandler {

    @Override
    public void error(SAXParseException x) throws SAXException {
        throw x;
    }

    @Override
    public void fatalError(SAXParseException x) throws SAXException {
        throw x;
    }

    @Override
    public void warning(SAXParseException x) {
        System.err.println(x.getSystemId() + ", Line " + x.getLineNumber()
                + ": (Warning) " + x.getMessage());
    }
}
