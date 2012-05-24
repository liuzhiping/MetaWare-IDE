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

import com.arc.dwarf2.model.ISectionReader;


class ElfSectionReader implements ISectionReader {

    private byte[] fData;
    private String fName;

    public ElfSectionReader(byte[] data, String name){
        fData = data;
        fName = name;
    }
    @Override
    public int getByteAt (int offset) throws IOException {
        try {
            return fData[offset] & 0xFF;
        } catch(ArrayIndexOutOfBoundsException x){
            throw new IOException("Out of bounds " + offset + ">=" + fData.length);
        }
    }

    @Override
    public int getLength () {
        return fData.length;
    }

    @Override
    public String getName () {
        return fName;
    }
   
    @Override
    public String getActualName (int offset) {
        return getName();
    }
    
    @Override
    public int getSectionOffset (int offset) {
        return offset;
    }    
}
