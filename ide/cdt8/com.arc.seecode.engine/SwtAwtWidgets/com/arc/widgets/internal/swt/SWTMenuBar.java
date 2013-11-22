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

import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Widget;

import com.arc.widgets.IContainer;
import com.arc.widgets.IMenuBar;

/**
 * @author David Pickens
 */
class SWTMenuBar extends AbstractContainer implements IMenuBar {
    private Menu mMenu = null;

    /**
     * @param parent
     * @param mapper
     */
    public SWTMenuBar(IContainer parent, IComponentMapper mapper) {
        super(parent, mapper);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.swt.Component#instantiate()
     */
    @Override
    protected Widget instantiate() {
        // A menu is not a Control!
        // We override getComponent to handle this.
        return null;
    }
    
    /* (non-Javadoc)
     * @see com.arc.widgets.IComponent#getComponent()
     */
    @Override
    public Object getComponent() {
        // Must override this because Menu is not a
        // control
        if (mMenu == null){
            mMenu = new Menu(getParentComposite());
        }
        return mMenu;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IContainer#getContainerStyle()
     */
    @Override
    public int getContainerStyle() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IContainer#setHorizontalSpacing(int)
     */
    @Override
    public void setHorizontalSpacing(int pixels) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IContainer#setVerticalSpacing(int)
     */
    @Override
    public void setVerticalSpacing(int pixels) {
        // TODO Auto-generated method stub

    }

}
