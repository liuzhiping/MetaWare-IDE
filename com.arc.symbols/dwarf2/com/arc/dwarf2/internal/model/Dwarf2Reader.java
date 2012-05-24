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

import com.arc.dwarf2.Dwarf2ReaderFactory;
import com.arc.dwarf2.DwarfConstants;
import com.arc.dwarf2.model.IDwarf2ErrorReporter;
import com.arc.dwarf2.model.IDwarf2Reader;
import com.arc.dwarf2.model.ILineSectionReader;
import com.arc.dwarf2.model.ISectionAccess;
import com.arc.dwarf2.model.ISectionReader;
import com.arc.dwarf2.model.IUnit;


/**
 * The implementation of the Dwarf 2 reader.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class Dwarf2Reader implements IDwarf2Reader {
    private List<IUnit> fUnits = new ArrayList<IUnit>(0);
    private ISectionReader fDebugInfo;
	/**
	 * 
	 * Construct an instance from the callback interfaces.
	 * @param sectionAccess callback for accessing section data.
	 * @param errorReporter callback for reporting errors in the Dwarf2 format.
	 */
	public Dwarf2Reader(ISectionAccess sectionAccess, IDwarf2ErrorReporter errorReporter){
		if (sectionAccess == null || errorReporter == null) throw new IllegalArgumentException("Argument is null");
		ISectionReader debugInfo;
        ISectionReader debugAbbrev;
        ISectionReader debugStr;
        try {
            debugInfo = sectionAccess.getSection(DwarfConstants.DEBUG_INFO);
            debugAbbrev = sectionAccess.getSection(DwarfConstants.DEBUG_ABBREV);
            debugStr = sectionAccess.getSection(DwarfConstants.DEBUG_STR);
        }
        catch (IOException e) {
            errorReporter.error(DwarfConstants.DEBUG_INFO,0,"Failure to read section data: " + e.getMessage());
            return;
        }
		if (debugInfo == null) {
			errorReporter.error(DwarfConstants.DEBUG_INFO,0,"Section \"" + DwarfConstants.DEBUG_INFO + "\" is missing");
			return;
		}
		if (debugAbbrev == null) {
			errorReporter.error(DwarfConstants.DEBUG_ABBREV,0,"Section \"" + DwarfConstants.DEBUG_ABBREV +"\" is missing");
			return;
		}
		Extractor str = debugStr != null?Extractor.create(debugStr,0,debugStr.getLength(),sectionAccess.isLittleEndian()):null;
		List<IUnit> units = new ArrayList<IUnit>();
		ILineSectionReader lineReader = Dwarf2ReaderFactory.createLineReader(sectionAccess, errorReporter);
		if (debugInfo.getLength() > 0){
	    	Extractor info = Extractor.create(debugInfo,0,debugInfo.getLength(),sectionAccess.isLittleEndian());
		    Extractor abbrev = Extractor.create(debugAbbrev,0,debugAbbrev.getLength(),sectionAccess.isLittleEndian());
		    while(info.getOffset() < debugInfo.getLength()){
		    	int offset = info.getOffset();
		    	Unit unit = new Unit(info,abbrev,str,errorReporter,lineReader);
                if (offset + unit.getLength() > debugInfo.getLength())
                    errorReporter.error(info.getName(),info.getOffset(),"Compile unit table length exceeds size of section");
                else
                    info.setOffset(offset + unit.getLength());	    	
                units.add(unit);
		    }
		}
		fDebugInfo = debugInfo;
		fUnits  = Collections.unmodifiableList(units);
	}

    @Override
    public List<IUnit> getUnits () {
        return fUnits;
    }

    /* (non-Javadoc)
     * @see com.arc.dwarf2.model.IDwarf2Reader#getInfoName(int)
     */
    @Override
    public String getInfoName (int infoOffset) {
        return this.fDebugInfo.getActualName(infoOffset);
    }

    /* (non-Javadoc)
     * @see com.arc.dwarf2.model.IDwarf2Reader#getInfoOffset(int)
     */
    @Override
    public int getInfoOffset (int infoOffset) {
        return this.fDebugInfo.getSectionOffset(infoOffset);
    }
}
