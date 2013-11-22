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
package com.arc.cdt.debug.seecode.internal.core.cdi.value;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatingPointValue;

import com.arc.seecode.engine.Value;


/**
 * @author David Pickens
 */
class FloatingPointValue extends CDIValue implements ICDIFloatingPointValue {

    /**
     * @param value
     * @param target
     */
    public FloatingPointValue(Value value, ICDITarget target) {
        super(value, target);
        // TODO Auto-generated constructor stub
    }

    /*override*/
    @Override
    public float floatValue() throws CDIException {
        return (float)doubleValue();
    }

    /*override*/
    @Override
    public double doubleValue() throws CDIException {
        String s = getSeeCodeValue().getValue();
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            throw new CDIException("Bad floating point value: " + s);
        }
    }

 

}
