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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.arc.dwarf2.DwarfConstants;
import com.arc.dwarf2.model.IAttribute;
import com.arc.dwarf2.model.IDwarf2ErrorReporter;
import com.arc.dwarf2.model.ILineSectionReader;
import com.arc.dwarf2.model.ILineTableReader;
import com.arc.dwarf2.model.ITag;
import com.arc.dwarf2.model.IUnit;


/**
 * Implementation of a compile unit.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
class Unit implements IUnit {
    private int fLength = 0;
    private int fVersion = 0;
    private int fAddrSize = 0;
    private int fAbbrOff = 0;
    private ITag fTag = null;
    private int fOffset = 0;
    private TagBuilder fBuilder;
    private ILineTableReader fLineTable = null;

    /**
	 * 
	 * @param info extractor for the ".debug_info" section, positioned at start
	 * of compile unit.
	 * @param abbrev extractor for the ".debug_abbrev" section.
	 * @param str extractor for the ".debug_str" section or <code>null</code> if there is no such section.
	 * @param reporter where errors are to be sent.
	 * @param lineReader interface to read line table.
	 */
	Unit(Extractor info, Extractor abbrev, Extractor str, IDwarf2ErrorReporter reporter, ILineSectionReader lineReader){

		fOffset = info.getOffset();
		int offset = info.getOffsetFromStart();
		
		try {
            fLength = info.readWord() + 4;
            fVersion = info.readUShort();
            fAbbrOff = info.readWord();
            fAddrSize = info.readByte();
            Extractor newInfo = info.createNew(offset,fLength);
    		newInfo.setOffsetFromStart(info.getOffsetFromStart() - offset);
    		Map<Integer,Abbrev> acodeTable = new HashMap<Integer,Abbrev>();
    		abbrev.setOffset(fAbbrOff);
    		while (abbrev.getOffset() < abbrev.getLength()){
    			int aoff = abbrev.getOffset();
    			int acode = abbrev.readULEB();
    			if (acode == 0) break;
    		    int tag = abbrev.readULEB();
    		    boolean hasChildren = abbrev.readByte() != 0;
    		    if (acodeTable.get(acode) != null){
    		    	reporter.error(abbrev.getName(),aoff,"Acode " + acode + " defined more than once");
    		    }
    		    Abbrev ap = new Abbrev(tag,acode,aoff,hasChildren);
    		    acodeTable.put(acode,ap);
    		    while (abbrev.getOffset() < abbrev.getLength()){
    		    	int at = abbrev.readULEB();
    		    	int form = abbrev.readULEB();
    		    	if (at == 0 && form == 0) break;
    		    	ap.add(at,form);
    		    }
    		}
    		
            fBuilder = new TagBuilder(acodeTable,newInfo,str,reporter);
            fTag = fBuilder.getTagAt(newInfo.getOffsetFromStart());
            if (fTag == null) {
    			reporter.error(newInfo.getName(),newInfo.getOffset(),"Bad acode reference in compile unit tag");
    			fTag = new DummyTag();
    		}
            else {
                IAttribute at = fTag.getAttribute(DwarfConstants.DW_AT_stmt_list);
                if (at != null) {
                    int lineTabOffset = (int)at.getIntValue();
                    List<ILineTableReader> ltReaders = lineReader.getLineTables();
                    for (ILineTableReader r: ltReaders){
                        if (r.getOffset() == lineTabOffset){
                            fLineTable = r;
                        }
                    }
                    if (fLineTable == null){
                        reporter.error(info.getName(),info.getOffset(),"stmt_list value is not valid: "+lineTabOffset);
                    }
                }
            }
        }
        catch (IOException e) {
            reporter.error(info.getName(),info.getOffset(), e.getMessage());
            fTag = new DummyTag();
        }		
	}
	
	static class DummyTag implements ITag {
        @Override
        public List<IAttribute> getAttributes () {
            return new ArrayList<IAttribute>(0);
        }

        @Override
        public List<ITag> getChildren () {
        	return new ArrayList<ITag>(0);
        }

        @Override
        public int getID () {
            return DwarfConstants.DW_TAG_compile_unit;
        }

        @Override
        public int getOffset () {
            return 0;
        }

        @Override
        public int getAbbrevOffset () {
            // @todo Auto-generated method stub
            return 0;
        }

        @Override
        public int getAcode () {
            // @todo Auto-generated method stub
            return 0;
        }

        @Override
        public boolean hasChildren () {
            // @todo Auto-generated method stub
            return false;
        }

        @Override
        public IAttribute getAttribute (int atID) {
            // @todo Auto-generated method stub
            return null;
        }
	}

    @Override
    public int getAbbrevOffset () {
        return fAbbrOff;
    }

   
    @Override
    public int getAddressSize () {
        return fAddrSize;
    }

    @Override
    public int getLength () {
        return fLength;
    }

    @Override
    public ITag getTag () {
        return fTag;
    }

    @Override
    public int getVersion () {
        return fVersion;
    }


    @Override
    public int getInfoOffset () {
        return fOffset;
    }
    
    @Override
    public ITag getTagRef(int offset){
        return fBuilder.getTagAt(offset);
    }


    @Override
    public ILineTableReader getLineTableReader () {
        return fLineTable;
    }

}
