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

import com.arc.symbols.types.ITypeAggregate;


/**
 * A member of a class or struct.
 * 
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface ISymbolMember extends ISymbol {
    ITypeAggregate getParentType();
    /**
     * Return whether or not this member is "static". If not static, then
     * it is an "instance" member.
     * @return whether or not this member is "static".
     */
    boolean isStatic();
    
    Visibility getVisibility();
}
