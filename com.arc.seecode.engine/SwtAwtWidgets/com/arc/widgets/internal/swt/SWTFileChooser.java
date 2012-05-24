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
package com.arc.widgets.internal.swt;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IFileChooser;

/**
 * A builder for a SWT file dialog.
 */
class SWTFileChooser implements IFileChooser {
    private Dialog mDialog;
    private String mFileName;
    private List<String> fFilters = null;
    private List<String> fDesc = null;

    SWTFileChooser(Control owner, int style) {
        int swtStyle = 0;
        if ((style & IComponentFactory.FILE_SAVE) != 0)
            swtStyle |= SWT.SAVE;
        if ((style & IComponentFactory.FILE_OPEN) != 0)
            swtStyle |= SWT.OPEN;
        if ((style & IComponentFactory.FILE_MULTI) != 0)
            swtStyle |= SWT.MULTI;
        Shell shell = owner == null ? null : owner.getShell();
        if (shell == null) shell = Display.getDefault().getActiveShell();
        if ((style & IComponentFactory.FILE_DIRS) != 0)
            mDialog = new DirectoryDialog(shell, swtStyle);
        else
            mDialog = new FileDialog(shell, swtStyle);
    }

    @Override
    public File open() {
        String fn;
        if (mDialog instanceof FileDialog) {
            if (fFilters != null) {
                ((FileDialog)mDialog).setFilterExtensions(fFilters.toArray(new String[fFilters.size()]));
                ((FileDialog)mDialog).setFilterNames(fDesc.toArray(new String[fDesc.size()]));
            }
            fn = ((FileDialog)mDialog).open();
        }
        else
            fn = ((DirectoryDialog)mDialog).open();
        mFileName = fn;
        if (fn == null || fn.length() == 0){
            mFileName = "";
            return null;
        }
        return new File(fn);
    }

    @Override
    public void setToolTip(String text) {
    }
    
    @Override
    public String getToolTip() { return null; }

    @Override
    public String getTitle() {
        return mDialog.getText();
    }

    @Override
    public void setTitle(String s) {
        mDialog.setText(s);
    }
    

    /**
     * Set directory
     */
    @Override
    public void setDirectory(File dir) {     
        String path; 
        try {
            path = dir.getCanonicalPath();
        }
        catch (IOException x) {
            path = dir.getAbsolutePath();
        }
        if (mDialog instanceof FileDialog)
            ((FileDialog)mDialog).setFilterPath(path);
        else
            ((DirectoryDialog)mDialog).setFilterPath(path);
    }
    
    @Override
    public File getDirectory(){
        String s= null;
        if (mDialog instanceof FileDialog){
            s = ((FileDialog)mDialog).getFilterPath();
        }
        else{
            s = ((DirectoryDialog)mDialog).getFilterPath();
        }
        return s!=null?new File(s):null;
    }

    /**
     * Set extensions that will filter the list.
     */
    @Override
    public void addFilter(String filter, String description) {
        if (mDialog instanceof FileDialog) {
            if (fFilters == null) {
                fFilters = new ArrayList<String>();
                fDesc = new ArrayList<String>();
            }
            if (description == null) description = "";
            fFilters.add(filter);
            fDesc.add(description);    
        }
    }

    /**
     * Return selected file or the first of multiple selected.
     * Returns <code>null</code> if dialog was cancelled.
     */
    @Override
    public File getFile() {
        if (mFileName == null || mFileName.length() == 0)
            return null;
        return new File(mFileName);
    }

    /**
     * Return all selected files.
     * Returns <code>null</code> if dialog was cancelled.
     */
    @Override
    public File[] getFiles() {
        String fn[] = ((FileDialog)mDialog).getFileNames();
        if (fn == null || fn.length == 0)
            return null;
        File f[] = new File[fn.length];
        for (int i = 0; i < fn.length; i++)
            f[i] = new File(fn[i]);
        return f;
    }

    /**
     * @see com.arc.widgets.IFileChooser#setFile(File)
     */
    @Override
    public void setFile (File f) {
        if (f != null) {
            if (mDialog instanceof FileDialog) {
                String parent = f.getParent();
                if (parent != null)
                    ((FileDialog) mDialog).setFilterPath(parent);
                ((FileDialog) mDialog).setFileName(f.getName());
            }
            else if (mDialog instanceof DirectoryDialog){
                String dir = f.isDirectory()?f.getPath():f.getParent();
                if (dir != null) {
                    ((DirectoryDialog)mDialog).setFilterPath(dir);
                }
            }
        }
    }

}
