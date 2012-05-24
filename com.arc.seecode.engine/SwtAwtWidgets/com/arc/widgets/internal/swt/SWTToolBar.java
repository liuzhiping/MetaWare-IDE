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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

import com.arc.widgets.IContainer;
import com.arc.widgets.IToolBar;
import com.arc.widgets.IToolItem;

/**
 * @author David Pickens
 */
public class SWTToolBar extends Component implements IToolBar {
    private List<IToolItem> mList = new ArrayList<IToolItem>();
    /**
     * @param parent
     * @param mapper
     */
    public SWTToolBar(IContainer parent, IComponentMapper mapper) {
        super(parent, mapper);
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.swt.Component#instantiate()
     */
    @Override
    protected Widget instantiate() {
        return new ToolBar(getParentComposite(),SWT.HORIZONTAL|SWT.WRAP|SWT.FLAT);
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IToolBar#makeItem(int)
     */
    @Override
    public IToolItem makeItem(int style) {
        IToolItem i = new SWTToolItem(this,style);
        mList.add(i);
        return i;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IToolBar#getItems()
     */
    @Override
    public IToolItem[] getItems() {
        return  mList.toArray(new IToolItem[mList.size()]);
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IToolBar#addSeparator()
     */
    @SuppressWarnings("unused")
    @Override
    public void addSeparator() {
        ToolBar tb = (ToolBar)getComponent();
        new ToolItem(tb,SWT.SEPARATOR);
    }

}
