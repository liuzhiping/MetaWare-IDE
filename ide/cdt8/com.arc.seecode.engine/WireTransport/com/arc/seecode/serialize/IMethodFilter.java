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
package com.arc.seecode.serialize;

import java.lang.reflect.Method;


/**
 * A filter to determine which methods can be called remotely.
 * @author David Pickens
 */
public interface IMethodFilter {
    boolean includeMethod(Method method);

}
