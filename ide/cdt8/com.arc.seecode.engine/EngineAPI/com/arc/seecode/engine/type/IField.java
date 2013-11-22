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
 * An instance of this interface represents a field
 * of a struct, union, or class. It is referenced from
 * {@link IType#getFields()}.
 * @author David Pickens
 */
public interface IField {
	/**
	 * Return the type in which this field resides.
	 * It should be a struct, union, or class.
	 * @return the parent type.
	 */
	public IType getParentType();
	/**
	 * Return the name of this field.
	 * @return the name of this field.
	 */
	public String getName();
	/**
	 * Return the byte-offset of this field relative
	 * to its parent structure. If this is a virtual
	 * method, it will be the virtual-table offset.
	 * If this is a non-virtual method, then the
	 * result of this value is undefined (and will likely
	 * be zero).
	 * @return the byte offset of this field, or zero.
	 */
	public int getOffset();
	
	/**
	 * Return the type of this field.
	 * @return the type of this field.
	 */
	public IType getType();
	
	/**
	 * If this field is a bit field (meaning that the
	 * type is an integer), return the bit position
	 * relative to the {@linkplain #getOffset() byte offset}
	 * where the field begins.
	 * @return the bit offset relative to the byte offset.
	 */
	public int getBitOffset();
	
	/**
	 * If this is a bitfield, the return its bit length.
	 * If zero, this is not a bit field.
	 * @return the bitfield length if this is a bitfield;
	 * otherwise, returns 0.
	 */
	public int getBitLength();
	
	/**
	 * Return whether or not this is a virtual
	 * method. If so, then {@link #getOffset()} is
	 * the virtual table offset.
	 * @return whether or not this is a virtual method.
	 */
	public boolean isVirtual();
	
	/**
	 * Return whether or not this is a pure virtual
	 * method (i.e., a virtual method with no defined
	 * body).
	 * @return whether or not this is a pure virtual method.
	 */
	public boolean isPureVirtual();
	
	/**
	 * Return whether or not this field is "static" in
	 * the C++ sense.
	 * @return whether or not this field is "static" in
	 * the C++ sense.
	 */
	public boolean isStatic();
	
	/**
	 * Return whether or not this field is private, in
	 * the C++ sense.
	 * @return whether or not this field is private.
	 */
	public boolean isPrivate();
	
	/**
	 * Return whether or not this field is protected, in
	 * the C++ sense.
	 * @return whether or not this field is protected.
	 */
	public boolean isProtected();
	
	
}
