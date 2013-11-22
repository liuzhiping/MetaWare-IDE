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
import java.util.Iterator;
import java.util.List;

import com.arc.dwarf2.model.IArange;
import com.arc.dwarf2.model.IArangesSectionReader;
import com.arc.dwarf2.model.IDwarf2ErrorReporter;
import com.arc.dwarf2.model.ISectionAccess;
import com.arc.dwarf2.model.ISectionReader;


public class ArangesSectionReader implements IArangesSectionReader {

    private static final String DEBUG_ARANGES = ".debug_aranges";
    private IDwarf2ErrorReporter fReporter;
    private Extractor fReader;
    private List<IArange> fList;

    public ArangesSectionReader(ISectionAccess sectionAccess, IDwarf2ErrorReporter reporter){
        fReporter = reporter;
        ISectionReader reader;
        try {
            reader = sectionAccess.getSection(DEBUG_ARANGES);
        }
        catch (IOException e) {
            reporter.error(DEBUG_ARANGES,0,"Couldn't extract .debug_aranges: " + e.getMessage());
            return;
        }
        if (reader == null){
            //reporter.error(".debug_line",0,".debug_line section is missing");
            fReader = null;
        }
        else
            fReader = Extractor.create(reader,0,reader.getLength(),sectionAccess.isLittleEndian());
    }
    
    @Override
    public Iterator<IArange> each() {
        if (fList == null){
            fList = new ArrayList<IArange>(fReader==null?0:100);
            if (fReader != null) {
                try {
                    while (fReader.getOffsetFromStart() < fReader.getLength()){
                        int baseOffset = fReader.getOffset();
                        int length = fReader.readWord();
                        boolean dwarf64 = false;
                        if (length == -1){
                            length = (int)fReader.readLong();
                            dwarf64 = true;
                        }
                        int offsetOfEnd = fReader.getOffset() + length;
                        int version = fReader.readUShort();
                        int compOffset = dwarf64?(int)fReader.readLong():fReader.readWord();
                        int addressSize = fReader.readByte();
                        if (addressSize != 4 && addressSize != 8){
                            fReporter.error(DEBUG_ARANGES,fReader.getOffset(), "Address size for Aranges entry appears to be nonsense: " + addressSize);
                            break;
                        }
                        int segSize = fReader.readByte();
                        if (segSize != 0){
                            fReporter.error(DEBUG_ARANGES,fReader.getOffset(), "Segment size for Aranges is non-zero(= " + 
                                segSize + "); such are not supported");
                            break;
                        }
                        
                        // Following "tuples" are required to be aligned.
                        int offsetDelta = fReader.getOffset() - baseOffset;
                        int tupleSize = addressSize*2;
                        int bdy = offsetDelta % tupleSize;
                        if (bdy != 0){
                            fReader.skipBytes(tupleSize-bdy);
                        }                       
                        
                        Arange a = new Arange(length,version,compOffset,addressSize,segSize);
                        fList.add(a);
                        boolean rangeSeen = false;
                        while (fReader.getOffset() < offsetOfEnd){
                            long adr,len;
                            int aoff = fReader.getOffset();
                            if (addressSize == 4){
                                adr = fReader.readWord();
                                len = fReader.readWord();
                            }
                            else {
                                adr = fReader.readLong();
                                len = fReader.readLong();
                            }
                            if (adr == 0 && len == 0){
                                break;
                            }
                            rangeSeen = true;
                            a.addRange(new IArange.Range(adr,(int)len,aoff));
                        }
                        if (!rangeSeen){
                            fReporter.error(DEBUG_ARANGES,fReader.getOffset(),
                                "Arange entry has no ranges!");
                            
                        }
                        if (offsetOfEnd != fReader.getOffset()){
                            fReporter.error(DEBUG_ARANGES,baseOffset,"arange table did not end where expected at 0x" + 
                                Integer.toHexString(offsetOfEnd) +", but at 0x" + Integer.toHexString(fReader.getOffset()) + " instead");
                            break;
                        }
                    }
                }
                catch (IOException e) {
                    fReporter.error(DEBUG_ARANGES, fReader.getOffset(), "Section ends prematurely");
                }
            }
        }
        return fList.iterator();
    }

    @Override
    public IArange getCompilationUnit (int offset) {
        Iterator<IArange> each = this.each();
        while (each.hasNext()){
            IArange a = each.next();
            if (a.getCompilationUnitOffset() == offset){
                return a;
            }
        }
        return null;
    }
}
