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
package com.arc.cdt.debug.seecode.core;

/**
 * A callback for displaying an error box.
 * @author David Pickens
 */
public interface IDisplayMessage {
    /**
     * Display an error box.
     * @param title the title to appear on top of the box.
     * @param msg the message.
     */
    public void displayError(String title, String msg);
    
    /**
     * Display a message box containing a note or warning.
     * @param title the title to appear on top of the box.
     * @param msg the message.
     */
    public void displayNote(String title, String msg);
}
