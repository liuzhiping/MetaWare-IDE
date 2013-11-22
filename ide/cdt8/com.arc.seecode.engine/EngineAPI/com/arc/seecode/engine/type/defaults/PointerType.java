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
class PointerType extends AbstractType {

    private IType mBase;
    /**
     * @param name
     * @param size
     */
    public PointerType(String name, IType base, int size) {
        super(name, size);
        mBase = base;
    }

    /*override*/
    @Override
    public int getKind() {
        return POINTER;
    }

    @Override
    public boolean equals(IType type) {
        return type.getKind() == POINTER &&
            type.getBaseType().equals(getBaseType());
    }
    @Override
    public IType getBaseType() {
        return mBase;
    }
    
    @Override
    public String toString(){
        if (getName() == null) return getBaseType() + "*";
        return super.toString();
    }
}
