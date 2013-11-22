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

import com.arc.seecode.engine.type.IType;


/**
 * @author David Pickens
 */
class ArrayType extends AbstractType {
    private IType mBase;
    private int mDimension;
    /**
     * @param name
     * @param base
     * @param dimension
     */
    public ArrayType(String name, IType base, int dimension) {
        super(name, base.getSize()*dimension);
        mBase= base;
        mDimension = dimension;
    }

    @Override
    public int getKind() {
        return ARRAY;
    }
    
    @Override
    public boolean equals(IType type){
        return type.getKind() == ARRAY &&
            type.getBaseType().equals(getBaseType()) &&
            type.getDimension() == getDimension();
    }

    @Override
    public IType getBaseType() {
        return mBase;
    }
    @Override
    public int getDimension() {
        return mDimension;
    }
    
    @Override
    public String toString(){
        return getBaseType().toString() + "[" + getDimension() + "]";
    }
}
