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
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIEnumType;

import com.arc.seecode.engine.type.IType;


/**
 * @author David Pickens
 */
class EnumType extends IntegerType implements ICDIEnumType {

    /**
     * @param name
     * @param size
     * @param target
     */
    public EnumType(String name, int size,ICDITarget target) {
        super(name, size, false, target);
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.type.IType#equals(com.arc.seecode.engine.type.IType)
     */
    @Override
    public boolean equals(IType type) {
        return type.getKind() == ENUM &&
        	type.getSize() == getSize() &&
        	type.getName() != null &&
        	type.getName().equals(getName())  ||
        	type == this;
    }
    
    @Override 
    public String getTypeName(){
    	String n = getName();
    	if (n != null) return n;
    	return "<enum>";
    }
    /* (non-Javadoc)
     * @see com.arc.seecode.engine.type.IType#getKind()
     */
    @Override
    public int getKind() {
        return ENUM;
    }
}
