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
 * @author David Pickens
 */
public interface ISplitPane extends IContainer {
    /**
     * Set the divider location, in pixels, from the left, or top.
     * @param location the location in pixels.
     */
    void setDividerLocation(int location);
    
    /**
     * Set the divider location as a ratio between 0 and 1, to
     * represent the proportion of how much space the left (top)
     * panel occupies relative to the whole.
     * @param location ration between 0 and 1.
     */
    void setDividerLocation(double location);
    
    /**
     * Designate how much the left/top panel changes when
     * the entire split pane size is changed. A value of 0 means
     * that the left/top panel will not change at all; the right/bottom
     * will absorb all change. A value of 1.0 means that the right/bottom
     * does not change. Any value between 0 and 1 will cause the
     * left/top panel to change preportionally.
     * @param weight a ratio between 0 and 1 that designates how much
     * of a resize should be absorbed by the left/top panel.
     */
    void setResizeWeight(double weight);
}
