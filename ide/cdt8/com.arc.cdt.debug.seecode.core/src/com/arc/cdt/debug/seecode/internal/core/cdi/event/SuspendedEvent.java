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
import org.eclipse.cdt.debug.core.cdi.event.ICDISuspendedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;


/**
 * An engine-stop event.
 * @author David Pickens
 */
public class SuspendedEvent extends Event implements ICDISuspendedEvent {
    private ICDISessionObject mReason;
    /**
     * The reason for the stop can be one of:
     * <dl>
     * <dt>Session
     * <dd> a user-requested stop.
     * <dt>ICDIBreakpointHit
     * <dd> a breakpoint/watchpoint was hit.
     * </dl>
     * @param source the process that stopped.
     * @param reason the reason for the stop.
     */
    public SuspendedEvent(ICDITarget source, ICDISessionObject reason) {
        super(source);
        mReason = reason;
    }
    
    /**
     * The reason for the stop can be one of:
     * <dl>
     * <dt>Session
     * <dd> a user-requested stop.
     * <dt>ICDIBreakpointHit
     * <dd> a breakpoint/watchpoint was hit.
     * </dl>
     * @param source the thread that stopped.
     * @param reason the reason for the stop.
     */
    public SuspendedEvent(ICDIThread source, ICDISessionObject reason) {
        super(source);
        mReason = reason;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.event.ICDISuspendedEvent#getReason()
     */
    @Override
    public ICDISessionObject getReason() {
        return mReason;
    }
    
    @Override
    public String toString(){
        return "SuspendedEvent(" + getSource() + ")";
    }

}
