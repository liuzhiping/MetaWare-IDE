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
package com.arc.cdt.debug.seecode.internal.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.arc.seecode.display.IMenuGenerator;
import com.arc.seecode.display.MenuDescriptor;

/**
 * This creates a menu in the menubar that corresponds
 * to a menu that the SeeCode engine has dynamically created.
 * @author David Pickens
 */
class DynamicEngineMenu extends Action implements IMenuCreator, IMenuGenerator {


    private MenuDescriptor mMenuDesc;
    private Menu mMenu;

    /**
     * 
     */
    public DynamicEngineMenu(String name, MenuDescriptor menu) {
        super(name,IAction.AS_DROP_DOWN_MENU);
        mMenuDesc = menu;
        setId("seecode." + name);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IAction#getMenuCreator()
     */
    @Override
    public IMenuCreator getMenuCreator() {
        return this;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IAction#setMenuCreator(org.eclipse.jface.action.IMenuCreator)
     */
    @Override
    public void setMenuCreator(IMenuCreator creator) {
        throw new IllegalArgumentException("Can't add creator");
    }

    /**
     * Called to dispose of the menu.
     *
     */
    @Override
    public void dispose() {
        if (mMenu != null){
            mMenu.dispose();
            mMenu = null;
        }
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
     */
    @Override
    public Menu getMenu(Control parent) {
        throw new IllegalArgumentException("Shouldn't get here!");
    }

    /**
     * Called to return the menu that is in the
     * given parent.
     * @param parent
     * @return generated menu
     */
    @Override
    public Menu getMenu(Menu parent) {
         // NOTE: parent is the main menu bar.
        if (mMenu != null && mMenu.getParentMenu() == parent){
            return mMenu;
        }
        if (mMenu != null){
            // shouldn't get here
            mMenu.dispose();
        }
        mMenu = new Menu(parent);
        mMenuDesc.generate(mMenu,this);
        return mMenu;
        
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.IMenuGenerator#makeMenuItem(java.lang.Object, java.lang.String, java.lang.String, com.arc.seecode.display.IMenuGenerator.IItemObserver)
     */
    @Override
    public void makeMenuItem(Object parent, final String name, String label, final IItemObserver observer, boolean enabled) {
        if (parent == null || label == null || observer == null)
            throw new IllegalArgumentException("arg is null");
        MenuItem item = new MenuItem((Menu)parent,SWT.PUSH);
        item.setText(label);
        item.addSelectionListener(new SelectionListener(){

            @Override
            public void widgetSelected(SelectionEvent e) {
                observer.menuItemSelected(name);
                
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
                
            }});
        item.setEnabled(enabled);
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.IMenuGenerator#makeCheckBoxItem(java.lang.Object, java.lang.String, java.lang.String, com.arc.seecode.display.IMenuGenerator.ICheckBoxObserver, boolean)
     */
    @Override
    public void makeCheckBoxItem(Object parent, final String name, String label, final ICheckBoxObserver observer, boolean enabled, boolean initValue) {
        if (parent == null || label == null || observer == null)
            throw new IllegalArgumentException("arg is null");
        final MenuItem item = new MenuItem((Menu)parent,SWT.CHECK);
        item.setText(label);
        item.setEnabled(enabled);
        item.addSelectionListener(new SelectionListener(){

            @Override
            public void widgetSelected(SelectionEvent e) {
                observer.selectionChanged(name,item.getSelection());
                
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
                
            }});
        
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.IMenuGenerator#makeRadioItem(java.lang.Object, java.lang.String, java.lang.String, com.arc.seecode.display.IMenuGenerator.ICheckBoxObserver, boolean, java.lang.String)
     */
    @Override
    public void makeRadioItem(Object parent, final String name, String label, final ICheckBoxObserver observer, boolean enabled, boolean initValue, String groupName) {
        if (parent == null || label == null || observer == null)
            throw new IllegalArgumentException("arg is null");
        final MenuItem item = new MenuItem((Menu)parent,SWT.RADIO);
        item.setText(label);
        item.setEnabled(enabled);
        item.addSelectionListener(new SelectionListener(){

            @Override
            public void widgetSelected(SelectionEvent e) {
                observer.selectionChanged(name,item.getSelection());
                
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
                
            }});
        //TODO: unselect others in same group
        
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.IMenuGenerator#makeSubmenu(java.lang.Object, java.lang.String, java.lang.String)
     */
    @Override
    public Object makeSubmenu(Object parent, String name, String label) {
        MenuItem item = new MenuItem((Menu)parent,SWT.CASCADE);
        Menu subMenu = new Menu(item);
        item.setMenu(subMenu);
        item.setText(label);
        return subMenu;
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.IMenuGenerator#makeSeparator(java.lang.Object)
     */
    @SuppressWarnings("unused")
    @Override
    public void makeSeparator(Object parent) {
        new MenuItem((Menu)parent,SWT.SEPARATOR);     
    }
}
