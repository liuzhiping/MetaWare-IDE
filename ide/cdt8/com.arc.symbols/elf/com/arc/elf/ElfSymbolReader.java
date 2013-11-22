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
package com.arc.elf;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.cdt.utils.elf.Elf.Symbol;

import com.arc.elf.internal.symbols.ElfSymbol;
import com.arc.symbols.ISymbol;


public class ElfSymbolReader {

    public static ISymbol[] getGlobalVariables (Elf elf) throws IOException {
        elf.loadSymbols();
        if (elf.getSymbols() != null) {
            List<ISymbol> list = new ArrayList<ISymbol>(400);
            for (Symbol s : elf.getSymbols()) {
                if (s.st_bind() == Symbol.STB_GLOBAL && s.st_type() == Symbol.STT_OBJECT) {
                    list.add(new ElfSymbol(s));
                }
            }
            return list.toArray(new ISymbol[list.size()]);
        }
        return new ISymbol[0];
        
    }
}
