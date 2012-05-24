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
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.arc.widgets.IMenu;
import com.arc.widgets.IMenuBar;

/**
 * @author David Pickens
 */
class Menu extends MenuItem implements IMenu {
    private Map<IMenu.IObserver,PropertyChangeListener> mObserverMap = null;

    /**
     * @param parent
     * @param mapper
     */
    public Menu(IMenu parent, IComponentMapper mapper) {
        super(parent, mapper);
    }
    
    /**
     * @param parent
     * @param mapper
     */
    public Menu(IMenuBar parent, IComponentMapper mapper) {
        super(parent, mapper);
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.swing.SwingComponent#instantiate()
     */
    @Override
    protected Component instantiate() {
        return new JMenu();
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IMenu#show(int, int)
     */
    @Override
    public void show(int x, int y) {
        JComponent menu = (JComponent)getComponent();
        Component parent = menu.getParent();
        Point p = new Point(x,y);
        SwingUtilities.convertPointFromScreen(p,parent);
        menu.setLocation(p);
        menu.setVisible(true);
        
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IMenu#appendSeparator()
     */
    @Override
    public void appendSeparator() {
        Object menu = getComponent();
        if (menu instanceof JMenu)
            ((JMenu)menu).addSeparator();  
        else
            ((JPopupMenu)menu).addSeparator();
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IMenu#addObserver(com.arc.widgets.IMenu.IObserver)
     */
    @Override
    public void addMenuObserver(final IMenu.IObserver o) {
        if (mObserverMap == null) mObserverMap = new HashMap<IMenu.IObserver,PropertyChangeListener>();
        PropertyChangeListener l = new PropertyChangeListener(){

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                boolean v = ((Boolean)evt.getNewValue()).booleanValue();
                if (v) o.menuShown(Menu.this);
                else o.menuHidden(Menu.this);
                
            }};
        mObserverMap.put(o,l);
        JComponent menu = (JComponent)getComponent();
        menu.addPropertyChangeListener("visible",l);
        
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IMenu#removeObserver(com.arc.widgets.IMenu.IObserver)
     */
    @Override
    public void removeMenuObserver(IMenu.IObserver o) {
        if (mObserverMap != null){
            PropertyChangeListener l = mObserverMap.remove(o);
            if (l != null)
                ((JComponent)getComponent()).removePropertyChangeListener("visible",l);
        }
        
    }
}
