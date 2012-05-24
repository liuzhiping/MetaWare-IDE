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
package com.arc.dwarf2.symbols;

import java.io.IOException;

import org.eclipse.cdt.utils.elf.Elf;

import com.arc.dwarf2.Dwarf2ReaderFactory;
import com.arc.dwarf2.internal.symbols.Dwarf2SymbolReader;
import com.arc.dwarf2.model.IDwarf2ErrorReporter;
import com.arc.dwarf2.model.IDwarf2Reader;
import com.arc.symbols.ISymbolReader;

/**
 * Create ISymbolReader instances from Dwarf2.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class Dwarf2Symbols {
    public static ISymbolReader readDwarf2(IDwarf2Reader reader){
        return new Dwarf2SymbolReader(reader);
    }
    
    public static ISymbolReader readDwarf2(Elf elfFile, IDwarf2ErrorReporter errorReporter) throws IllegalArgumentException, IOException{
        return readDwarf2(Dwarf2ReaderFactory.createInfoReader(elfFile, errorReporter));
    }
}
