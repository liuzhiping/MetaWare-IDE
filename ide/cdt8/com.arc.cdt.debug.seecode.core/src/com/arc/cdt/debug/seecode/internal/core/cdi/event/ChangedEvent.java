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

import org.eclipse.cdt.debug.core.cdi.event.ICDIChangedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;

/**
 * @author David Pickens
 */
public class ChangedEvent extends Event implements ICDIChangedEvent {

    /**
     * Change in the state of a breakpoint.
     * 
     * @param source
     */
    public ChangedEvent(ICDIBreakpoint source) {
        super(source);
    }

    /**
     * Change in the value of an expression.
     * 
     * @param source
     */
    public ChangedEvent(ICDIExpression source) {
        super(source);
    }
    
    /**
     * Change in the value of a variable.
     * 
     * @param source
     */
    public ChangedEvent(ICDIVariable source) {
        super(source);
    }
    /**
     * Change in the value of a register.
     * 
     * @param source
     */
    public ChangedEvent(ICDIRegister source) {
        super(source);
    }
    
    public ChangedEvent(ICDIObject source){
        super(source);
    }
    
    /**
     * Change in memory block contents. See 
     * {@link MemoryChangedEvent}.
     * 
     * @param source
     */
    protected ChangedEvent(ICDIMemoryBlock source) {
        super(source);
    }
    
    @Override
    public String toString(){
        return "ChangedEvent(" + getSource() + ")";
    }

}
