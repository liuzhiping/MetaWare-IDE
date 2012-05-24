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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.arc.dwarf2.DwarfConstants;
import com.arc.dwarf2.model.IDwarf2Reader;
import com.arc.dwarf2.model.ITag;
import com.arc.dwarf2.model.IUnit;
import com.arc.symbols.ICompilationUnit;
import com.arc.symbols.ISymbol;
import com.arc.symbols.ISymbolReader;
import com.arc.symbols.types.IType;

/**
 * Reads Dwarf information and wraps it with our Symbols hierarchy.
 * @todo davidp needs to add a class description.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class Dwarf2SymbolReader implements ISymbolReader {
    private IDwarf2Reader dwarf2;
    private List<ICompilationUnit> fUnits = null;
    private List<ISymbol> fGlobals = null;
    
    private Map<ITag, ISymbol> fSymbolMap = null;
    private Map<ITag, IType> fTypeMap = null;
    
    public Dwarf2SymbolReader(IDwarf2Reader dwarf2){
        this.dwarf2 = dwarf2;
    }
    @Override
    public List<ISymbol> getGlobalVariables () {
        if (fGlobals == null){
            ArrayList<ISymbol>list = new ArrayList<ISymbol>(1000);
            Set<String> seen = new HashSet<String>(1000);
            for (ICompilationUnit unit: getCompilationUnits()){
                for (ISymbol sym: unit.getSymbols()){
                    switch(sym.getKind()){
                        case VARIABLE:
                        case CONSTANT:
                            if (sym.isGlobal()){
                                if (seen.add(sym.getName())){
                                    list.add(sym);
                                }
                            }
                            break;
                        default: break;                               
                    }
                }
            }
            list.trimToSize();
            fGlobals = Collections.unmodifiableList(list);
        }
        return fGlobals;
    }

    @Override
    public List<ICompilationUnit> getCompilationUnits () {
        if (fUnits == null){
            fSymbolMap = new HashMap<ITag,ISymbol>(1000);
            fTypeMap = new HashMap<ITag,IType>(100);
            List<IUnit> units = dwarf2.getUnits();
            fUnits  = new ArrayList<ICompilationUnit>(units.size());
            for (IUnit unit:units){
                ITag tag = unit.getTag();
                if ( tag.getID() == DwarfConstants.DW_TAG_compile_unit) {
                    fUnits.add(new CompilationUnit(unit, fSymbolMap,fTypeMap));
                }
            }
            fUnits = Collections.unmodifiableList(fUnits);
        }
        return fUnits;
    }
}
