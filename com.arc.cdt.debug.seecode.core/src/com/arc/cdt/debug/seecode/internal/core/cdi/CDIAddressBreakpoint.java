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

import org.eclipse.cdt.debug.core.cdi.model.ICDIAddressBreakpoint;

import com.arc.seecode.engine.LocationBreakpoint;


class CDIAddressBreakpoint extends CDILocationBreakpoint implements ICDIAddressBreakpoint {

    public CDIAddressBreakpoint(Target target, LocationBreakpoint bp) {
        super(target, bp);
    }

}
