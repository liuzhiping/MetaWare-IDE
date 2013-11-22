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
package com.metaware.guihili;


import org.xml.sax.InputSource;


/**
 * Interface for opening a stream given an unqualified name.
 * @author David Pickens
 * @version April 24, 2002
 */
public interface IFileResolver {

    /**
     * @param name the unqualified name of the file to be opened.
     * @return a reader for the file, or null if file can't be resolved.
     */
    public InputSource openFile (String name);
}
