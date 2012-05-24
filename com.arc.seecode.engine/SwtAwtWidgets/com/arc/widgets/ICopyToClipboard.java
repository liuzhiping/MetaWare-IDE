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


/**
 * A callback method that can be passed to a {@link ITextCanvas} object to
 * be called when a copyo-to-clipboard peration is done.
 * @author dpickens
 *
 */
public interface ICopyToClipboard {
    /**
     * Copy the selected text of a {@link ITextCanvas} to the clipboard. If the parameter "all" is true,
     * then there is a pending "Select All" operation and the implementor is to
     * copy all associated content to the clip board, whether it is showing or not.
     * <P>
     * If the method returns <code>false</code> then the caller is expected to
     * copy all visible text to the clip board.
     * <P>
     * If the method returns true, the caller is to assume that the operation
     * has taken place successfully.
     * <P>
     * @param all if true then copy all of the associated content whether it is showing or not.
     * @return true if the operation was successful; otherwise,
     * assume the caller is responsible for the operation.
     */
    boolean copyToClipboard(ITextCanvas canvas, boolean all);

}
