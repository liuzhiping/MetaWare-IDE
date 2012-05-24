/*
 * GenContextHelpFromJavaHelp
 * $Revision$
 * $Date$
 *
 * CONFIDENTIAL AND PROPRIETARY INFORMATION.
 * Copyright 2005 ARC International (Unpublished).
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
package com.arc.seecode.engine.gen;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;


/**
 * Generates the context xml help files from the JavaHelp files.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class GenContextHelpFromJavaHelp {

    public static void main (String[] args){
        if (args.length != 4){
            System.err.println("Args are: <javahelpTOC.xml> <javahelpMAP.xml> <docdir> <output.xml>");
            System.exit(1);
        }
        File tocFile = new File(args[0]);
        File mapFile = new File(args[1]);
        String docDir = args[2];
        if (!tocFile.exists()){
            System.err.println(args[0] + " does not exist!");
            System.exit(2);
        }
        if (!mapFile.exists()){
            System.err.println(args[1] + " does not exist!");
            System.exit(3);
        }
        try {
            File outputFile = new File(args[3]);
            SAXReader reader = new SAXReader();
            Document tocdoc = reader.read(tocFile);
            Document mapdoc = reader.read(mapFile);
            Document newDoc = transform(tocdoc,mapdoc,docDir);
            Writer out = new FileWriter(outputFile);
            XMLWriter writer = new XMLWriter(out,OutputFormat.createPrettyPrint());
            writer.write(newDoc);
            out.close();
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }      
    }
    
    /**
     * Given a JavaHelp TOC and MAP document, produce an Eclipse
     * Context Help XML file.
     * @param tocDoc the JavaHelp toc document
     * @param mapDoc the javaHelp map document.
     * @param docDir a document folder where the HTML files are to be located.
     * @return an Eclipse context help document.
     */
    private static Document transform(Document tocDoc, Document mapDoc, String docDir)
    throws DocumentException{
        Map<String,String> map = createMap(mapDoc);
        Document newDoc = DocumentFactory.getInstance().createDocument();
        
        Element root = tocDoc.getRootElement();
        if (!root.getName().equals("toc")){
            throw new DocumentException("toc document does not have propert root: " + root.getName());
        }
        
        Element contexts = newDoc.addElement("contexts");
        Set<String> targetsSeen = new HashSet<String>();
        appendContexts(contexts,root, map,docDir, targetsSeen);
        
        //
        // Now add context ID's that are not referenced in the TOC
        for (Map.Entry<String,String> entry: map.entrySet()){
            String target = entry.getKey();
            String url = entry.getValue();
            if (targetsSeen.add(target)){
                addToContext(contexts,target,url,"link","For information on this SeeCode feature, select:",docDir);
            }
        }
        
        addExtras(contexts);
        return newDoc;
    }
    
    private static void addExtras(Element contexts){
        Element context = contexts.addElement("context");
        context.addAttribute("id","seecode_command");
        Element topic = context.addElement("topic");
        topic.addAttribute("label","SeeCode Command View");
        topic.addAttribute("href","reference/command.html");
        
        topic = context.addElement("topic");
        topic.addAttribute("label","SeeCode commands summary");
        topic.addAttribute("href","seecode/displays/command_summary.htm");
        
        Element desc = context.addElement("description");
        desc.addText("For information on SeeCode commands, select one of the following topics:");
    }
    
    @SuppressWarnings("unchecked")
    private static void appendContexts (Element contexts, Element tocItemsRoot, Map<String, String> map, String docDir,
            Set<String> targetsSeen) {
        List<Element> tocItems = tocItemsRoot.elements("tocitem");
        
        for (Element tocItem : tocItems) {
            String text = tocItem.attributeValue("text");
            String target = tocItem.attributeValue("target");
            if (target != null && text != null && targetsSeen.add(target)) {
                String url = map.get(target);
                if (url == null)
                    System.err.println("Target id " + target + " is not defined!");
                else {
                    addToContext(contexts,target,url,text,"For information on this SeeCode feature, select the following link:",docDir);
                }
            }
            appendContexts(contexts,tocItem,map,docDir,targetsSeen);
        }

    }
    
    private static void addToContext(Element contexts, String target, String url,String text, String desc, String docDir){
        Element context = contexts.addElement("context");
        context.addAttribute("id", target);
        Element descElement = context.addElement("description");
        descElement.addText(desc);
        Element topic = context.addElement("topic");
        topic.addAttribute("label", text);
        topic.addAttribute("href", docDir + "/" + url);
    }
    
    @SuppressWarnings("unchecked")
    private static Map<String,String> createMap(Document mapDoc) throws DocumentException{
        Element root = mapDoc.getRootElement();
        if (!root.getName().equals("map")){
            throw new DocumentException("root of map file is not \"map\": " + 
                    root.getName());
        }
        Map<String,String> result = new HashMap<String,String>();
        List<Element> kids = root.elements("mapID");
        for (Element child: kids){
            String target = child.attributeValue("target");
            String location = child.attributeValue("url");
            result.put(target,location);
        }
        return result;    
    }
}
