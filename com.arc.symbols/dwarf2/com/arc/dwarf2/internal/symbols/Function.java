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
package com.arc.dwarf2.internal.symbols;

import java.util.Map;

import com.arc.dwarf2.model.ITag;
import com.arc.dwarf2.model.IUnit;
import com.arc.symbols.types.IType;


class Function extends Symbol {

    public Function(ITag tag, Map<ITag, IType> typeMap, IUnit unit) {
        super(tag, typeMap, unit);
        // @todo Auto-generated constructor stub
    }

    @Override
    public Kind getKind () {
        return Kind.FUNCTION;
    }

}
