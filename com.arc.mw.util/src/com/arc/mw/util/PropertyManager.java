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
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.HashMap;

/**
 * An object that manages bound properties that are dynamic.
 * 
 * @author David Pickens
 * @version May 20, 2002
 */
public class PropertyManager implements IPropertyManager {
    public PropertyManager() {
    }

    /**
     * Add listener for a specific property.
     */
    @Override
    public void addPropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        mPropertySupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Remove listener for a specific property.
     */
    @Override
    public void removePropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        mPropertySupport.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * Add listener that is invoked when any property is changed.
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        mPropertySupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove listener that is invoked when any property is changed.
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        mPropertySupport.removePropertyChangeListener(listener);
    }

    /**
     * Return value of a property.
     * 
     * @param name
     *            the name of the property
     * @return the value corresponding to property name, or null.
     */
    @Override
    public Object getProperty(String name) {
        return mMap.get(name);
    }

    /**
     * Set value of a property.
     * 
     * @param name
     *            the name of the property
     * @param value
     *            the value to be assigned to the property.
     * @exception PropertyVetoException
     *                value is not valid.
     */
    @Override
    public void setProperty(String name, Object value)
            throws PropertyVetoException {
        Object old = mMap.get(name);
        mMap.put(name, value);
        if (old != value)
            mPropertySupport.firePropertyChange(name, old, value);
    }

    /**
     * Get collection of all property names. Each element of the collection is a
     * string.
     */
    @Override
    public Collection<String> getPropertyNames() {
        return mMap.keySet();
    }

    private HashMap<String, Object> mMap = new HashMap<String, Object>();

    private PropertyChangeSupport mPropertySupport = new PropertyChangeSupport(
            this);
}
