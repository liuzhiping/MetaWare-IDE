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
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;

import com.arc.widgets.IComponent;
import com.arc.widgets.IContainer;
import com.arc.widgets.ILabel;

/**
 * A builder for an SWT label.
 */

class SWTLabel extends Component implements ILabel {
   
    private String mText;
    private int mBorder = ILabel.NO_BORDER;
    private boolean mWrap = false;
    private int mAlignFlags = SWT.BEGINNING;

    SWTLabel(IContainer parent, String text, IComponentMapper mapper) {
        super(parent, mapper);
        mText = text;
    }

    @Override
    protected Widget instantiate () {
        int flags = mAlignFlags;
        int border = 0;
        if (mBorder == IComponent.BEVEL_IN_BORDER) {
            border = SWT.SHADOW_IN;
        }
        else if (mBorder == IComponent.BEVEL_OUT_BORDER) {
            border = SWT.SHADOW_OUT;
        }

        if (mWrap)
            flags |= SWT.WRAP;
        Widget widget;
        if (border != 0) {
            CLabel l = new CLabel(getParentComposite(), flags | border);
            widget = l;
            l.setText(mText);
        }
        else {
            Label l = new Label(getParentComposite(), flags);
            l.setText(mText);
            widget = l;
        }
        return widget;
    }

    @Override
    public String getText() {
        if (mComponent instanceof Label){
            return ((Label)mComponent).getText();
        }
        if (mComponent instanceof CLabel)           
            return ((CLabel) mComponent).getText();
        return mText;
    }

    @Override
    public void setText (String text) {
        mText = text;
        if (mComponent != null)
            if (mComponent instanceof CLabel)
                ((CLabel) mComponent).setText(text);
            else if (mComponent instanceof Label)
                ((Label) mComponent).setText(text);
    }

    
    @Override
    public void setBorder(int border) {
        mBorder = border;
    }

    @Override
    public void setWrap (boolean v) {
        mWrap = v;       
    }

    @Override
    public void setHorizontalAlignment (int align) {
        super.setHorizontalAlignment(align);
        if (align == IComponent.BEGINNING){
            mAlignFlags = SWT.BEGINNING;
        }
        else if (align == IComponent.CENTER){
            mAlignFlags = SWT.CENTER;
        }
        else if (align == IComponent.END){
            mAlignFlags = SWT.END;
        }
    }
}
