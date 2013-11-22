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
import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;


class CDIGlobalVariable extends CDIVariable implements ICDIGlobalVariable {



    public CDIGlobalVariable(ISeeCodeVariableDescriptor vd, VariableManager vmgr) {
        super(vd, vmgr);
        
    }
    
    /**
     * The UI seems to reference stale global variables. In such a case, we need to
     * resurrect them.
     */
    @Override
    public ICDIValue getValue () throws CDIException {
        if (!this.getVariableManager().isActive(this)){
            this.getVariableManager().reconnect(this);
        }
        return super.getValue();
    }

}
