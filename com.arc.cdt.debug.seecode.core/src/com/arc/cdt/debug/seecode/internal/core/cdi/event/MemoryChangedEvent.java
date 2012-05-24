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

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.event.ICDIMemoryChangedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;


/**
 * @author David Pickens
 */
public class MemoryChangedEvent extends ChangedEvent implements ICDIMemoryChangedEvent {
    private BigInteger[] mAddresses;
    /**
     * @param source
     */
    public MemoryChangedEvent(ICDIMemoryBlock source, BigInteger[] addresses) {
        super(source);
        mAddresses = addresses;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.event.ICDIMemoryChangedEvent#getAddresses()
     */
    @Override
    public BigInteger[] getAddresses() {
        return mAddresses;
    }
    
    @Override
    public String toString(){
        return "MemoryChangeEvent(" + getSource() + ")";
    }

}
