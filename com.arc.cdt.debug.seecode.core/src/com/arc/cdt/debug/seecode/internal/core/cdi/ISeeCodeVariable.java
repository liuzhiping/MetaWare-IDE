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

import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;

/**
 * We augment the ICDIVariable interface with stuff needed by our SeeCode implementation.
 */

public interface ISeeCodeVariable extends ISeeCodeVariableDescriptor, ICDIVariable, IRefresh {
    ISeeCodeVariableDescriptor getDescriptor();
}
