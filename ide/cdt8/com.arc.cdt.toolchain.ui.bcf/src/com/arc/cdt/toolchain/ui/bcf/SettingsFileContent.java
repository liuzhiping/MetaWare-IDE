package com.arc.cdt.toolchain.ui.bcf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.xml.sax.SAXException;

import com.arc.cdt.toolchain.internal.ui.bcf.io.BCFFileContent;
import com.arc.cdt.toolchain.internal.ui.bcf.io.SettingsFileReader;

/**
 * A complete description of the content of a Settings file
 * @author pickensd
 *
 */
public class SettingsFileContent {
    
    public static final String PROFILE_TAG = "profile";
    public static final String COMPILER_TAG = "compiler";
    public static final String LINKER_TAG = "linker";
    public static final String LINKER_COMMAND_TAG = "command";
    public static final String LINKER_OBJECT_TAG = "object";
    public static final String ASM_TAG  = "assembler";
    public static final String BCF_TAG = "bcf";
    public static final String BCF_PATH_ATTR = "path";
    
    Properties properties = null;
    String linkerCommandContent = null;
    List<String> compilerOptions = Collections.emptyList();
    List<String> assemblerOptions  = Collections.emptyList();
    List<String> linkerOptions  = Collections.emptyList();
    List<String> linkerCommandFiles  = Collections.emptyList();
    List<String> linkerObjectFiles  = Collections.emptyList();
    File bcfFile;
    long modTime; // mod time of the file.
    
    
    static Map<File, SettingsFileContent> cache = new HashMap<File,SettingsFileContent>();
   

    public SettingsFileContent() {
        // TODO Auto-generated constructor stub
    }
    
    public static SettingsFileContent read(File file, IConfiguration config) throws IOException, SAXException{
        // First see if it is in the cache.
        SettingsFileContent s = cache.get(file);
        if (s != null && s.modTime == file.lastModified()) return s;
        s = new SettingsFileReader(file).read(config);
        s.modTime = file.lastModified();
        cache.put(file, s);
        return s;
    }
    
    public void setBcfFile(File bcfFile){
        if (bcfFile == null){
            properties = new Properties();
            this.bcfFile = null;
        }
        else if (!bcfFile.equals(this.bcfFile)){
            this.bcfFile = bcfFile;
            this.properties = null; //force it to be re-read
        }
    }
    
    public File getBcfFile() { return bcfFile; }
    
    public ProcessorFamily getProcessorFamily() throws BadPropertyException{
        ProcessorFamily family = null;
        Properties properties;
        try {
            properties = getProperties();
        } catch (Exception e1) {
            return null; // presumably error will reoccur later
        } 
        String v = properties.getProperty("processor.family", "-1");
        String c = properties.getProperty("processor.core_version", "0");
        try {
            switch (Integer.parseInt(v)) {
            case 0:
                family = ProcessorFamily.A4;
                break;
            case 1:
                family = ProcessorFamily.A5;
                break;
            case 2:
                if (c.length() == 1
                && c.charAt(0) >= '5'
                && "0".equals(properties.getProperty(
                        "processor.Xbarrel_shifter", "0"))) {
                    family = ProcessorFamily.ARC601;
                } else
                    family = ProcessorFamily.ARC600;
                break;
            case 3: family = ProcessorFamily.ARC700; break;
            case 4: family = ProcessorFamily.ARCEM; break;
            case 5: family = ProcessorFamily.ARCHS; break;
            default:
                throw new BadPropertyException(
                        "Unrecognized value for processor.family: " + v);
            }

        } catch (NumberFormatException e) {
            throw new BadPropertyException(
                    "Unrecognized value for processor.family: " + v);
        }
        return family;
    }
    
    public void setProperties(Properties props) { properties = props; }
    public void setCompilerOptions(List<String>options){
        compilerOptions = new ArrayList<String>(options);
    }
    public void setAssemblerOptions(List<String>options){
        assemblerOptions = new ArrayList<String>(options);
    }
    public void setLinkerOptions(List<String>options){
        linkerOptions = new ArrayList<String>(options);
    }
    public void setLinkerCommandFiles(List<String>list){
        linkerCommandFiles = new ArrayList<String>(list);
    }
    
    public void setLinkerObjectFiles(List<String>list){
        linkerObjectFiles = new ArrayList<String>(list);
    }
    
    private void readBcf() throws IOException, SAXException {
        if (bcfFile != null){
            BCFFileContent bcf = BCFFileContent.read(bcfFile);
            properties = bcf.getProperties();
            linkerCommandContent = bcf.getLinkerCommandFileContent();
        }
        else {
            properties = new Properties();
            linkerCommandContent = "";
        }
    }
    
    /**
     * 
     * @return the content of the default linker command file.
     * @throws IOException
     * @throws SAXException 
     */
    public String getLinkerCommandFileContent() throws IOException, SAXException{
        if (properties == null) readBcf();
        return linkerCommandContent;
    }
    
    /**
     * Return the appropriate name for the extracted linker command file.
     * @return the appropriate name for the extracted linker command file.
     */
    public String getLinkerCommandFileName(){
        if (this.bcfFile == null) return "link.lcf";
        String fn = bcfFile.getName();
        int i = fn.lastIndexOf('.');
        if (i > 0){
            fn =  fn.substring(0,i);
        }
        return fn + ".lcf";
    }
    
    
    public Properties getProperties() throws IOException, SAXException{
        if (properties == null){
            readBcf();
        }
        return properties;
    }
   
    public List<String> getCompilerOptions() { 
        return Collections.unmodifiableList(compilerOptions);
    }

    public List<String> getLinkerOptions() {
        return Collections.unmodifiableList(linkerOptions);
    }
    
    public List<String> getAssemblerOptions(){
        return Collections.unmodifiableList(assemblerOptions);
    }
    
    public List<String> getLinkerCommandFiles(){
        return Collections.unmodifiableList(linkerCommandFiles);
    }
    
    public List<String> getLinkerObjectFiles() {
        return Collections.unmodifiableList(linkerObjectFiles);
    }
}
