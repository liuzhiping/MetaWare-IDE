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
 * A scrolling container. It is only permitted to
 * have a single child.
 * @author David Pickens
 */
public interface IScrollPane extends IContainer {
    /**
     * Return the vertical scroll bar. If it is
     * not visible, then the returned object's
     * "visible" property will be false.
     * @return the vertical scroller
     */
    IScrollBar getVerticalScrollBar();
    
    /**
     * Return the horizontal scroll bar. If it is
     * not visible, then the returned object's
     * "visible" property will be false.
     * @return the horizontal scroller
     */
    IScrollBar getHorizontalScrollBar();

}
