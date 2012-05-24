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

import org.xml.sax.SAXException;

import com.arc.mw.util.Caller;

/**
 * A utility class for setting property of a Builder object from an attribute
 * definition.
 * 
 * @author David Pickens
 * @version July 19, 1999
 */
class PropertySetter {
	/**
	 * Given an attribute definition and an object value, do whatever is
	 * necessary to assign the value.
	 * <p>
	 * If it is a property and is an aggregate, we will call "add
	 * <em>attribute</em>(object)" on it. If not an attribute, we well call "set
	 * <em>Attribute</em>(object)".
	 * <p>
	 * if not a property, we mere call "addAttribute" or "setAttribute"
	 * 
	 */
	static void setProperty(IBuilder builder, IAttributeDef attr, Object value)
			throws NoSuchMethodException, InvocationTargetException,
			IllegalAccessException, SAXException {
		String setter;
		if (attr.isAggregate())
			setter = "add" + capitalize(attr.getName());
		else
			setter = "set" + capitalize(attr.getName());
		try {
			Caller.invoke(builder, setter, new Object[] { value });
		} catch (InvocationTargetException x) {
			Throwable t = x.getTargetException();
			if (t instanceof SAXException)
				throw (SAXException) t;
			if (t instanceof RuntimeException)
				throw (RuntimeException) t;
			throw x;
		}
	}

	/**
	 * Set a property or attribute that is a String, integer, float or boolean
	 */
	static void setProperty(IBuilder builder, IAttributeDef attr, String value)
			throws NoSuchMethodException, InvocationTargetException,
			IllegalAccessException, SAXException {
		Object ovalue = value;
		switch (attr.getType()) {
		case IAttributeDef.INT:
			ovalue = new Integer(value);
			break;
		case IAttributeDef.FLOAT:
			ovalue = new Float(value);
			break;
		case IAttributeDef.BOOLEAN:
			ovalue = new Boolean(value);
			break;
		default:
			break;
		}
		setProperty(builder, attr, ovalue);
	}

	/**
	 * Capitolize the first letter of a name.
	 */
	static String capitalize(String name) {
		StringBuffer buf = new StringBuffer(name.length());
		buf.append(Character.toUpperCase(name.charAt(0)));
		buf.append(name.substring(1));
		return buf.toString();
	}
}
