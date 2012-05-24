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

import org.eclipse.cdt.debug.core.cdi.event.ICDICreatedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;

/**
 * A target or thread creation event.
 * @author David Pickens
 */
public class CreatedEvent extends Event implements ICDICreatedEvent {
	/**
	 *  The creation of a new target.
	 */
	public CreatedEvent(ICDITarget source) {
		super(source);
	}
	
	/**
	 * The creation of a new thread.
	 */
	public CreatedEvent(ICDIThread source) {
		super(source);
	}
	
	/**
	 * The creation of a new shared library.
	 */
	public CreatedEvent(ICDISharedLibrary source) {
		super(source);
	}
	
	/**
	 * The creation of a new breakpoint
	 */
	public CreatedEvent(ICDIBreakpoint source) {
		super(source);
	}
    
    /**
     * The creation of a new variable
     */
    public CreatedEvent(ICDIVariable source) {
        super(source);
    }
    
    /**
     * The creation of a new register value
     */
    public CreatedEvent(ICDIRegister source) {
        super(source);
    }
    
    /**
     * The creation of a new expression
     */
    public CreatedEvent(ICDIExpression source) {
        super(source);
    }
    
    @Override
    public String toString(){
        return "CreatedEvent(" + getSource() + ")";
    }

}
