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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;

/**
 * @author David Pickens
 */
class MenuBarUpdateManager implements IMenuBarUpdateManager {

    private IMenuManager mMenuBar;
    private boolean mDirty = false;
    private Set<IAction> mSet = new HashSet<IAction>();
    private String mIdOfMenuToInsertBefore;

    /**
     * 
     */
    public MenuBarUpdateManager(IMenuManager menubar) {
        mMenuBar = menubar;
        IContributionItem[] items = menubar.getItems();
        if (items.length > 2){
            mIdOfMenuToInsertBefore = items[items.length-2].getId();
        }
    }

    /* (non-Javadoc)
     * @see com.arc.cdt.debug.seecode.internal.ui.IMenuBarUpdateManager#insertMenu(org.eclipse.jface.action.IAction)
     */
    @Override
    public void insertMenu(IAction action) {
        if (mSet.add(action)){
            mDirty = true;
            if (mIdOfMenuToInsertBefore != null){
                mMenuBar.insertBefore(mIdOfMenuToInsertBefore,action);
            }
            else // shouldn't get here
                mMenuBar.appendToGroup("additions",action);           
        }
    }

    /* (non-Javadoc)
     * @see com.arc.cdt.debug.seecode.internal.ui.IMenuBarUpdateManager#removeMenu(org.eclipse.jface.action.IAction)
     */
    @Override
    public void removeMenu(IAction action) {
        if (mSet.remove(action)){
            mDirty = true;
            mMenuBar.remove(action.getId());
        }
    }

    /* (non-Javadoc)
     * @see com.arc.cdt.debug.seecode.internal.ui.IMenuBarUpdateManager#removeAll()
     */
    @Override
    public void removeAll() {
        for (IAction action: mSet){
            mMenuBar.remove(action.getId());
            mDirty = true;
        }
        mSet.clear();
    }

    /* (non-Javadoc)
     * @see com.arc.cdt.debug.seecode.internal.ui.IMenuBarUpdateManager#getMenuManager()
     */
    @Override
    public IMenuManager getMenuManager() {
        return mMenuBar;
    }

    /* (non-Javadoc)
     * @see com.arc.cdt.debug.seecode.internal.ui.IMenuBarUpdateManager#update()
     */
    @Override
    public void update() {
        if (mDirty) {
            mMenuBar.update(false);
            mDirty = false;
        }
        
    }

    /* (non-Javadoc)
     * @see com.arc.cdt.debug.seecode.internal.ui.IMenuBarUpdateManager#contains(org.eclipse.jface.action.IAction)
     */
    @Override
    public boolean contains(IAction action) {
        return mSet.contains(action);
    }
}
