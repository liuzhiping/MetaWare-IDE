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

import java.beans.PropertyVetoException;

/**
 * An interface for setting and retrieving property values.
 *
 * @author David Pickens
 * @version May 15, 2002
 */
public interface IPropertyMap {
    /**
     * Return value of a property.
     * @param name the name of the property
     * @return the value corresponding to property name, or null.
     */
    public Object getProperty(String name);
    /**
     * Set value of a property.
     * @param name the name of the property
     * @param value the value to be assigned to the property.
     * @exception PropertyVetoException value is not valid.
     */
    public void setProperty(String name, Object value) throws PropertyVetoException;
    }
