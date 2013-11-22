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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

import com.arc.widgets.IContainer;
import com.arc.widgets.ISplitPane;

/**
 * @author David Pickens
 */
class SWTSplitPane extends AbstractContainer implements ISplitPane {
    private boolean mHorizontal;
    private double mResizeWeight = 0.5;
    //private double mSliderRatio = 0.5;
    //private int mSliderLocation = -1;
    private SashForm mForm = null;
    private boolean mWeightsSet = false;
    /**
     * @param parent
     * @param mapper
     */
    public SWTSplitPane(IContainer parent, boolean horizontal, IComponentMapper mapper) {
        super(parent, mapper);
        mHorizontal = horizontal;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.swt.Component#instantiate()
     */
    @Override
    protected Widget instantiate() {
        //We cannot apply the weights until all children are added. But there is no
        // "child-added" event. So we check for things by overrideing "computeSize"
        mForm = new SashForm(getParentComposite(),mHorizontal?SWT.HORIZONTAL:SWT.VERTICAL){
            @Override
            public Point computeSize(int wHint, int hHint, boolean flush){
                if (!mWeightsSet){
                    mWeightsSet = true;
                    SWTSplitPane.this.setWeights();
                }
                return super.computeSize(wHint,hHint,flush);
            }
        };
        return mForm;

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IContainer#getContainerStyle()
     */
    @Override
    public int getContainerStyle() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IContainer#setHorizontalSpacing(int)
     */
    @Override
    public void setHorizontalSpacing(int pixels) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IContainer#setVerticalSpacing(int)
     */
    @Override
    public void setVerticalSpacing(int pixels) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.ISplitPane#setDividerLocation(int)
     */
    @Override
    public void setDividerLocation(int location) {
        //mSliderLocation = location;
        
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.ISplitPane#setDividerLocation(double)
     */
    @Override
    public void setDividerLocation(double location) {
       if (location < 0 || location > 1)
           throw new IllegalArgumentException("bad divider location: " + location);
       //mSliderRatio = location;      
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.ISplitPane#setResizeWeight(double)
     */
    @Override
    public void setResizeWeight(double weight) {
        if (weight < 0 || weight > 1)
            throw new IllegalArgumentException("bad resize weight: " + weight);
        mResizeWeight = weight;  
        if (mWeightsSet)
            setWeights();
    }

    /**
     * 
     */
    private void setWeights() {
        Control kids[] = mForm.getChildren();
        if (kids.length == 2){ // should aways be true
            int weights[] = new int[]{(int)(100*mResizeWeight),(int)(100*(1-mResizeWeight))};
            mForm.setWeights(weights);
        }
    }

}
