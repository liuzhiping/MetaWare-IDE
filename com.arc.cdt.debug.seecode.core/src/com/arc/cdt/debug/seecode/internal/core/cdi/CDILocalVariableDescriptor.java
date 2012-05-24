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

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;

import com.arc.seecode.engine.Variable;

/**
 * @author David Pickens
 */
class CDILocalVariableDescriptor extends CDIVariableDescriptor implements
        ICDILocalVariableDescriptor {

    /**
     * @param target
     * @param var
     * @param sf
     * @param vmgr
     */
    public CDILocalVariableDescriptor(Target target, Variable var,
            StackFrame sf, VariableManager vmgr) {
        super(target, var, sf, vmgr);
    }
    
    @Override
    public ISeeCodeVariable allocateVariable(){
        return new CDILocalVariable(this,getVariableManager());
    }
    
    @Override
	protected CDIVariableDescriptor makeClone() {
		return new CDILocalVariableDescriptor((Target)getTarget(),
				getSeeCodeCookie(),(StackFrame)getStackFrame(),getVariableManager());
	}
    
    @Override
    public ICDIVariableDescriptor getVariableDescriptorAsArray(int start, int length) throws CDIException {
          return new ArrayPartitionLocalDescriptor(this,start,length,this.getVariableManager());
    }

}
