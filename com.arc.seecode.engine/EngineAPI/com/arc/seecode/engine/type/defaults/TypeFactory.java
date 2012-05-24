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
package com.arc.seecode.engine.type.defaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.arc.seecode.engine.type.IType;
import com.arc.seecode.engine.type.ITypeFactory;

/**
 * A type factory suitable for command processor. The type factory for the
 * Eclipse GUI exists in the Eclipse plugin.
 * 
 * @author David Pickens
 */
public class TypeFactory implements ITypeFactory {

    private IType mVoidType = null;

    private List<IType> mIntTypes = new ArrayList<IType>();
    private List<IType> mFloatTypes = new ArrayList<IType>();
    private Map<IType,IType> mPointerTypes = new HashMap<IType,IType>();
    private Map<IType,IType> mRefTypes = new HashMap<IType,IType>();

    /**
     *  
     */
    public TypeFactory() {
        super();
    }

    /* override */
    @Override
    public IType createVoidType(String name) {
        if (mVoidType == null) {
            mVoidType = new VoidType("void");
        }
        if (name == null || name.equals(mVoidType.getName())) return mVoidType;
        return new VoidType(name);
    }

    /* override */
@Override
public IType createInteger(String name, int size, boolean isUnsigned) {
        int cnt = mIntTypes.size();
        for (int i = 0; i < cnt; i++){
            IType t = mIntTypes.get(i);
            if (t.getSize() == size && t.isUnsigned() == isUnsigned &&
                    (name == null || name.equals(t.getName())))
                    return t;
        }
        IType t = new IntegerType(name,size,isUnsigned);
        mIntTypes.add(t);
        return t;
    }
    /* override */
    @Override
    public IType createFloatingPoint(String name, int size) {
        int cnt = mFloatTypes.size();
        for (int i = 0; i < cnt; i++){
            IType t = mFloatTypes.get(i);
            if (t.getSize() == size && 
                    (name == null || name.equals(t.getName())))
                    return t;
        }
        IType t = new FloatType(name,size);
        mFloatTypes.add(t);
        return t;
    }

    /* override */
    @Override
    public IType createPointer(String name, IType base, int size) {
        IType t = mPointerTypes.get(base);
        if (t != null && (name == null || name.equals(t.getName())))
            return t;
        t = new PointerType(name,base,size);
        mPointerTypes.put(base,t);
        return t;
    }

    /* override */
    @Override
    public IType createArray(String name, IType base, int dimension) {
        return new ArrayType(name,base,dimension);
    }

    /* override */
    @Override
    public IType createRef(String name, IType base, int size) {
        IType t = mRefTypes.get(base);
        if (t != null && (name == null || name.equals(t.getName())))
            return t;
        t = new RefType(name,base,size);
        mRefTypes.put(base,t);
        return t;
    }

    @Override
    public IType createEnum(String name, int size) {
        return new EnumType(name,size);
    }

    /* override */
    @Override
    public IType createStruct(String name, int size) {
        return new StructType(name,size,IType.STRUCT);
    }

    /* override */
    @Override
    public IType createUnion(String name, int size) {
        return new StructType(name,size,IType.UNION);
    }

    /* override */
    @Override
    public IType createClass(String name, int size) {
        return new StructType(name,size,IType.CLASS);
    }

}
