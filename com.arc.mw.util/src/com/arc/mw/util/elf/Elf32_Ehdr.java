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
package com.arc.mw.util.elf;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An instance of this class represents the content of an ELF header.
 * It exactly parallels the corresponding C struct. See the C struct definition
 * for more commentary.
 * 
 * @author David Pickens
 */
public class Elf32_Ehdr {
    public static final int EI_NIDENT = 16;
    public static final int EI_MAG0 = 0;
    public static final int EI_MAG1 = 1;
    public static final int EI_MAG2 = 2;
    public static final int EI_MAG3 = 3;
    public static final int EI_CLASS = 4;
    public static final int EI_DATA = 5;
    public static final int EI_VERSION = 6;
    public static final int EI_PAD = 7;
    public byte e_ident[] = new byte[EI_NIDENT];       // ident bytes
    public int e_type;                          // file type
    public int e_machine;                       // target machine 
    public int e_version;                       // file version
    public int e_entry;                         // entry point address
    public int e_phoff;                         // file offset of program headers
    public int e_shoff;                         // file offset of section headers
    public int e_flags;                         // file flags
    public int e_ehsize;                        // size of this struct on disk
    public int e_phentsize;                     // Size of each program header
    public int e_phnum;                         // Number of program headers
    public int e_shentsize;                     // size of each section header
    public int e_shnum;                         // Number of section headers
    public int e_shstrndx;                      // index of section's string table
    
    /**
     * @return whether or not this ELF header is for a little-endian machine.
     */
    public boolean isLittleEndian(){
        return e_ident[EI_DATA] == 1;
    }
    
    /**
     * @return whether or not this ELF header is for a big-endian machine.
     */
    public boolean isBigEndian(){
        return !isLittleEndian();
    }
    
    /**
     * Fill in the contents of this class by reading from an input stream
     * that is positioned at the start of header.
     * Endianess will be detected appropriately.
     * <P>
     * If not a valid ELF header, an exception will be thrown.
     * @param input the input stream position to start of ELF32 header.
     */
    public void readFrom(InputStream input) throws IOException, ElfFormatException{
        readFrom((DataInput)new DataInputStream(input));    
    }
    
    /**
     * Fill in the contents of this class by reading from an input stream
     * that is positioned at the start of header.
     * Endianess will be detected appropriately.
     * <P>
     * If not a valid ELF header, an exception will be thrown.
     * @param input the input stream position to start of ELF32 header.
     */
    public void readFrom(DataInput input) throws IOException, ElfFormatException {
         input.readFully(e_ident);
         if (e_ident[EI_MAG0] != 0x7F || 
             e_ident[EI_MAG1] != 'E' || 
             e_ident[EI_MAG2] != 'L' ||
             e_ident[EI_MAG3] != 'F')
             throw new ElfFormatException("Not a ELF32 header");
         if (isLittleEndian()) { //little endian
             input = new DataInputEndianSwapper(input);
         }
         e_type = input.readShort();
         e_machine = input.readShort();
         e_version = input.readInt();
         e_entry = input.readInt();
         e_phoff = input.readInt();
         e_shoff = input.readInt();
         e_flags = input.readInt();
         e_ehsize = input.readShort();
         e_phentsize = input.readShort();
         e_phnum = input.readShort();
         e_shentsize = input.readShort();
         e_shnum = input.readShort();
         e_shstrndx = input.readShort();
    }
    
    /**
     * Write this header back to an output stream.
     * @param out the output stream.
     * @throws IOException if write failed.
     */
    public void write(OutputStream out) throws IOException{
        write((DataOutput)new DataOutputStream(out));
    }
    
    /**
     * Write this header back to an output stream.
     * @param out the output stream.
     * @throws IOException if write failed.
     */
    public void write(DataOutput out)throws IOException{
        if (isLittleEndian()) { out = new DataOutputEndianSwapper(out); }
        out.write(e_ident);
        out.writeShort(e_type);
        out.writeShort(e_machine);
        out.writeInt(e_version);
        out.writeInt(e_entry);
        out.writeInt(e_phoff);
        out.writeInt(e_shoff);
        out.writeInt(e_flags);
        out.writeShort(e_ehsize);
        out.writeShort(e_phentsize);
        out.writeShort(e_phnum);
        out.writeShort(e_shentsize);
        out.writeShort(e_shnum);
        out.writeShort(e_shstrndx);
    }
    
    public static Elf32_Ehdr read(InputStream input) throws IOException, ElfFormatException {
        Elf32_Ehdr e = new Elf32_Ehdr();
        e.readFrom(input);
        return e;
    }
    

}
