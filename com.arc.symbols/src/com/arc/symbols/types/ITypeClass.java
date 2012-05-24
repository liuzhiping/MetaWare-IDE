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
package com.arc.symbols.types;

import java.util.List;

import com.arc.symbols.IScope;


public interface ITypeClass extends ITypeAggregate, IScope {

    /**
     * Return an immutable list of base classes.
     * If there is no base class, this method returns a zero-length list.
     * @return an immutable list of base classes.
     */
    List<IBaseClass> getBaseClasses();
}
