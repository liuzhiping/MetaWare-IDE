/*
 * Main
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
package com.arc.pluginpurger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class Main {
    /**
     * Given the name of an eclipse installation directory, provide the list of all obsolete plugins and
     * features.
     * @param eclipseDir the Eclipse installation directory.
     * @return the list of obsolete plugins and features.
     */
    public static File[] getFilesToPurge(File eclipseDir){
        if (eclipseDir == null){
            throw new IllegalArgumentException("Argument is null");
        }
        if (!eclipseDir.isDirectory()){
            throw new IllegalArgumentException("\""+eclipseDir + "\" is not a directory");
        }
        if (!new File(eclipseDir,"features").exists() || !new File(eclipseDir,"plugins").exists()){
            throw new IllegalArgumentException("\"" + eclipseDir + "\" does not appear to be an Eclipse installation");
        }
        List<File> files = new ArrayList<File>();
        appendObsoleteItems(new File(eclipseDir,"plugins"),files);
        appendObsoleteItems(new File(eclipseDir,"features"),files);
        return files.toArray(new File[files.size()]);       
    }
    
    private static boolean isNumeric(String s){
        for (int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            if (c < '0' || c > '9'){
                return false;
            }
        }
        return true;
    }
    
    private static int getInt(String s){
        try{
            return Integer.parseInt(s);
        }
        catch(NumberFormatException x){
            return -1;
        }
    }
    
    /**
     * Given a directory with obsolete items (whether features or plugins) and a list,
     * append any obsolete plugins or features to the list.
     * @param dir either plugins or features directory.
     * @param list list to append to.
     */
    private static void appendObsoleteItems(File dir, List<File>list){
        File plugins[] = dir.listFiles();
        Arrays.sort(plugins, new Comparator<File>(){

            @Override
            public int compare (File o1, File o2) {
                String s1 = o1.getName();
                String s2 = o2.getName();
                String[] info1 = getNameAndVersion(s1);
                String[] info2 = getNameAndVersion(s2);
                if (info1 == null) return -1;
                if (info2 == null) return +1;
                if (!info1[0].equals(info2[0])){
                    return info1[0].compareTo(info2[0]);
                }
                String ver1[] = info1[1].split("\\.");
                String ver2[] = info2[1].split("\\.");
                int cnt = Math.min(ver1.length,ver2.length);
                for (int i = 0; i < cnt; i++){
                    if (!ver1[i].equals(ver2[i])){
                        // Eclipse appears to only recognize numeric version items
                        // in the first 3 fields. The 4'th field appars to be alphabetic only.
                        if (i < 3 && isNumeric(ver1[i]) && isNumeric(ver2[i])){
                            return getInt(ver1[i]) - getInt(ver2[i]);
                        }
                        return ver1[i].compareTo(ver2[i]);
                    }
                }
                if (ver1.length == ver2.length) return 0;
                if (ver1.length < ver2.length) return -1;
                return 1;
            }});
        File lastPluginFile = null;
        String lastPlugin = null;
        for (File plugin: plugins){
            String[] info = getNameAndVersion(plugin.getName());
            if (info != null){
                if (info[0].equals(lastPlugin)){
                    list.add(lastPluginFile);
                }
                lastPluginFile = plugin;
                lastPlugin = info[0];
            }
        }
    }

    /**
     * java com.arc.pluginpurger.Main [-d|-s] <em>eclipseDir</em>
     * @param args
     */
    public static void main (String[] args) {
        if (args.length == 0){
            explain();
            System.exit(1);
        }
        boolean delete = false;
        boolean genShell = false;
        boolean quiet = false;
        List<String> dirs = new ArrayList<String>();
        
        for (String arg: args){
            if (arg.startsWith("-")){
                if (arg.length() == 1){
                    System.err.println("Unknown argument: " + arg);
                    explain();
                    System.exit(2);
                }
                for (int j = 1; j < arg.length(); j++){
                    switch(arg.charAt(j)){
                        case 'h':
                            explain();
                            System.exit(1);
                        case 'd': 
                            if (genShell) {
                                System.err.println("-d and -s are mutually exclusive");
                                System.exit(3);
                            }
                            delete = true; 
                            break;
                        case 's':
                            if (delete){
                                System.err.println("-d and -s are mutually exclusive");
                                System.exit(3);
                            }
                            genShell = true;
                            break;
                        case 'q': quiet = true;
                        default:
                            System.err.println("Flag \"" + arg.charAt(j) + "\" is not recognized in " + arg);
                            System.exit(4);
                    }
                }
            }
            else dirs.add(arg);
        }
        
        if (dirs.size() == 0){
            explain();
            System.exit(5);
        }
        
        List<File> fileList = new ArrayList<File>();
        try {
            for (String dir: dirs) {
                File[] files = getFilesToPurge(new File(dir));
                fileList.addAll(Arrays.asList(files));
            }           
        }
        catch(IllegalArgumentException x){
            System.err.println("Terminal error: " + x);
        }
        if (genShell){
            for (File f: fileList){
                if (f.isDirectory()){
                    System.out.println("rm -rf " + f.getPath().replaceAll("\\\\","/"));
                }
                else System.out.println("rm " + f.getPath().replaceAll("\\\\","/"));
            }
        }
        else if (delete){
            for (File f: fileList){
                if (f.isDirectory()){
                    deleteDirectory(f,quiet);
                }
                else {
                    if (!quiet) System.out.println("Deleting " + f);
                    if (!f.delete()){
                        System.err.println("Couldn't delete " + f);
                    }
                }
            }
        }
        else {
            for (File f: fileList){
                System.out.println(f);
            }
        }
    }
    
    private static void deleteDirectory(File dir, boolean quiet){
        for (File child: dir.listFiles()){
            if (child.isDirectory()){
                deleteDirectory(child,quiet);
            }
            else {
                if (!child.delete()){
                    System.err.println("Couldn't delete " + child);
                }
                else if (!quiet) System.out.println("Deleting " + child);
            }
        }
        
        if (dir.delete()){
            if (!quiet) System.out.println("Deleting directory " + dir);
        }
        else {      
            System.err.println("Couldn't delete directory " + dir);
        }
    }

    private static void explain () {
        System.err.println("Arguments are: [-d|-s] [-q] <eclipse-dir>");
        System.err.println("where:");
        System.err.println("    -d indicates that the obsolete plugins and features are to be removed.");
        System.err.println("    -s indicates that shell commands are to be generated on stdout that will");
        System.err.println("       remove the obsolete plugins and features.");
        System.err.println("    -q don't display names of files as they are deleted.");
        System.err.println("    <eclipse-dir>  the path of the Eclipse installation directory.");
        System.err.println("");
        System.err.println("If neither -d or -s is specified, the list of obsolete files will be displayed on stdout");
    }

    static String[] getNameAndVersion(String fullName){
        int index = fullName.indexOf('_');
        if (index > 0) {
            if (fullName.endsWith(".jar")) {
                fullName = fullName.substring(0,fullName.length()-4);
            }
            return new String[]{fullName.substring(0,index),fullName.substring(index+1)};
        }
        return null;
    }
}
