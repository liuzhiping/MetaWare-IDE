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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;

import com.arc.widgets.ITabItem;
import com.arc.widgets.ITabbedPane;

/**
 * A builder for an SWT tab item 
 */

class SWTCTabItem implements ITabItem {
    private CTabItem mTabItem;
    SWTCTabItem(CTabFolder folder, Control control, String title) {
        mTabItem = new CTabItem(folder, SWT.NULL);
        mTabItem.setControl(control);
        mTabItem.setText(title);
    }
    CTabItem getTabItem() {
        return mTabItem;
    }
    @Override
    public String getText() {
        return mTabItem.getText();
    }
    @Override
    public void setText(String txt) {
        mTabItem.setText(txt);
    }
    @Override
    public Object getImage() {
        return mTabItem.getImage();
    }
    @Override
    public void setImage(Object image) {
        mTabItem.setImage((Image) image);
    }

    @Override
    public ITabbedPane getParent() {
        throw new Error("getParent not supported");
    }

    @Override
    public void setToolTipText(String tip) {
        mTabItem.setToolTipText(tip);
    }
    @Override
    public String getToolTipText() {
        return mTabItem.getToolTipText();
    }
    @Override
    public Object getComponent() {
        return mTabItem.getControl();
    }
}
