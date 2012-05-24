/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;

/**
 * Extends the IValue interface by C/C++ specific functionality.
 */
public interface ICValue extends IValue, ICDebugElement {
	
	ICType getType() throws DebugException;

	String evaluateAsExpression( ICStackFrame frame );
	
	/**
	 * Returns this value as a <code>String</code> with an associated format.
	 *
	 * @return a String representation of this value
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <P>
	 * CUSTOMIZATION
	 */
	public String getValueString(CVariableFormat format) throws DebugException;
}
