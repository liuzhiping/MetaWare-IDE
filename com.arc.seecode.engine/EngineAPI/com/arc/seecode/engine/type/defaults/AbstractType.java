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

import com.arc.seecode.engine.type.IEnum;
import com.arc.seecode.engine.type.IField;
import com.arc.seecode.engine.type.IType;


/**
 * An abstract base class for all types.
 * @author David Pickens
 */
abstract  class AbstractType implements IType {
    private String mName;
    private int mSize;
    /**
     * 
     */
    public AbstractType(String name, int size) {
        super();
        mName = name;
        mSize = size;
    }


    /*override*/
    @Override
    public String getName() {
        return mName;
    }

    /*override*/
    @Override
    public int getSize() {
        return mSize;
    }

    /*override*/
    @Override
    public IType getBaseType() {
        return null;
    }

    /*override*/
    @Override
    public IType getIndexType() {
        return null;
    }

    /*override*/
    @Override
    public IField[] getFields() {
        return null;
    }

    /*override*/
    @Override
    public IType[] getParameterTypes() {
        return null;
    }

    /*override*/
    @Override
    public IType[] getBaseClasses() {
        return null;
    }

    /*override*/
    @Override
    public IEnum[] getEnums() {
        return null;
    }

    /*override*/
    @Override
    public long getLowRange() {
        return 0;
    }

    /*override*/
    @Override
    public long getHighRange() {
        return 0;
    }

    /*override*/
    @Override
    public boolean isUnsigned() {
        return false;
    }

    /*override*/
    @Override
    public boolean equals(IType type) {
        if (type == this) return true;
        if (type.getKind() != getKind())
            return false;
        if (type.getSize() != getSize())
            return false;
        if (type.getName() == null ||
                    !type.getName().equals(getName()))
        return false;
        return true;
    }

    /*override*/
    @Override
    public int getDimension() {
        return 0;
    }
    
    @Override
    public String toString(){
        String name = getName();
        if (name != null) return name;
        return "<type>";
    }

}
