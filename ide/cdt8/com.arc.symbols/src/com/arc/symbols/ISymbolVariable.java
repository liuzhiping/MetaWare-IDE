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
package com.arc.symbols;


public interface ISymbolVariable extends ISymbol {
    enum Binding {
        STACKFRAME,
        STATIC,
        THREADLOCAL,
        REGISTER,
    }
    
    Binding getBinding();
    /**
     * Return the scope of the variable.
     * @return the scope of the variable.
     */
    IScope getScope();
    
    /**
     * Return the address of the variable if it can be computed without any context
     * information.
     * @return the address of the variable if it can be computed without any context
     * information.
     * @throws IllegalStateException variable is not static.
     */
    long getAddress() throws IllegalStateException;
    
    
}
