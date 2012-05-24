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

import org.eclipse.cdt.debug.core.cdi.model.ICDIFunctionBreakpoint;

import com.arc.seecode.engine.LocationBreakpoint;


class CDIFunctionBreakpoint extends CDILocationBreakpoint implements ICDIFunctionBreakpoint {

    public CDIFunctionBreakpoint(Target target, LocationBreakpoint bp) {
        super(target, bp);
        if (bp.getLocation().getFunction() == null)
            throw new IllegalArgumentException("Function is null");
    }

}
