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



/**
 * An object that defines an attribute definition
 * of a meta-XML program.
 *
 * @author David Pickens; July 1999
 */
public interface IAttributeDef {
	/**
	 * Return name of attribute
	 */
    public String getName();

	/**
	 * Return the type of the attribute
	 */
    public int getType();

    public static final int STRING = 0;
    public static final int INT = 1;
    public static final int BOOLEAN = 2;
    public static final int FLOAT = 3;
    public static final int OBJECT = 4;
    public static final int ACTION = 5; // Defer evaluation until action to be performed
    public static final int LIST = 6; // A list of strings

	/**
	 * An attribute that is a "property" is assumed to be
	 * the name a property of the corresponding Builder class.
	 */
    public boolean isProperty();

	/**
	 * If an aggregate, this means that the attribute corresponds
	 * to a property that contains 0 or more instances of an object.
	 * Note: if isAggregate is true, then the type must be OBJECT.
	 */
    public boolean isAggregate();

	/**
	 * If this attribute is an object, return the "binding" that
	 * defines the object. The result of this method is null
	 * if the type is not OBJECT.
	 */
    public IBinding getBinding();

	/**
	 * If true, the attribute must be present.
	 */
    public boolean isRequired();

       /**
	* Determine whether or not we must delay evaluation.
	* If false, we evaluate it immediately.
	*/
    public boolean delayEvaluation();
    }
