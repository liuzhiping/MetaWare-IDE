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

import com.arc.dwarf2.DwarfConstants;
import com.arc.dwarf2.model.IDwarf2ErrorReporter;
import com.arc.dwarf2.model.ILineTableReader;


class LineTableReader implements ILineTableReader {

    private Extractor fReader;

    private IDwarf2ErrorReporter fReporter;

    private int fLength;

    private int fVersion;

    private int fHeaderEnd;

    private int fInstructionLength;

    private boolean fDefaultIsStatement;

    private int fLineBase;

    private int fLineRange;

    private int fOpcodeBase;

    private byte[] fOpcodeOperandCounts;

    private List<String> fIncludes;

    private List<FileRef> fFiles;

    private int fOffset;

    public LineTableReader(Extractor reader, IDwarf2ErrorReporter reporter) {
        try {
            if (reporter == null) throw new IllegalArgumentException("reporter is null");
            fReporter = reporter;
            fOffset = reader.getOffset();
            fLength = reader.readWord();
            if (fOffset + fLength > reader.getLength()) {
                reporter.error(reader.getName(),fOffset,"Bogus line table length: " + fLength);
                return;
            }
            fReader = reader.createNew(fOffset,fLength+4);
            reader.setOffset(fOffset + fLength+4); // reference next table.
            fReader.setOffset(fOffset+4);
            fVersion = fReader.readUShort();
            if (fVersion > 2) {
                reporter.error(fReader.getName(), fReader.getOffset() - 2, "Unrecognized line table version: "
                    + fVersion);
            }
            int headerOff = fReader.getOffset();
            fHeaderEnd = fReader.readWord() + headerOff + 4;
            fInstructionLength = fReader.readByte();
            fDefaultIsStatement = fReader.readByte() != 0;
            fLineBase = (byte) fReader.readByte();
            fLineRange = fReader.readByte();
            if (fLineRange == 0) {
                reporter.error(fReader.getName(), fReader.getOffset() - 1, "Line range is zero!");
                fLineRange = 1;
            }
            fOpcodeBase = fReader.readByte();
            fOpcodeOperandCounts = new byte[fOpcodeBase];
            for (int i = 1; i < fOpcodeBase; i++) {
                fOpcodeOperandCounts[i - 1] = (byte) fReader.readByte();
            }

            // Read in includes.
            fIncludes = new ArrayList<String>();
            while (true) {
                String s = fReader.readString();
                if (s == null || s.length() == 0)
                    break;
                fIncludes.add(s);
            }
            fIncludes = Collections.unmodifiableList(fIncludes);

            fFiles = new ArrayList<FileRef>();
            while (true) {
                String fileName = fReader.readString();
                if (fileName == null || fileName.length() == 0)
                    break;
                FileRef fileRef = new FileRef();
                fileRef.name = fileName;
                fileRef.includeIndex = fReader.readULEB();
                if (fileRef.includeIndex < 0 || fileRef.includeIndex > fIncludes.size()) {
                    reporter.error(fReader.getName(), fReader.getOffset() - 1, "Include reference is out of bounds: "
                        + fileRef.includeIndex);
                }
                fileRef.modTime = fReader.readULEB();
                fileRef.size = fReader.readULEB();
                fFiles.add(fileRef);
            }
            fFiles = Collections.unmodifiableList(fFiles);
            // We should now be at the header end.
            if (fReader.getOffset() > fHeaderEnd) {
                reporter.error(fReader.getName(), headerOff, "Header length is wrong; actual=0x"
                    + Integer.toHexString(fHeaderEnd - headerOff)
                    + "; should be 0x"
                    + Integer.toHexString(fReader.getOffset() - headerOff));
                fHeaderEnd = fReader.getOffset();
            }
        }
        catch (IOException e) {
            reporter.error(fReader.getName(), fReader.getOffset(), "line table read error: " + e.getMessage());
        }

    }

    @Override
    public void eachStatement (IStatementReceiver receiver) {
        if (fReader == null) return; // construction error
        fReader.setOffset(fHeaderEnd);
        int end = fOffset + fLength + 4;
        int lineNo = 1;
        int pc = 0;
        int column = 0;
        boolean isStatement = this.fDefaultIsStatement;
        receiver.setIsStatement(isStatement);
        if (fFiles.size() > 0){
            setFile(receiver,0);
        }
        try {
            while (fReader.getOffset() < end) {
                int op = fReader.readByte();
                if (op >= fOpcodeBase){
                    int adjustedOpcode = op - fOpcodeBase;
                    int a = adjustedOpcode/fLineRange * fInstructionLength;
                    int l = fLineBase + adjustedOpcode % fLineRange;
                    lineNo += l;
                    pc += a;
                    receiver.addLine(pc,lineNo,column);
                }
                else if (op == 0) {
                    // Extended op
                    int xoplen = fReader.readULEB();
                    int xop = fReader.readByte();
                    switch (xop){
                    case DwarfConstants.DW_LNE_end_sequence:
                        break; // is this the end of the table?
                    case DwarfConstants.DW_LNE_set_address:
                        pc = fReader.readWord();
                        break;
                    case DwarfConstants.DW_LNE_define_file:{
                        String file = fReader.readString();
                        String include = ".";
                        int includeIndex = fReader.readULEB();
                        if (includeIndex > fIncludes.size())
                            fReporter.error(fReader.getName(),fReader.getOffset()-1,"Bad include index: " + includeIndex);
                        else include =  includeIndex > 0?fIncludes.get(includeIndex-1):".";
                        int modTime = fReader.readULEB();
                        int size = fReader.readULEB();
                        receiver.setFile(include,file, modTime, size);
                        break;
                    }
                    default:
                        fReader.skipBytes(xoplen-1);
                        fReporter.error(fReader.getName(),fReader.getOffset()-1,"Unrecognized extended opcode: " + xop);
                        break;
                    }
                    
                }
                else switch(op){
                case DwarfConstants.DW_LNS_copy:
                    receiver.addLine(pc,lineNo,column);
                    break;
                case DwarfConstants.DW_LNS_advance_pc:
                    pc += fReader.readULEB() * fInstructionLength;
                    break;
                case DwarfConstants.DW_LNS_advance_line:
                    lineNo += fReader.readSLEB();
                    break;
                case DwarfConstants.DW_LNS_set_file: {
                    int i = fReader.readULEB();
                    setFile(receiver, i-1);
                    break;
                }
                case DwarfConstants.DW_LNS_set_column:
                    column = fReader.readULEB();
                    break;
                case DwarfConstants.DW_LNS_negate_stmt:
                    isStatement = !isStatement;
                    receiver.setIsStatement(isStatement);
                    break;
                case DwarfConstants.DW_LNS_set_basic_block: 
                    receiver.setBasicBlock();
                    break;
                    
                    
                case DwarfConstants.DW_LNS_const_add_pc:{
                    int adjustedOpcode = 255 - fOpcodeBase;
                    int a = adjustedOpcode/fLineRange * fInstructionLength;
                    int l = fLineBase + adjustedOpcode % fLineRange;
                    lineNo += l;
                    pc += a;
                    break;
                }
                    
                case DwarfConstants.DW_LNS_fixed_advance_pc:
                    pc += fReader.readUShort();
                    break;
                case DwarfConstants.DW_LNS_set_prologue_end:
                    receiver.setPrologEnd();
                    break;
                case DwarfConstants.DW_LNS_set_epilog_begin:
                    receiver.setEpilogBegin();
                }
                
            }
        }
        catch (Exception e) {
            fReporter.error(fReader.getName(),fReader.getOffset(),"Line table read error: " + e.getMessage());
        }

    }

    /**
     * @todo davidp needs to add a method comment.
     * @param receiver
     * @param i
     */
    private void setFile (IStatementReceiver receiver, int i) {
        if ( i < 0 || i >= fFiles.size())
            fReporter.error(fReader.getName(),fReader.getOffset()-1,"Bad file index: " + i);
        else {
            FileRef f = fFiles.get(i);
            String include = ".";
            if (f.includeIndex > 0) include = fIncludes.get(f.includeIndex-1);
            receiver.setFile(include, f.name, f.modTime, f.size);
        }
    }

    @Override
    public boolean getDefaultIsStatement () {
        return this.fDefaultIsStatement;
    }

    @Override
    public List<FileRef> getFiles () {
        return fFiles;
    }

    @Override
    public List<String> getIncludes () {
        return fIncludes;
    }

    @Override
    public int getInstructionLength () {
        return this.fInstructionLength;
    }

    @Override
    public int getLength () {
        return this.fLength;
    }

    @Override
    public int getLineBase () {
        return this.fLineBase;
    }

    @Override
    public int getLineRange () {
        return this.fLineRange;
    }

    @Override
    public int getOffset () {
        return this.fOffset;
    }

    @Override
    public int getOpcodeBase () {
        // @todo Auto-generated method stub
        return 0;
    }

    @Override
    public byte[] getStandardOpcodeOperandCounts () {
        // @todo Auto-generated method stub
        return null;
    }

    @Override
    public int getVersion () {
        // @todo Auto-generated method stub
        return 0;
    }

}
