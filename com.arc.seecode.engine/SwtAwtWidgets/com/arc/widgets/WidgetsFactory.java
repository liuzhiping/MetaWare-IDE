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
package com.arc.widgets;

import org.eclipse.swt.widgets.Display;


/**
 * Class for creating component factories.
 * @author David Pickens
 */
public class WidgetsFactory {
    /**
     * Create swing-based component factory.
     * @return swing-based component factory.
     */
    public static IComponentFactory createSwing(){
        return new com.arc.widgets.internal.swing.ComponentFactory();
    }
    
    /**
     * Create SWT-based component factory.
     * @return SWT-based component factory.
     */ 
    public static IComponentFactory createSWT(){
        return new com.arc.widgets.internal.swt.ComponentFactory();
    }
    
    /**
     * Create SWT-based component factory from the given Dislay object.
     * @return SWT-based component factory.
     */ 
    public static IComponentFactory createSWT(Display display){
        return new com.arc.widgets.internal.swt.ComponentFactory(display);
    }

}
