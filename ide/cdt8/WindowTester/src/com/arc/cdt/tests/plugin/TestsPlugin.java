/*
 * TestsPlugin
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
package com.arc.cdt.tests.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;


public class TestsPlugin extends Plugin {
    private static TestsPlugin instance;
    
    public TestsPlugin(){
        instance = this;
    }
    
    public static TestsPlugin getDefault(){
        return instance;
    }
    
    /**
     * Returns URL to access a resource.
     * @param relativePath path relative to this plugin project.
     * @param forWriting if true, make intermediate directories if necessary.
     */
    public URL getResource (String relativePath, boolean forWriting) {
        URL url = FileLocator.find(getBundle(),new Path(relativePath),null);
        if (url == null && forWriting) {
            String parent = new File(relativePath).getParent();
            if (parent != null && parent.length() > 0){
                URL parentURL = getResource(parent,forWriting);
                if (parentURL != null) {
                    try {
                        parentURL = FileLocator.toFileURL(parentURL);
                        File f = new File(parentURL.getFile(), new File(relativePath).getName());
                        url = f.toURI().toURL();
                    }
                    catch (MalformedURLException e) {
                       //malformed.
                    }
                    catch (IOException e) {
                        
                    }
                }
            }
        }
        return url;
    }
    
    public OutputStream getOutputStream(String relativePath) throws IOException{
        URL url = getResource(relativePath,true);
        if (url != null) url = FileLocator.toFileURL(url);
        if (url != null) {
            return new FileOutputStream(url.getPath());
        }
        throw new IOException("Cannot open \"" + relativePath + "\" for writing");
    }
    
    /**
     * Return input stream to a resource, or <code>null</code> if it doesn't
     * exists.
     * @param relativePath
     * @return input stream for resource.
     * @throws IOException if the URL exists but cannot be opened for some reason.
     */
    public InputStream getInputStream(String relativePath) throws IOException {
        URL url = getResource(relativePath,false);
        if (url != null) {
            return url.openStream();
        }
        return null;
        
    }
}
