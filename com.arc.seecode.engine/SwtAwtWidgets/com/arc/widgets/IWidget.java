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
 * A common super-interface for components
 * and "items".
 * @author David Pickens
 */
public interface IWidget {
    /**
     * Set a name for the component.
     * @param name the name.
     */
    void setName(String name);
    /**
     * Return the name of the component.
     */
    String getName();

    /**
     * Set "enbled" property.
     */
    void setEnabled(boolean v);
    /**
     * Return whether or not the underlying component is enabled.
     */
    boolean isEnabled();
    
    /**
     * Set tooltip text for the underlying GUI component.
     * @param tip the tooltip text.
     */
    void setToolTipText(String tip);
    String getToolTipText();
}
