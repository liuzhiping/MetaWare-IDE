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

import com.arc.seecode.engine.display.IDisplayCreator;
import com.arc.seecode.internal.display.SeeCodeTextViewerFactory;
import com.arc.widgets.IComponentFactory;

/**
 * @author David Pickens
 */
public class Factory {
    /**
     * Returns a factory that builds SeeCode displays. Under Eclipse,
     * we supply a toolbar builder that places items in the View's toolbar.
     * @param widgetFactory the widget factory to create 
     * GUI widgets.
     * @param tbBuilderFactory an object from which a toolbar builder can be
     * created, or <code>null</code> if an appropriate default is to be used.
     * @param threadInfo a callback to get list of threads is needs to create
     * a widget to select one.
     * @param scDir the path to the debugger installation (e.g. "C:/arc/metaware/arc").
     * @param displayCreator a callback for activating or creating a display of a particular kind.
     * @return the text display view builder.
     */
    public static ISeeCodeTextViewerFactory createTextDisplayViewFactory(IComponentFactory widgetFactory, IToolBarBuilderFactory tbBuilderFactory,
        IContext threadInfo, String scDir, IDisplayCreator displayCreator){
        if (tbBuilderFactory == null){
            tbBuilderFactory = new DefaultToolBarBuilderFactory(widgetFactory);
        }
        return new SeeCodeTextViewerFactory(widgetFactory,tbBuilderFactory, threadInfo,scDir,displayCreator);
    }
}
