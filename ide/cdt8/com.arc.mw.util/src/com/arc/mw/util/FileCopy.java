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
package com.arc.mw.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility methods for copying files and directories.
 * @author David Pickens
 */
public class FileCopy {
    /**
     * Copy the contents of one file to another.
     * @param dest the path of the new file.
     * @param src the path of the file being copied from.
     * @throws IOException if an error occurred reading the source file, or
     * an error occurred writing the destination file.
     * 
     * @pre dest != null && src != null
     * @post $none
     */
    static public void copyFile(String dest, String src) throws IOException{
        copyFile(new File(dest), new File(src));
    }
    
    /**
     * Copy the contents of one file to another.
     * @param dest the path of the new file.
     * @param src the path of the file being copied from.
     * @throws IOException if an error occurred reading the source file, or
     * an error occurred writing the destination file.
     * 
     * @pre dest != null && src != null
     * @post $none
     */
    static public void copyFile(File dest, File src) throws IOException{
        if (dest.getCanonicalFile().equals(src.getCanonicalFile())){
            // Don't allow one file to be copied on top of itself.
            return;           
        }
        FileInputStream input = new FileInputStream(src);
        FileOutputStream output = new FileOutputStream(dest);

        byte buffer[] = new byte[8192];
        int cnt = input.read(buffer);
        while (cnt > 0){
            output.write(buffer,0,cnt);
            cnt = input.read(buffer);           
        }
        input.close();
        output.close();
        //if (!src.canWrite()) dest.setReadOnly();
    }
    
    /**
     * Copy the content of one directory to another.
     * @param dest the name of the target directory.
     * @param src the source directory.
     * @throws IOException if a source file could not be read or
     * a target file could not be written, or if the source is not a directory.
     */
    static public void copyDirectory(String dest, String src) throws IOException{
        copyDirectory(new File(dest),new File(src));      
    }
    
    /**
     * Copy the content of one directory to another.
     * @param dest the name of the target directory.
     * @param src the source directory.
     * @throws IOException if a source file could not be read or
     * a target file could not be written, or if the source is not a directory.
     */
    static public void copyDirectory(File dest, File src) throws IOException{
        if (!src.isDirectory()){
            throw new IOException("Source is not a directory (\"" + src + "\"");
        }
        if (dest.exists()){
            if (!dest.isDirectory())
                throw new IOException("Destination exists, and is not a directory\"" +
                                dest + "\"");
        }
        else if (!dest.mkdirs()){
            throw new IOException("Destination directory, " + dest + ", can't be created");
        }
        //Don't copy one directory on top of itself.
        if (src.getCanonicalFile().equals(dest.getCanonicalFile()))
            return;
        
        File[] files = src.listFiles();
        if (files == null){
            throw new IOException("Can't read source directory \"" +
                    src + "\"");
        }
        for (int i = 0; i < files.length; i++){
            if (!files[i].equals("..")){
                String name = files[i].getName();
                File target = new File(dest,name);
                if (files[i].isDirectory()){
                    copyDirectory(target,files[i]);
                }
                else if (files[i].isFile()){
                    copyFile(target,files[i]);
                }
                target.setLastModified(files[i].lastModified());
            }           
        }
    }
}
