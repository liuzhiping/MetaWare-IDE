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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

import com.arc.cdt.debug.seecode.ui.UISeeCodePlugin;
import com.arc.widgets.IChoice;
import com.arc.widgets.ILabel;

/**
 * Create a combo that can be instantiated later when the view toolbar is being
 * rendered.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
class DeferredCombo extends DeferredComponent implements IChoice, IToolBarItem {
    private List<IObserver>mObservers = new ArrayList<IObserver>();
    private List<ITextObserver>mTextObservers = null;
    private List<String>mItems = new ArrayList<String>();
    private int mSelectionIndex = -1;
    private ArrayList<ActionListener> mActionListeners;
    private int mColumns = 0;
    private boolean mEditable;
    private static int sItem = 0;
    private String mID;
    
    private CCombo mCombo = null; // set when added to View's toolbar
    private ILabel mLabel;
    private boolean mContentsChanged = false;
    private IContributionItem mItem = null;
    private IToolBarManager mToolBarManager;
    // To avoid recursive selection event when "setSelection()" is called from
    // selection event handler.
    private boolean _suppressSelectionEvent = false;

    /**
     * 
     * @param label label that prefixes this combo, or <code>null</code>; used to
     * enable/disable.
     * @param editable
     */
    DeferredCombo(ILabel label, boolean editable){
        mLabel = label;
        mEditable = editable;
        mID = "combo" + sItem++;
        if (editable) mColumns = 10;
    }
    @Override
    public void addObserver(IObserver o) {
        mObservers.add(o);
    }

    @Override
    public void removeObserver(IObserver o) {
       mObservers.remove(o);

    }
    
    @Override
    public void addTextObserver(ITextObserver o) {
        if (mTextObservers == null){
            mTextObservers = new ArrayList<ITextObserver>();
        }
        mTextObservers.add(o);
    }

    @Override
    public void removeTextObserver(ITextObserver o) {
        if (mTextObservers != null)
           mTextObservers.remove(o);
    }
    
    @Override
    public void setItems(String[] items){
        mItems.clear();
        mItems.addAll(Arrays.asList(items));
        if (mCombo != null){
            mCombo.setItems(items);
            if (items.length > 0) setSelectionIndex(0);
            mContentsChanged = true;
        }
    }


    @Override
    public int addItem(String text) {
        mItems.add(text);
        if (mCombo != null){
            mCombo.add(text);
            mCombo.getParent().layout(true);
            setSelectionIndex(mItems.size()-1);
            mContentsChanged = true; // make contribution item dynamic
        }
        return mItems.size()-1;
    }

    @Override
    public String getText() {
        if (mCombo != null){
            return mCombo.getText();
        }
        return mSelectionIndex >= 0 && mSelectionIndex < mItems.size()?mItems.get(mSelectionIndex):null;
    }

    @Override
    public void clear() {
        if (mCombo != null){
            mCombo.removeAll();
        }
        mItems.clear();

    }

    @Override
    public int getSelectionIndex() {
        if (mCombo != null){
            return mCombo.getSelectionIndex();
        }
        return mSelectionIndex;
    }

    @Override
    public void setSelection(String text) {
        
        if (mCombo != null){
            String items[] = mCombo.getItems();
            boolean selected = false;
            for (int i = 0; i < items.length; i++){
                if (items[i].equals(text)){
                    setSelectionIndex(i);
                    selected = true;
                    break;
                }
            }
            if (!selected){
                addItem(text);
            }
        }
        else {
            int prev = mSelectionIndex;
            mSelectionIndex = mItems.indexOf(text);
            if (mSelectionIndex < 0){
                mItems.add(text);
                mSelectionIndex = mItems.size()-1;
            }
            if (prev != mSelectionIndex) notifyObservers();
        }

    }

    @Override
    public int getItemCount() {
        if (mCombo != null){
            return mCombo.getItemCount();
        }
        return mItems.size();
    }

    @Override
    public void removeAllItems() {
        if (mCombo != null){
            mCombo.removeAll();
        }
        mItems.clear();

    }

    @Override
    public Object getItemAt(int i) {
        if (mCombo != null){
            return mCombo.getItem(i);
        }
        return mItems.get(i);
    }

    @Override
    public void removeItemAt(int i) {
        if (mCombo != null){
            mCombo.remove(i);
        }
       mItems.remove(i);
    }

    @Override
    public void setSelectionIndex(int i) {
        if (getSelectionIndex() != i){
            mSelectionIndex = i;
            if (mCombo != null){
                mCombo.select(i);
            }
            notifyObservers();
        }
    }

    @Override
    public void addActionListener(ActionListener listener) {
        if (mActionListeners == null){
            mActionListeners = new ArrayList<ActionListener>();
        }
        mActionListeners.add(listener);

    }

    @Override
    public void removeActionListener(ActionListener listener) {
        if (mActionListeners != null){
            mActionListeners.remove(listener);
        }

    }
    
    @Override
    public void setEnabled(boolean v){
        super.setEnabled(v);
        if (mCombo != null){
            mCombo.setEnabled(v);
        }
        if (mLabel != null){
            mLabel.setEnabled(v);
        }
    }

    @Override
    public void setColumns(int col) {
        mColumns = col;
    }
    
    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param tip
     */
    @Override
    public void setToolTipText(String tip) {
        super.setToolTipText(tip);
        if (mCombo != null){
            mCombo.setToolTipText(tip);
        }
    }
    
    private void notifyObservers(){
        if (mObservers != null && !_suppressSelectionEvent){
            for (IObserver o: mObservers){
                o.selectionChanged(this);
            }
        }
    }
    
    private void notifyTextObservers(){
        if (mTextObservers != null){
            String text = mCombo.getText();
            for (ITextObserver o: mTextObservers){
                o.textChanged(this,text);
            }
        }
    }
    
    @Override
    public void revalidate(){      
        if (mCombo != null){
            
            // Bug in Windows version as of Eclipse 3.1M5: the toolbar
            // isn't redrawn!
            // We must remove all items and restore them to fix this!
            if (isWindows()){
                IContributionItem items[] = mToolBarManager.getItems();
                mToolBarManager.removeAll();
                mToolBarManager.update(true);
                for (IContributionItem item: items){
                    mToolBarManager.add(item);
                }           
            }
            mToolBarManager.update(true);
        }
    }
    
    static boolean isWindows(){
        return Platform.getOS().equals("win32");
    }
    
    @Override
    public void addToToolBar(IToolBarManager manager) {
        mContentsChanged = false;
        mToolBarManager = manager;
        mItem = new MyContributionItem(getName() != null?getName():mID);
        manager.add(mItem);
    }
    
    class MyContributionItem extends ControlContribution {



        protected MyContributionItem(String id) {
            super(id);
        }

        @Override
        protected Control createControl(Composite parent) {
            //NOTE: we must use "CCombo" instead of "Combo" because
            // the latter doesn't render correctly under Windows
            // when the toolbar wraps.
            //NOTE2: we must wrap the control in a composite so that we
            // can control the layout. Otherwise, Combos end up very narrow if
            // they are empty.
            Composite comboContainer = new Composite(parent,SWT.NONE);
            comboContainer.setLayout(new MyComboLayout());
            final CCombo c = new CCombo(comboContainer,SWT.DROP_DOWN | SWT.BORDER | (mEditable?0:SWT.READ_ONLY));
            DeferredCombo.this.mCombo = c;
            if (getName() != null){
                c.setData("name",getName()); // for GUI tester.
            }
            //Use fixed-width font it combobox is editable because it
            // typically corresponds to program text.
            // Use the SeeCode display font. Also, listener for a font change
            // and respond accordingly.
            if (mEditable){
                c.setFont(UISeeCodePlugin.getSeeCodeFont());
                IPropertyChangeListener fontListener = new IPropertyChangeListener(){

                    @Override
                    public void propertyChange(PropertyChangeEvent event) {
                        if (event.getProperty().equals(UISeeCodePlugin.SEECODE_FONT)){
                            c.setFont(UISeeCodePlugin.getSeeCodeFont());
                        }
                        
                    }
                    
                };
                JFaceResources.getFontRegistry().addListener( fontListener );
            }

            c.addDisposeListener(new DisposeListener(){

                @Override
                public void widgetDisposed(DisposeEvent e) {
                    DeferredCombo.this.mCombo = null;
                    
                }});
            c.setEnabled(isEnabled());

            for (String item: mItems){
                c.add(item);
            }
            if (mSelectionIndex < 0 && !mEditable)
                mSelectionIndex = 0;
            if (mSelectionIndex >= 0){
                c.select(mSelectionIndex);
            }
            if (getToolTipText() != null){
                c.setToolTipText(getToolTipText());
            }
            
            c.addSelectionListener(new SelectionListener(){

                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (!_suppressSelectionEvent) {
                        mSelectionIndex = c.getSelectionIndex();
                        notifyObservers(); 
                    }
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    // "setSelection()" causes a recursive selection event,
                    // so set flag to prevent it.
                    if (!_suppressSelectionEvent) {                       
                        try {
                            widgetSelected(e);
                            _suppressSelectionEvent = true;
                            setSelection(mCombo.getText());
                            if (mActionListeners != null) {
                                ActionEvent ae = new ActionEvent(c,
                                        ActionEvent.ACTION_PERFORMED, "");
                                for (ActionListener a : mActionListeners) {
                                    a.actionPerformed(ae);
                                }
                            }
                        } finally {
                            _suppressSelectionEvent = false;
                        }
                    }

                }});
              
            c.addModifyListener(new ModifyListener(){

                @Override
                public void modifyText(ModifyEvent e) {
                    notifyTextObservers();       
                }});
            //When writable combo loses focus, the whatever is typed is added
            //to the item list.
            if (mEditable) 
                c.addFocusListener(new FocusListener() {
                    private Button saveDefaultButton;

                    @Override
                    public void focusGained(FocusEvent event) {
                        // We want to process return key; don't dismiss shell!
                        saveDefaultButton = mCombo.getShell().getDefaultButton();
                        mCombo.getShell().setDefaultButton(null);
                    }

                    @Override
                    public void focusLost(FocusEvent event) {
                        mCombo.getShell().setDefaultButton(saveDefaultButton);
                        // Don't fire selection event when combo goes out of focus.
                        // We expect "<enter>" key to do that.
                        // in Eclipse 4.2, we get two selection events if we do this (STAR 9000613472)
                        //setSelection(mCombo.getText());
                    }
                });
            return comboContainer;
        }
        
        @Override
        public boolean isDynamic(){
            return mContentsChanged;
        }
        
        @Override
        public void dispose(){
            if (mCombo != null){
                mCombo.dispose();
            }
        }
            
        @Override
        protected int computeWidth(Control control) {
            return control.computeSize(SWT.DEFAULT,SWT.DEFAULT,true).x;
        }
    }

    @Override
    protected Composite getParentComposite() {
        if (mCombo != null){
            return mCombo.getParent();
        }
        return null;
    }
    @Override
    public boolean isEditable() {
        return mEditable;
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
                if (mColumns > 0){
                    GC gc = new GC(kid);
                    FontMetrics fm = gc.getFontMetrics();
                    // We assume the downarrow on the right is a square: its width is the same
                    // as its height. Also assume a left border of 2 pixels.
                    // Often the "average" char width seems too conservatibe. Add 1.
                    size.x = (mColumns+1)*fm.getAverageCharWidth() + size.y + 2;
                    gc.dispose();
                }
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
}
