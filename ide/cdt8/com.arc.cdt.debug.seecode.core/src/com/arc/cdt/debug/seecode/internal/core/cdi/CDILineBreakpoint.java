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
import org.eclipse.cdt.debug.core.cdi.model.ICDILineBreakpoint;

import com.arc.seecode.engine.LocationBreakpoint;

// Make it implement ICDIFunctionBreakpoint to avoid casting issues since the engine
// fills in function info info source/line.
class CDILineBreakpoint extends CDILocationBreakpoint implements ICDILineBreakpoint, ICDIFunctionBreakpoint {

    CDILineBreakpoint(Target target, LocationBreakpoint bp) {
        super(target, bp);
        if (bp.getLocation().getSource() == null)
            throw new IllegalArgumentException("Missing source");
    }

}
