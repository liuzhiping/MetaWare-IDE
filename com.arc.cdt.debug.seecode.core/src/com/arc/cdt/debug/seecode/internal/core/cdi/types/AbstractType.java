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
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.core.runtime.IAdaptable;

import com.arc.seecode.engine.type.IEnum;
import com.arc.seecode.engine.type.IField;
import com.arc.seecode.engine.type.IType;

/**
 * A baseclass for all our type classes.
 * @author David Pickens
 */
abstract class AbstractType implements IType, ICDIType, IAdaptable {
    private ICDITarget mTarget;
    private String mName;
    private int mSize;
    AbstractType(String name, int size, ICDITarget target){
        mTarget = target;
        mName = name;
        mSize = size;
    }
    
    @Override
    public String getName(){
        return mName;
    }
    
    @Override
    public IType getBaseType() {
        return null;
    }

    @Override
    public IType getIndexType() {
        return null;
    }
    
    @Override
    public int getSize(){
        return mSize;
    }

    @Override
    public IField[] getFields() {

        return null;
    }

    @Override
    public IType[] getParameterTypes() {
        return null;
    }

    @Override
    public IType[] getBaseClasses() {
        return null;
    }

    @Override
    public IEnum[] getEnums() {
        return null;
    }

    @Override
    public long getLowRange() {
        return 0;
    }

    @Override
    public long getHighRange() {
        return 0;
    }

    @Override
    public boolean isUnsigned() {
        return false;
    }

    @Override
    public boolean equals(IType type) {
    	if (this == type) return true;
    	if (type == null) return false; //shouldn't happen but sometimes does.
        if (getKind() != type.getKind()) return false;
        String name = getName();
        if (name != null) { return name.equals(type.getName()); }
        return false;
    }

    @Override
    public String getTypeName() {
        return getName();
    }

    @Override
    public String getDetailTypeName() {
        return getName();
    }

    @Override
    public ICDITarget getTarget() {
        return mTarget;
    }
    
    @Override
    public int getDimension(){
        return 0;
    }
    
    @Override
    public String toString(){
        if (getName() != null) 
            return getName();
        return getTypeName();
        /*String name = getClass().getName();
        int i = name.lastIndexOf('.');
        if (i >= 0) return name.substring(i);
        return name;*/
    }


    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter (Class adapter) {
        if (adapter == IType.class){
            return this;
        }
        if (adapter == ICDIType.class)
            return this;
        return null;
    }

}
