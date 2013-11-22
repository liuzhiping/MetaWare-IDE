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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Given an array of menu description selectors from
 * the SeeCode engine
 * process it in order to generate menus.
 * <P>
 * See {@link com.arc.seecode.engine.IEngineAPI#getDisplaySelectors}
 * for a description.
 * @author David Pickens
 */
public class DisplayMenuGenerator {
    public interface ICallback {
        /** 
         * Create a submenu named "name"
         * @param name naem of sub menu
         * @return a hanble that references the submenu.
         */
        Object createSubmenu(String name, Object parent);
        /**
         * Create a menu item with label "label"
         * associated with an "id".
         * @param id
         * @param label
         * @param parent the parent menu.
         */
        void createMenuItem(String id, String label, Object parent);
        /**
         * Called to report an exception.
         * @param e the exception.
         */
        void error(String msg, Exception e);
    }
    
    private String[] mSelectors;
    private Map<String,Object> mSubMenus = null;
    public DisplayMenuGenerator(String selectors[]){
        mSelectors = selectors;
    }
    
    /**
     * Parse the selectors by invoking methods in 
     * the callback interface.
     * @param callback callback methods as selectors are
     * parsed.
     * @param parentMenu the parent menu into which
     * a menu is being placed.
     */
    public void generate(ICallback callback, Object parentMenu){
        for (int i = 0; i < mSelectors.length; i++){
            makeMenuItemFromSelectorProperties(parentMenu,mSelectors[i],callback);
        }
    }
    
    /**
     * Look for menu path of form "A/B/C" and return "A" "B" "C". 
     * Beware of escapes: "A\\/B/C" returns as "A/B" "C".
     * @param menu
     * @return menu path list.
     */
    private static String[] parseMenuPath(String menu){
        if (menu.indexOf('/') < 0) return new String[]{menu};
        StringBuilder buf = new StringBuilder(menu.length());
        int len = menu.length();
        List<String>list = new ArrayList<String>();
        for (int i = 0; i < len; i++) {
            char c = menu.charAt(i);
            switch(c){
                case '\\': 
                    if (i+1 < len){
                        i++;
                        buf.append(menu.charAt(i));
                    }
                    break;
                case '/':{
                    if (buf.length() > 0){
                        list.add(buf.toString());
                        buf.setLength(0);
                    }
                    break;
                }
                default:
                    buf.append(c);
                break;
            }
        }
        if (buf.length() > 0){
            list.add(buf.toString());
        }
        return list.toArray(new String[list.size()]);
        
    }
    
    private void makeMenuItemFromSelectorProperties(Object menu, String propString,
            ICallback callback){
        ByteArrayInputStream input = new ByteArrayInputStream(propString.getBytes());
        Properties props = new Properties();
        try {
            props.load(input);
        } catch (IOException e) {
            callback.error(e.getMessage(),e);
            return;
        }
        String label = props.getProperty("menu_name","<no label>");
        final String id = props.getProperty("id");
        if (id == null){
            callback.error("id missing in display selector:" + propString,null);
            return;
        }
        String parent = props.getProperty("parent_menu_name");
        if (parent != null){
            // If parent is of form "A/B" then we have submenu A and sub-submen B
            String path[] = parseMenuPath(parent);
            if (mSubMenus == null) mSubMenus = new HashMap<String,Object>();
            String fullName = "";
            for (String menuName: path) {
                if (fullName.length() > 0) fullName += "/";
                fullName += menuName;
                Object submenu = mSubMenus.get(fullName);
                if (submenu == null){
                    menu = callback.createSubmenu(menuName,menu); 
                }
                else menu = submenu;
                mSubMenus.put(fullName,menu);
            }
        }
        callback.createMenuItem(id,label,menu);
    }

}
