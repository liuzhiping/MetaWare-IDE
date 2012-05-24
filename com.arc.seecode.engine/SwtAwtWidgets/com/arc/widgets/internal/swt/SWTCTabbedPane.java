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
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

import com.arc.widgets.IComponent;
import com.arc.widgets.IContainer;
import com.arc.widgets.ITabItem;
import com.arc.widgets.ITabbedPane;

/**
 * A builder for an SWT tabbed panel
 */

class SWTCTabbedPane extends AbstractContainer implements ITabbedPane {
    private int mStyle;

    SWTCTabbedPane(IContainer parent, int style, IComponentMapper mapper) {
        super(parent,mapper);
        mStyle = style;
    }

    @Override
    protected Widget instantiate() {
        CTabFolder c = new CTabFolder(getParentComposite(), mStyle){
            // We must override this because it
            // mistakenly computes the size to the
            // longest tab bar. We rather have the tabs
            // fold
            @Override
            public Point computeSize(int wHint, int hHint, boolean changed){
                int height = super.computeSize(wHint,hHint,changed).y;
                CTabItem items[] = getItems();
                int width = 0;
                if (wHint != SWT.DEFAULT) width = wHint;
                for (int i = 0; i < items.length; i++){
                    Control control = items[i].getControl();
                    Point size = control.computeSize(wHint,wHint,changed);
                    width = Math.max(size.x,width);
                }
                return new Point(width,height);
                }
            
            @Override
            public void setBounds(int x, int y, int w, int h){
                super.setBounds(x,y,w,h);
                }
            };
        c.setSimple(false);
        return c;
    }

    @Override
    public void setSelectedItem(ITabItem item) {
        CTabItem ti = ((SWTCTabItem) item).getTabItem();
        ((CTabFolder) getComponent()).setSelection(ti);
    }

    @Override
    public ITabItem addTab(IComponent component, String title) {
        ITabItem i =
            new SWTCTabItem(
                (CTabFolder) getComponent(),
                (Control) component.getComponent(),
                title);
        ((CTabFolder) getComponent()).setSelection(0); // is this necessary?
        return i;
    }

    @Override
    public void layout() {
        ((CTabFolder) getComponent()).layout();
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
