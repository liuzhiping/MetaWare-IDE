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
package com.arc.dwarf2.internal.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.arc.dwarf2.model.IDwarf2ErrorReporter;
import com.arc.dwarf2.model.ILineSectionReader;
import com.arc.dwarf2.model.ILineTableReader;
import com.arc.dwarf2.model.ISectionAccess;
import com.arc.dwarf2.model.ISectionReader;


/**
 * The class the reads in the ".debug_line" table.
 * @todo davidp needs to add a class description.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class LineSectionReader implements ILineSectionReader {

    private IDwarf2ErrorReporter fReporter;
    private Extractor fReader;
    private List<ILineTableReader> fLineTableReaders = null;
    public LineSectionReader(ISectionAccess sectionAccess, IDwarf2ErrorReporter reporter){
        fReporter = reporter;
        ISectionReader reader;
        try {
            reader = sectionAccess.getSection(".debug_line");
        }
        catch (IOException e) {
            reporter.error(".debug_line",0,"Couldn't extract .debug_line: " + e.getMessage());
            return;
        }
        if (reader == null){
            reporter.error(".debug_line",0,".debug_line section is missing");
            fReader = null;
        }
        else
            fReader = Extractor.create(reader,0,reader.getLength(),sectionAccess.isLittleEndian());
    }
    
    @Override
    public List<ILineTableReader> getLineTables () {
        if (fLineTableReaders == null) {
            ArrayList<ILineTableReader> readers = new ArrayList<ILineTableReader>();
            if (fReader != null) {
                while (fReader.getOffset() < fReader.getLength()) {
                    readers.add(new LineTableReader(fReader, fReporter));
                }
            }
            fLineTableReaders = Collections.unmodifiableList(readers);
        }
        return fLineTableReaders;
    }

}
