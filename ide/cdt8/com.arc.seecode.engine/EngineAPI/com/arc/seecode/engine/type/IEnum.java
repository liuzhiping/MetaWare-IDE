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
package com.arc.seecode.engine.type;

/**
 * An enumerated type designator.
 * @author David Pickens
 */
public interface IEnum {
	/**
	 * Return the corresponding enum type.
	 * @return the corresponding enum type.
	 */
	IType getType();
	/**
	 * Return the name of the enum ID.
	 * @return the name of this enum ID.
	 */
	String getName();
	/**
	 * The value of this enum id.
	 * @return the value of this enum id.
	 */
	long getValue();
}
