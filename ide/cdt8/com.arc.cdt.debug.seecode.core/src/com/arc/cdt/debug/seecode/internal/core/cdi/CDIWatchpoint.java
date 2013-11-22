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
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;

import com.arc.seecode.engine.Breakpoint;
import com.arc.seecode.engine.Watchpoint;


/**
 * @author David Pickens
 */
class CDIWatchpoint extends CDIBreakpoint implements ICDIWatchpoint {

    /**
     * @param target
     * @param bp
     */
    CDIWatchpoint(Target target, Breakpoint bp) {
        super(target, bp);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint#isWriteType()
     */
    @Override
    public boolean isWriteType() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint#isReadType()
     */
    @Override
    public boolean isReadType() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint#getWatchExpression()
     */
    @Override
    public String getWatchExpression() throws CDIException {
        return ((Watchpoint)getSeeCodeBreakpoint()).getVariable();
    }

}
