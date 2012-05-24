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
package com.arc.cdt.debug.seecode.ui.internal.display;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.arc.widgets.IColor;
import com.arc.widgets.ILabel;

class DeferredLabel extends DeferredComponent implements ILabel, IToolBarItem {

    private String mText;
    private static int sItem = 0;
    private String mID;
    private Label mLabel = null;
    private boolean mWrap = false;
    
    DeferredLabel(String text){ 
        mID = "label" + sItem++;
        if (text != null)
            setText(text);
        }

    @Override
    public String getText() {
        return mText;
    }

    @Override
    public void setText(String text) {
        // Must pad labels because the layout manager for a toolbar doesn't have any insets for such
        // (at least under Windows)
        mText = "  " + text + " ";
        if (mLabel != null){
            mLabel.setText(mText);
        }

    }
    
    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param color
     */
    @Override
    public void setBackground(IColor color) {
        super.setBackground(color);
        if (mLabel != null && color != null){
            mLabel.setBackground((Color)color.getObject());
        }
    }
    
  
    @Override
    public void setToolTipText(String tip) {
        super.setToolTipText(tip);
        if (mLabel != null){
            mLabel.setToolTipText(tip);
        }
    }


    @Override
    public void addToToolBar(IToolBarManager manager) {
        IContributionItem c = new MyLabelContribution(mID);
        manager.add(c);       
    }
    
    class MyLabelContribution extends ControlContribution {

        protected MyLabelContribution(String id) {
            super(id);
        }

        @Override
        protected Control createControl(Composite parent) {
            Label label = new Label(parent,SWT.CENTER | (mWrap?SWT.WRAP:0));
            label.addDisposeListener(new DisposeListener(){

                @Override
                public void widgetDisposed(DisposeEvent e) {
                    DeferredLabel.this.mLabel = null;
                    
                }});
            label.setText(mText);
            label.setEnabled(isEnabled());
            if (getToolTipText() != null)
                label.setToolTipText(getToolTipText());
            DeferredLabel.this.mLabel = label;
            Color bg = DeferredLabel.this.getBackground();
            // Don't know how it happens, but the color gets disposed underneath
            // us sometimes.
            if (bg != null && !bg.isDisposed()) label.setBackground(bg);
            return label;
        }
        
    }

    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param v
     */
    @Override
    public void setEnabled(boolean v) {
        super.setEnabled(v);
        if (mLabel != null){
            mLabel.setEnabled(v);
        }
    }
    
    @Override 
    protected Composite getParentComposite(){
        if (mLabel != null){
            return mLabel.getParent();
        }
        return null;
    }

    @Override
    public void setWrap (boolean v) {
        mWrap = v;       
    }
}
