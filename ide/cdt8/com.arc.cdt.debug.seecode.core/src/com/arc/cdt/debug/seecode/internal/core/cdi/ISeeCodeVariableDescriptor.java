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

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;

import com.arc.seecode.engine.Variable;

/**
 * @author David Pickens
 */
public interface ISeeCodeVariableDescriptor extends ICDIVariableDescriptor {
    /**
     * Return the cookie on which this variable is based.
     * @return the cookie on which this variable is based.
     */
    public Variable getSeeCodeCookie();
    
    /**
     * Set this variable to another cookie if the engine has generated
     * a new one to reference the same variable after a step operation.
     * @param v the new seecode cookie.
     * @throws CDIException if something messes up.
     */
    public void setSeeCodeCookie(Variable v) throws CDIException;
    
    /**
     * Return true if this variable went out of scope.
     * @return true if this variable went out of scope.
     */
    public boolean isOutOfScope();
    
    /**
     * Return the stackframe on which it is based, if applicable.
     * @return the applicable stackframe or null.
     */
    public ICDIStackFrame getStackFrame() throws CDIException;
    
    /**
     * If this is actually a pseudo variable that is the cast of another one,
     * then return the name of the type being cast to. Otherwise, return null,
     * which is the normal case.
     */
    public String getCastType();
    
    /**
     * Allocate, or return, the associated variable.
     * @return the associated variable.
     */
    public ISeeCodeVariable allocateVariable();
    
    /**
     * True if this is a C pointer that is being viewed as an array.
     * @return whether or not this is a C pointer that is being viewed as an array.
     */
    public boolean isArrayPartition();
}
