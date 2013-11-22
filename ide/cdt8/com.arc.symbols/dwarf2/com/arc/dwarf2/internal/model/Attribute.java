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

import com.arc.dwarf2.Dwarf2;
import com.arc.dwarf2.DwarfConstants;
import com.arc.dwarf2.Dwarf2.AttributeFormat;
import com.arc.dwarf2.model.IAttribute;
import com.arc.dwarf2.model.IDwarf2ErrorReporter;


class Attribute implements IAttribute, DwarfConstants {

    private int fAtID;

    private Dwarf2.AttributeFormat fFormat;
    
    private Object fValue;
    
    private long fIntValue;

    private int fFormID;

    public Attribute(int atID, int formID, Extractor info, Extractor str, IDwarf2ErrorReporter reporter) throws IOException {
        fAtID = atID;
        fFormID = formID;
        boolean indirect = false;
        do {
            switch (formID) {
            case DW_FORM_addr:
                fFormat = Dwarf2.AttributeFormat.INT_FORM;
                fIntValue = info.readWord();
                break;
            case DW_FORM_block2: {
                int len = info.readUShort();
                fFormat = Dwarf2.AttributeFormat.BLOCK_FORM;
                fValue = readBlock(info, len);
                break;
            }
            case DW_FORM_block4: {
                int len = info.readWord();
                fFormat = Dwarf2.AttributeFormat.BLOCK_FORM;
                fValue = readBlock(info, len);
                break;
            }

            case DW_FORM_data2:
                fFormat = Dwarf2.AttributeFormat.INT_FORM;
                fIntValue = info.readUShort();
                break;
            case DW_FORM_data4:
                fFormat = Dwarf2.AttributeFormat.INT_FORM;
                fIntValue = info.readWord();
                break;
            case DW_FORM_data8:
                fFormat = Dwarf2.AttributeFormat.INT_FORM;
                fIntValue = info.readLong();
                break;
            case DW_FORM_string:
                fFormat = Dwarf2.AttributeFormat.STRING_FORM;
                fValue = info.readString();
                break;
            case DW_FORM_block: {
                int len = info.readULEB();
                fFormat = Dwarf2.AttributeFormat.BLOCK_FORM;
                fValue = readBlock(info, len);
                break;
            }
            case DW_FORM_block1: {
                int len = info.readByte();
                fFormat = Dwarf2.AttributeFormat.BLOCK_FORM;
                fValue = readBlock(info, len);
                break;
            }
            case DW_FORM_flag:
            case DW_FORM_data1:
                fFormat = Dwarf2.AttributeFormat.INT_FORM;
                fIntValue = info.readByte();
                break;
            case DW_FORM_sdata:
                fFormat = Dwarf2.AttributeFormat.INT_FORM;
                fIntValue = info.readSLEB();
                break;
            case DW_FORM_strp: {
                fFormat = Dwarf2.AttributeFormat.STRING_FORM;
                int off = info.readWord();
                if (str != null) {
                    str.setOffsetFromStart(off);
                    fValue = str.readString();
                }
                else {
                    reporter.error(
                        info.getName(),
                        info.getOffset() - 4,
                        "DW_FORM_strp reference, but no .debug_str section!");
                    fValue = "???";
                }
                break;
            }
            case DW_FORM_udata:
                fFormat = Dwarf2.AttributeFormat.INT_FORM;
                fIntValue = info.readULEB();
                break;
            case DW_FORM_ref_addr:
                fFormat = Dwarf2.AttributeFormat.INT_FORM;
                fIntValue = info.readWord();
                break;
            case DW_FORM_ref1:
                fFormat = Dwarf2.AttributeFormat.REF_FORM;
                fIntValue = info.readByte()/* + info.getStart()*/;
                break;
            case DW_FORM_ref2:
                fFormat = Dwarf2.AttributeFormat.REF_FORM;
                fIntValue = info.readUShort()/* + info.getStart()*/;
                break;
            case DW_FORM_ref4:
                fFormat = Dwarf2.AttributeFormat.REF_FORM;
                fIntValue = info.readWord() /* + info.getStart()*/;
                break;
            case DW_FORM_ref8:
                fFormat = Dwarf2.AttributeFormat.REF_FORM;
                fIntValue = info.readLong()/*+ info.getStart()*/;
                break;
            case DW_FORM_ref_udata:
                fFormat = Dwarf2.AttributeFormat.REF_FORM;
                fIntValue = info.readULEB() /*+ info.getStart()*/;
                break;
            case DW_FORM_indirect:
                formID = info.readByte();
                indirect = true;
                break;
            default:
                reporter.error(info.getName(), info.getOffset(), "Unrecognized attribute format: 0x"
                    + Integer.toHexString(formID));
            }
        }while (indirect);

    }

    @Override
    public byte[] getBlock () throws IllegalStateException {
        if (fFormat != Dwarf2.AttributeFormat.BLOCK_FORM)
            throw new IllegalStateException("Not a block form");
        return (byte[])fValue;
    }

    @Override
    public AttributeFormat getFormat () {
        return fFormat;
    }
    
    @Override
    public Object getValue() {
        return fValue == null?new Long(fIntValue):fValue;
    }
    
    private byte[] readBlock(Extractor info, int len) throws IOException {
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++){
            result[i] = (byte)info.readByte();
        }
        return result;
    }

    @Override
    public int getID () {
        return fAtID;
    }

    @Override
    public long getIntValue () throws IllegalStateException {
        if (fFormat != Dwarf2.AttributeFormat.INT_FORM &&
            fFormat != Dwarf2.AttributeFormat.REF_FORM)
            throw new IllegalStateException("Not an integer form");
        return fIntValue;
    }

    @Override
    public String getStringValue () throws IllegalStateException {
        if (fFormat != Dwarf2.AttributeFormat.STRING_FORM)
            throw new IllegalStateException("Not a string");
        return fValue.toString();
    }

    @Override
    public int getFormID () {
        return fFormID;
    }

}
