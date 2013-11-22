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
package com.arc.seecode.display;

import com.arc.widgets.DefaultToolBarBuilder;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;
import com.arc.widgets.IToolBarBuilder;

public class DefaultToolBarBuilderFactory implements IToolBarBuilderFactory {

    private IComponentFactory mWidgetFactory;
    
    public DefaultToolBarBuilderFactory(IComponentFactory widgetFactory){
        if (widgetFactory == null)
            throw new IllegalArgumentException("factory is null");
        mWidgetFactory = widgetFactory;
        
    }
    @Override
    public IToolBarBuilder createToolBarBuilder(IContainer parent) {
        return new DefaultToolBarBuilder(mWidgetFactory,parent);
    }
}
