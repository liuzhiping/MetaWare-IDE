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


/**
 * @author David Pickens
 */
class IntegerType extends AbstractType {
    private boolean mUnsigned;
    /**
     * @param name
     * @param target
     */
    IntegerType(String name, int size, boolean isUnsigned, ICDITarget target) {
        super(name, size, target);
        mUnsigned = isUnsigned;
    }
    
    @Override
    public int getKind() {
        return INTEGER;
    }
    
    @Override
    public boolean isUnsigned(){
        return mUnsigned;
    }
    
    @Override
    public boolean equals(IType type){
        return type != null &&
        type.getKind() == INTEGER && type.getSize() == getSize() &&
        type.isUnsigned() == isUnsigned();
    }

}
