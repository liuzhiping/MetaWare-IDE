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
import com.arc.dwarf2.model.ILineTableReader;
import com.arc.dwarf2.model.ITag;
import com.arc.dwarf2.model.IUnit;
import com.arc.symbols.ISymbol;
import com.arc.symbols.types.IType;


abstract class Symbol implements ISymbol {
    private String fName;
    private IType fType;
    private boolean fGlobal;
    private ILineTableReader.FileRef fSource;
    private int fLine = 0;
    private IUnit fUnit;
    private String fLinkageName;
    
    protected Symbol(ITag tag, Map<ITag,IType>typeMap, IUnit unit){
        fUnit = unit;
        fName = Utility.getStringAttribute(tag,DwarfConstants.DW_AT_name);
        fType = Utility.getTypeAttribute(tag,DwarfConstants.DW_AT_type,typeMap, unit);
        fGlobal = Utility.getBooleanAttribute(tag,DwarfConstants.DW_AT_external);
        fSource = Utility.getFileRefAttribute(tag,DwarfConstants.DW_AT_decl_file,unit);
        IAttribute at = tag.getAttribute(DwarfConstants.DW_AT_decl_line);
        if (at != null){
            fLine = (int)at.getIntValue();
        }
        fLinkageName = fName;
        String m = Utility.getStringAttribute(tag,DwarfConstants.DW_AT_MIPS_linkage_name);
        if (m != null) fLinkageName = m;
    }
    
    @Override
    abstract public Kind getKind();

    @Override
    public String getName () {
        return fName;
    }

    @Override
    public IType getType () {
        return fType;
    }
    
    @Override
    public String getLinkageName(){
        return fLinkageName;
    }

    @Override
    public boolean isGlobal () {
        return fGlobal;
    }

    @Override
    public String getSourceFile () {
        if (fSource != null) {
            if (fSource.includeIndex > 0){
                ILineTableReader reader = fUnit.getLineTableReader();
                String dir;
                try {
                    dir = reader.getIncludes().get(fSource.includeIndex-1);
                }
                catch (RuntimeException e) {
                    dir = "?" + fSource.includeIndex + "?";
                }
                return dir + "/" + fSource.name;
            }
            return fSource.name;
        }
        return null;
    }

    @Override
    public int getSourceLine () {
        return fLine;
    }

}
