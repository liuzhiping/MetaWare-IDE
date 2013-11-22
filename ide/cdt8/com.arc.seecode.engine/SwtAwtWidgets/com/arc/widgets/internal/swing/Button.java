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
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JButton;

import com.arc.widgets.IButton;
import com.arc.widgets.IContainer;
import com.arc.widgets.IImage;

class Button extends SwingComponent implements IButton {

    Button(IContainer parent, IComponentMapper mapper) {
        super(parent,mapper);
    }

    @Override
    public int getButtonKind() {
        return PUSH;
    }

    @Override
    public void setText(String s) {
        mText = s;
        doText();
    }
    @Override
    public void setImage(IImage image) {
        mImage = image;
        doImage();
    }

    @Override
    public void setMnemonic(char c) {
        mMnemonic = c;
        if (mComponent != null)
             ((AbstractButton) mComponent).setMnemonic(c);
    }

    @Override
    public String getText() {
        return mComponent != null
            ? ((AbstractButton) mComponent).getText()
            : mText;
    }
    @Override
    public IImage getImage() {
        return mImage;
    }

    @Override
    public Object getComponent() {
        if (mComponent == null) {
            AbstractButton b = (AbstractButton)super.getComponent();
            b.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    notifyObservers();
                    
                }});
            doText();
            doSelected();
            doImage();
            
            if (mMnemonic != 0)
                 ((AbstractButton) mComponent).setMnemonic(mMnemonic);
        }
        return super.getComponent();
    }
    @Override
    protected Component instantiate() {
        return new JButton();
    }

    @Override
    public void addActionListener(ActionListener listener) {
        ((AbstractButton) getComponent()).addActionListener(listener);
    }
    @Override
    public void removeActionListener(ActionListener listener) {
        ((AbstractButton) getComponent()).removeActionListener(listener);
    }
    public void addItemListener(ItemListener listener) {
        ((AbstractButton) getComponent()).addItemListener(listener);
    }

    @Override
    public void setSelected(boolean v) {
        mSelected = v;
        doSelected();
    }

    @Override
    public boolean isSelected() {
        if (mComponent == null)
            return mSelected;
        return ((AbstractButton) getComponent()).isSelected();
    }

    public void doSelected() {
        if (mComponent != null)
             ((AbstractButton) mComponent).setSelected(mSelected);
    }
    public void doText() {
        if (mComponent != null)
             ((AbstractButton) mComponent).setText(mText);
    }
    public void doImage() {
        if (mComponent != null && mImage != null)
             ((AbstractButton) mComponent).setIcon((Icon)mImage.getObject());
    }

    private boolean mSelected;
    private String mText;
    private IImage mImage;
    private char mMnemonic;
    private List<IObserver> mObservers = null;
    /* (non-Javadoc)
     * @see com.arc.widgets.IToolItem#getSelection()
     */

    /* (non-Javadoc)
     * @see com.arc.widgets.IToolItem#addObserver(com.arc.widgets.IToolItem.IObserver)
     */
    @Override
    public synchronized void addObserver(IObserver observer) {
       if (mObservers == null){
           mObservers = new ArrayList<IObserver>();
       }
       mObservers.add(observer);
        
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IToolItem#removeObserver(com.arc.widgets.IToolItem.IObserver)
     */
    @Override
    public synchronized void removeObserver(IObserver observer) {
        if (mObservers != null){
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
       return getButtonKind();
    }

}
