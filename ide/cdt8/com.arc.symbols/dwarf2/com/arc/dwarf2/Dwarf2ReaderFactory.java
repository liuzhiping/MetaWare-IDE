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
package com.arc.dwarf2;

import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.cdt.utils.elf.Elf;

import com.arc.dwarf2.internal.elf.ElfCompositeSectionAccess;
import com.arc.dwarf2.internal.model.ArangesSectionReader;
import com.arc.dwarf2.internal.model.Dwarf2Reader;
import com.arc.dwarf2.internal.model.LineSectionReader;
import com.arc.dwarf2.model.IArangesSectionReader;
import com.arc.dwarf2.model.IDwarf2ErrorReporter;
import com.arc.dwarf2.model.IDwarf2Reader;
import com.arc.dwarf2.model.ILineSectionReader;
import com.arc.dwarf2.model.ISectionAccess;


/**
 * The factory for creating the instance of {@link IDwarf2Reader}.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class Dwarf2ReaderFactory {
    private static Map<Object,Dwarf2Reader> dwarfReaders = new WeakHashMap<Object,Dwarf2Reader>();
    private static Map<Object,LineSectionReader> lineReaders = new WeakHashMap<Object,LineSectionReader>();
    private static Map<Object,ArangesSectionReader> arangesReaders = new WeakHashMap<Object,ArangesSectionReader>();
    /**
     * Create an instance of the Dwarf 2 reader. The client must provide a callback
     * interface to access section data and to receive error messages to diagnose bad DWARF.
     * <P>
     * The error reporter callback may be invoked during construction, or later as the Dwarf
     * information is read lazily.
     * <P>
     * @param sectionAccess the callback interface for accessing section data.
     * @param errorReporter a callback interface by which dwarf formatting errors can be reported.
     * @return newly created Dwarf 2 info reader.
     * @throws IllegalArgumentException if any of the parameters are <code>null</code>.
     */
    public static IDwarf2Reader createInfoReader(ISectionAccess sectionAccess, IDwarf2ErrorReporter errorReporter) throws IllegalArgumentException {
        Dwarf2Reader reader = dwarfReaders.get(sectionAccess);
        if (reader == null) {
            reader = new Dwarf2Reader(sectionAccess, errorReporter);
            dwarfReaders.put(sectionAccess,reader);
        }
        return reader;
    }
    
    /**
     * Create an instance of the Dwarf 2 reader from an ELF file object.
     * <P>
     * The error reporter callback may be invoked during construction, or later as the Dwarf
     * information is read lazily.
     * <P>
     * @param elfFile the callback interface for accessing section data.
     * @param errorReporter a callback interface by which dwarf formatting errors can be reported.
     * @return newly created Dwarf 2 info reader.
     * @throws IllegalArgumentException if any of the parameters are <code>null</code>.
     * @throws IOException 
     */
    public static IDwarf2Reader createInfoReader(Elf elfFile, IDwarf2ErrorReporter errorReporter) throws IllegalArgumentException, IOException {
        Dwarf2Reader reader = dwarfReaders.get(elfFile);
        if (reader == null) {
            reader = new Dwarf2Reader(new ElfCompositeSectionAccess(elfFile), errorReporter);
            dwarfReaders.put(elfFile,reader);
        }
        return reader;
    }

    /**
     * Create an instance of the Dwarf 2 line table reader. The client must provide a callback
     * interface to access section data and to receive error messages to diagnose bad DWARF.
     * <P>
     * The error reporter callback may be invoked during construction, or later as the Dwarf
     * information is read lazily.
     * <P>
     * @param sectionAccess the callback interface for accessing section data.
     * @param errorReporter a callback interface by which dwarf formatting errors can be reported.
     * @return newly created Dwarf 2 line table reader.
     * @throws IllegalArgumentException if any of the parameters are <code>null</code>.
     */
    public static ILineSectionReader createLineReader(ISectionAccess sectionAccess, IDwarf2ErrorReporter errorReporter) throws IllegalArgumentException {
        LineSectionReader reader = lineReaders.get(sectionAccess);
        if (reader == null) {
            reader = new LineSectionReader(sectionAccess,errorReporter);
            lineReaders.put(sectionAccess,reader);
        }
        return reader;    
    }
    
    /**
     * Create an instance of the Dwarf 2 line table reader from Elf object. 
     * <P>
     * The error reporter callback may be invoked during construction, or later as the Dwarf
     * information is read lazily.
     * <P>
     * @param elfFile the callback interface for accessing section data.
     * @param errorReporter a callback interface by which dwarf formatting errors can be reported.
     * @return newly created Dwarf 2 line table reader.
     * @throws IllegalArgumentException if any of the parameters are <code>null</code>.
     * @throws IOException 
     */
    public static ILineSectionReader createLineReader(Elf elfFile, IDwarf2ErrorReporter errorReporter) throws IllegalArgumentException, IOException {
        LineSectionReader reader = lineReaders.get(elfFile);
        if (reader == null) {
            reader = new LineSectionReader(new ElfCompositeSectionAccess(elfFile),errorReporter);
            lineReaders.put(elfFile,reader);
        }
        return reader;          
    }
    
    /**
     * Create an instance of the Dwarf 2 address-range table reader from Elf object. 
     * <P>
     * The error reporter callback may be invoked during construction, or later as the Dwarf
     * information is read lazily.
     * <P>
     * @param elfFile the callback interface for accessing section data.
     * @param errorReporter a callback interface by which dwarf formatting errors can be reported.
     * @return newly created Dwarf 2 line table reader.
     * @throws IllegalArgumentException if any of the parameters are <code>null</code>.
     * @throws IOException 
     */
    public static IArangesSectionReader createARangesReader(Elf elfFile, IDwarf2ErrorReporter errorReporter) throws IllegalArgumentException, IOException {
        ArangesSectionReader reader = arangesReaders.get(elfFile);
        if (reader == null) {
            reader = new ArangesSectionReader(new ElfCompositeSectionAccess(elfFile),errorReporter);
            arangesReaders.put(elfFile,reader);
        }
        return reader;          
    }
}
