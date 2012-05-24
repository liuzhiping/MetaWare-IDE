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
package com.arc.seecode.display;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.arc.widgets.IButton;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IMenu;
import com.arc.widgets.IMenuItem;
import com.arc.widgets.IToolItem;


/**
 * An implementation of the menu generator that
 * builds an {@link IMenu} instance.
 * <P>
 * An instance of this object is passed to
 * {@link MenuDescriptor#generate}.
 * @author David Pickens
 */
public class MenuBuilder implements IMenuGenerator {

    private IComponentFactory mFactory;
    private Map<String,List<IButton>> mGroups = null;

    /**
     * 
     */
    public MenuBuilder(IComponentFactory factory) {
        mFactory = factory;
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.IMenuGenerator#makeMenuItem(java.lang.Object, java.lang.String, java.lang.String, com.arc.seecode.display.IMenuGenerator.IItemObserver)
     */
    @Override
    public void makeMenuItem(Object parent, final String name, String label,
            final IItemObserver observer, boolean enabled) {
        if (observer == null || parent == null || label == null)
            throw new IllegalArgumentException("arg is null");
        IMenuItem item = mFactory.makeMenuItem((IMenu)parent);
        item.setName(name);
        item.setText(label);
        item.addObserver(new IToolItem.IObserver(){

            @Override
            public void itemChanged(IToolItem menuItem) {
                observer.menuItemSelected(name);
                
            }});
        item.setEnabled(enabled);

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.IMenuGenerator#makeCheckBoxItem(java.lang.Object, java.lang.String, java.lang.String, com.arc.seecode.display.IMenuGenerator.ICheckBoxObserver, boolean)
     */
    @Override
    public void makeCheckBoxItem(Object parent, final String name, String label,
            final ICheckBoxObserver observer, boolean enabled, boolean initValue) {
        if (observer == null || parent == null || label == null)
            throw new IllegalArgumentException("arg is null");
        IButton checkbox = mFactory.makeCheckBoxMenuItem((IMenu)parent);
        checkbox.setName(name);
        checkbox.setText(label);
        checkbox.setSelected(initValue);
        checkbox.addObserver(new IToolItem.IObserver(){

            @Override
            public void itemChanged(IToolItem menuItem) {
                observer.selectionChanged(name,menuItem.isSelected());
                
            }});
        checkbox.setEnabled(enabled);
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.IMenuGenerator#makeRadioItem(java.lang.Object, java.lang.String, java.lang.String, com.arc.seecode.display.IMenuGenerator.ICheckBoxObserver, boolean, java.lang.String)
     */
    @Override
    public void makeRadioItem(Object parent, final String name, String label,
            final ICheckBoxObserver observer, boolean  enabled, boolean initValue, String groupName) {
        if (observer == null || parent == null || label == null)
            throw new IllegalArgumentException("arg is null");
        IButton checkbox = mFactory.makeRadioButtonMenuItem((IMenu)parent);
        List<IButton> buttonsInGroup = null;
        if (groupName != null){
            if (mGroups == null){
                mGroups = new HashMap<String,List<IButton>>();
            }
            List<IButton> list = mGroups.get(groupName);
            if (list == null){
                list = new ArrayList<IButton>();
                mGroups.put(groupName,list);
            }
            buttonsInGroup = list;
        }
        checkbox.setName(name);
        checkbox.setText(label);
        checkbox.setSelected(initValue);
        checkbox.setEnabled(enabled);
        final List<IButton>buttonsInGroup_ = buttonsInGroup;
        checkbox.addObserver(new IToolItem.IObserver(){

            @Override
            public void itemChanged(IToolItem menuItem) {
                if (buttonsInGroup_ != null && menuItem.isSelected()){
                    for (IButton b: buttonsInGroup_){
                        if (b != menuItem){
                            b.setSelected(false);
                        }
                    }
                }
                observer.selectionChanged(name,menuItem.isSelected());
                
            }});
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.IMenuGenerator#makeSubmenu(java.lang.Object, java.lang.String, java.lang.String)
     */
    @Override
    public Object makeSubmenu(Object parent, String name, String label) {
       IMenu menu = mFactory.makeMenu((IMenu)parent);
       menu.setName(name);
       menu.setText(label);
       return menu;
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.IMenuGenerator#makeSeparator(java.lang.Object)
     */
    @Override
    public void makeSeparator(Object parent) {
        ((IMenu)parent).appendSeparator();
    }

}
