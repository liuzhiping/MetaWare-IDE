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
package com.arc.dwarf2.internal.elf;


import java.io.IOException;

import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.cdt.utils.elf.Elf.Section;

import com.arc.dwarf2.model.ISectionAccess;
import com.arc.dwarf2.model.ISectionReader;


/**
 * An implementation of Section access based on ELF object.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class ElfSectionAccess implements ISectionAccess {

    private Elf fElf;

    public ElfSectionAccess(Elf elf) {
        fElf = elf;
    }

    @Override
    public ISectionReader getSection (String name) throws IOException {
        Section section = fElf.getSectionByName(name);
        if (section != null) {
            return new ElfSectionReader(section.loadSectionData(), name);
        }
        return null;
    }

    @Override
    public boolean isLittleEndian () {
        try {
            return fElf.getELFhdr().e_ident[Elf.ELFhdr.EI_DATA] == Elf.ELFhdr.ELFDATA2LSB;
        }
        catch (IOException e) {
            return false;
        }
    }

}
