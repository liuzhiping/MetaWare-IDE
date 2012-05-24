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


public interface ICompilationUnit {
    /**
     * Return the source path.
     * @return the associated source path.
     */
    String getSourcePath();
    
    /**
     * Return an immutable list of symbols defined at the outer-most scope.
     * @return an immutable list of symbols defined at the outer-most scope.
     */
    List<ISymbol> getSymbols();
}
