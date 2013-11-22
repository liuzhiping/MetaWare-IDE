package com.arc.cdt.toolchain.internal.ui.bcf.io;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.Reader;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class BCFContentHandler extends DefaultHandler {
    public static final String BCF_ROOT_NODE_NAME = "config_list";
    private transient int withinIDESection = 0;
    private transient int withinIDEStringSection = 0;
    private transient int withinLinkerSection = 0;
    private transient int withinLinkerStringSection = 0;
    private transient CharArrayWriter buf = new CharArrayWriter(1000);
    private transient int elementLevel = 0;
    private String linkerCommandFileContent = "";

    public BCFContentHandler() {
        super();
        // TODO Auto-generated constructor stub
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        if (elementLevel == 0 && !BCF_ROOT_NODE_NAME.equals(qName)){
            throw new SAXException("Top node of BCF is \"" + qName + "\"; expected \"config_list\"");
        }
        if (elementLevel == 1 && "configuration".equals(qName)){
            if ("IDE".equals(attributes.getValue("name"))) {
                withinIDESection++;
            }
            else if ("linker_command_file".equals(attributes.getValue("name"))){
                withinLinkerSection++;
            }
        } else if (withinIDESection > 0 && "string".equals(qName) && elementLevel == 2) {
            withinIDEStringSection++;
        }
        else if (withinLinkerSection > 0 && "string".equals(qName) && elementLevel == 2){
            withinLinkerStringSection++;
        }
        elementLevel++;

    }

    public Reader getPropertiesReader() {
        return new CharArrayReader(buf.toCharArray());
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (withinIDEStringSection == 1 && elementLevel == 3) {
            buf.write(ch, start, length);
        }
        else if (withinLinkerStringSection == 1 && elementLevel == 3){
            linkerCommandFileContent += new String(ch,start,length);         
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        elementLevel--;
        if (withinIDESection > 0) {
            if ("configuration".equals(qName)) {
                withinIDESection--;
                assert (withinIDESection >= 0);
            } else if ("string".equals(qName)) {
                withinIDEStringSection--;
                assert (withinIDEStringSection >= 0);
            }
        }
        else if (withinLinkerSection > 0){
            if ("configuration".equals(qName)) {
                withinLinkerSection--;
                assert (withinLinkerSection >= 0);
            } else if ("string".equals(qName)) {
                withinLinkerStringSection--;
                assert (withinLinkerStringSection >= 0);
            }            
        }
    }

    public String getLinkerCommandFileContent() { return linkerCommandFileContent; }
}
