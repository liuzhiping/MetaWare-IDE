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

import com.arc.mw.util.Instantiator;

/**
 * A default implementation of of IBuilderInstantiator that
 * creates a Builder for an XML node.
 * @author David Pickens
 * @version  July 29, 1999
 */
public class BuilderInstantiator implements IBuilderInstantiator {
    @Override
    public IBuilder instantiate(Class<? extends IBuilder> klass)
	throws IllegalAccessException,
		ClassCastException,
		InstantiationException,
		InvocationTargetException,
		NoSuchMethodException
	{
	return (IBuilder) Instantiator.instantiate(klass,getArguments());
	}

    private static Object[] nullArgs = new Object[0];
    /**
     * Overridden by subclasses to fill in the constructor arguments
     */
    protected Object[] getArguments() {
	return nullArgs;
	}
    }
