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

import javax.swing.JSplitPane;

import com.arc.widgets.IContainer;
import com.arc.widgets.ISplitPane;

/**
 * @author David Pickens
 */
class SplitPane extends AbstractContainer implements ISplitPane {

    private boolean mHorizontal;
    private double mSliderRatio = 0.5;
    private double mResizeWeight = 0.5;
    private int mSliderLocation = -1;
    private JSplitPane mPane = null;

    /**
     * @param parent
     * @param mapper
     */
    SplitPane(IContainer parent, boolean horizontal, IComponentMapper mapper) {
        super(parent, mapper);
        mHorizontal = horizontal;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.ISplitPane#setDividerLocation(int)
     */
    @Override
    public void setDividerLocation(int location) {
        mSliderLocation = location;
        if (mPane != null)
            mPane.setDividerLocation(location);
        

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.ISplitPane#setDividerLocation(double)
     */
    @Override
    public void setDividerLocation(double location) {
        if (location < 0 || location > 1.0)
            throw new IllegalArgumentException("Must be in range 0..1");
        mSliderRatio = location;
        if (mPane != null)
            mPane.setDividerLocation(location);

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.swing.SwingComponent#instantiate()
     */
    @Override
    protected Component instantiate() {
        JSplitPane pane = new JSplitPane(mHorizontal?JSplitPane.HORIZONTAL_SPLIT:JSplitPane.VERTICAL_SPLIT);
        pane.setContinuousLayout(true);
        pane.setResizeWeight(mResizeWeight);
        if (mSliderLocation >= 0)
            pane.setDividerLocation(mSliderLocation);
        else
            pane.setDividerLocation(mSliderRatio);
        mPane = pane;
        return pane;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IContainer#getContainerStyle()
     */
    @Override
    public int getContainerStyle() {
        return 0;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IContainer#setHorizontalSpacing(int)
     */
    @Override
    public void setHorizontalSpacing(int pixels) {
        
        
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IContainer#setVerticalSpacing(int)
     */
    @Override
    public void setVerticalSpacing(int pixels) {
 
        
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.ISplitPane#setResizeWeight(double)
     */
    @Override
    public void setResizeWeight(double weight) {
        if (weight < 0 || weight > 1)
            throw new IllegalArgumentException("invalid resize weight: " + weight);
        mResizeWeight = weight;
        if (mPane != null)
            mPane.setResizeWeight(weight);
        
    }

}
