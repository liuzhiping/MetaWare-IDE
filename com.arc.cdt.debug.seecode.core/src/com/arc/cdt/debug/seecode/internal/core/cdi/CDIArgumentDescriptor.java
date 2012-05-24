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
package com.arc.cdt.debug.seecode.internal.core.cdi;

import org.eclipse.cdt.debug.core.cdi.model.ICDIArgumentDescriptor;

import com.arc.seecode.engine.Variable;

/**
 * @author David Pickens
 */
class CDIArgumentDescriptor extends CDIVariableDescriptor implements
        ICDIArgumentDescriptor {

 

	/**
     * @param target
     * @param var
     * @param sf
     * @param vmgr
     */
    public CDIArgumentDescriptor(Target target, Variable var, StackFrame sf,
            VariableManager vmgr) {
        super(target, var, sf, vmgr);
    }
    
    @Override
	protected CDIVariableDescriptor makeClone() {
		return new CDIArgumentDescriptor((Target)getTarget(),
				getSeeCodeCookie(),(StackFrame)getStackFrame(),getVariableManager());
	}
    
    @Override
    public ISeeCodeVariable allocateVariable(){
        return new CDIArgument(this, getVariableManager());
    }

}
