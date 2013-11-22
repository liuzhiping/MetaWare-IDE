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

public class ArrayPartitionLocalDescriptor extends ArrayPartitionDescriptor implements
		ICDILocalVariableDescriptor {

	public ArrayPartitionLocalDescriptor(ICDILocalVariableDescriptor var, int start, int length, VariableManager vmgr) throws CDIException {
		super((ISeeCodeVariableDescriptor)var, start, length,vmgr);
	}
	

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#getVariableDescriptorAsArray(int, int)
     */
	@Override
    public ICDIVariableDescriptor getVariableDescriptorAsArray(int start, int length) throws CDIException {
        return new ArrayPartitionLocalDescriptor(this,start,length,mVarMgr);
    }
	
	@Override
	protected ArrayPartitionDescriptor makeClone() throws CDIException {
		return new ArrayPartitionLocalDescriptor((ICDILocalVariableDescriptor)mVar,mStart,mLength,mVarMgr);
	}
	
	@Override
    public ISeeCodeVariable allocateVariable(){
        return new CDILocalVariable(this,mVarMgr);
    }

}
