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
package com.arc.cdt.debug.seecode.ui.display;

import com.arc.cdt.debug.seecode.ui.internal.display.ViewToolBarBuilder;
import com.arc.seecode.display.IToolBarBuilderFactory;
import com.arc.widgets.IContainer;

/**
 * A Toolbar builder factory that produces toolbar builder that populates
 * the Eclipse View panel toolbar.
 */
class ViewToolBarBuilderFactory implements IToolBarBuilderFactory {

    @Override
    public IViewToolBarBuilder createToolBarBuilder(IContainer parent) {
        return new ViewToolBarBuilder();
    }

}
