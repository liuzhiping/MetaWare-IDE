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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import com.arc.widgets.IImage;
import com.arc.widgets.IToolItem;

class DeferredButton extends DeferredComponent implements IToolItem,
        IToolBarItem {
    private static int sItem = 0;
    
    private IAction mAction;
    private List<IObserver> mObservers = new ArrayList<IObserver>();
    
    DeferredButton(int actionStyle){
        mAction = new Action("",actionStyle){
            @Override
            public void run(){
                notifyObservers();
            }
            
        };
        mAction.addPropertyChangeListener(new IPropertyChangeListener(){

			@Override
            public void propertyChange(PropertyChangeEvent event) {
				if (IAction.CHECKED.equals(event.getProperty()))
				    notifyObservers();
				
			}});
        mAction.setId("Button" + sItem++);
    }
    
    protected void notifyObservers(){
        for (IObserver o: mObservers){
            o.itemChanged(this);
        }
    }

    @Override
    public boolean isSelected() {
        return mAction.isChecked();
    }

    @Override
    public void setImage(IImage image) {
        if (image != null){
            final Image im = (Image)image.getObject();
            mAction.setImageDescriptor(ImageDescriptor.createFromImage(im));
        }
        else mAction.setImageDescriptor(null);

    }
    
    public void setImage(ImageDescriptor image){
        mAction.setImageDescriptor(image);
    }
    

    @Override
    public void setText(String text) {
        mAction.setText(text);

    }

    @Override
    public void addObserver(IObserver observer) {
        mObservers.add(observer);

    }

    @Override
    public void removeObserver(IObserver observer) {
        mObservers.remove(observer);
    }

    @Override
    public int getStyle() {
        return 0;
    }

    @Override
    public void setSelected(boolean v) {
        mAction.setChecked(v);

    }

    @Override
    public void addToToolBar(IToolBarManager manager) {
        manager.add(mAction);
    }

    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param tip
     */
    @Override
    public void setToolTipText(String tip) {
        super.setToolTipText(tip);
        mAction.setToolTipText(tip);
    }

   
    @Override
    public boolean isEnabled() {
        return mAction.isEnabled();
    }

    
    @Override
    public void setEnabled(boolean v) {
        mAction.setEnabled(v);
    }
    
    @Override
    public Composite getParentComposite(){
        return null; // don't need it; we don't resize buttons
    }

    @Override
    public void setName (String name) {
        super.setName(name);
        mAction.setId(name);
    }

}
