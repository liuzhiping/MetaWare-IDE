package com.arc.cdt.toolchain.internal.ui.bcf.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Reads the properties from a BCF file.
 * 
 * @author pickensd
 * 
 */
public class BCFFileContent {    
    private Properties properties;
    private String linkerCommandContent;
    private long modTime;
    
    private static Map<File,BCFFileContent> cache = new HashMap<File,BCFFileContent>();

    private BCFFileContent()  {
    }
    
    public static BCFFileContent read(File f) throws IOException, SAXException {
        BCFFileContent b = cache.get(f);
        if (b != null && b.modTime == f.lastModified()) return b;
        Reader r = new FileReader(f);
        b = new BCFFileContent();
        b.readContent(r);
        b.modTime = f.lastModified();
        r.close();
        cache.put(f, b);
        return b;
    }
    public Properties getProperties() { return properties; }
    public String getLinkerCommandFileContent() { return linkerCommandContent; }

    private void readContent(Reader reader) throws IOException, SAXException {
        BCFContentHandler handler = new BCFContentHandler();
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        xmlReader.setContentHandler(handler);
        InputSource input = new InputSource(reader);
        xmlReader.parse(input);
        properties = new Properties();
        properties.load(handler.getPropertiesReader());
        linkerCommandContent = handler.getLinkerCommandFileContent();
    }
}
