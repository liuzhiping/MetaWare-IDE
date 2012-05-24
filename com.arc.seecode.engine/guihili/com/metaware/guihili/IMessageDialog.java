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


/**
 * A callback interface for displaying message dialogs
 * @author David Pickens
 */
public interface IMessageDialog {
    
    /**
     * Display an information dialog.
     * @param msg the message to appear.
     */
    void showMessageDialog(String msg);
    /**
     * Display an error dialog.
     * @param msg the message to appear.
     */
    void showErrorDialog(String msg);

}
