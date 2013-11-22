/*
 * FileUtil
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
package com.arc.cdt.testutil;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.arc.cdt.tests.plugin.TestsPlugin;


public class FileUtil {
    
    /**
     * Compare two files.
     * @param file1 relative path of the first file to compare.
     * @param file2 relative path of the second file to compare.
     * @return true if the two files compare equal; false otherwise.
     * @throws IOException 
     */
    public static boolean compareFiles(String file1, String file2) throws IOException{
        InputStream streamA = null;      
        InputStream streamB = null;
        try {
            streamA = TestsPlugin.getDefault().getInputStream(file1);
            streamB = TestsPlugin.getDefault().getInputStream(file2);
            return compareStreams(streamA,streamB);
        }
        finally {
            if (streamA != null)
                streamA.close();
            if (streamB != null)
                streamB.close();
        }       
    }
    
    /**
     * Copy one file to another.
     * @param file1 the source file.
     * @param file2 the target file.
     * @throws IOException 
     */
    public static void copyFile(File file1, File file2) throws IOException{
    	FileInputStream input = new FileInputStream(file1);
    	FileOutputStream output = new FileOutputStream(file2);
    	copyStream(input,output);
    }
    
    /**
     * Compare two files.
     * @param file1 first file to compare.
     * @param file2 second file to compare.
     * @param ignoreCR if true, CR/LF vs. LF differences will be ignored.
     * @return true if the two files compare equal; false otherwise.
     * @throws IOException 
     */
    public static boolean compareFiles(File file1, File file2, boolean ignoreCR) throws IOException{
        InputStream streamA = null;      
        InputStream streamB = null;
        try {
            streamA = new FileInputStream(file1);
            streamB = new FileInputStream(file2);
            return compareStreams(streamA,streamB,ignoreCR);
        }
        finally {
            if (streamA != null)
                streamA.close();
            if (streamB != null)
                streamB.close();
        }       
    }
    
    /**
     * Return the content of a stream as an ASCII string.
     * @param input input stream.
     * @return the content of the stream as an ASCII string.
     * @throws IOException
     */
    public static String getContent(InputStream input) throws IOException{
        ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
        copyStream(input,out);
        return out.toString();
    }


    /**
     * Copy the input to the output.
     * @param input
     * @param output
     * @throws IOException 
     */
    public static void copyStream(InputStream input, OutputStream output) throws IOException{
        byte buffer[] = new byte[4096];
        while(true){
            int amount = input.read(buffer);
            if (amount < 0) break;
            output.write(buffer,0,amount);
        }
    }
    
    /**
     * Locate the first byte that differs in two input stream.
     * @param a first stream to compare.
     * @param b second stream to compare.
     * @return -1 if they compare; otherwise the byte position where they do not.
     * @throws IOException
     */
    public static int findFirstDifference(InputStream a, InputStream b) throws IOException{
        byte bufferA[] = new byte[8192];
        byte bufferB[] = new byte[8192];
        int cnt = 0;
        while (true){
            int amountA = a.read(bufferA);
            int amountB = b.read(bufferB);
            if (amountA < 0) {
                if (amountB >= 0) return cnt;
                return -1; // no differences.
            }
            else if (amountB < 0) return cnt;
            int amount = Math.min(amountA,amountB);
            for (int i = 0; i < amount; i++){
                if (bufferA[i] != bufferB[i]){
                    return cnt + i;
                }
            }
            cnt += amount;
            if (amountA > amount || amountB > amount) return cnt;
        }     
    }
    
    public static boolean compareStreams(InputStream a, InputStream b) throws IOException{
        return findFirstDifference(a,b) < 0;
    }
    
    public static boolean compareStreams(InputStream a, InputStream b, boolean ignoreCR) throws IOException{
        if (!ignoreCR) return compareStreams(a,b);
        BufferedReader ba = new BufferedReader(new InputStreamReader(a));
        BufferedReader bb = new BufferedReader(new InputStreamReader(b));
        while (true){
            String line1 = ba.readLine();
            String line2 = bb.readLine();
            if (line1 == null) 
                return line2 == null;
            if (line2 == null) 
                return false;
            if (!line1.equals(line2)) 
                return false;
        }     
    }
}
