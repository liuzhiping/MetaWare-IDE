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
import java.util.Map;

/**
 * Interface for a <em>binding definition</em>
 * A binding definition defines a tag node name and the attributes
 * that are associated with it.
 * <P>
 * @author David Pickens; July 29,1999
 */
public interface IBinding {
	/**
	 * Return the tag name for this binding.
	 */
    public String getTagName(); 

	/**
	 * Return an attribute definition named "name".
	 * Returns null if not found.
	 */
    public IAttributeDef getAttribute(String name);

	/**
	 *Get the attribute map.
	 */
    public Map<String,IAttributeDef> getAttributes();
	/**
	 *Get the sub-binding map.
	 */
    public Map<String,IBinding> getBindings();

	/**
	 * Return a binding for a node name that may appear as
	 * a child of this node.
	 */
    public IBinding getBinding(String tagname);

	/**
	 * Return the parent.
	 * This binding is one of the members of the binding map of the
	 * parent.
	 */
    public IBinding getParent();

	/**
	 * Get another binding that this is a subclass of.
	 */
    public IBinding getBase();

	/**
	 * Return the builder
	 * @exception ClassNotFoundException can't find associated class.
	 * @exception NoSuchMethodException can't find constructor.
	 */
    public IBuilder getBuilder() 
		throws ClassNotFoundException,
			NoSuchMethodException,
			InstantiationException,
			IllegalAccessException,
			IllegalArgumentException,
			InvocationTargetException,
			SecurityException;
    }
