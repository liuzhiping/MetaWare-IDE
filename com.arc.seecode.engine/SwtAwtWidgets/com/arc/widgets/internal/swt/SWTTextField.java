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
import java.awt.event.TextListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import com.arc.widgets.IContainer;
import com.arc.widgets.ITextField;

/**
 * A builder for a SWT text field
 */
class SWTTextField extends Component implements ITextField {
    private SelectionActionListener mActionListener;

    private ModifyTextListener mTextListener;

    private String mText;
    
    private Text mTextField = null;

    private int mPreferredColumnCount = 0;

    private boolean mEditable = true;
    
    private String mName = null;
    
    private List<IObserver> mObservers = null;

    private boolean fFireActionWhenFocusLost = true;

    SWTTextField(IContainer parent, IComponentMapper mapper) {
        super(parent, mapper);
    }

    @Override
    protected Widget instantiate() {
        Composite parent = getParentComposite();
        Composite wrapper = null;
        if (mPreferredColumnCount > 0){
            // If a preferred column with is imposed, we need to wrap it
            // so that we can constraint its layout. There is not
            // "setPreferredSize()" method!
            wrapper = new Composite(parent,SWT.NONE){
                @Override
                public void setEnabled(boolean v){
                    super.setEnabled(v);
                    mTextField.setEnabled(v);
                }
            };
            parent = wrapper;
            wrapper.setLayout(new MyTextLayout());
        }
        mTextField = new Text(parent, SWT.SINGLE | SWT.BORDER);
        if (mText != null)
            mTextField.setText(mText);
        if (!mEditable)
            mTextField.setEditable(false);
        if (mName != null){
            mTextField.setData("name",mName);
        }
        if (wrapper == null)
            return mTextField;
        return wrapper;
        
    }

    @Override
    public void setText(String text) {
        if (text == null) text = "";
        mText = text;
        if (mTextField != null) {
            //Under linux, we unconditionally get a modify event if
            // we call setText, even if it didn't change. This can
            // cause havic, so check for it.
            if (!text.equals(mTextField.getText())){
                mTextField.setText(text);
            }
        }
    }

    @Override
    public String getText() {
        if (mTextField != null)
            return mTextField.getText();
        return mText;
    }

    /**
     * Add action listener when enter key is pressed, or focus is lost after
     * something is typed. For SWT implementation, we contrive the ActionEvent
     */
    @Override
    public void addActionListener(ActionListener listener) {
        if (mActionListener == null) {
            mActionListener = new SelectionActionListener(true,this);
            final String[] lastTextContent = new String[1];
            lastTextContent[0] = "";
            mActionListener.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed (ActionEvent e) {
                    lastTextContent[0] = mTextField.getText();
                    
                }});
            getComponent();
            mTextField.addSelectionListener(mActionListener);
            mTextField.addFocusListener(new FocusListener(){

                @Override
                public void focusGained (FocusEvent e) {                 
                }

                @Override
                public void focusLost (FocusEvent e) {
                    if (fFireActionWhenFocusLost && !lastTextContent[0].equals(mTextField.getText())){
                        mActionListener.widgetDefaultSelected(null);
                    }
                    
                }});
        }
        mActionListener.addActionListener(listener);
    }

    /**
     * Set approximate minimum size in terms of character positions.
     */
    @Override
    public void setColumns(int col) {
        mPreferredColumnCount = col;
        if (mTextField != null){
            getParentComposite().layout(true);
        }
    }

    /**
     * Add listener for each time a key is typed in this text field. For SWT
     * implementation, we contrive the TextEvent.
     */
    @Override
    public void addTextListener(TextListener listener) {
        if (mTextListener == null) {
            mTextListener = new ModifyTextListener();
            getComponent();
            mTextField.addModifyListener(mTextListener);
        }
        mTextListener.add(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.ITextField#setEditable(boolean)
     */
    @Override
    public void setEditable(boolean v) {
        mEditable = v;
        if (mTextField != null) {
            mTextField.setEditable(v);
        }

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.ITextField#setSelection(int, int)
     */
    @Override
    public void setSelection(int start, int end) {
        getComponent();
        mTextField.setSelection(start,end);
        
    }
    
    /* (non-Javadoc)
     * @see com.arc.widgets.IChoice#addObserver(com.arc.widgets.IChoice.IObserver)
     */
    @Override
    public synchronized void addObserver(IObserver o) {
        if (mObservers == null){
            mObservers = new ArrayList<IObserver>();
            getComponent();
            mTextField.addModifyListener(new ModifyListener(){

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
        for (int i = 0; i < observers.length; i++){
            observers[i].selectionChanged(this);
        }
    }
    
    @Override
    protected Font getActualFont () {
        if (mTextField != null)
            return mTextField.getFont();
        return null;
    }
    
    /**
     * A layout that wraps a text widget that is constrained to a particular
     * width.
     * @author davidp
     * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
     * @version $Revision$
     * @lastModified $Date$
     * @lastModifiedBy $Author$
     * @reviewed 0 $Revision:1$
     */
    class MyTextLayout extends Layout {
       
        MyTextLayout(){
        }
        @Override
        protected Point computeSize (Composite composite, int wHint, int hHint, boolean flushCache) {
            for (Control kid: composite.getChildren()){
                Point size = kid.computeSize(wHint,hHint,flushCache);
                GC gc = new GC(kid);
                String text = "WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW";
                int l = Math.min(mPreferredColumnCount,text.length());
                size.x = gc.stringExtent(text.substring(0,l)).x;
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
    public String getName () {
        return mName;
    }

    @Override
    public void setName (String name) {
        if (mTextField != null){
            mTextField.setData("name",name);
        }
        mName = name;
    }

    @Override
    public void setFireActionWhenFocusLost (boolean v) {
        fFireActionWhenFocusLost  = v;      
    }
}
