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
package org.eclipse.cdt.debug.core.cdi.model;

// CUSTOMIZATION
public interface ICDIGlobalVariableDescriptor2 extends ICDIGlobalVariableDescriptor {
    /**
     * Return the source location.
     * @return the source location or <code>null</code> if not known.
     */
    public String getPath();
}
