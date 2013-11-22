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
package com.arc.cdt.debug.seecode.internal.ui;

import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;

/**
 * This adaptor factory permits the user to click on the "Launch" of the debugger
 * and be able to identify the stackframe, etc.
 * @author davidp
 *
 */
public class SeeCodeAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof ILaunch){
			ILaunch launch = (ILaunch)adaptableObject;
			IDebugTarget target = launch.getDebugTarget();	
			if (target != null)
		        return target.getAdapter(adapterType);
		}
		return null;
	}

	private static Class<?>[] classList = { ICDITarget.class, ICDISession.class };
	
	@SuppressWarnings("rawtypes")
    @Override
    public Class[] getAdapterList() {
		return classList;
	}

}
