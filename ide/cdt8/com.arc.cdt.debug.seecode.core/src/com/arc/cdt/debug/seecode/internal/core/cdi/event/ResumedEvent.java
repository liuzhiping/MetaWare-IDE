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

import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;


/**
 * An event to denote a resume, step, run-until, etc.
 * @author David Pickens
 */
public class ResumedEvent extends Event implements ICDIResumedEvent {
    private int mType;
    /**
     * @param source
     * @param type see {@link ICDIResumedEvent}.
     */
    public ResumedEvent(ICDITarget source, int type) {
        super(source);
        mType = type;
    }
    
    public ResumedEvent(ICDIThread source, int type) {
        super(source);
        mType = type;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent#getType()
     */
    @Override
    public int getType() {
        return mType;
    }
    
    @Override
    public String toString(){
        return "ResumedEvent(" + getSource() + ")";
    }

}
