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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;

import com.arc.widgets.IContainer;
import com.arc.widgets.ITextField;

class TextField extends SwingComponent implements ITextField {

    private String mText;

    private int mCol;
    
    private List<IObserver>mObservers = null;

    TextField(IContainer parent, String text, IComponentMapper mapper) {
        super(parent, mapper);
        mText = text;
    }

    TextField(IContainer parent, IComponentMapper mapper) {
        this(parent, "", mapper);
    }

    @Override
    public String getText() {
        if (mComponent != null)
            return ((JTextField) mComponent).getText();
        return mText;
    }

    @Override
    public void setText(String s) {
        String old = mText;
        mText = s;
        if (mComponent != null) {
            ((JTextField) mComponent).setText(s);
            if (!old.equals(s))
                notifyObservers();
        }
    }

    @Override
    protected Component instantiate() {
        JTextField tf = new JTextField(mText);
        if (mCol != 0)
            tf.setColumns(mCol);
        return tf;
    }

    @Override
    public void addActionListener(ActionListener listener) {
        ((JTextField) getComponent()).addActionListener(listener);
    }

    @Override
    public void setColumns(int col) {
        mCol = col;
        if (mComponent != null)
            ((JTextField) mComponent).setColumns(col);
    }

    @Override
    public void addTextListener(final TextListener listener) {
        ((JTextField) getComponent()).addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent event) {
                TextEvent te = new TextEvent(event.getSource(),
                        TextEvent.TEXT_VALUE_CHANGED);
                listener.textValueChanged(te);
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.ITextField#setEditable(boolean)
     */
    @Override
    public void setEditable(boolean v) {
        ((JTextField) getComponent()).setEditable(v);
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.ITextField#setSelection(int, int)
     */
    @Override
    public void setSelection(int start, int end) {
        ((JTextField) getComponent()).select(start,end);
        
    }
    
    /* (non-Javadoc)
     * @see com.arc.widgets.IChoice#addObserver(com.arc.widgets.IChoice.IObserver)
     */
    @Override
    public synchronized void addObserver(IObserver o) {
        if (mObservers == null){
            mObservers = new ArrayList<IObserver>();
            addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    notifyObservers();
                    
                }});
        }
        mObservers.add(o);
        
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IChoice#removeObserver(com.arc.widgets.IChoice.IObserver)
     */
    @Override
    public synchronized void removeObserver(IObserver o) {
       if (mObservers != null){
           mObservers.remove(o);
       }       
    }
    
    private void notifyObservers(){
        IObserver[] observers;
        synchronized(this){
            if (mObservers == null || mObservers.size() == 0)
                return;
            observers =  mObservers.toArray(new IObserver[mObservers.size()]);
        }
        for (int i = 0; i < observers.length; i++){
            observers[i].selectionChanged(this);
        }
    }

    @Override
    public void setFireActionWhenFocusLost (boolean v) {
       // What to do?
    }
}
