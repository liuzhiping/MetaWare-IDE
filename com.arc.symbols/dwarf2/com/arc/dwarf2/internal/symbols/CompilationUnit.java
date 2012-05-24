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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.arc.dwarf2.DwarfConstants;
import com.arc.dwarf2.model.IAttribute;
import com.arc.dwarf2.model.ITag;
import com.arc.dwarf2.model.IUnit;
import com.arc.symbols.ICompilationUnit;
import com.arc.symbols.ISymbol;
import com.arc.symbols.types.IType;


class CompilationUnit implements ICompilationUnit {
    private IUnit fUnit;
    private List<ITag> fChildren = null;
    private List<ISymbol>fSymbols = null;
    private Map<ITag, ISymbol> fSymbolMap;
    private Map<ITag, IType> fTypeMap;

    CompilationUnit(IUnit unit, Map<ITag,ISymbol>symbolMap, Map<ITag,IType>typeMap){
        ITag tag = unit.getTag();
        if (tag == null || tag.getID() != DwarfConstants.DW_TAG_compile_unit){
            throw new IllegalArgumentException("Not a valid compile_unit tag");
        }
        fUnit = unit;
        fSymbolMap = symbolMap;
        fTypeMap = typeMap;
    }
    
    @Override
    public String getSourcePath () {
        IAttribute attr = fUnit.getTag().getAttribute(DwarfConstants.DW_AT_name);
        if (attr == null) return null;
        return attr.getStringValue();
    }

    @Override
    public List<ISymbol> getSymbols () {
        if (fSymbols == null) {
            if (fChildren == null) {
                fChildren = fUnit.getTag().getChildren();
            }
            List<ISymbol> list = new ArrayList<ISymbol>(fChildren.size());
            for (ITag kid : fChildren) {
                ISymbol sym = fSymbolMap.get(kid);
                if (sym == null) {
                    switch (kid.getID()) {
                        case DwarfConstants.DW_TAG_constant:
                           sym = new Constant(kid,fTypeMap, fUnit);
                           break;
                        case DwarfConstants.DW_TAG_variable:
                            sym = new Variable(kid,fTypeMap, fUnit);
                            break;
                        case DwarfConstants.DW_TAG_subprogram:
                            sym = new Function(kid,fTypeMap, fUnit);
                            break;
                        // TODO: Handle types
                    }
                    if (sym != null)
                        fSymbolMap.put(kid,sym);
                }
                if (sym != null)
                    list.add(sym);
            }
            fSymbols = Collections.unmodifiableList(list);
        }
        return fSymbols;
    }
    
}
