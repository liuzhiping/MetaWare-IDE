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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Widget;

import com.arc.widgets.IChoice;
import com.arc.widgets.IContainer;
import com.arc.widgets.IFont;

/**
 * A builder for an SWT choice box
 */

class SWTChoice extends Component implements IChoice {
    private boolean mEditable;

    private int mPreferredColumnCount = 0;
    
    private List<IObserver> mObservers = null;
    
    private List<ITextObserver> mTextObservers = null;
    
    private String mLastText;

    private SelectionActionListener mActionListener;

    private Combo mCombo;

    private String mName = null;
    
    private IFont mFont;
    
    private boolean mTextTyped = false; // true when text typed since last selection event

    SWTChoice(IContainer parent, boolean editable, IComponentMapper mapper) {
        super(parent, mapper);
        mEditable = editable;
    }

    @Override
    protected Widget instantiate() {
        Composite parent = getParentComposite();
        Widget result = null;
        if (mPreferredColumnCount > 0){
            // If a preferred column with is imposed, we need to wrap it
            // so that we can constraint its layout. There is not
            // "setPreferredSize()" method!
            Composite wrapper = new Composite(parent,0){
                @Override
                public void setEnabled(boolean v){
                    super.setEnabled(v);
                    mCombo.setEnabled(v);
                }
            };
            wrapper.setLayout(new MyComboLayout());
            parent = wrapper;
            result = wrapper;            
        }
        mCombo = new Combo(parent, SWT.DROP_DOWN
                        | (mEditable ? 0 : SWT.READ_ONLY));
        if (mFont != null){
            mCombo.setFont((Font)mFont.getObject());
        }
        if (mEditable)
            mCombo.addKeyListener(new KeyListener(){

                @Override
                public void keyPressed(KeyEvent e) {               
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    mTextTyped = true;                   
                }});
            mCombo.addFocusListener(new FocusListener() {
                private Button saveDefaultButton;

                @Override
                public void focusGained(FocusEvent event) {
                    // We want to process return key; don't dismiss shell!
                    saveDefaultButton = mCombo.getShell().getDefaultButton();
                    mCombo.getShell().setDefaultButton(null);
                    mTextTyped = false;
                }

                @Override
                public void focusLost(FocusEvent event) {
                    mCombo.getShell().setDefaultButton(saveDefaultButton);
                    String text = mCombo.getText();
                    if (mTextTyped && text.length() > 0
                            && (mCombo.getSelectionIndex() < 0 || !mCombo.getItem(
                                    mCombo.getSelectionIndex()).equals(text))) {
                        mTextTyped = false;
                        mCombo.add(text);
                        mCombo.select(mCombo.getItemCount() - 1);
                        Event e = new Event();
                        e.widget = event.widget;
                        e.display = event.display;
                        // Prevent duplicate event if "<enter>" key already fired it.
                        if (!text.equals(mLastText)){
                            notifyObservers();
                            if (mActionListener != null)
                                mActionListener
                                .widgetDefaultSelected(new SelectionEvent(e));
                        }
                    }
                }
            });
        if (mName != null) mCombo.setData("name",mName);
       
        if (result == null) result = mCombo;
        return result;
    }

    @Override
    public void setSelection(String s) {
        getComponent();
        Combo c = mCombo;
        int oldSelectIndex = c.getSelectionIndex();
        for (int i = 0; i < c.getItemCount(); i++)
            if (s.equals(c.getItem(i))) {
                c.select(i);
                if (i != oldSelectIndex)
                    notifyObservers();
                return;
            }
        addItem(s);
    }

    @Override
    public void clear() {
        getComponent();
        mCombo.removeAll();
    }
    
    @Override
    public void setItems(String items[]){
        getComponent();
        mCombo.setItems(items);
        if (items.length>0) mCombo.select(0);
        notifyObservers();
    }

    @Override
    public int addItem(String text) {
        getComponent();
        Combo c = mCombo;
        c.add(text);
        int i = c.getItemCount() - 1;
        c.select(i);
        notifyObservers();
        return i;
    }

    @Override
    public String getText() {
        getComponent();
        return mCombo.getText();
    }

    /**
     * Return selected item index, or -1.
     */
    @Override
    public int getSelectionIndex() {
        getComponent();
        return mCombo.getSelectionIndex();
    }

    /**
     * Add action listener when something happens. For SWT implementation, we
     * contrive the ActionEvent
     */
    @Override
    public void addActionListener(ActionListener listener) {
        if (mActionListener == null) {
            mActionListener = new SelectionActionListener(false,this);
            getComponent();
            mCombo.addSelectionListener(mActionListener);
            //Fire event when combobox changes after focus is removed.
            mActionListener.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    mLastText = getText();                   
                }});

        }
        mActionListener.addActionListener(listener);
    }

    /**
     * @see com.arc.widgets.IChoice#setSelectionIndex(int)
     */
    @Override
    public void setSelectionIndex(int i) {
        getComponent();
        mCombo.select(i);
    }

    /**
     * @see com.arc.widgets.IChoice#getItemAt(int)
     */
    @Override
    public Object getItemAt(int i) {
        getComponent();
        return mCombo.getItem(i);
    }

    /**
     * @see com.arc.widgets.IChoice#getItemCount()
     */
    @Override
    public int getItemCount() {
        getComponent();
        return mCombo.getItemCount();
    }

    /**
     * @see com.arc.widgets.IChoice#removeActionListener(ActionListener)
     */
    @Override
    public void removeActionListener(ActionListener listener) {
        if (mActionListener != null)
            mActionListener.removeActionListener(listener);
    }

    /**
     * @see com.arc.widgets.IChoice#removeItemAt(int)
     */
    @Override
    public void removeItemAt(int i) {
        getComponent();
        Combo c = mCombo;
        c.remove(i);
        if (i > 0)
            i--;
        if (i < getItemCount())
            c.select(i);
        else
            c.setText("");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IChoice#setColumns(int)
     */
    @Override
    public void setColumns(int col) {
        mPreferredColumnCount = col;
//        GC gc = new GC(mCombo);
//        FontMetrics f = gc.getFontMetrics();
//        mPreferredWidth = f.getAverageCharWidth() * col;
//        gc.dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IChoice#removeAllItems()
     */
    @Override
    public void removeAllItems() {
        if (mCombo != null){
            mCombo.removeAll();
        }
    }
    
    /* (non-Javadoc)
     * @see com.arc.widgets.IChoice#addObserver(com.arc.widgets.IChoice.IObserver)
     */
    @Override
    public synchronized void addObserver(IObserver o) {
        if (mObservers == null){
            mObservers = new ArrayList<IObserver>();
            getComponent();
            mCombo.addSelectionListener(new SelectionListener(){

                @Override
                public void widgetSelected (SelectionEvent e) {
                    mTextTyped = false;
                    notifyObservers();                  
                }

                @Override
                public void widgetDefaultSelected (SelectionEvent e) {
                    mTextTyped = false;
                    notifyObservers();                    
                }});
            mCombo.addModifyListener(new ModifyListener(){

                @Override
                public void modifyText(ModifyEvent e) {
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
        mLastText = getText();
        for (int i = 0; i < observers.length; i++){
            observers[i].selectionChanged(this);
        }
    }

    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param font
     */
    @Override
    public void setFont (IFont font) {
        super.setFont(font);
        if (mCombo != null){
            mCombo.setFont((Font)font.getObject());
        }
        else mFont = font;
    }
    
    /**
     * A layout that wraps a combobox that is constrained to a particular
     * width.
     * @author davidp
     * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
     * @version $Revision$
     * @lastModified $Date$
     * @lastModifiedBy $Author$
     * @reviewed 0 $Revision:1$
     */
    class MyComboLayout extends Layout {
       
        MyComboLayout(){
        }
        @Override
        protected Point computeSize (Composite composite, int wHint, int hHint, boolean flushCache) {
            for (Control kid: composite.getChildren()){
                Point size = kid.computeSize(wHint,hHint,flushCache);
                GC gc = new GC(kid);
                // We assume the downarrow on the right is a square: its width is the same
                // as its height. Also assume a left border of 2 pixels.
                // Assume "s" is an average width character.
                size.x = mPreferredColumnCount*gc.getCharWidth('s') + size.y + 2;
                gc.dispose();
                return size;
            }     
            return new Point(0,0); // shouldn't get here
        }

        @Override
        protected void layout (Composite composite, boolean flushCache) {
            Point size = composite.getSize();
            for (Control kid: composite.getChildren()){
                kid.setBounds(0,0,size.x,size.y);
            }          
        }       
    }

    @Override
    public void addTextObserver(ITextObserver observer) {
        if (mTextObservers == null){
            mTextObservers = new ArrayList<ITextObserver>();
            getComponent();
            mCombo.addModifyListener(new ModifyListener(){

                @Override
                public void modifyText(ModifyEvent e) {
                    notifyTextObservers();
                    
                }});
        }
        mTextObservers.add(observer);
    }
    
    private void notifyTextObservers(){
        String text = mCombo.getText();
        for (ITextObserver o: mTextObservers){
            o.textChanged(this,text);
        }
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

    @Override
    public String getName () {
        return mName;
    }

    @Override
    public void setName (String name) {
        mName = name;
        if (mCombo != null) mCombo.setData("name",name);
    }
}
