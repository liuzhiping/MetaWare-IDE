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

import java.awt.Dimension;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

import com.arc.widgets.IContainer;
import com.arc.widgets.ILayoutManager;

/**
 * @author David Pickens
 */
class SWTLayout extends Layout {

    private ILayoutManager mLayout;
    private IContainer mContainer;

    SWTLayout(ILayoutManager layout, IContainer c){
        mLayout = layout;
        mContainer = c;
    }
    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite, int, int, boolean)
     */
    @Override
    protected Point computeSize(Composite composite, int wHint, int hHint,
            boolean flushCache) {
        Dimension d = mLayout.computeSize(mContainer,wHint,hHint);
        return new Point(d.width,d.height);
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite, boolean)
     */
    @Override
    protected void layout(Composite composite, boolean flushCache) {
        mLayout.layout(mContainer);
    }

}
