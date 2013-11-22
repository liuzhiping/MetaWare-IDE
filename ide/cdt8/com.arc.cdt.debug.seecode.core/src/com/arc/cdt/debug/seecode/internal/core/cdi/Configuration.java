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

import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration4;

import com.arc.seecode.engine.EngineException;

/**
 * Configuration information for SeeCode.
 * @author David Pickens
 */
class Configuration implements ICDITargetConfiguration4 {
    private Target mTarget;
    private boolean hasThreadControlSet = false;
    private boolean hasThreadControl = false;

    Configuration(Target target){
        mTarget = target;
    }
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsTerminate()
	 */
	@Override
    public boolean supportsTerminate() {
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsDisconnect()
	 */
	@Override
    public boolean supportsDisconnect() {
		return mTarget.getEngineInterface().canDisconnect();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsSuspend()
	 */
	@Override
    public boolean supportsSuspend() {
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsResume()
	 */
	@Override
    public boolean supportsResume() {
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsRestart()
	 */
	@Override
    public boolean supportsRestart() {
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsStepping()
	 */
	@Override
    public boolean supportsStepping() {
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsInstructionStepping()
	 */
	@Override
    public boolean supportsInstructionStepping() {
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsBreakpoints()
	 */
	@Override
    public boolean supportsBreakpoints() {
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsRegisters()
	 */
	@Override
    public boolean supportsRegisters() {
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsRegisterModification()
	 */
	@Override
    public boolean supportsRegisterModification() {
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsSharedLibrary()
	 */
	@Override
    public boolean supportsSharedLibrary() {
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsMemoryRetrieval()
	 */
	@Override
    public boolean supportsMemoryRetrieval() {
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsMemoryModification()
	 */
	@Override
    public boolean supportsMemoryModification() {
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsExpressionEvaluation()
	 */
	@Override
    public boolean supportsExpressionEvaluation() {
		return true;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getTarget()
     */
    @Override
    public ICDITarget getTarget() {
        return mTarget;
    }
    
    @Override
    public boolean supportsPassiveVariableUpdate () {
        return false;
    }
    @Override
    public boolean supportsRuntimeTypeIdentification () {
        return false;
    }
    @Override
    public boolean supportsThreadControl () {
        if (!hasThreadControlSet) {
            try {
                hasThreadControlSet = true;
                hasThreadControl = mTarget.getEngineInterface().hasThreadControl();
            }
            catch (EngineException e) {
                // whatever the problem is will presumably occur elsewhere
            }
        }
        return hasThreadControl;
    }
    
    @Override
    public boolean supportsAddressBreaksOnStartup () {
        return true;
    }
    @Override
    public boolean supportsHardwareBreakpoints () {
        return true;
    }
    @Override
    public boolean needsRegistersUpdated (ICDIEvent event) {
        // @todo Auto-generated method stub
        return false;
    }
    @Override
    public boolean needsVariablesUpdated (ICDIEvent event) {
        // @todo Auto-generated method stub
        return false;
    }
}
