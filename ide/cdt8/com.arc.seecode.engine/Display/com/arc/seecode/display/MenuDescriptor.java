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

/**
 * A description of a menu that can be dynamically created as required.
 * 
 * @author David Pickens
 */
public class MenuDescriptor {
    /**
     * Called when a checkbox-style menu changed.
     * @author David Pickens
     */
    public interface ICheckBoxObserver {
        void selectionChanged(String name, boolean value);
    }

    /**
     * Called when a radiobutton of a group changed.
     * @author David Pickens
     */
    public interface IRadioButtonObserver {
        void selectionChanged(String groupName, String value);
    }
    
    /**
     * Called when an ordinary menu item was pressed.
     * @author David Pickens
     */
    public interface IActionObserver {
        void actionPerformed(String name);
    }

    static class Item {
        Item(String name, String label, Object content){
            this.name = name;
            this.label = label;
            this.content = content;
            this.enabled = true;
        }
        String name;
        String label;
        Object content;
        boolean enabled;
    }
    private List<Item> mItems = new ArrayList<Item>();

    private static final String SEPARATOR = "";
    
    private static final String INSERT = "";

    // Map<String,IRadioButtonObserver>
    private Map<String,IRadioButtonObserver> mRadioObservers = null;

    public MenuDescriptor() {

    }
    
    public void removeAll(){
        mItems.clear();
        if (mRadioObservers != null) {
            mRadioObservers.clear();
        }
    }

    /**
     * Add a menuitem.
     * 
     * @param name
     *            id of the menu for later referencing.
     * @param text
     *            label to appear in menu item.
     * @param listener
     *            callabck to be invoked when menu item selected.
     */
    public void addMenuItem(String name, String text, IActionObserver listener) {
        if (name != null)
            removeMenuItem(name); // replace anyone with same name
        mItems.add(new Item(name,text,listener));
    }
    
    /**
     * Return the names of the immediate menu items, in order.
     * @return the names of the immediate menu items.
     */
    public String[] getItemNames(){
        String names[] = new String[mItems.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = mItems.get(i).name;
        }
        return names;
    }

    /**
     * Add a checkbox-style menu item.
     * 
     * @param name
     *            id of the menu for later referencing.
     * @param text
     *            the label to appear on menu item.
     * @param listener
     *            callback to be invoked when selection changes.
     * @param initValue
     *            initial value.
     * @param radioGroup
     *            set to non-null if this is actually a radiobutton within a
     *            group.
     */
    public void addCheckBoxMenuItem(String name, String text,
            ICheckBoxObserver listener, boolean initValue, String radioGroup) {
        if (name != null)
            removeMenuItem(name); // replace any with same name
        mItems.add(new Item(name,text,new CheckBoxDescriptor(listener, initValue, radioGroup,
                null)));
    }
    
    /**
     * Find a checkbox menu item, and set its value. Return true
     * if menu item found.
     * @param name the name of the checkbox menu item.
     * @param v the value to be assigned.
     * @return true if checkbox menu item found.
     */
    public boolean setCheckBoxValue(String name, boolean v){
        int cnt = mItems.size();
        for (int i = 0; i < cnt; i++){
            if (name.equals(mItems.get(i).name)){
                Object content = mItems.get(i).content;
                if (content instanceof CheckBoxDescriptor){
                    CheckBoxDescriptor cd = (CheckBoxDescriptor)content;
                    cd.currentValue = v;
                    return true;
                }
            }
            else if (mItems.get(i).content instanceof MenuDescriptor){
                if (((MenuDescriptor)mItems.get(i).content).setCheckBoxValue(name,v))
                    return true;
            }
        }
        return false;
    }

    /**
     * Add a radio-button-style menu item.
     * 
     * @param name
     *            id of the menu for later referencing.
     * @param text
     *            the label to appear on menu item.
     * @param groupName
     *            name of radio group; only one menu item will be selected
     *            within the group.
     * @param value
     *            the value to be assigned to group when this item is selected.
     */
    public void addRadioMenuItem(String name, String text, String groupName,
            String value, boolean initValue) {
        mItems.add(new Item(name,text,new CheckBoxDescriptor(null, initValue, groupName, value)));
    }

    public void addSubMenu(String name, String text, MenuDescriptor md) {
        if (name != null)
            removeMenuItem(name); // remove any with same name.
        mItems.add(new Item(name,text,md));
    }
    
    /**
     * Insert another menu here.
     * @param md the descriptor for the menu to be inserted here.
     */
    public void addAllMenuItems(MenuDescriptor md){
        mItems.add(new Item(INSERT,"",md));
    }

    /**
     * Set the radio button observer for a particular group.
     * 
     * @param groupName
     *            name of radio button group
     * @param observer
     *            observer to be called when value of group changes.
     */
    public void setRadioListener(String groupName, IRadioButtonObserver observer) {
        if (mRadioObservers == null) {
            mRadioObservers = new HashMap<String,IRadioButtonObserver>();
        }
        mRadioObservers.put(groupName, observer);
    }
    
    private Item findItem(String name){
        for (Item item: mItems){
            if (name.equals(item.name))
                return item;
        }
        return null;
    }
    
    private Item findItemTransitively(String name){
        for (Item item: mItems){
            if (name.equals(item.name))
                return item;
            if (item.content instanceof MenuDescriptor) {
                Item i = ((MenuDescriptor)item.content).findItemTransitively(name);
                if (i != null) return i;
            }
        }
        return null;
    }

    /**
     * Remove menu item named "name". Does nothing if menu item does not exist.
     * 
     * @param name
     *            name of menu item to remove.
     * @return true if successful; false if item doesn't exist.
     */
    public boolean removeMenuItem(String name) {
        Item item = findItem(name);
        if (item != null) {
            mItems.remove(item);
            return true;
        }
        for (Item m: mItems) {
            if (m.content instanceof MenuDescriptor) {
                if (((MenuDescriptor)m.content).removeMenuItem(name))
                    return true;
            }
        }
        return false;
    }
    
    /**
     * Return the label associated with a menu item name.
     * @param name the name to seek.
     * @return the associated label, or <code>null</code> if there is no item with the name.
     */
    public String getLabel(String name){
        Item item = findItemTransitively(name);
        if (item != null) return item.label;
        return null;
    }
    
    /**
     * Change the label associated with a menu item.
     * @param name name of item to change.
     * @param newLabel new label to assign.
     * @throws IllegalArgumentException if there is no item of the given name.
     */
    public void setLabel(String name, String newLabel)throws IllegalArgumentException{
        if (!replaceLabel(name,newLabel)){
            throw new IllegalArgumentException("Can't find menu item " + name);
        }
        
    }
    
    private boolean replaceLabel (String name, String newLabel) {
        Item item = findItemTransitively(name);
        if (item != null) {
            item.label = newLabel;
            return true;
        }
        return false;
    }
    
    public void setEnabled(String name, boolean v){
        Item item = findItemTransitively(name);
        if (item != null) item.enabled = v;
    }


    public void addSeparator () {
        mItems.add(new Item(SEPARATOR,"",SEPARATOR));
    }
    
    public interface ITopLevelCallback{
        public void doMenu(String label, MenuDescriptor menu);
    }
    
    /**
     * Yield each top-level submenu.
     * @param callback invoked for each top-level submen with its name.
     */
    public void eachTopLevelMenu(ITopLevelCallback callback){
        for (Item item: mItems) {
            if (item.content instanceof MenuDescriptor){
                callback.doMenu(item.label,(MenuDescriptor)item.content);
            }
        }
    }


    /**
     * Append the menu items in this descriptor to an actual menu.
     */
    public void generate(Object parent, IMenuGenerator callback) {
        for (Item item: mItems) {
            final String name =  item.name;
            String label =  item.label;
            final Object c = item.content;
            if (c instanceof IActionObserver) {
                callback.makeMenuItem(parent,name,label,new IMenuGenerator.IItemObserver(){

                    @Override
                    public void menuItemSelected(String id) {
                        ((IActionObserver)c).actionPerformed(id);
                        
                    }},item.enabled);
            } else if (c instanceof MenuDescriptor) {
                if (name == INSERT){
                    ((MenuDescriptor) c).generate(parent,callback);
                }
                else {
                    Object submenu = callback.makeSubmenu(parent,name,label);
                    ((MenuDescriptor)c).generate(submenu,callback);
                }
            } else if (c instanceof CheckBoxDescriptor) {
                final CheckBoxDescriptor cd = (CheckBoxDescriptor)c;
                IMenuGenerator.ICheckBoxObserver mo = new IMenuGenerator.ICheckBoxObserver() {
                    @Override
                    public void selectionChanged(String id, boolean b) {
                        if (cd.observer != null)
                            cd.observer.selectionChanged(id, b);
                        cd.currentValue = b;
                        if (cd.radioGroup != null && b) {
                            if (mRadioObservers != null && cd.radioValue != null) {
                                IRadioButtonObserver o =  mRadioObservers
                                        .get(cd.radioGroup);
                                if (o != null)
                                    o.selectionChanged(cd.radioGroup, cd.radioValue);
                            }
                        }
                    }
                };
                if (cd.radioGroup != null){
                    callback.makeRadioItem(parent,name,label,mo,item.enabled,cd.currentValue,cd.radioGroup);
                }
                else {
                    callback.makeCheckBoxItem(parent,name,label,mo,item.enabled,cd.currentValue);  
                }
                if (cd.initValue != cd.currentValue){
                    // If the value of the checkbox was changed since it was first defined,
                    // then materialize the change by calling the observer. CR2278
                    mo.selectionChanged(name,cd.currentValue);
                }
            } else {
                callback.makeSeparator(parent);
            }
        }

    }


    static class CheckBoxDescriptor {
        private boolean initValue;
        
        private boolean currentValue;

        private ICheckBoxObserver observer;

        private String radioGroup;

        private String radioValue;

        CheckBoxDescriptor(ICheckBoxObserver observer, boolean initValue,
                String radioGroup, String value) {
            this.currentValue = initValue;
            this.initValue = initValue;
            this.observer = observer;
            this.radioGroup = radioGroup;
            this.radioValue = value;
        }
    }

}
