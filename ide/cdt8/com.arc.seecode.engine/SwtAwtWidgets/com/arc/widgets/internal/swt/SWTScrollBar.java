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

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Scrollable;

import com.arc.widgets.IScrollBar;

/**
 * @author David Pickens
 */
class SWTScrollBar implements IScrollBar {
    private ScrollBar mScrollBar;
    private int mPortSize;
    private List<IObserver> mObservers = new ArrayList<IObserver>();
    private boolean mNotifyOnChange = true;
    /**
     * 
     */
    public SWTScrollBar(ScrollBar sb) {
        mScrollBar = sb;
        sb.addSelectionListener(new SelectionListener(){

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (mNotifyOnChange)
                    fireObservers();              
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
                
            }});
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IScrollBar#getValue()
     */
    @Override
    public int getValue() {
        return mScrollBar.getSelection();
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IScrollBar#setValue(int)
     */
    @Override
    public void setValue(int value) {
        Scrollable parent = mScrollBar.getParent();
        if (parent instanceof ScrolledComposite){
            // Setting scrollbar selection doesn't work for
            // ScrolledComposite. Don't know shy.
            ScrolledComposite sb = (ScrolledComposite)parent;
            sb.setOrigin(0,value);
        }
        else
            mScrollBar.setSelection(Math.min(value,mScrollBar.getMaximum()-mScrollBar.getThumb()+1));
        fireObservers();
    }
    
    @Override
    public void setCurrentValue(int value){
        try{
            mNotifyOnChange = false;
            mScrollBar.setSelection(value);
        }
        finally {
            mNotifyOnChange = true;
        }
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IScrollBar#getMinimum()
     */
    @Override
    public int getMinimum() {
        return mScrollBar.getMinimum();
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IScrollBar#getMaximum()
     */
    @Override
    public int getMaximum() {
        return mScrollBar.getMaximum();
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IScrollBar#getIncrementAmount()
     */
    @Override
    public int getIncrementAmount() {
        return mScrollBar.getIncrement();
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IScrollBar#getPageAmount()
     */
    @Override
    public int getPageAmount() {
        return mScrollBar.getPageIncrement();
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IScrollBar#setMinimum(int)
     */
    @Override
    public void setMinimum(int v) {
        mScrollBar.setMinimum(v);

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IScrollBar#setMaximum(int)
     */
    @Override
    public void setMaximum(int v) {
        mScrollBar.setMaximum(v);

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IScrollBar#setIncrementAmount(int)
     */
    @Override
    public void setIncrementAmount(int amount) {
        mScrollBar.setIncrement(amount);

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IScrollBar#setPageAmount(int)
     */
    @Override
    public void setPageAmount(int amount) {
        mScrollBar.setPageIncrement(amount);
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IScrollBar#getPortSize()
     */
    @Override
    public int getPortSize() {
        return mPortSize;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IScrollBar#setPortSize(int)
     */
    @Override
    public void setPortSize(int size) {
        mPortSize = size;
        mScrollBar.setThumb(size);

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IScrollBar#isVisible()
     */
    @Override
    public boolean isVisible() {
        return mScrollBar.isVisible();
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IScrollBar#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean v) {
        mScrollBar.setVisible(v);

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IScrollBar#addObserver(com.arc.widgets.IScrollBar.IObserver)
     */
    @Override
    public void addObserver(IObserver observer) {
        synchronized(mObservers){
            mObservers.add(observer);
        }
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IScrollBar#removeObserver(com.arc.widgets.IScrollBar.IObserver)
     */
    @Override
    public void removeObserver(IObserver observer) {
        synchronized(mObservers){
            mObservers.remove(observer);
        }

    }
    
    protected void fireObservers(){
        IObserver list[];
        synchronized(mObservers){
            int size = mObservers.size();
            if (size == 0) return;
            list = mObservers.toArray(new IObserver[size]);       
        }
        for (int i = 0; i < list.length; i++){
            list[i].scrollBarChanged(this);
        }
    }

}
