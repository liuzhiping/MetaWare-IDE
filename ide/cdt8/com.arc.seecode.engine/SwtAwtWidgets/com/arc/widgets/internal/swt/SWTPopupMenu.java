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

import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;

import com.arc.widgets.IComponent;
import com.arc.widgets.IImage;
import com.arc.widgets.IMenu;

/**
 * @author David Pickens
 */
class SWTPopupMenu extends Component implements IMenu {
    private IComponent mParentComponent;
    private Map<IMenu.IObserver,MenuListener> mObserverMap = null;
    /**
     * @param parent
     * @param mapper
     */
    public SWTPopupMenu(IComponent parent, IComponentMapper mapper) {
        super(null, mapper);
        mParentComponent = parent;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.swt.Component#instantiate()
     */
    @Override
    protected Widget instantiate() {
        Control control = (Control)mParentComponent.getComponent();
        Menu menu = new Menu(control);
        control.setMenu(menu);
        return menu;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IButton#getText()
     */
    @Override
    public String getText() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IButton#setText(java.lang.String)
     */
    @Override
    public void setText(String txt) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IButton#setImage(com.arc.widgets.IImage)
     */
    @Override
    public void setImage(IImage image) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IButton#getImage()
     */
    @Override
    public IImage getImage() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IButton#setSelected(boolean)
     */
    @Override
    public void setSelected(boolean v) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IButton#setMnemonic(char)
     */
    @Override
    public void setMnemonic(char c) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IButton#getButtonKind()
     */
    @Override
    public int getButtonKind() {
        return PUSH;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IButton#addActionListener(java.awt.event.ActionListener)
     */
    @Override
    public void addActionListener(ActionListener listener) {
        throw new IllegalStateException("Actions not valid for popups");

    }
    
    /* (non-Javadoc)
     * @see com.arc.widgets.IButton#removeActionListener(java.awt.event.ActionListener)
     */
    @Override
    public void removeActionListener(ActionListener listener) {
        throw new IllegalStateException("Actions not valid for popups");

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IButton#isSelected()
     */
    @Override
    public boolean isSelected() {
        return false;
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
                //Hack: the menuHidden event if fired
                // before the menu item selection is fired!
                // If the observer disposes of the menu, then the
                // selection won't be fired.
                // Thus, we cause it to be delayed until after
                // the menu item selection is fired...
                e.display.asyncExec(
                        new Runnable(){
                            @Override
                            public void run(){
                                o.menuHidden(SWTPopupMenu.this);
                            }
                        });                        
            }

            @Override
            public void menuShown(MenuEvent e) {
                o.menuShown(SWTPopupMenu.this);
                
            }};
        mObserverMap.put(o,l);
        ((Menu)getComponent()).addMenuListener(l);
        
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IMenu#removeObserver(com.arc.widgets.IMenu.IObserver)
     */
    @Override
    public void removeMenuObserver(IMenu.IObserver o) {
        if (mObserverMap != null){
            MenuListener l = mObserverMap.remove(o);
            if (l != null && !((Menu)getComponent()).isDisposed())
                ((Menu)getComponent()).removeMenuListener(l);
        }
        
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IToolItem#addObserver(com.arc.widgets.IToolItem.IObserver)
     */
    @Override
    public void addObserver(com.arc.widgets.IToolItem.IObserver observer) {
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IToolItem#removeObserver(com.arc.widgets.IToolItem.IObserver)
     */
    @Override
    public void removeObserver(com.arc.widgets.IToolItem.IObserver observer) {
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IToolItem#getStyle()
     */
    @Override
    public int getStyle() {
        return getButtonKind();
    }

}
