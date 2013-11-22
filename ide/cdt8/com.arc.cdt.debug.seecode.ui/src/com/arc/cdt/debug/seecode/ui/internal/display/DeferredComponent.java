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

import java.awt.Dimension;
import java.awt.Point;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

import com.arc.widgets.IColor;
import com.arc.widgets.IComponent;
import com.arc.widgets.IComponentObserver;
import com.arc.widgets.IContainer;
import com.arc.widgets.IFont;

abstract class DeferredComponent implements IComponent {

    //private IColor mForeground;
    private double mHorizontalWeight;
    private int mHorizontalAlignment;
    private String mName;
    private boolean mEnabled = true;
    private String mTip;
    private IColor mBackground;


    @Override
    public IContainer getParent() {
        return null;
    }
    
    @Override
    public Object getComponent(){
        return null;
    }
    
    abstract protected Composite getParentComposite();
    
    protected Color getBackground(){
        if (mBackground != null){
            return (Color)mBackground.getObject();
        }
        return null;
    }

    @Override
    public void setBorderTitle(String title) {

    }

    @Override
    public String getBorderTitle() {
        return null;
    }

    @Override
    public void setBorder(int border) {
    

    }

    @Override
    public void setFont(IFont font) {
     
    }

    @Override
    public IFont getFont() {
       return null;
    }

    @Override
    public void setForeground(IColor color) {
        //mForeground = color;

    }

    @Override
    public void setBackground(IColor color) {
        mBackground = color;

    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public void setVisible(boolean v) {
      

    }

    @Override
    public void setGridSpan(int rowSpan, int colSpan) {


    }

    @Override
    public void setVerticalWeight(double weight) {

    }

    @Override
    public void setHorizontalWeight(double weight) {
        mHorizontalWeight = weight;

    }

    @Override
    public void setVerticalAlignment(int position) {
        
    }

    @Override
    public void setHorizontalAlignment(int position) {
        mHorizontalAlignment = position;

    }

    @Override
    public Dimension computeSize(int wHint, int hHint) {
       
        return null;
    }

    @Override
    public void dispose() {
       

    }

    @Override
    public void setPreferredSize(int width, int height) {
      
    }

    @Override
    public void setMargins(int top, int left, int bottom, int right) {
       
    }

    @Override
    public Point convertToScreenPoint(int x, int y) {
        return null;
    }

    @Override
    public int getWidth() {
      
        return 0;
    }

    @Override
    public int getHeight() {
     
        return 0;
    }

    @Override
    public void repaint() {
      
    }

    @Override
    public void revalidate() {
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        
    }

    @Override
    public Object getLayoutData() {
        
        return null;
    }

    @Override
    public void setLayoutData(Object object) {
       

    }

    @Override
    public Dimension getSize() {
      
        return null;
    }

    @Override
    public void setName(String name) {
       mName = name;

    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public void setEnabled(boolean v) {
        mEnabled = v;

    }

    @Override
    public boolean isEnabled() {
        return mEnabled;
    }

    @Override
    public void setToolTipText(String tip) {
        mTip = tip;

    }

    @Override
    public String getToolTipText() {return mTip;}
    
    protected double getHorizontalWeight(){
        return mHorizontalWeight;
    }
    
    protected int getHorizontalAlignment(){
        return mHorizontalAlignment;
    }

    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param observer
     */
    @Override
    public void addObserver (IComponentObserver observer) {
        // @todo Auto-generated method stub
        
    }

    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param observer
     */
    @Override
    public void removeObserver (IComponentObserver observer) {
        // @todo Auto-generated method stub
        
    }
}
