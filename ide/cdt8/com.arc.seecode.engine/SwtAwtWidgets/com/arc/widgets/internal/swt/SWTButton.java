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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Widget;

import com.arc.widgets.IContainer;

/**
 * A builder for an SWT button.
 */
class SWTButton extends AbstractButton {
    SWTButton(IContainer parent, int style, IComponentMapper mapper) {
        super(parent, mapper, style);
    }

    @Override
    protected Widget instantiate() {
        Button b = new Button(getParentComposite(), mStyle);
        if (mText != null)
            b.setText(mText);
        if (mImage != null)
            b.setImage((Image) mImage.getObject());
        b.setSelection(mSelected);
        return b;
    }
}
