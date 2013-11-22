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
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIDoubleType;

import com.arc.seecode.engine.type.IType;


/**
 * @author David Pickens
 */
class FloatingPointType extends AbstractType implements ICDIDoubleType {

    /**
     * @param name
     * @param size
     * @param target
     */
    public FloatingPointType(String name, int size, ICDITarget target) {
        super(name != null?name:size==4?"float":size==8?"double":null, size, target);
        // TODO Auto-generated constructor stub
    }

    /*override*/
    @Override
    public boolean isImaginary() {
        return false;
    }

    /*override*/
    @Override
    public boolean isComplex() {
        return false;
    }

    /*override*/
    @Override
    public boolean isLong() {
        return getSize() > 8;
    }

    /*override*/
    @Override
    public int getKind() {
        return FLOAT;
    }
    
    @Override
	public String getTypeName(){
    	String n = super.getTypeName();
    	if (n != null) return n;
    	return "float*" + getSize();
    }
    
	@Override
	public boolean equals(IType type) {
		if ( type == null || type.getKind() != getKind() || type.getSize() != getSize()) return false;
		if (type instanceof ICDIDoubleType){
			ICDIDoubleType dtype = (ICDIDoubleType)type;
			return dtype.isImaginary() == isImaginary() && dtype.isComplex() == this.isComplex() &&
				this.isLong() == dtype.isLong();
		}
		return !isImaginary() && isComplex();
			
	}

}
