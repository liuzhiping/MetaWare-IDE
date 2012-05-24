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
package com.arc.mw.util;

import java.beans.PropertyChangeListener;
import java.util.Collection;

/**
 * An interface for being able to listen for property changes.
 *
 * @author David Pickens
 * @version May 20, 2002
 */
public interface IPropertyManager extends IPropertyMap {
    /**
     * Add listener for a specific property.
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);
    /**
     * Remove listener for a specific property.
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);
    /**
     * Add listener that is invoked when any property is changed.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);
    /**
     * Remove listener that is invoked when any property is changed.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Get collection of all property names.
     * Each element of the collection is a string.
     */
    public Collection<String> getPropertyNames();
    }
