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


import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.eclipse.cdt.utils.elf.Elf;

import com.arc.dwarf2.model.IArange;
import com.arc.dwarf2.model.IArangesSectionReader;
import com.arc.dwarf2.model.IAttribute;
import com.arc.dwarf2.model.IDwarf2ErrorReporter;
import com.arc.dwarf2.model.IDwarf2Reader;
import com.arc.dwarf2.model.ILineSectionReader;
import com.arc.dwarf2.model.ILineTableReader;
import com.arc.dwarf2.model.ITag;
import com.arc.dwarf2.model.IUnit;


public class Dwarf2Dump {

    static void dumpDwarf (String filename, PrintStream stream) throws IllegalArgumentException, IOException {
        Elf elf = new Elf(filename);
        
        IDwarf2ErrorReporter reporter = new IDwarf2ErrorReporter() {

            @Override
            public void error (String section, int offset, String message) {
                System.err.println("From section \"" +
                    section +
                    "\" near offset 0x" +
                    Integer.toHexString(offset) +
                    ": " +
                    message);

            }
        };
        IDwarf2Reader reader = Dwarf2ReaderFactory.createInfoReader(elf, reporter);
        IArangesSectionReader aranges = Dwarf2ReaderFactory.createARangesReader(elf, reporter);
        stream.printf(".debug_info contents\n");
        int unitCnt = 0;
        for (IUnit unit : reader.getUnits()) {
            dumpUnit(reader, ++unitCnt, unit, stream, aranges);
        }

        ILineSectionReader lineReader = Dwarf2ReaderFactory.createLineReader(elf, reporter);
        stream.printf("\n\nLine table\n");
        for (ILineTableReader lr : lineReader.getLineTables()) {
            dumpLineTable(lr, stream);
        }
    }

    static void dumpUnit (IDwarf2Reader reader, int unitCnt, IUnit unit, PrintStream out, IArangesSectionReader aranges) {
        out.printf("    Compilation unit #%d at 0x%x", unitCnt, unit.getInfoOffset());
        out.printf("        Length=%d(0x%x) version=%d address_size=%d\n", unit.getLength(), unit.getLength(), unit
            .getVersion(), unit.getAddressSize());
        IArange arange = aranges.getCompilationUnit(unit.getInfoOffset());
        if (arange != null) {
            out.printf("        Address Ranges\n");
            for (IArange.Range range : arange.getRanges()) {
                out.printf(
                    "            0x%x..0x%x (length=0x%x)\n",
                    range.address,
                    range.address + range.length,
                    range.length);
            }
        }
        ITag tag = unit.getTag();
        if (tag != null) {
            dumpTag(reader, tag, 8, out, unit.getInfoOffset());
        }
    }

    static void dumpTag (IDwarf2Reader reader, ITag tag, int indentBy, PrintStream out, int unitOrigin) {
        int tagOff = tag.getOffset();
        int actual = reader.getInfoOffset(tagOff + unitOrigin);
        if (actual == 0){
            out.printf(" --- %s --- \n", reader.getInfoName(tagOff+unitOrigin));
        }
        String hex = "0x" + Integer.toHexString(tagOff);
        out.print(hex);
        if (indentBy > hex.length())
            indent(out, indentBy - hex.length());
        out.printf("%s, Acode #%d, DIE=0x%x, aoff=0x%x%s\n", Dwarf2.getTagName(tag.getID()), tag.getAcode(), tag
            .getOffset(), tag.getAbbrevOffset(), tag.hasChildren() ? ",children=true" : "");
        for (IAttribute attribute : tag.getAttributes()) {
            dumpAttribute(attribute, indentBy + 4, out, unitOrigin);
        }

        for (ITag child : tag.getChildren()) {
            dumpTag(reader, child, indentBy + 4, out, unitOrigin);
        }
    }

    static void dumpBlock (byte[] block, int indentBy, PrintStream out, boolean littleEndian) {
        ByteArrayInputStream inArray = new ByteArrayInputStream(block);
        DataInputStream input = new DataInputStream(inArray);
        try {
            while (true) {               
                int op = input.readByte() & 0xFF; // throws IOException on EOF
                indent(out, indentBy);
                switch (op) {
                    case DwarfConstants.DW_OP_lit0:
                    case DwarfConstants.DW_OP_lit1:
                    case DwarfConstants.DW_OP_lit2:
                    case DwarfConstants.DW_OP_lit3:
                    case DwarfConstants.DW_OP_lit4:
                    case DwarfConstants.DW_OP_lit5:
                    case DwarfConstants.DW_OP_lit6:
                    case DwarfConstants.DW_OP_lit7:
                    case DwarfConstants.DW_OP_lit8:
                    case DwarfConstants.DW_OP_lit9:
                    case DwarfConstants.DW_OP_lit10:
                    case DwarfConstants.DW_OP_lit11:
                    case DwarfConstants.DW_OP_lit12:
                    case DwarfConstants.DW_OP_lit13:
                    case DwarfConstants.DW_OP_lit14:
                    case DwarfConstants.DW_OP_lit15:
                    case DwarfConstants.DW_OP_lit16:
                    case DwarfConstants.DW_OP_lit17:
                    case DwarfConstants.DW_OP_lit18:
                    case DwarfConstants.DW_OP_lit19:
                    case DwarfConstants.DW_OP_lit20:
                    case DwarfConstants.DW_OP_lit21:
                    case DwarfConstants.DW_OP_lit22:
                    case DwarfConstants.DW_OP_lit23:
                    case DwarfConstants.DW_OP_lit24:
                    case DwarfConstants.DW_OP_lit25:
                    case DwarfConstants.DW_OP_lit26:
                    case DwarfConstants.DW_OP_lit27:
                    case DwarfConstants.DW_OP_lit28:
                    case DwarfConstants.DW_OP_lit29:
                    case DwarfConstants.DW_OP_lit30:
                    case DwarfConstants.DW_OP_lit31:
                        out.println("lit " + (op - DwarfConstants.DW_OP_lit0));
                        break;
                    case DwarfConstants.DW_OP_addr:
                        out.println("addr 0x" + Integer.toString(extractWord(input, littleEndian), 16));
                        break;
                    case DwarfConstants.DW_OP_const1u:
                        out.println("const1u " + (input.readByte() & 0xFF));
                        break;
                    case DwarfConstants.DW_OP_const1s:
                        out.println("const1s " + input.readByte());
                        break;
                    case DwarfConstants.DW_OP_const2u: {
                        int v = extractHalfword(input, littleEndian);
                        out.println("const2u " + v + " (0x" + Integer.toString(v, 16) + ")");
                        break;
                    }
                    case DwarfConstants.DW_OP_const2s: {
                        int v = (short) extractHalfword(input, littleEndian);
                        out.println("const2s " + v + " (0x" + Long.toString(v & 0xFFFFFFFFL, 16) + ")");
                        break;
                    }
                    case DwarfConstants.DW_OP_const4u: {
                        long v = extractWord(input, littleEndian) & 0xFFFFFFFFL;
                        out.println("const4u " + v + " (0x" + Long.toString(v, 16) + ")");
                        break;
                    }
                    case DwarfConstants.DW_OP_constu: {
                        long v = extractULEB(input) & 0xFFFFFFFFL;
                        out.println("constu " + v + " (0x" + Long.toString(v, 16) + ")");
                        break;
                    }
                    case DwarfConstants.DW_OP_const4s: {
                        int v = extractWord(input, littleEndian);
                        out.println("const4s " + v + " (0x" + Long.toString(v & 0xFFFFFFFFL, 16) + ")");
                        break;
                    }
                    case DwarfConstants.DW_OP_consts: {
                        long v = extractSLEB(input) & 0xFFFFFFFFL;
                        out.println("constu " + (int)v + " (0x" + Long.toString(v, 16) + ")");
                        break;
                    }
                    case DwarfConstants.DW_OP_reg0:
                    case DwarfConstants.DW_OP_reg1:
                    case DwarfConstants.DW_OP_reg2:
                    case DwarfConstants.DW_OP_reg3:
                    case DwarfConstants.DW_OP_reg4:
                    case DwarfConstants.DW_OP_reg5:
                    case DwarfConstants.DW_OP_reg6:
                    case DwarfConstants.DW_OP_reg7:
                    case DwarfConstants.DW_OP_reg8:
                    case DwarfConstants.DW_OP_reg9:
                    case DwarfConstants.DW_OP_reg10:
                    case DwarfConstants.DW_OP_reg11:
                    case DwarfConstants.DW_OP_reg12:
                    case DwarfConstants.DW_OP_reg13:
                    case DwarfConstants.DW_OP_reg14:
                    case DwarfConstants.DW_OP_reg15:
                    case DwarfConstants.DW_OP_reg16:
                    case DwarfConstants.DW_OP_reg17:
                    case DwarfConstants.DW_OP_reg18:
                    case DwarfConstants.DW_OP_reg19:
                    case DwarfConstants.DW_OP_reg20:
                    case DwarfConstants.DW_OP_reg21:
                    case DwarfConstants.DW_OP_reg22:
                    case DwarfConstants.DW_OP_reg23:
                    case DwarfConstants.DW_OP_reg24:
                    case DwarfConstants.DW_OP_reg25:
                    case DwarfConstants.DW_OP_reg26:
                    case Dwarf2.DW_OP_reg27:
                    case Dwarf2.DW_OP_reg28:
                    case Dwarf2.DW_OP_reg29:
                    case Dwarf2.DW_OP_reg30:
                    case Dwarf2.DW_OP_reg31:
                        out.println("reg R" + (op - Dwarf2.DW_OP_reg0));
                        break;
                    case Dwarf2.DW_OP_regx:
                        out.println("reg R" + extractULEB(input));
                        break;
                    case Dwarf2.DW_OP_fbreg: {
                        out.println("FrameRef " + extractSLEB(input));
                        break;
                    }
                    case Dwarf2.DW_OP_breg0:
                    case Dwarf2.DW_OP_breg1:
                    case Dwarf2.DW_OP_breg2:
                    case Dwarf2.DW_OP_breg3:
                    case Dwarf2.DW_OP_breg4:
                    case Dwarf2.DW_OP_breg5:
                    case Dwarf2.DW_OP_breg6:
                    case Dwarf2.DW_OP_breg7:
                    case Dwarf2.DW_OP_breg8:
                    case Dwarf2.DW_OP_breg9:
                    case Dwarf2.DW_OP_breg10:
                    case Dwarf2.DW_OP_breg11:
                    case Dwarf2.DW_OP_breg12:
                    case Dwarf2.DW_OP_breg13:
                    case Dwarf2.DW_OP_breg14:
                    case Dwarf2.DW_OP_breg15:
                    case Dwarf2.DW_OP_breg16:
                    case Dwarf2.DW_OP_breg17:
                    case Dwarf2.DW_OP_breg18:
                    case Dwarf2.DW_OP_breg19:
                    case Dwarf2.DW_OP_breg20:
                    case Dwarf2.DW_OP_breg21:
                    case Dwarf2.DW_OP_breg22:
                    case Dwarf2.DW_OP_breg23:
                    case Dwarf2.DW_OP_breg24:
                    case Dwarf2.DW_OP_breg25:
                    case Dwarf2.DW_OP_breg26:
                    case Dwarf2.DW_OP_breg27:
                    case Dwarf2.DW_OP_breg28:
                    case Dwarf2.DW_OP_breg29:
                    case Dwarf2.DW_OP_breg30:
                    case Dwarf2.DW_OP_breg31:
                        out.println("BaseRef R" + (op-Dwarf2.DW_OP_breg0) + "+" + extractSLEB(input));
                        break;
                    case Dwarf2.DW_OP_bregx:
                        out.println("BaseRef R" + extractULEB(input) + "+" + extractSLEB(input));
                        break;
                    case Dwarf2.DW_OP_deref_size:
                    case Dwarf2.DW_OP_xderef_size:
                    case Dwarf2.DW_OP_pick:
                        out.println("<" + op + "> "+ input.readUnsignedByte());
                        break;
                    case Dwarf2.DW_OP_piece:
                    case Dwarf2.DW_OP_plus_uconst:
                        out.println(Dwarf2.getOpcodeName(op)+ extractULEB(input));
                        break;
                    case Dwarf2.DW_OP_bra:
                        out.println("bra " + (short)extractHalfword(input,littleEndian));
                        break;
                    case Dwarf2.DW_OP_const8s:
                    case Dwarf2.DW_OP_const8u:
                        out.println("const8 0x" + Long.toString(extractWord(input,littleEndian)&0xFFFFFFFFL,16) +
                            " 0x" + Long.toString(extractWord(input,littleEndian)&0xFFFFFFFFL,16) );
                        break;
                    default:{
                        out.println(Dwarf2.getOpcodeName(op));
                    }                          
                }
            }
        }
        catch (IOException e) {
           // Presumably EOF
        }
    }

    static int extractSLEB (DataInput input) throws IOException {
        int result = 0, shift = 0;
        int b;
        while (true) {
            b = input.readUnsignedByte();
            result |= ((0x7f & b) << shift);
            shift += 7;
            if ((b & 0x80) == 0)
                break;
        }
        if ((shift < 32) && (b & 0x40) != 0)
            result |= -(1 << shift);
        return result;
    }

    static int extractULEB (DataInput input) throws IOException {
        int result = 0, shift = 0;
        int b;
        while (true) {
            b = input.readUnsignedByte();
            result |= ((0x7f & b) << shift);
            shift += 7;
            if ((b & 0x80) == 0)
                break;
        }
        return result;
    }

    static int extractWord (DataInput input, boolean littleEndian) throws IOException {
        if (littleEndian) {
            int a = input.readUnsignedByte();
            int b = input.readUnsignedByte();
            int c = input.readUnsignedByte();
            int d = input.readUnsignedByte();

            return a | (b << 8) | (c << 16) | (d << 24);
        }
        return input.readInt();
    }

    static int extractHalfword (DataInput input, boolean littleEndian) throws IOException {
        if (littleEndian) {
            int a = input.readUnsignedByte();
            int b = input.readUnsignedByte();
            return a | (b << 8);
        }
        return input.readUnsignedShort();
    }

    static void dumpAttribute (IAttribute attr, int indentBy, PrintStream out, int unitOrigin) {
        boolean littleEndian = true; // Need to make as an argument
        indent(out, indentBy);
        out.printf("%s %s", Dwarf2.getAttributeName(attr.getID()), Dwarf2.getFormName(attr.getFormID()));
        if (attr.getFormat() == Dwarf2.AttributeFormat.BLOCK_FORM) {
            byte[] block = attr.getBlock();
            out.printf(" %d bytes\n", block.length);
            dumpBlock(block, indentBy + 1, out, littleEndian);
        }
        else if (attr.getFormat() == Dwarf2.AttributeFormat.REF_FORM) {
            out.printf(" 0x%x\n", attr.getIntValue() + unitOrigin);
        }
        else if (attr.getFormat() == Dwarf2.AttributeFormat.STRING_FORM) {
            out.printf(" \"%s\"\n", attr.getStringValue());
        }
        else if (attr.getFormat() == Dwarf2.AttributeFormat.INT_FORM) {
            out.printf(" %d (=0x%x)\n", attr.getIntValue(), attr.getIntValue());
        }
        else
            out.println();

    }

    static void indent (PrintStream out, int amount) {
        for (int i = 0; i < amount; i++)
            out.print(' ');
    }

    static void dumpLineTable (ILineTableReader reader, final PrintStream out) {
        ILineTableReader.IStatementReceiver receiver = new ILineTableReader.IStatementReceiver() {

            private String file = "??";

            private String basicBlock = "";

            private boolean isStatement = false;

            @Override
            public void addLine (int pc, int sourceLine, int column) {
                if (isStatement)
                    out.printf(">");
                else
                    out.printf(" ");
                out.printf("    0x%x %s:%d", pc, file, sourceLine);
                if (column > 0)
                    out.printf("/%d", column);
                out.println(basicBlock);
                basicBlock = "";
            }

            @Override
            public void setBasicBlock () {
                basicBlock = " <- BB";

            }

            @Override
            public void setEpilogBegin () {
                out.println("   EPILOG:");

            }

            @Override
            public void setFile (String includePath, String fileName, long modTime, int size) {
                this.file = includePath + "/" + fileName;

            }

            @Override
            public void setIsStatement (boolean v) {
                this.isStatement = v;

            }

            @Override
            public void setPrologEnd () {
                out.println("   END PROLOG:");

            }
        };
        out.println("\nLine table at 0x" + Integer.toHexString(reader.getOffset()));
        reader.eachStatement(receiver);
    }

    /**
     * Display the contents a file's Dwarf 2 section.
     * @throws IOException 
     * @throws IllegalArgumentException 
     */
    public static void main (String[] args) throws IllegalArgumentException, IOException {
        for (String fn : args) {
            dumpDwarf(fn, System.out);
        }
    }

}
