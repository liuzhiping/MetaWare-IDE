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
package com.arc.cdt.debug.seecode.internal.core.cdi.types;

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;

import com.arc.seecode.engine.type.IType;
import com.arc.seecode.engine.type.ITypeFactory;

/**
 * This factory is passed directly to the SeeCode engine to construct types that
 * apply to variables and values.
 * <P>
 * The types also happen to match the CDI-defined interfaces. Thus, we can
 * convert between the two domains by a simple cast!
 * 
 * @author David Pickens
 */
public class TypeFactory implements ITypeFactory {

    private ICDITarget mTarget;

    private IType mVoidType = null;
    
    private IType mFloatType = null;
    private IType mDoubleType = null;
    private IType mCharType = null;
    private IType mUCharType = null;
    private IType mSCharType = null;
    private IType mShortType = null;
    private IType mUShortType = null;
    private IType mSShortType = null;
    private IType mIntType = null;
    private IType mUIntType = null;
    private IType mLongType = null;
    private IType mULongType = null;
    private IType mCharPtr = null;

    public TypeFactory() {
      
    }
    
    public void setTarget(ICDITarget target){
        mTarget = target;
    }

    /* override */
    @Override
    public IType createVoidType(String name) {
        if (mVoidType == null) {
            mVoidType = new VoidType(mTarget);
        }
        return mVoidType;
    }

    /* override */
    @Override
    public IType createInteger(String name, int size, boolean isUnsigned) {
        if (size <= 0 || size > 8)
                throw new IllegalArgumentException("bad size: " + size);
        //Check of "char"
        if (size == 1){
            if (name == null) name = "char";
            if ("char".equals(name)){
                if (mCharType == null){
                    mCharType = new CharType(name,isUnsigned,mTarget);
                }
                return mCharType;
            }
            if ("unsigned char".equals(name) || "uchar".equals(name)){
                if (mUCharType == null){
                    mUCharType = new CharType(name,true,mTarget);
                }
                return mUCharType;
            }
            if ("signed char".equals(name) || "schar".equals(name)){
                if (mSCharType == null){
                    mSCharType = new CharType(name,false,mTarget);
                }
                return mSCharType;
            }
            return new CharType(name,isUnsigned,mTarget);
        }
        if (size == 2){
            if (name == null) name = "short";
            if ("short".equals(name) || "short int".equals(name)){
                if (mShortType == null){
                    mShortType = new ShortType(name,isUnsigned,mTarget);
                }
                return mShortType;
            }
            if ("unsigned short".equals(name) || "unsigned short int".equals(name)){
                if (mUShortType == null){
                    mUShortType = new ShortType(name,isUnsigned,mTarget);
                }
                return mUShortType;
            }
            if ("signed short".equals(name) || "signed short int".equals(name)){
                if (mSShortType == null){
                    mSShortType = new ShortType(name,isUnsigned,mTarget);
                }
                return mSShortType;
            }
            return new ShortType(name,isUnsigned,mTarget);
            
        }
        if (size == 4){
            if (name == null) name = "int";
            if ("int".equals(name)){
                if (mIntType == null){
                    mIntType = new IntType(name,isUnsigned,mTarget);
                }
                return mIntType;
            }
            if ("unsigned".equals(name) || "unsigned int".equals(name)){
                if (mUIntType == null){
                    mUIntType = new IntType(name,isUnsigned,mTarget);
                }
                return mUIntType;
            }
            if ("long".equals(name) || "long int".equals(name)){
                if (mLongType == null){
                    mLongType = new LongType(name,isUnsigned,mTarget);
                }
                return mLongType;
            }
            if ("unsigned long".equals(name)){
                if (mULongType == null){
                    mULongType = new LongType(name,isUnsigned,mTarget);
                }
                return mULongType;
            }
            return new LongType(name,isUnsigned,mTarget);           
        }
        if (size != 8)
            throw new IllegalArgumentException("Bad integer size: " + size);
        if (name == null) name = "long";
        return new LongLongType(name,isUnsigned,mTarget);

    }

    /* override */
    @Override
    public IType createFloatingPoint(String name, int size) {
        IType result = null;
        if (size == 4) {
            if (name == null) name = "float";
            if (mFloatType == null) {
                mFloatType = new FloatType(name,mTarget);
            }
            result = mFloatType;
        } else if (size == 8) {
            if (name == null) name = "double";
            if (mDoubleType == null) {
                mDoubleType = new DoubleType(name, mTarget);
            }
            result = mDoubleType;
        }
        if (result == null || name != null && !name.equals(result.getName())){
            result = size == 4?(IType)new FloatType(name,mTarget):
                    size==8?(IType)new DoubleType(name,mTarget):
                        (IType)new FloatingPointType(name,size,mTarget);
        }
        return result;

    }

    /* override */
    @Override
    public IType createPointer(String name, IType base, int size) {
        if (name == null && base == mCharType){
            if (mCharPtr == null){
                mCharPtr = new PointerType(name,size,base,mTarget);
            }
            return mCharPtr;
        }
        return new PointerType(name,size,base,mTarget);
  
    }

    /* override */
    @Override
    public IType createArray(String name, IType base, int dimension) {
        return new ArrayType(name,base,dimension,base.getSize()*dimension,mTarget);
    }

    /* override */
    @Override
    public IType createRef(String name, IType base, int size) {
        return new ReferenceType(name,base,size,mTarget);
    }

    /* override */
    @Override
    public IType createEnum(String name, int size) {
        return new EnumType(name,size,mTarget);
    }

    /* override */
    @Override
    public IType createStruct(String name, int size) {
        return new StructType(name,size,IType.STRUCT,mTarget);
    }

    /* override */
    @Override
    public IType createUnion(String name, int size) {
        return new StructType(name,size,IType.UNION,mTarget);
    }

    /* override */
    @Override
    public IType createClass(String name, int size) {
        return new StructType(name,size,IType.CLASS,mTarget);
    }

}
