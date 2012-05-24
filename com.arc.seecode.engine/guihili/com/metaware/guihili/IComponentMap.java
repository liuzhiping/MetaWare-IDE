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
package com.metaware.guihili;


import com.arc.widgets.IComponent;


/**
 * Maps component names to components. Used by {@link LispFunctions}
 * @author David Pickens
 * @version May 13, 2002
 */
public interface IComponentMap {

    /**
     * Get the component with the given name.
     * @param name name of component.
     * @return the component with the given name.
     */
    public IComponent getComponent (String name);
}
