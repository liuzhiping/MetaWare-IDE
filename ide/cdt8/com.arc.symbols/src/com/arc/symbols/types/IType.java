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


/**
 * Represents a type.
 * "toString()" will show the type in a user-friendly manner.
 * {@link #getName()} returns non-null if there is an explicit
 * typedef name assigned for the type.
 * @todo davidp needs to add a class description.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface IType {
    enum Kind{
        INTEGER,
        FLOAT,
        POINTER,
        REF, /*C++ Reference*/
        ARRAY,
        STRING,
        CLASS,
        STRUCT,
        UNION,
        ENUM,
    }
    
    Kind getKind();

    /**
     * Return the name of the type (e.g., "int"), or <code>null</code> if it has no name
     * (e.g., "char*").
     */
    String getName();
    
    /**
     * Return the number of bytes that variables of this type occupy, or 0 if
     * it isn't known.
     * @return  the number of bytes that variables of this type occupy, or 0 if
     * it isn't known.
     */
    int getSize();
    
    
}
