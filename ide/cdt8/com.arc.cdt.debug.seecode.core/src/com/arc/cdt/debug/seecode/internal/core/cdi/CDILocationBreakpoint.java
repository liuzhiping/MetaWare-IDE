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

import org.eclipse.cdt.debug.core.cdi.ICDILocator;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;

import com.arc.seecode.engine.Location;
import com.arc.seecode.engine.LocationBreakpoint;


/**
 * A location breakpoint.
 * @author David Pickens
 */
abstract class CDILocationBreakpoint extends CDIBreakpoint implements ICDILocationBreakpoint {
    private ICDILocator mLocation = null;
    /**
     * @param target
     * @param bp
     */
    CDILocationBreakpoint(Target target, LocationBreakpoint bp) {
        super(target,bp);
    }
    
    @Override
    public boolean isTemporary() {
        return ((LocationBreakpoint)getSeeCodeBreakpoint()).isTemporary();
    }

    @Override
    public ICDILocator getLocator() {
        if (mLocation != null) return mLocation;
        Location location = ((LocationBreakpoint)getSeeCodeBreakpoint()).getLocation();
        if (location instanceof ICDILocator){
            mLocation = (ICDILocator)location; //one we created.
        }
        else {
            // Created implicitly by engine 
            mLocation = new CDILocation(location);
        }
        return mLocation;
    }

}
