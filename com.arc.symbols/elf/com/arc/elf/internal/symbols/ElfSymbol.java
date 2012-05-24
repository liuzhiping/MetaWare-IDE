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
package com.arc.elf.internal.symbols;

import org.eclipse.cdt.utils.elf.Elf.Symbol;

import com.arc.symbols.ISymbol;
import com.arc.symbols.types.IType;


public class ElfSymbol implements ISymbol {

    private Symbol fSym;

    public ElfSymbol(Symbol sym){
        fSym = sym;
    }
    @Override
    public Kind getKind () {
        return fSym.st_type() == Symbol.STT_FUNC? Kind.FUNCTION:Kind.VARIABLE;
    }

    @Override
    public String getLinkageName () {
        return getName();
    }

    @Override
    public String getName () {
        return fSym.toString();
    }

    @Override
    public String getSourceFile () {
        return null;
    }

    @Override
    public int getSourceLine () {
        // @todo Auto-generated method stub
        return 0;
    }

    @Override
    public IType getType () {
        return null;
    }

    @Override
    public boolean isGlobal () {
        return fSym.st_bind() == Symbol.STB_GLOBAL;
    }

}
