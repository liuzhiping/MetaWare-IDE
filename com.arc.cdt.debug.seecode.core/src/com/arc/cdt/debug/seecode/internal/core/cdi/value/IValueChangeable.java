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
package com.arc.cdt.debug.seecode.internal.core.cdi.value;

import org.eclipse.cdt.debug.core.cdi.CDIException;

import com.arc.seecode.engine.Value;


/**
 * An interface for changing the SeeCode value that
 * an object wraps.
 * @author David Pickens
 */
public interface IValueChangeable {
    /**
     * Alter the underlying SeeCode cookie that is the
     * basis of a CDI value.
     * @param v
     * @throws CDIException of something messes up when
     * propagating to aggregate elements.
     */
    void setValue(Value v) throws CDIException;
}
