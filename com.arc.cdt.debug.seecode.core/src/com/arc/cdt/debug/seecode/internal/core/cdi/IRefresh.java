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

import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;


/**
 * An interface for refreshing variables.
 * @author David Pickens
 */
public interface IRefresh {
    /**
     * Refresh an object and return true if
     * a change occurred.
     * @param listToUpdate if not null, a list of
     * things that changed from which change events can generated.
     * @return true if a change occurred during refresh.
     */
    boolean refresh(List<ICDIObject> listToUpdate) throws CDIException;
    
}
