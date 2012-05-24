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
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Widget;

import com.arc.widgets.IContainer;
import com.arc.widgets.IProgress;

/**
 * @author David Pickens
 */
class SWTProgress extends Component implements IProgress{

    private boolean mVertical;

    /**
     * @param parent
     * @param mapper
     */
    public SWTProgress(IContainer parent, IComponentMapper mapper, boolean vertical) {
        super(parent, mapper);
        mVertical = vertical;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.swt.Component#instantiate()
     */
    @Override
    protected Widget instantiate() {
        ProgressBar bar = new ProgressBar(this.getParentComposite(),SWT.SMOOTH|(mVertical?SWT.VERTICAL:SWT.HORIZONTAL));
        bar.setMinimum(0);
        bar.setMaximum(100);
        return bar;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IProgress#setProgress(int)
     */
    @Override
    public void setProgress(int percentage) {
        ProgressBar bar = (ProgressBar)getComponent();
        bar.setSelection(percentage);       
    }
}
