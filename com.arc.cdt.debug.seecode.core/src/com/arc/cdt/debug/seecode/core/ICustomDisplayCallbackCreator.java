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
package com.arc.cdt.debug.seecode.core;

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;

import com.arc.seecode.engine.ICustomDisplayCallback;

/**
 * To avoid circular dependencies between this package
 * and the UI package, we can't create the
 * CustomeDisplayCallback instance directly because it
 * is tied to UI stuff.
 * <P>
 * We have the UI package set a callback to it
 * that creates it.
 * @author David Pickens
 */
public interface ICustomDisplayCallbackCreator {
    public ICustomDisplayCallback create(ICDITarget target);
}
