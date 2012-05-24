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

import com.arc.seecode.engine.EngineInterface;

/**
 * The action that is inserted into the MenuManager to
 * denote the SeeCode display menu.
 * @author David Pickens
 */
class SeeCodeDisplayAction extends Action {


    private SeeCodeSelectorMenuCreator mMenuCreator;
    private static int sInstanceCount = 0;

    /**
     * 
     */
    public SeeCodeDisplayAction(EngineInterface engine) {
        super("Debugger",IAction.AS_DROP_DOWN_MENU);
        mMenuCreator = new SeeCodeSelectorMenuCreator(engine);
        setId("seecode.selectors." + ++sInstanceCount);
    }
    
    public void refresh(){
        mMenuCreator.refresh();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IAction#getMenuCreator()
     */
    @Override
    public IMenuCreator getMenuCreator() {
        return mMenuCreator;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IAction#setMenuCreator(org.eclipse.jface.action.IMenuCreator)
     */
    @Override
    public void setMenuCreator(IMenuCreator creator) {
        // can't override
        throw new IllegalArgumentException("Can't override menu creator");
    }
    
    public EngineInterface getEngine(){
        return mMenuCreator.getEngine();
    }
}
