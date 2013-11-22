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
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import com.arc.widgets.IChoice;
import com.arc.widgets.IContainer;

class Choice extends SwingComponent implements IChoice {
    private List<String> mItems = new ArrayList<String>();
    private boolean mEditable;
    private List<IObserver> mObservers = null;
    private List<ITextObserver> mTextObservers = null;

    Choice(IContainer parent, boolean writable, IComponentMapper mapper) {
        super(parent,mapper);
        mEditable = writable;
    }

    public void setEditable(boolean v) {
        mEditable = v;
        if (mComponent != null)
             ((JComboBox) mComponent).setEditable(v);
    }

    @Override
    protected Component instantiate() {
        JComboBox b = new JComboBox();
        if (mItems != null) {
            for (int i = 0; i < mItems.size(); i++)
                b.addItem(mItems.get(i));
            mItems = null;
        }
        b.setEditable(mEditable);
        b.addItemListener(new ItemListener(){

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getID() == ItemEvent.SELECTED){
                    notifyObservers();
                }
                
            }});
        return b;
    }

    @Override
    public void clear() {
        if (mComponent != null)
             ((JComboBox) mComponent).removeAllItems();
        else
            mItems.clear();
    }
    
    @Override
    public void setItems(String items[]) {
        if (mComponent != null){
            ((JComboBox)mComponent).removeAllItems();
            for (String s: items)
                ((JComboBox)mComponent).addItem(s);
        }
        else {
            mItems.clear();
            mItems.addAll(Arrays.asList(items));
        }
    }

    @Override
    public int addItem(String text) {
        if (mComponent != null) {
            ((JComboBox) mComponent).addItem(text);
            return ((JComboBox) mComponent).getComponentCount() - 1;
        }
        else {
            mItems.add(text);
            return mItems.size() - 1;
        }
    }
    @Override
    public String getText() {
        return (String) ((JComboBox) getComponent()).getSelectedItem();
    }
    @Override
    public int getSelectionIndex() {
        return ((JComboBox) getComponent()).getSelectedIndex();
    }
    @Override
    public void setSelection(String text){
        ((JComboBox)getComponent()).setSelectedItem(text);
    }
    @Override
    public void addActionListener(ActionListener listener) {
        ((JComboBox) getComponent()).addActionListener(listener);
    }
    /**
     * @see com.arc.widgets.IChoice#setSelectionIndex(int)
     */
    @Override
    public void setSelectionIndex(int i) {
        ((JComboBox)getComponent()).setSelectedIndex(i);
    }

    /**
     * @see com.arc.widgets.IChoice#getItemAt(int)
     */
    @Override
    public Object getItemAt(int i) {
        return ((JComboBox)getComponent()).getItemAt(i);
    }

    /**
     * @see com.arc.widgets.IChoice#getItemCount()
     */
    @Override
    public int getItemCount() {
        return ((JComboBox)getComponent()).getItemCount();
    }

    /**
     * @see com.arc.widgets.IChoice#removeActionListener(ActionListener)
     */
    @Override
    public void removeActionListener(ActionListener listener) {
        ((JComboBox) getComponent()).removeActionListener(listener);
    }

    /**
     * @see com.arc.widgets.IChoice#removeItemAt(int)
     */
    @Override
    public void removeItemAt(int i) {
        ((JComboBox)getComponent()).removeItemAt(i);
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IChoice#setColumns(int)
     */
    @Override
    public void setColumns(int col) {
        JComboBox c = (JComboBox)getComponent();
        Component t = c.getEditor().getEditorComponent();
        if (t instanceof JTextField){
            ((JTextField)t).setColumns(col);
        }
        
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IChoice#removeAllItems()
     */
    @Override
    public void removeAllItems() {
        if (mComponent != null) {
            ((JComboBox) mComponent).removeAllItems();
        }
        else {
            mItems.clear();
        }
        
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IChoice#addObserver(com.arc.widgets.IChoice.IObserver)
     */
    @Override
    public synchronized void addObserver(IObserver o) {
        if (mObservers == null){
            mObservers = new ArrayList<IObserver>();
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
    
    private void notifyTextObservers(){
        ITextObserver[] observers;
        synchronized(this){
            if (mTextObservers == null || mTextObservers.size() == 0)
                return;
            observers =  mTextObservers.toArray(new ITextObserver[mTextObservers.size()]);
        }
        String text = ((JTextComponent)((JComboBox)getComponent()).getEditor().getEditorComponent()).getText();
        for (int i = 0; i < observers.length; i++){
            observers[i].textChanged(this,text);
        }
    }

    @Override
    public void addTextObserver(ITextObserver observer) {
        if (mTextObservers == null){
            mTextObservers = new ArrayList<ITextObserver>();
            JComboBox combo = (JComboBox)getComponent();
            combo.addKeyListener(new KeyAdapter(){
                @Override
                public void keyTyped(KeyEvent event){
                    notifyTextObservers();
                }
                
            });
        }
        mTextObservers.add(observer);      
    }

    @Override
    public void removeTextObserver(ITextObserver observer) {
        if (mTextObservers != null){
            mTextObservers.remove(observer);
        }      
    }

    @Override
    public boolean isEditable() {
        return mEditable;
    }

}
