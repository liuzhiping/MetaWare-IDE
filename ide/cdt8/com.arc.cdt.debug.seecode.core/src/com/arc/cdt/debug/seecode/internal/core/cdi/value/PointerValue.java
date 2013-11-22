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

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIPointerValue;

import com.arc.seecode.engine.StackFrameRef;
import com.arc.seecode.engine.Value;
import com.arc.seecode.engine.type.IType;


/**
 * @author David Pickens
 */
class PointerValue extends CDIValue implements ICDIPointerValue {

 
    /**
     * @param value
     * @param target
     */
    public PointerValue(ICDIVariable var, IType type, Value value, ICDITarget target,
            StackFrameRef sf) {
        super(var,type, value, target,sf);
        
    }

    /*override*/
    @Override
    public BigInteger pointerValue() throws CDIException {
        String v = getSeeCodeValue().getValue();
        try {
            if (v.startsWith("0x")){
                return new BigInteger(v.substring(2),16);
            }
            return new BigInteger(v);
        } catch (NumberFormatException e) {
            // The viewer will display the content
            // of the exception as the value.
            throw new CDIException(v);
        }
    }

    /**
     * @param i should be 0
     * @return the dereferenced of this pointer.
     */
    @Override
    protected ICDIVariable createChild(int i) {
         return new DerefVariable(this,getSeeCodeType()!=null?getSeeCodeType().getBaseType():null);
    }
    
    /**
     * @return the number of children of a pointer dereference: 1.
     * @throws CDIException
     */
    @Override
    public int getChildrenNumber () throws CDIException {
        return 1;
    }

}
