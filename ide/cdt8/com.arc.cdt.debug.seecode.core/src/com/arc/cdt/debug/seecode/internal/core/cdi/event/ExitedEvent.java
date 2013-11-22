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
package com.arc.cdt.debug.seecode.internal.core.cdi.event;

import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.event.ICDIExitedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;


/**
 * @author David Pickens
 */
public class ExitedEvent extends DestroyedEvent implements ICDIExitedEvent {

    private ICDISessionObject mReason;

    /**
     * @param source
     */
    public ExitedEvent(ICDITarget source, ICDISessionObject reason) {
        super(source);
        mReason = reason;
    }

 

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.event.ICDIExitedEvent#getReason()
     */
    @Override
    public ICDISessionObject getReason() {
        return mReason;
    }

    @Override
    public String toString(){
        return "ExitedEvent(" + getSource() + ")";
    }
}
