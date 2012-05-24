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
package com.arc.widgets;


import java.io.File;


/**
 * A file chooser window. Factory method determines whether file is to be one for reading or writing, and whether
 * multiple files can be selected.
 */
public interface IFileChooser {

    /**
     * Open dialog. Will return when dialog is dismissed. If file was selected, the first will be returned; otherwise
     * <code>null</code>.
     */
    File open ();

    void setToolTip (String s);

    String getToolTip ();

    /**
     * Set title to appear on dialog.
     */
    void setTitle (String s);

    String getTitle ();

    /**
     * Set directory
     */
    void setDirectory (File dir);

    /**
     * Return directory that dialog is set to.
     * @return directory that dialog is set to.
     */
    File getDirectory ();

    /**
     * Set extensions that will filter the list.
     * @param extension a file extension (e.g. "*.obj"). Multiple may be specified separated by semicolons.
     * @param description a description of the file type (e.g. "Object files").
     */
    void addFilter (String extension, String description);

    /**
     * Return selected file or the first of multiple selected. Returns <code>null</code> if dialog was cancelled.
     */
    File getFile ();

    /**
     * Set selected file
     */
    void setFile (File f);

    /**
     * Return all selected files. Returns <code>null</code> if dialog was cancelled.
     */
    File[] getFiles ();

}
