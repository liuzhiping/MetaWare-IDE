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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Widget;

import com.arc.widgets.IContainer;
import com.arc.widgets.IList;

/**
 * @author David Pickens
 */
public class SWTList extends Component implements IList, SelectionListener {
    private List _list = null;
    private ArrayList<IObserver> mObservers;
    private boolean mMulti;
    /**
     * @param parent
     * @param mapper
     */
    public SWTList(IContainer parent, boolean multi, IComponentMapper mapper) {
        super(parent, mapper);
        mMulti = multi;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.swt.Component#instantiate()
     */
    @Override
    protected Widget instantiate() {
        _list = new List(getParentComposite(), (mMulti?SWT.MULTI:SWT.SINGLE) | SWT.H_SCROLL
                | SWT.V_SCROLL);
        _list.addSelectionListener(this);
        _list.addMouseListener(new MouseListener(){

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                fireDoubleClick(e.y);
            }

            @Override
            public void mouseDown(MouseEvent e) {
            }

            @Override
            public void mouseUp(MouseEvent e) {
            }});
        return _list;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IList#setItems(java.lang.String[])
     */
    @Override
    public void setItems(String[] list) {
        List listWidget = (List)getComponent();
        for (int i = 0; i < list.length; i++){
            listWidget.add(list[i]);
        }
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IList#setSelection(int)
     */
    @Override
    public void setSelection(int index) {
        List listWidget = (List)getComponent();
        listWidget.setSelection(index);

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IList#getSelectionIndex()
     */
    @Override
    public int getSelectionIndex() {
        List listWidget = (List)getComponent();
        return listWidget.getSelectionIndex();
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IList#getSelection()
     */
    @Override
    public String getSelection() {
        List listWidget = (List)getComponent();
        String s[] = listWidget.getSelection();
        if (s.length > 0) return s[0];
        return null;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IList#addObserver(com.arc.widgets.IList.IObserver)
     */
    @Override
    public synchronized void addObserver(IObserver observer) {
        if (mObservers == null){
            mObservers = new ArrayList<IObserver>();
        }
        mObservers.add(observer);
        
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IList#removeObserver(com.arc.widgets.IList.IObserver)
     */
    @Override
    public void removeObserver(IObserver observer) {
        if (mObservers != null){
            mObservers.remove(observer);
        }
        
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IList#setSelections(int[])
     */
    @Override
    public void setSelections(int[] index) {
        List listWidget = (List)getComponent();
        listWidget.setSelection(index);
        
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IList#getSelectionIndecies()
     */
    @Override
    public int[] getSelectionIndices() {
        List listWidget = (List)getComponent();
        return listWidget.getSelectionIndices();
    }
    
    /**
     * Fire double-click event based on y position.
     * @param y
     */
    private void fireDoubleClick(int y){
        if (mObservers != null){
            IObserver[] observers;
            synchronized(mObservers){
                observers = mObservers.toArray(new IObserver[mObservers.size()]);               
            }
            List listWidget = (List)getComponent();
            int line = listWidget.getTopIndex() + 
                        y/listWidget.getItemHeight();
            listWidget.setSelection(line);
            for (IObserver o: observers){
                o.onDoubleClicked(this,line);
            }
        }

        
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    @Override
    public void widgetSelected(SelectionEvent e) {
        if (mObservers != null){
            IObserver[] observers;
            synchronized(mObservers){
                observers = mObservers.toArray(new IObserver[mObservers.size()]);               
            }
            for (IObserver o: observers){
                o.onSelected(this);
            }
        }
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
     */
    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
        
    }

}
