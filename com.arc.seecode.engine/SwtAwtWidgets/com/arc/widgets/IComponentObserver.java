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

/**
 * Observer for generic components.
 * 
 */
public interface IComponentObserver {
    /**
     * Called when a component's size changes.
     * @param component the component whose size has changed.
     */
    void onSizeChange(IComponent component);
}
