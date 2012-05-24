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
package com.arc.seecode.engine;


/**
 * An interface used to evaluate expressions that denote
 * locations (as would be required for a breakpoint).
 * @author David Pickens
 */
public interface IStaticEvaluator {
    /**
     * Given the name of a static symbol, return its location.
     * <P>
     * If the name is not global, then the implementation will
     * need to somehow derive the compilation unit from which
     * to search (e.g., the PC counter of the current thread).
     * @param name name of a static symbol to lookup.
     * @return the corresponding location object.
     */
    public Location lookupStaticSymbol(String name);

}
