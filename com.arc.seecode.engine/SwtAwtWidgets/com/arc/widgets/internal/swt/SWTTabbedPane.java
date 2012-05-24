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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Widget;

import com.arc.widgets.IComponent;
import com.arc.widgets.IContainer;
import com.arc.widgets.ITabItem;
import com.arc.widgets.ITabbedPane;

/**
 * A builder for an SWT tabbed panel
 */

class SWTTabbedPane extends AbstractContainer implements ITabbedPane {
    private int mStyle;

    SWTTabbedPane(IContainer parent, int style, IComponentMapper mapper) {
        super(parent,mapper);
        mStyle = style;
    }

    @Override
    protected Widget instantiate() {
        TabFolder c = new TabFolder(getParentComposite(), mStyle);
        return c;
    }

    @Override
    public void setSelectedItem(ITabItem item) {
        TabFolder tf = (TabFolder)getComponent();
        TabItem ti = ((SWTTabItem) item).getTabItem();
        tf.setSelection(tf.indexOf(ti));
    }

    @Override
    public ITabItem addTab(IComponent component, String title) {
        ITabItem i =
            new SWTTabItem(
                (TabFolder) getComponent(),
                (Control) component.getComponent(),
                title);
        ((TabFolder) getComponent()).setSelection(0); // is this necessary?
        return i;
    }

    @Override
    public void layout() {
        ((TabFolder) getComponent()).layout();
    }

    /*override*/
    @Override
    public int getContainerStyle() {
        // TODO Auto-generated method stub
        return 0;
    }

    /*override*/
    @Override
    public void setHorizontalSpacing(int pixels) {
        // TODO Auto-generated method stub
        
    }

    /*override*/
    @Override
    public void setVerticalSpacing(int pixels) {
        // TODO Auto-generated method stub
        
    }
}
