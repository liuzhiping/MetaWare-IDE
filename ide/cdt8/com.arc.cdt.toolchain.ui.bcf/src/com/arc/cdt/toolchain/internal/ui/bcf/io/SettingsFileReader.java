package com.arc.cdt.toolchain.internal.ui.bcf.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.xml.sax.SAXException;

import com.arc.cdt.toolchain.internal.ui.bcf.BcfUtils;
import com.arc.cdt.toolchain.ui.bcf.SettingsFileContent;
import com.arc.mw.util.StringUtil;

/**
 * Reads the content of a BCF profile.
 * 
 * @author pickensd
 * 
 */
public class SettingsFileReader {
    private File file;

    public SettingsFileReader(File f)  {
        file = f;
    }
    
    /**
     * We pass the configuration so that the BCF path can be decoded using build variables.
     * @param config
     * @return
     * @throws IOException
     * @throws SAXException
     */
    public SettingsFileContent read(IConfiguration config) throws IOException, SAXException {
        SettingsFileContent profile = new SettingsFileContent();
        // If this is just a .bcf file (not augmented,) then just return it
        if (file.getName().endsWith(".bcf")){
            profile.setBcfFile(file);
            return profile;
        }
        SAXReader reader = new SAXReader(new DocumentFactory());
        Document doc;
        try {
            doc = reader.read(file);
        } catch (DocumentException e) {
            throw new SAXException(e);
        }
        Element root = doc.getRootElement();
        if (!SettingsFileContent.PROFILE_TAG.equals(root.getName())){
            if (BCFContentHandler.BCF_ROOT_NODE_NAME.equals(root.getName())){
                // A misnamed BCF file!
                profile.setBcfFile(file);
                return profile;
            }
            throw new SAXException("Top node is \"" + root.getName() + "\"; expected\"profile\"");
        }
        @SuppressWarnings("unchecked")
        List<Element> bcfList = root.elements(SettingsFileContent.BCF_TAG);
        
        if (bcfList.size() > 1) {
            throw new SAXException("Too many bcf nodes in profile");
        }
        
        if (bcfList.size() == 1) {
            Element bcf = bcfList.get(0);
            String bcfPath = bcf.attributeValue(SettingsFileContent.BCF_PATH_ATTR,null);
            if (bcfPath == null)
                throw new SAXException("path attribute missing from bcf node");
            if (config != null) bcfPath = BcfUtils.decodeBcfPath(bcfPath,config);
            File bcfFile = new File(bcfPath);
            if (!bcfFile.isAbsolute()){
                // Make it relative to where we are.
                bcfFile = new File(this.file.getParentFile(),bcfPath);
            }
            
            profile.setBcfFile(bcfFile);            
        }
        profile.setCompilerOptions(extractOptions(SettingsFileContent.COMPILER_TAG,root));
        profile.setAssemblerOptions(extractOptions(SettingsFileContent.ASM_TAG,root));
        profile.setLinkerOptions(extractOptions(SettingsFileContent.LINKER_TAG,root));
        profile.setLinkerCommandFiles(extractLinkerThings(SettingsFileContent.LINKER_COMMAND_TAG,root));
        profile.setLinkerObjectFiles(extractLinkerThings(SettingsFileContent.LINKER_OBJECT_TAG,root));
        
        return profile;
       
    }
    
    private static List<String> extractOptions(String node, Element root){
        @SuppressWarnings("unchecked")
        List<Element> kids = root.elements(node);
        List<String> options = new ArrayList<String>(kids.size());
        for (Element e: kids) {
            String s = e.getTextTrim();
            if (s.length() > 0) {
                List<String> tokens = StringUtil.stringToList(s);
                options.addAll(tokens);
            }
        }
        return options;
    }
    
    @SuppressWarnings("unchecked")
    private static List<String> extractLinkerThings(String node, Element root){
        List<Element> kids = root.elements("linker");
        List<String> options = new ArrayList<String>();
        for (Element e: kids){
            for (Element ekid: ((List<Element>)e.elements(node))){
                options.add(ekid.getTextTrim());
            }
        }
        return options;
    }
}
