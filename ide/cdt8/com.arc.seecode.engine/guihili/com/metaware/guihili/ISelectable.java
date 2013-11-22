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

import java.util.List;

/**
 * An interface from which we can get properties.
 * See {@link LispFunctions#do_select(List,IEvaluator,IEnvironment) LispFunction.do_select()}.
 *
 * @author David Pickens
 * @version May 15, 2002
 */
public interface ISelectable {
    /**
     * return a property associated with this object, or null if
     * there is no such property.
     * @param name the name of the property to retrieve.
     * @return the value of the property, or null.
     */
    public Object getSelection(String name);
    /**
     * Set a property to a value.
     * @param name the name of the property to set.
     * @param value the value to set the property to.
     */
    public void putSelection(String name, Object value);
    }
