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
package com.arc.seecode.internal.display;

/**
 * @author David Pickens
 */
interface ITextScrollObserver {
    /**
     * Called when user is scrolling vertically.
     * @param line the new line position.
     */
    public abstract void onScrollToLine(int line);

    /**
     * Called when user is scrolling horizontally.
     * @param column the zero-based column to scroll to horizontally.
     */
    public abstract void onScrollToColumn(int column);
}
