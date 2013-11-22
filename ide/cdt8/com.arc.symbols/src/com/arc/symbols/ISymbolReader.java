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

import java.util.List;


public interface ISymbolReader {
    /**
     * Return all compilation units.
     * @return the compilation units as an immutable list.
     */
    List<ICompilationUnit>getCompilationUnits();
    
    /**
     * Return all global variables and constants.
     * @return all global variables as an immutable list.
     */
    List<ISymbol> getGlobalVariables();
}
