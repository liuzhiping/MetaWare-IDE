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
class DoubleType extends AbstractType implements ICDIDoubleType {

    /**
     * @param name
     * @param target
     */
    public DoubleType(String name,  ICDITarget target) {
        super(name != null?name:"double", 8, target);
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
        return false;
    }

    /*override*/
    @Override
    public int getKind() {
        return FLOAT;
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
