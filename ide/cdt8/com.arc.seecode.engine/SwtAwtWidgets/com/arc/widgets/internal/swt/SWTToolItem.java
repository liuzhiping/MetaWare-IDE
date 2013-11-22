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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.arc.widgets.IImage;
import com.arc.widgets.IToolItem;

/**
 * @author David Pickens
 */
class SWTToolItem implements IToolItem {
    private ToolItem mItem;
    private int mStyle;
    private String mName;
    private List<IObserver> mObservers = new ArrayList<IObserver>();
    /**
     * 
     */
    public SWTToolItem(SWTToolBar parent, int style) {
        ToolBar tb = (ToolBar)parent.getComponent();
        mItem = new ToolItem(tb,computeSwtStyle(style));
        mStyle = style;
        mItem.addSelectionListener(new SelectionListener(){

            @Override
            public void widgetSelected(SelectionEvent e) {
                notifyObservers();              
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
                
            }});
    }
    
    private static int computeSwtStyle(int style){
        switch(style){
            case PUSH: return SWT.PUSH;
            case CHECK: return SWT.CHECK;
            case RADIO: return SWT.RADIO;
            case PULLDOWN: return SWT.DROP_DOWN;
            default:
                throw new IllegalArgumentException("bad style");
        }
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IToolItem#getSelection()
     */
    @Override
    public boolean isSelected() {
       return mItem.getSelection();
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IToolItem#setImage(com.arc.widgets.IImage)
     */
    @Override
    public void setImage(IImage image) {
        mItem.setImage(image!=null?(Image)image.getObject():null);

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IToolItem#setText(java.lang.String)
     */
    @Override
    public void setText(String text) {
        mItem.setText(text);
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IToolItem#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        mName = name;

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IToolItem#getName()
     */
    @Override
    public String getName() {
        return mName;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IToolItem#addObserver(com.arc.widgets.IToolItem.IObserver)
     */
    @Override
    public void addObserver(IObserver observer) {
        synchronized(mObservers){
            mObservers.add(observer);
        }

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IToolItem#removeObserver(com.arc.widgets.IToolItem.IObserver)
     */
    @Override
    public void removeObserver(IObserver observer) {
        synchronized(mObservers){
            mObservers.remove(observer);
        }
    }
    
    private void notifyObservers(){
        IObserver observers[];
        synchronized(mObservers){
            observers =  mObservers.toArray(new IObserver[mObservers.size()]);
        }
        for (IObserver o:observers){
            o.itemChanged(this);
        }
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IToolItem#getStyle()
     */
    @Override
    public int getStyle() {
        return mStyle;
    }
    
    @Override
    public void setSelected(boolean v){
        mItem.setSelection(v);
    }
    
    @Override
    public void setToolTipText(String t){
        mItem.setToolTipText(t);
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IToolItem#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean v) {
        mItem.setEnabled(v);
        
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IToolItem#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return mItem.isEnabled();
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IWidget#getToolTipText()
     */
    @Override
    public String getToolTipText() {
        return mItem.getToolTipText();
    }

}
