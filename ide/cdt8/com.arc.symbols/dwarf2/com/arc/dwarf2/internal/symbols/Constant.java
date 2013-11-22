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

import com.arc.dwarf2.DwarfConstants;
import com.arc.dwarf2.model.IAttribute;
import com.arc.dwarf2.model.ITag;
import com.arc.dwarf2.model.IUnit;
import com.arc.symbols.ISymbolConstant;
import com.arc.symbols.types.IType;


class Constant extends Symbol implements ISymbolConstant {

    private long fValue = 0;

    Constant(ITag tag, Map<ITag,IType>typeMap, IUnit unit){
        super(tag,typeMap,unit);
        IAttribute attr = tag.getAttribute(DwarfConstants.DW_AT_const_value);
        if (attr != null){
            fValue = attr.getIntValue();
        }
    }
    @Override
    public long getValue () {
        return fValue;
    }

    @Override
    public Kind getKind () {
        return Kind.CONSTANT;
    }

   

}
