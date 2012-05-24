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
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIReferenceType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;

import com.arc.seecode.engine.type.IType;


/**
 * @author David Pickens
 */
class ReferenceType extends AbstractType implements ICDIReferenceType {
    private IType mBase;
    /**
     * @param name
     * @param size
     * @param target
     */
    public ReferenceType(String name, IType base, int size, ICDITarget target) {
        super(name, size, target);
        mBase = base;
    }

    /*override*/
    @Override
    public int getKind() {
        return REF;
    }

    /*override*/
    @Override
    public ICDIType getComponentType() {
        return (ICDIType)mBase;
    }

    @Override
    public boolean equals(IType type) {
        return type != null && type.getKind() == REF &&
            type.getBaseType() != null && 
            type.getBaseType().equals(getBaseType());
    }
    @Override
    public IType getBaseType() {
        return mBase;
    }
    
    @Override
    public String getTypeName(){
        if (getName() != null) return getName();
        if (getBaseType() != null)
            return getBaseType() + "&";
        return "void&";
    }
}
