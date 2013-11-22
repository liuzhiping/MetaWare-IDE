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

import org.eclipse.ui.IViewPart;

import com.arc.widgets.IToolBarBuilder;

public interface IViewToolBarBuilder extends IToolBarBuilder {
    void populateView(IViewPart viewPart);
}
