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

import com.arc.symbols.types.IType;

/**
 * Represents a symbols, whether it be a variable, function, type, or class.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface ISymbol {
    enum Kind {
        UNKNOWN,
        VARIABLE,
        CONSTANT,
        TYPE,
        CLASS,
        FUNCTION,
        METHOD,
        FIELD,
    }
    
    Kind getKind();
    
    boolean isGlobal();
    
    /**
     * Return the name as the user would expect to see it.
     * @return the name as the user would expect to see it.
     */
    String getName();
    
    IType getType();
    
    /**
     * Return the name that the linkage editor would expect to see
     * for this symbol.
     * @return mangled name for this symbol.
     */
    String getLinkageName();
    
    /**
     * Return the path of the source file where this symbol is declared, or <code>null</code>
     * if unknown, or not relavent.
     * @return the path of the source file where this symbol is declared, or <code>null</code>
     * if unknown, or not relavent.
     */
    String getSourceFile();
    
    /**
     * Return the associated source line where this symbol is declared, or <code>0</code> if not known
     * or is not applicable.
     * @return  Return the associated source line where this symbol is declared, or <code>0</code> if not known
     * or is not applicable.
     */
    int getSourceLine();

}
