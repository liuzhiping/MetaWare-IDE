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

import org.eclipse.cdt.debug.core.cdi.event.ICDIDestroyedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;

/**
 * @author David Pickens
 */
public class DestroyedEvent extends Event implements ICDIDestroyedEvent {
	/**
	 * The destruction of a breakpiont.
	 * @param source
	 */
	public DestroyedEvent(ICDIBreakpoint source) {
		super(source);
	}
	
	/**
	 * The destruction of a thread.
	 * @param source
	 */
	public DestroyedEvent(ICDIThread source) {
		super(source);
	}
	
	/**
	 * The destruction of a target.
	 * @param source
	 */
	public DestroyedEvent(ICDITarget source) {
		super(source);
	}
	
	/**
	 * The destruction of a shared library.
	 * @param source
	 */
	public DestroyedEvent(ICDISharedLibrary source) {
		super(source);
	}
	
	/**
	 * The destruction of a variable
	 * @param source the variable being removed from 
	 * variable manager.
	 */
	public DestroyedEvent(ICDIVariableDescriptor source) {
		super(source);
	}
    
    /**
     * The destruction of an expression
     * @param source the expression being removed from 
     * expression manager.
     */
    public DestroyedEvent(ICDIExpression source) {
        super(source);
    }
    
    /**
     * The destruction of a memory block
     * @param source the memory block being removed from 
     * memory manager.
     */
    public DestroyedEvent(ICDIMemoryBlock source) {
        super(source);
    }
    
    @Override
    public String toString(){
        return "DestroyedEvent(" + getSource() + ")";
    }
}
