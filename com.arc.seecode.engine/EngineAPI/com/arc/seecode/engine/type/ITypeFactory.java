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
 * A factory for constructing types. We rely on the UI to provide the
 * implementation so that the engine can construct the types in the domain of
 * the UI.
 * <P>
 * For each method in this interface, the resulting object may be cached so that
 * the same one is returned by subsequent calls with the same arguments.
 * <P>
 * The UI may not need such detail. For example, it may need to know that a type
 * is a struct, but need not need the fields enumerated.
 * 
 * @author David Pickens
 */
public interface ITypeFactory {

    /**
     * Return the void type.
     * @param name
     * @return the void type.
     * @post $result.getKind() == IType.VOID
     */
    public IType createVoidType(String name);

    /**
     * Get the primitive type that denotes an
     * integer of a particular size.

     * @param name
     * @param size
     * @param isUnsigned true if type is unsigned.
     * @return a primitive integer type.
     * @post $result.getKind() == IType.INTEGER &&
     *       $result.getSize() == size &&
     * 		 $result.isUnsigned() == isUnsigned
     */
    public IType createInteger(String name, int size, boolean isUnsigned);

    /**
     * Get the primitive type that denotes 
     * floating point type of a particular size.
     * @param name
     * @param size
     * @return a primitive integer type.
     * @post $result.getKind() == IType.IFLOAT &&
     *       $result.getSize() == size
     */
    public IType createFloatingPoint(String name, int size);

    /**
     * Create a pointer type.
     * @param name the name or <code>null<code>.
     * @param base the base type.
     * @param size the size in bytes of the resulting pointer.
     * @pre base != null
     * @post $result.getKind() == IType.POINTER &&
     *   $result.getName() == name && $result.getBaseType().equals(base)
     */
    public IType createPointer(String name, IType base, int size);
    
    /**
     * Create an array type.
     * @param name the name or <code>null<code>.
     * @param base the base type.
     * @param dimension the number of elements.
     * @pre base != null
     * @post $result.getKind() == IType.ARRAY &&
     *   $result.getName().equals(name) && $result.getBaseType().equals(base)
     */
    public IType createArray(String name, IType base, int dimension);

    /**
     * Create a C++-style ref type.
     * @param name the name or <code>null<code>.
     * @param base the base type.
     * @param size the size in bytes of the resulting pointer.
     * @pre base != null
     * @post $result.getKind() == IType.POINTER &&
     *   $result.getName() == name && $result.getBaseType().equals(base)
     */
    public IType createRef(String name, IType base, int size);

    /**
     * Create an enumerated type, whose enumerations may
     * be defined subsequently by invocations of
     * {#link #defineEnum(IType,String,long)}.
     * (It is optional to define the enum IDs).
     * @param name the name of the type or <code>null<code>
     * if the type has no name.
     * @param size the size in bytes.
     * @return an enumerated type.
     * @post $result.getKind() == IType.ENUM &&
     * $result.getSize() == size && $result.getName() == name
     */
    public IType createEnum(String name, int size);

    //	/**
    //	 * Define an enumerated constant that is part of
    //	 * an enumerated type.
    //	 * @param enumType the enumerated type to which the
    //	 * constant belongs.
    //	 * @param name the name of the enumerated constant.
    //	 * @param value the value.
    //	 * @return the enumerated constant id.
    //	 * @post $result.getType() == enumType &&
    //	 * $result.getName() == name &&
    //	 * $result.getValue() == value
    //	 */
    //	public IEnum defineEnum(IType enumType, String name, long value);

    /**
     * Create a C-style struct type of a particular size.
     * <p>
     * Fields will be defined by subsequent calls to {#link
     * #defineField(IType,String,IType,int)} or {#link
     * #defineBitField(IType,String,IType,int,int,int)}.
     * 
     * @param name
     *            name of struct.
     * @param size
     *            size in bytes.
     * @return the struct type.
     */
    public IType createStruct(String name, int size);

    /**
     * Create a C-style union type of a particular size.
     * <p>
     * Fields will be defined by subsequent calls to {#link
     * #defineField(IType,String,IType,int)} or {#link
     * #defineBitField(IType,String,IType,int,int,int)}.
     * 
     * @param name
     *            name of union.
     * @param size
     *            size in bytes.
     * @return the struct type.
     */
    public IType createUnion(String name, int size);

    //	/**
    //	 * Define a field that is part of a struct, union, or
    //	 * class.
    //	 * @param structType a struct, union, or class type.
    //	 * @param fieldName the name of the field.
    //	 * @param fieldType the type of the field.
    //	 * @param fieldOff the byte-offset of the field.
    //	 * @return the new field reference.
    //	 */
    //	public IField defineField(IType structType, String fieldName, IType
    // fieldType,
    //			int fieldOff);
    //	
    //	/**
    //	 * Define a bit field that is part of a struct, union, or
    //	 * class.
    //	 * @param structType a struct, union, or class type.
    //	 * @param fieldName the name of the field.
    //	 * @param fieldType the type of the field (an integer).
    //	 * @param fieldOff the byte offset to the field.
    //	 * @param bitOff the bit offset from the byte offset.
    //	 * @param bitLen the bit length.
    //	 * @return the new field reference.
    //	 */
    //	public IField defineBitField(IType structType, String fieldName, IType
    // fieldType,
    //			int fieldOff, int bitOff, int bitLen);

    /**
     * Create a C++-style class type of a particular size.
     * <p>
     * Fields will be defined by subsequent calls to {#link
     * #defineField(IType,String,IType,int)} or {#link
     * #defineBitField(IType,String,IType,int,int,int)}.
     * 
     * @param name
     *            name of struct.
     * @param size
     *            size in bytes.
     * @return the struct type.
     */
    public IType createClass(String name, int size);

}
