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

import com.arc.seecode.engine.Value;
import com.arc.seecode.engine.Variable;

/**
 * Representation of a type. A {@link Variable}and a {@link Value}can have a
 * type.
 * <P>
 * 
 * @author David Pickens
 */
public interface IType {

    /**
     * A kind to denote a void type. This is one of the possible values returned
     * from {@link #getKind()}.
     */
    public final int VOID = 0;

    /**
     * A kind to denote an integer type.
     * <P>
     * The methods {@link #getLowRange()}and {@link #getHighRange()}can be called to
     * get the range of the integer.
     * <P>
     * This is one of the possible values returned from {@link #getKind()}.
     */
    public final int INTEGER = 1;

    /**
     * A kind to denote a floating-point type. This is one of the possible
     * values returned from {@link #getKind()}.
     */
    public final int FLOAT = 2;

    /**
     * A kind to denote a pointer. The base type is obtained by invoking
     * {@link #getBaseType()}. This is one of the possible values returned from
     * {@link #getKind()}.
     */
    public final int POINTER = 3;

    /**
     * A kind to denote a C++-style reference. The base type is obtained by
     * invoking {@link #getBaseType()}. This is one of the possible values
     * returned from {@link #getKind()}.
     */
    public final int REF = 4;

    /**
     * A kind to denote an array. The element type is obtained by invoking
     * {@link #getBaseType()}. The index type is obtained by calling
     * {@link #getIndexType()}. This is one of the possible values returned
     * from {@link #getKind()}.
     */
    public final int ARRAY = 5;

    /**
     * A kind to denote a C-style struct.
     * <P>
     * The fields are obtained by invoking {@link #getFields()}.
     * <P>
     * This is one of the possible values returned from {@link #getKind()}.
     */
    public final int STRUCT = 6;

    /**
     * A kind to denote a C-style union.
     * <P>
     * The fields are obtained by invoking {@link #getFields()}.
     * <P>
     * This is one of the possible values returned from {@link #getKind()}.
     */
    public final int UNION = 7;

    /**
     * A kind to denote a C++-style class.
     * <P>
     * The fields are obtained by invoking {@link #getFields()}.
     * <P>
     * This is one of the possible values returned from {@link #getKind()}.
     */
    public final int CLASS = 8;

    /**
     * A kind to denote a function or method.
     * <P>
     * The return type is obtained by calling {@link #getBaseType()}. The
     * argument types are obtained by calling {@link #getParameterTypes()}.
     * <P>
     * This is one of the possible values returned from {@link #getKind()}.
     */
    public final int FUNCTION = 9;

    /**
     * A kind to denote an enum type.
     * <P>
     * To obtain the enumeration, invoke {@link #getEnums()}.
     * <P>
     * This is one of the possible values returned from {@link #getKind()}.
     */
    public final int ENUM = 10;

    /**
     * Return a manifest constant value to identify the kind of type this is.
     * 
     * @return the kind of type.
     */
    public int getKind();

    /**
     * Return the name of the type (e.g., "int").
     * 
     * @return the name of the type.
     */
    public String getName();

    /**
     * Returns the size of an instance of this type.
     * 
     * @return the size (in bytes) of an instance of this type.
     */
    public int getSize();

    /**
     * Return the element type of an array, or the
     * base type of an pointer or reference.
     * @return the element type or null.
     * @pre getKind() == ARRAY || getKind()==POINTER }}
     * getKind() == REF || getKind()==FUNCTION
     */
    public IType getBaseType();

    /**
     * Return the index type of an array, which will be
     * an integer type with a range.
     * @return the index type of an array.
     * @pre getKind() == ARRAY
     */
    public IType getIndexType();

    /**
     * Return the fields of C-style struct/union, or
     * a C++-style class.
     * @return the fields of a struct or class.
     * @pre getKind() == STRUCT || getKind() == UNION |
     * getKind() == CLASS
     */
    public IField[] getFields();

    /**
     * Return the parameter types of a function.
     * @return the parameter type of a function.
     * @pre getKind() == FUNCTION
     */
    public IType[] getParameterTypes();

    /**
     * If this is a C++-like class, return the
     * base classes. If there are no base classes, then
     * a zero-length array will be returned.
     * @return the base classes of a C++ class.
     * @pre getKind() == CLASS
     */
    public IType[] getBaseClasses();

    /**
     * Return the enumerations for an enumerated type.
     * @return the enumerations of an enumerated type.
     * @pre getKind() == ENUM
     */
    public IEnum[] getEnums();

    /**
     * Return the lowest valid value of this integer
     * or enum type, or the lowest index of an array.
     * @return the lowest valid value.
     * @pre getKind() == INTEGER || getKind() == ENUM ||
     * getKind() == ARRAY
     */
    public long getLowRange();

    /**
     * Return the hightest valid value of this integer
     * or enum type or the highest index of an array.
     * @return the lowest valid value.
     * @pre getKind() == INTEGER || getKind() == ENUM ||
     * getKind() == ARRAY
     */
    public long getHighRange();

    /**
     * Return whether or not this integer type is unsigned.
     * 
     * @return whether or not this integer type is unsigned.
     */
    public boolean isUnsigned();
    
    /**
     * Return the dimension of an array type.
     * @return the dimension of an array type.
     * @pre getKind() == ARRAY
     */
    public int getDimension();

    /**
     * Return whether or not this type is equivalent to another type.
     * 
     * @param type
     *            the type being compared with.
     * @return true if this type is equivalent to the argument type.
     */
    public boolean equals(IType type);

}
