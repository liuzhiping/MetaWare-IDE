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
package com.arc.xml;

import java.lang.reflect.InvocationTargetException;

/**
 * Interface to a class that can instantiate
 * a class that implements the IBuilder interface.
 * We make it an interface because actual implementations
 * may need to pass esoteric arguments to the constructor.
 * 
 * @author David Pickens
 * @version July 29, 1999
 */
public interface IBuilderInstantiator {
    public IBuilder instantiate(Class<? extends IBuilder> klass)
	throws IllegalAccessException,
		ClassCastException,
		InvocationTargetException,
		NoSuchMethodException,
		InstantiationException;
    }
