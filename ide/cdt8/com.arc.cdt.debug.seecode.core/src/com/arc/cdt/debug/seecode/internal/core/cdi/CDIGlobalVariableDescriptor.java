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

import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariableDescriptor;

import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.Variable;

/**
 * A variable that implements the CDI Global Variable descriptor interface.
 *
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
class CDIGlobalVariableDescriptor extends CDIVariableDescriptor implements ICDIGlobalVariableDescriptor {

    private int mInstances = 0;
    public CDIGlobalVariableDescriptor(Target target, Variable var, StackFrame sf, VariableManager vmgr) {
        super(target, var, sf, vmgr);
    }
    
    @Override
    public ISeeCodeVariable allocateVariable(){
        try {
            // The UI handles global variables in a weird way. 
            // If we delete them when we think we should, the UI 
            // may reference stale copies.
            // When re-allocating variable and we have an old seecode cookie,
            // make sure that it is upto date.
            // There can be stale values here.
            if (mInstances > 0) {
               this.getSeeCodeCookie().update();
            }
        }
        catch (EngineException e) {
            //Presumbly, a sick engine will be caught later.
        }
        mInstances++;
        return new CDIGlobalVariable(this,getVariableManager());
    }
    
    @Override
	protected CDIVariableDescriptor makeClone() {
		return new CDIGlobalVariableDescriptor((Target)getTarget(),
				getSeeCodeCookie(),(StackFrame)getStackFrame(),getVariableManager());
	}
    
    @Override
    public boolean isOutOfScope(){
        // The UI seems to assume that global variables are always present in the display.
        // Don't mistakenly destroy if the engine thinks they are out of scope.
        return false;
    }

}
