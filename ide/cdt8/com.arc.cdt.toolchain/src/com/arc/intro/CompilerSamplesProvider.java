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
package com.arc.intro;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.intro.config.IIntroContentProvider;
import org.eclipse.ui.intro.config.IIntroContentProviderSite;

import com.arc.cdt.toolchain.ToolchainPlugin;
import com.arc.mw.util.Cast;

/**
 * Dynamically generate the HTML for Sample compiler projects.
 * Referenced from introContent.xml file.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class CompilerSamplesProvider implements IIntroContentProvider {
    
    private static final String SAMPLES = "samples";
    private static final String CATEGORIES = "categories";
    private static final String DEFAULT_DEMOS_DIR = "$INSTALL/MetaWare/$TARGET/demos";
    private static final String SAMPLES_FILE = "samples.xml";
    private static final String DEMOS_ATTR = "demos";
    private static final String SAMPLES_TAG = "samples";
    private static final String TITLE_ATTR = "title";
    private static final String CATEGORY_TAG = "category";
    private static final String TARGET_TAG="target";
    private static final String PROJECT_TAG="project";
    private static final String NAME_ATTR="name";
    private static final String DESC_ATTR="description";
    private static final String ID_ATTR="id";
    private static final String WORKSPACE_ATTR="workspace";

    
    /**
     * These are used if there is no samples.xml file.
     */
    private static  String[] targetNames = new String[]{
        "arc", "ARCompact",
        "hcac", "ARCompact",
        "hcarc", "ARC 4",
        "arc4", "ARC 4",
        "videocore", "VideoCore",
        "vc", "VideoCore",
        "hcvc", "VideoCore",
        "arm", "ARM",
        "hcarm", "ARM",
        "ppc", "PowerPC",
        "powerpc", "PowerPC",
        "hcppc", "PowerPC",
        "mips", "MIPS",
        "hcmips", "MIPS"};
    private static Document samplesDoc;
    private Toolset[] mRecognizedToolsets = null;
    private static String sChosenToolset = null;
    private static String sChosenCategory = null;

    public void init (IIntroContentProviderSite site) {
        if (samplesDoc == null) {
            File samplesXML = computeSampleXML();
            Document doc = null;
            if (samplesXML.exists()) {
                SAXReader reader = new SAXReader();
                try {
                    doc = reader.read(samplesXML);
                }
                catch (Exception e) {
                    ToolchainPlugin.log("Error in reading " + samplesXML, e);
                }
            }
            if (doc == null) {
                // Create a makeshift version
                DocumentFactory f = DocumentFactory.getInstance();
                Element root = f.createElement(SAMPLES_TAG);
                root.addAttribute(DEMOS_ATTR, DEFAULT_DEMOS_DIR);
                for (int i = 0; i < targetNames.length; i += 2) {
                    Element target = root.addElement(TARGET_TAG);
                    target.addAttribute(NAME_ATTR, targetNames[i]);
                    target.addAttribute(DESC_ATTR, targetNames[i + 1]);
                }
                Element category = root.addElement(CATEGORY_TAG);
                category.addAttribute(TITLE_ATTR, "Compiler Samples");
                category.addAttribute(WORKSPACE_ATTR, ".*mide_workspace");
                category.addAttribute(ID_ATTR, "compiler");
                doc = f.createDocument(root);
            }
            samplesDoc = doc;
        }
    }
    
    static File computeSampleXML(){
        String dir = System.getProperty("SAMPLES",null);
        if (dir == null || !new File(dir).isDirectory()){
            return new File(computeInstallLocation(),SAMPLES_FILE);           
        }
        return new File(dir,SAMPLES_FILE);       
    }
    
    static class Sample {
        String toolsetName;
        String projectName;
        File project;
        String description;
    }
    
    static File computeInstallLocation(){
        URL url = Platform.getInstallLocation().getURL();
        return url == null?new File("."):new File(url.getPath()).getParentFile();
    }
    
    static File computeMetaWareDir() {
        String mw = System.getProperty("METAWARE_INSTALL",null);
        File ide;
        if (mw != null && new File(mw).isDirectory())
            ide = new File(mw);
        else {
            File d = computeInstallLocation(); // E.g. C:/arc/metaware/ide
            // Being invoked independent of MetaWare installation? (e.g., debugger)
            if (d.getParentFile() == null) ide = d;
            else ide = d.getParentFile();
        }
        return ide; // E.g. C:/arc/metaware
    }
    
    static class Toolset {
        Toolset(String name, String desc){
            this.name = name;
            this.desc = desc;
        }
        String name; // e.g. "arc"
        String desc; // e.g. "ARCompact"
    }
    static class ToolsetDemosLocation {
        ToolsetDemosLocation(File location, Toolset toolset){
            this.location = location;
            this.toolset = toolset;
        }
        File location;
        Toolset toolset;
    }
    
    private Toolset[] computeRecognizedToolsets () {
        if (mRecognizedToolsets == null) {
            List<Element> targets = Cast.toType(samplesDoc.getRootElement().elements(TARGET_TAG));
            Toolset[] result = new Toolset[targets.size()];
            int i = 0;
            for (Element target : targets) {
                String name = target.attributeValue(NAME_ATTR, "???");
                String desc = target.attributeValue(DESC_ATTR, "???");
                result[i++] = new Toolset(name, desc);
            }
            assert (i == result.length);
            mRecognizedToolsets = result;
        }
        return mRecognizedToolsets;
    }
    
    private Toolset findToolset(String name){
        Toolset[] toolsets = computeRecognizedToolsets();
        for (Toolset t: toolsets){
            if (t.name.equals(name)) return t;
        }
        return null;
    }
    
    private File computeDemosDirForToolset(String toolset){   
        File dir = computeMetaWareDir().getParentFile();
        String demosDir = samplesDoc.getRootElement().attributeValue(DEMOS_ATTR,DEFAULT_DEMOS_DIR);
        demosDir = demosDir.replaceFirst("\\$INSTALL", dir == null?"/":dir.toString().replace('\\','/'));
        demosDir = demosDir.replaceFirst("\\$TARGET",toolset);
        return new File(demosDir);
    }
    
    private ToolsetDemosLocation[] computeToolsetDemosLocations(){
        List<ToolsetDemosLocation> list = new ArrayList<ToolsetDemosLocation>();
        for (Toolset t: computeRecognizedToolsets()){       
            File f = computeDemosDirForToolset(t.name);
            if (f.isDirectory()) list.add(new ToolsetDemosLocation(f,t));          
        }
        return list.toArray(new ToolsetDemosLocation[list.size()]);
    }
    
    static boolean isWorkspace(File dir){
        return dir.isDirectory() && new File(dir,".metadata").isDirectory();
    }
    
    static class Category {
        Sample[] samples;
        String id;
        String title;
    }
    
    private static File[] findWorkspacesIn(File demosDir, String wsPattern){
        final Pattern pattern = Pattern.compile(wsPattern);
        File wsDirs[] = demosDir.listFiles(new FilenameFilter(){

            public boolean accept (File dir, String name) {
                Matcher matcher = pattern.matcher(name);
                return matcher.matches() && new File(new File(dir,name),".metadata").isDirectory();
            }});
        return wsDirs;
    }
    
    private static File[] computeProjectsWithin(File ws){
        return ws.listFiles(new FileFilter(){

            public boolean accept (File pathname) {
               return pathname.isDirectory() && new File(pathname,".project").exists();
            }});
    }
    
    private String getProjectDescription(Element cat, File projectFile){
        List<Element>projects = Cast.toType(cat.elements(PROJECT_TAG));
        String projectName = projectFile.getName();
        for (Element projectElement: projects){
            if (projectName.equals(projectElement.attributeValue(NAME_ATTR,null))){
                return projectElement.getTextTrim();
            }
        }
        File readmeFile = new File(projectFile,"README.txt");
        if (readmeFile.exists()){
            try {
                FileReader reader = new FileReader(readmeFile);
                BufferedReader input = new BufferedReader(reader);
                StringBuilder buf = new StringBuilder();
                String line = input.readLine();
                while (line != null){
                    buf.append(line);
                    buf.append("\n");
                    line = input.readLine();
                }
                return buf.toString();
            }
            catch (IOException e) {
               //couldn't read the "README.txt" file.
            }
        }
        return "";
    }
    
    private Category[] computeSampleCategories(Toolset ts){
        List<Element> categories = Cast.toType(samplesDoc.getRootElement().elements(CATEGORY_TAG));
        List<Category> list = new ArrayList<Category>();
        File demosDir = computeDemosDirForToolset(ts.name);
        if (demosDir.isDirectory()){
            for (Element c: categories){
                String title = c.attributeValue(TITLE_ATTR,"?title?");
                String id = c.attributeValue(ID_ATTR,"compiler");
                String wsPattern = c.attributeValue(WORKSPACE_ATTR,".*");
                File wsDirs[] = findWorkspacesIn(demosDir,wsPattern);
                List<Sample>samples = new ArrayList<Sample>();
                for (File wsDir: wsDirs){
                    File projects[] = computeProjectsWithin(wsDir);
                    for (File project: projects){
                        Sample s = new Sample();
                        s.projectName  = project.getName();
                        s.toolsetName = ts.desc;
                        s.project = project;
                        s.description = getProjectDescription(c,project);
                        samples.add(s);
                    }               
                }
                if (samples.size() > 0){
                    Category cat = new Category();
                    cat.samples = samples.toArray(new Sample[samples.size()]);
                    cat.title = title;
                    cat.id = id;
                    list.add(cat);          
                }
            }
        }
        return list.toArray(new Category[list.size()]);       
    }
    
    private Category findCategory(Toolset ts, String id){
        Category cat[] = computeSampleCategories(ts);
        for (Category c: cat){
            if (id.equals(c.id)) return c;
        }
        return null;
    }
    
    public void createContent (String id, PrintWriter out) {
        if (id.indexOf("toolsets") >= 0){
            // Here we generate the HTML to show the list of all known installed
            // toolsets. If there is just one toolset, then we display the categories
            // page and bypass the list of toolsets.
            ToolsetDemosLocation loc[] = this.computeToolsetDemosLocations();
            if (loc.length > 1){
                createContentForTargets(loc,out);
            }
            else if (loc.length == 1){
                createContentForCategories(loc[0].toolset.name,out);
            }
            else
                createContentForNoTargets(out);
        }
        else if (id.equals(CATEGORIES) && sChosenToolset != null) {
            createContentForCategories(sChosenToolset,out);
        }
        else if (id.equals(SAMPLES) && sChosenToolset != null &&
            sChosenCategory != null){
            createContentForSamples(sChosenToolset,sChosenCategory,out);
        }
    }
    
    private void createContentForCategories (String toolsetName, PrintWriter out) {
        Toolset toolset = findToolset(toolsetName);
        if (toolset == null) {
            createContentForNoSamples(out); // shouldn't get here
        }
        Category categories[] = this.computeSampleCategories(toolset);
        if (categories.length > 0) {
            if (categories.length == 1) {
                createContentForSamples(toolsetName, categories[0].id, out);
            }
            else {
                out.println("<div style-id=\"page-content\">");
                out.println("    <div class=\"samples-group\">");
                out.println("        <h4><span class=\"div-label\">Categories of Sample Projects</span></h4>");
                out.println("        <p><span class=\"group-description\">Click on one of the icons below to choose the category of interest. ");
                out.println("           </span>");
                out.println("        </p>");
                out.println("    </div>");
                out.println("    <div class=\"samples-group\" >");
                for (Category c : categories) {
                    writeLink(out, c.title, "", "com.arc.intro.ChooseCategoryAction", "samples-link", "category=" + c.id);
                }
                out.println("    </div>");
                out.println("</div>");
            }
        }
        else
            createContentForNoSamples(out);
    }
    
    private void createContentForTargets(ToolsetDemosLocation toolsets[], PrintWriter out){
        out.println("<div style-id=\"page-content\">");
        out.println("    <div class=\"samples-group\">");
        out.println("        <h4><span class=\"div-label\">Installed Toolsets with Sample Projects</span></h4>");
        out.println("        <p><span class=\"group-description\">Click on one of the icons below to choose the toolset of interest. ");
        out.println("           </span>");
        out.println("        </p>");
        out.println("    </div>");
        out.println("    <div class=\"samples-group\" >");
        for (ToolsetDemosLocation toolset: toolsets){
            writeLink(out,toolset.toolset.desc,"Sample projects for the " + toolset.toolset.desc + " toolset",
                "com.arc.intro.ChooseToolsetAction","toolset-link","toolset=" + toolset.toolset.name);
        }
        out.println("    </div>");
        out.println("</div>");
    }
    
    /**
     * Called from {@link ChooseToolsetAction} to set the chosen
     * toolset.
     * @param toolsetName the name of the chosen toolset.
     */
    public static void setChosenToolset(String toolsetName){
        sChosenToolset = toolsetName;
    }
    
    /**
     * Called from {@link ChooseCategoryAction} to set the chosen
     * category.
     * @param categoryName the name of the chosen category.
     */
    public static void setChosenCategory(String categoryName){
        sChosenCategory = categoryName;
    }
    
    private void createContentForSamples(String toolsetName, String categoryID, PrintWriter out){
        Toolset toolset = findToolset(toolsetName);
        if (toolset == null){
            createContentForNoSamples(out);
            return;
        }
        Category category = findCategory(toolset,categoryID);
        if (category == null){
            createContentForNoSamples(out);
            return;
        }
        out.println("<div style-id=\"page-content\">");
        // for (int i = 0; i < 10; i++) out.println("<br>");
        out.println("    <img class=\"samples-img\" src=\"css/graphics/contentpage/samples.gif\" alt=\"The MetaWare IDE Samples Page\"></img>");
        Sample samples[] = category.samples;
        if (samples.length > 0) {
            out.println("    <div class=\"samples-group\">");
            out.println("        <h4><span class=\"div-label\">Sample Projects</span></h4>");
            out.println("        <p><span class=\"group-description\">Click on any of the icons below to import the sample project. ");
            out.println("           </span>");
            out.println("        </p>\n");
            out.println("    </div>");
            out.println("    <div class=\"samples-group\" ><h4><span class=\"div-label\">Available sample projects</span></h4>");

            for (Sample s : samples) {
                String projName = s.projectName;
                if (s.toolsetName.length() > 0)
                    projName += " for " + s.toolsetName;
                writeLink(out, projName, s.description, s.project, "ImportSampleProject", "project-link");
            }

            out.println("    </div>");
            out.println("</div>");
        }
        else {
            createContentForNoSamples(out);
        }
    }
    
    private void createContentForNoSamples(PrintWriter out){
        out.println("<div class=\"samples-group\">");
        out.println("    <h4><span class=\"div-label\">No sample projects found in the toolset installation.</span></h4>");
        out.println("    <br>");
        out.println("    <p><span class=\"group-description\">They should be located in");
        out.println("            " + computeMetaWareDir().toString().replace('\\','/') + "/" + (sChosenToolset!=null?sChosenToolset:"arc")+"/demos");
        out.println("       </span>");
        out.println("    </p>\n");
        out.println("</div>");
    }
    
    private void createContentForNoTargets(PrintWriter out){
        out.println("<div class=\"samples-group\">");
        out.println("    <h4><span class=\"div-label\">No installed toolset with sample projects found.</span></h4>");
        out.println("    <br>");
        out.println("</div>");
    }

    public void createContent (String id, Composite parent, FormToolkit toolkit) {
        System.out.println("Hey, wrong one called");

    }
    
    private static void writeLink(PrintWriter out, String name, String desc, File workspace,String action,String style){
        writeLink(out,name,desc,"com.arc.intro." + action+"Action",style,"path="+workspace);
    }
    
    private static void writeLink(PrintWriter out, String name, String desc, String klass,String style, String parms){
        out.printf("<A class=\"%s\" href=\"http://org.eclipse.ui.intro/runAction?pluginId=com.arc.cdt.toolchain&amp;class=%s&amp;%s\">\n",
                style,klass,parms);
        out.printf("    <IMG src=\"css/graphics/icons/etool/blank.gif\">\n");
        out.printf("    <SPAN class=\"link-label\">%s</SPAN>\n",name);
        out.printf("    <P><SPAN class=\"text\">%s</SPAN></P></A>\n",desc);
    }

    public void dispose () {
        // @todo Auto-generated method stub

    }

}
