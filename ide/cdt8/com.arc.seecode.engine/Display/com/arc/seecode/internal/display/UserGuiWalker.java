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
package com.arc.seecode.internal.display;

import java.util.List;

import org.dom4j.Element;

import com.arc.mw.util.Cast;
import com.arc.seecode.display.MenuDescriptor;
import com.arc.seecode.engine.display.IDisplayCreator;

/**
 * Walks the XML representation of a user-defined menus
 * 
 * @author David Pickens
 */
class UserGuiWalker {
    private IValueSender mSender;

    private int mGuiNumber;

    private ICheckBoxRecorder mCheckBoxRecorder;
    private IDisplayCreator mDisplayActivator;

    UserGuiWalker(IValueSender sender, int guiNumber, ICheckBoxRecorder checkBoxRecorder, IDisplayCreator displayActivator) {
        mSender = sender;
        mGuiNumber = guiNumber;
        mCheckBoxRecorder = checkBoxRecorder;
        mDisplayActivator = displayActivator;
    }

    public MenuDescriptor walk(Element root) {
        // Form component (or component vector?) from a specification.

        MenuDescriptor md = new MenuDescriptor();
        walk(root, md);
        return md;
    }

    /**
     * Walk a menu description and fill in an existing
     * menu description.
     * @param root the XML representation of the menu.
     * @param md the menu description to fill in.
     */
    public void walk(Element root, MenuDescriptor md) {
        String rootName = root.getName();
        if (rootName.equals("menu")) {
            String name = root.attributeValue("name", "missing-menu-name");
            MenuDescriptor subMD = new MenuDescriptor();
            doKids(root, subMD);
            md.addSubMenu(name, name, subMD);
        } else if (rootName.equals("menuitem") || rootName.equals("menu_item")) {
            final String menu_name = root.attributeValue("name",
                    "missing-menuitem-name");
            final String id = root.attributeValue("id",menu_name);
 //           String sc = root.attributeValue("shortcut");
            String checkbox = root.attributeValue("checkbox");
            String radio_group = root.attributeValue("checkbox_group");
            String selectValue = root.attributeValue("on_select");
            if (selectValue == null)
                selectValue = root.attributeValue("onselect", menu_name);
            final String displayKind = root.attributeValue("display");
            final String valueWhenSelected = selectValue;
            // Now record the group with the created menu item.
            if (checkbox != null && checkbox.charAt(0) == '1') {
                final String initial = root.attributeValue("initial");

                md.addCheckBoxMenuItem(id, menu_name,
                        new MenuDescriptor.ICheckBoxObserver() {

                            @Override
                            public void selectionChanged(String checkBoxID,
                                    boolean value) {
                                String s = (value ? "1" : "0")
                                        + valueWhenSelected;
                                //                          First digit is state; rest is identification.
                                doSelection(s);
                                mCheckBoxRecorder.saveCheckBoxValue(checkBoxID,value);
                            }
                        }, initial != null && initial.equals("1"), radio_group);

            } else {
                md.addMenuItem(id, menu_name, new MenuDescriptor.IActionObserver() {

                    @Override
                    public void actionPerformed(String name) {
                    	//NOTE: "display" was added late, and we have
                    	// specifications that include "select_on" in case "display"
                    	// isn't supported. So, "display" overrides "select_on"
                    	if (displayKind != null) {
                    		mDisplayActivator.activateDisplay(displayKind);
                    	}
                    	else
                    	if (valueWhenSelected != null)
                            doSelection(valueWhenSelected);
                    	
                        
                    }
                });
            }
            //            String enabled = lookupAttribute("enabled");
            //            if (enabled != null)
            //                M.setEnabled(enabled.charAt(0) != '0');
            //            if (sc != null && sc.length() > 0) {
            //                M.setMnemonic(sc.charAt(0)/*
            //                                           * , lookupAttribute("shortcut_shift") !=
            //                                           * null
            //                                           */);
            //            }
        } else if (rootName.equals("menuseparator")
                || rootName.equals("menu_separator")) {
            md.addSeparator();
        } else if (rootName.equals("sequence") || rootName.equals("gui")) {
            // A vector of objects.
            // Absorb any vectors below into this one so we just have
            // one top-level vector.
            doKids(root, md);
        } else {
            throw new IllegalArgumentException(
                    "Missing component property in form: " + root.getName());
        }

    }

    private void doKids(Element parent, MenuDescriptor md) {
        // Process the kids of this tree and return a vector of formed
        // components. Recursively erase any sequences that appear below
        // so we get just the flattened tree.
        List<Element> kids = Cast.toType(parent.elements());
        for (Element kid: kids){
            walk(kid, md);
        }
    }

    private void doSelection(String s) {
        mSender
                .sendValueUpdate("user_gui_selection", s + " ugui "
                        + mGuiNumber);
    }

}
