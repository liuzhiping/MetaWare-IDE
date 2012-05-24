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

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;

import com.arc.widgets.IButton;
import com.arc.widgets.IComponent;
import com.arc.widgets.IMenu;
import com.arc.widgets.IMenuBar;
import com.arc.widgets.IMenuItem;

/**
 * @author David Pickens
 */
class SWTMenuItem extends AbstractButton implements IMenuItem {
    private IComponent mMenu; // IMenu or IMenuBar
    SWTMenuItem(IMenu menu, IComponentMapper mapper, int style){
        super(null,mapper,style);
        mMenu = menu;
    }
    
    protected SWTMenuItem(IMenuBar menu, IComponentMapper mapper){
        super(null,mapper,IButton.PUSH);
        mMenu = menu;
    }

    @Override
    protected Widget instantiate() {
        MenuItem b = new MenuItem((Menu) mMenu.getComponent(), mStyle);
        if (mText != null)
            b.setText(mText);
        if (mImage != null)
            b.setImage((Image) mImage.getObject());
        b.setSelection(mSelected);
        return b;
    }

}
