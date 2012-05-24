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
package com.arc.cdt.debug.seecode.internal.ui.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import com.arc.cdt.debug.seecode.ui.UISeeCodePlugin;


public class ShowAnimationWidgetsActionDelegate implements IViewActionDelegate, IActionDelegate2 {

    private IViewPart fDebugView;
    private IAction fAction;
    
    private static final String PREFERENCE_KEY = "com.arc.cdt.debug.seecode.ui.ShowAnimationItems";

    @Override
    public void init (IViewPart view) {
        fDebugView = view;
        fAction.setChecked(getPreferenceValue());
        apply(fAction.isChecked());
    }

    @Override
    public void run (IAction action) {
        
        IPreferenceStore store = getPreferenceStore();
        String key = getPreferenceKey(); 
        store.setValue(key, action.isChecked());
        apply(action.isChecked());
    }
    
    protected void apply(boolean selected){
        AnimateToolBarManager mgr = UISeeCodePlugin.getDefault().getAnimateToolBarManager();
        mgr.setVisible(fDebugView,selected);
    }

    @Override
    public void selectionChanged (IAction action, ISelection selection) {
    }

    @Override
    public void dispose () {
        // @todo Auto-generated method stub
        
    }

    @Override
    public void init (IAction action) {
        fAction = action;        
    }

    @Override
    public void runWithEvent (IAction action, Event event) {
        run(action);    
    }
    
    protected IPreferenceStore getPreferenceStore() {
        return UISeeCodePlugin.getDefault().getPreferenceStore();
    }
    
    /**
     * Returns the key used by this action to store its preference value/setting.
     * Based on a base key (suffix) and part id (prefix).
     *  
     * @return preference store key
     */
    protected String getPreferenceKey() {
        return PREFERENCE_KEY; 
    }
    
    /**
     * Returns the value of this filters preference (on/off) for the given
     * view.
     * 
     * @param part
     * @return boolean
     */
    protected boolean getPreferenceValue() {
        String key = getPreferenceKey();
        IPreferenceStore store = getPreferenceStore();
        return store.getBoolean(key);    
    }

}
