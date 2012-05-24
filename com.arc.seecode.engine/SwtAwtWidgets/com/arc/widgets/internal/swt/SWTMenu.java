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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;

import com.arc.widgets.IMenu;
import com.arc.widgets.IMenuBar;

/**
 * @author David Pickens
 */
class SWTMenu extends SWTMenuItem implements IMenu {
    private Menu mMenu = null;
    private Map<IMenu.IObserver,MenuListener> mObserverMap = null;
    /**
     * @param menu
     * @param mapper
     */
    public SWTMenu(IMenu menu, IComponentMapper mapper) {
        super(menu, mapper, SWT.CASCADE);
    }
    
    /**
     * @param menuBar
     * @param mapper
     */
    public SWTMenu(IMenuBar menuBar, IComponentMapper mapper) {
        super(menuBar, mapper);
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.swt.Component#instantiate()
     */
    @Override
    protected Widget instantiate() {
        MenuItem item = (MenuItem) super.instantiate();
        mMenu =  new Menu(item); 
        item.setMenu(mMenu);
        return item;
    }
    
    @Override
    public Object getComponent(){
        if (mMenu == null){
            super.getComponent();
        }
        return mMenu;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IMenu#show(int, int)
     */
    @Override
    public void show(int x, int y) {
        Menu menu = (Menu)getComponent();
        menu.setLocation(x,y);
        menu.setVisible(true);
        
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IMenu#appendSeparator()
     */
    @SuppressWarnings("unused")
    @Override
    public void appendSeparator() {
        Menu menu = (Menu)getComponent();
        new MenuItem(menu,SWT.SEPARATOR);       
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IMenu#addObserver(com.arc.widgets.IMenu.IObserver)
     */
    @Override
    public void addMenuObserver(final IMenu.IObserver o) {
        if (mObserverMap == null) mObserverMap = new HashMap<IMenu.IObserver,MenuListener>();
        MenuListener l = new MenuListener(){

            @Override
            public void menuHidden(MenuEvent e) {
                o.menuHidden(SWTMenu.this);
                
            }

            @Override
            public void menuShown(MenuEvent e) {
                o.menuShown(SWTMenu.this);
                
            }};
        mObserverMap.put(o,l);
        mMenu.addMenuListener(l);
        
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IMenu#removeObserver(com.arc.widgets.IMenu.IObserver)
     */
    @Override
    public void removeMenuObserver(IMenu.IObserver o) {
        if (mObserverMap != null){
            MenuListener l = mObserverMap.remove(o);
            if (l != null)
                mMenu.removeMenuListener(l);
        }
        
    }
}
