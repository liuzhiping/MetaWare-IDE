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
package com.arc.widgets.internal.swing;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IFileChooser;

class FileChooser extends JFileChooser implements IFileChooser {
    private Component mOwner;
    private int mStyle;
    private ArrayList<String> fFilters;

    FileChooser(Component owner, int style) {
        mOwner = owner;
        mStyle = style;
        if ((style & IComponentFactory.FILE_MULTI) != 0)
            setMultiSelectionEnabled(true);
        if ((style & IComponentFactory.FILE_DIRS) != 0)
            setFileSelectionMode(DIRECTORIES_ONLY);
    }

    /**
     * Open dialog. Will return when dialog is dismissed.
     * If file was selected, the first will be returned; otherwise <code>null</code>.
     */
    @Override
    public File open() {
        int status;
        setFilters();
        if ((mStyle & IComponentFactory.FILE_SAVE) != 0)
            status = showSaveDialog(mOwner);
        else
            status = showOpenDialog(mOwner);
        if (status != APPROVE_OPTION)
            return null;
        return getSelectedFile();
    }

    @Override
    public void setToolTip(String s) {
        setApproveButtonToolTipText(s);
    }
    @Override
    public String getToolTip() {
        return getApproveButtonToolTipText();
    }

    public void dispose() {
        // What do we do?
    }

    /**
     * Set title to appear on dialog.
     */
    @Override
    public void setTitle(String s) {
        setDialogTitle(s);
    }

    @Override
    public String getTitle() {
        return getDialogTitle();
    }

    /**
     * Set directory
     */
    @Override
    public void setDirectory(File dir) {
        setCurrentDirectory(dir);
    }
    
    /**
     * Set directory
     */
    @Override
    public File getDirectory() {
        return getCurrentDirectory();
    }
    
    @Override
    public void addFilter(String extension, String desc){
        if (fFilters == null) {
            fFilters = new ArrayList<String>();
        }
        fFilters.add(extension);
        if (desc == null) desc = extension;
        fFilters.add(desc);
    }

    /**
     * Set extensions that will filter the list.
     */
    private void setFilters() {
        if (fFilters != null) {
            for ( int i = 0; i < fFilters.size(); i+=2) {
                final String patterns[] = fFilters.get(i).split(";");
                for (int j = 0; j < patterns.length; j++){
                    patterns[j] = patterns[j].startsWith("*")?patterns[j].substring(1):patterns[j];
                }
                final String desc = fFilters.get(i+1);
                addChoosableFileFilter(new FileFilter(){

                    @Override
                    public boolean accept (File f) {
                        for (String pattern: patterns) {
                            if (f.getName().endsWith(pattern)){
                                return true;
                            }
                        }
                        return false;                      
                    }

                    @Override
                    public String getDescription () {
                        return desc;
                    }});
            }
        }
    }

    /**
     * Return selected file or the first of multiple selected.
     * Returns <code>null</code> if dialog was cancelled.
     */
    @Override
    public File getFile() {
        return getSelectedFile();
    }

    /**
     * Return all selected files.
     * Returns <code>null</code> if dialog was cancelled.
     */
    @Override
    public File[] getFiles() {
        return getSelectedFiles();
    }

    /**
     * @see com.arc.widgets.IFileChooser#setFile(File)
     */
    @Override
    public void setFile(File f) {
        this.setSelectedFile(f);

    }

}
