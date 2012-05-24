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
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIAggregateType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;

import com.arc.seecode.engine.type.IType;


/**
 * @author David Pickens
 */
class ArrayType extends AbstractType implements ICDIArrayType, ICDIAggregateType {
    private int mDimension;
    private IType mBase;
    /**
     * @param name
     * @param size
     * @param target
     */
    public ArrayType(String name, IType base, int dimension, int size, ICDITarget target) {
        super(name, size, target);
        if (base == null) throw new IllegalArgumentException("base is null");
        mDimension = dimension;
        mBase = base;
        
    }

    /*override*/
    @Override
    public ICDIType getComponentType() {
        return (ICDIType)mBase;
    }

    /*override*/
    @Override
    public int getKind() {
        return ARRAY;
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
    public long getHighRange() {
        return mDimension-1;
    }
    
    @Override
    public String getTypeName(){
    	if (getName() != null)
            return getName();
    	int d = getDimension();
    	if (getBaseType() == null) return "void[]"; //shouldn't happen
    	if (d > 0)
            return getBaseType() + "[" + d + "]";
    	return getBaseType() + "[]";
    }
    
    @Override
    public String toString(){
        return getTypeName();
    }

	@Override
	public boolean equals(IType type) {
		if (type == null) return false; //shouldn't happen
		if (type.getKind() != ARRAY) return false;
		if (type.getDimension() != getDimension()) return false;
		if (!type.getBaseType().equals(this.getBaseType())) return false;
		return true;
	}
}
