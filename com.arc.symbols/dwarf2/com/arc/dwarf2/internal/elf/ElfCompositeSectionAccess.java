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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.cdt.utils.elf.Elf.Section;
import org.eclipse.cdt.utils.elf.Elf.Symbol;

import com.arc.dwarf2.model.ISectionAccess;
import com.arc.dwarf2.model.ISectionReader;


public class ElfCompositeSectionAccess implements ISectionAccess {

    private Elf fElf;
    
    private Map<Section,Section> fLinkMap = new HashMap<Section,Section>();
    private Map<Section,Section> fRelMap = new HashMap<Section,Section>();
    
    private int fREL32 = 0; // fixup type for adding 32-bits
    private int fNREL32 = 0; // Fixup type for subtracting 32 bits.
    private int fDIFF32 = 0; // Videocore compound fixup.

    private Symbol[] fSymbols;

    private Section fSections[];

    public ElfCompositeSectionAccess(Elf elf) throws IOException {
        fElf = elf;
        elf.loadSymbols();
        fSymbols = elf.getSymtabSymbols();
        fSections = elf.getSections();
        for (int i = 0; i < fSections.length; i++){
            if (fSections[i].sh_type == Section.SHT_PROGBITS && fSections[i].sh_link > 0 && fSections[i].sh_link < fSections.length){
                Section first = fSections[(int)fSections[i].sh_link];
                if (first.sh_type == Section.SHT_PROGBITS)
                    addLink(first,fSections[i]);
            }
            
            if (fSections[i].sh_type == Section.SHT_RELA && fSections[i].sh_info > 0 &&
                fSections[i].sh_info < fSections.length){
                Section dataSec = fSections[(int)fSections[i].sh_info];
                if (dataSec.sh_type == Section.SHT_PROGBITS){
                    fRelMap.put(dataSec,fSections[i]);
                }
            }
        }
        
        switch (fElf.getELFhdr().e_machine){
            case Elf.ELFhdr.EM_ARC:
            case Elf.ELFhdr.EM_ARC_A5:
                fREL32 = 4; //R_ARC_32
                fNREL32 = 11; //R_ARC_n32
                break;
            case Elf.ELFhdr.EM_VIDEOCORE:
                fREL32 = 1; // R_VC_32
                fDIFF32 = 36; // R_VC_DIFF32
                break;
            case Elf.ELFhdr.EM_VIDEOCORE3:
                fREL32=1; //R_VC3_32
                fDIFF32 = 34; // R_VC3_DIFF32
                break;
        }
        
        
    }
    
    private void addLink(Section first, Section second){
        Section existing = fLinkMap.get(first);
        while (existing != null){
            Section next = fLinkMap.get(existing);
            if (next == null){
                first = existing;
                break;
            }
            existing = next;
        }
        fLinkMap.put(first,second);
    }

    @Override
    public ISectionReader getSection (String name) throws IOException {
        Section section = fElf.getSectionByName(name);
        if (section != null) {
            List<Section> list = new ArrayList<Section>();
            Section s = section;
            while (s != null){
                list.add(s);
                s = fLinkMap.get(s);
            }
            List<ISectionReader> secs = new ArrayList<ISectionReader>();
            for (Section sec: list) {
                secs.add(new ElfSectionReader(loadSectionData(sec,list),sec.toString()));
            }
            return new CompositeSectionReader(secs.toArray(new ISectionReader[secs.size()]));
        }
        return null;
    }
    
    class Reloc{
        private int[] fData;
        private boolean fLE;

        public Reloc(Section sec) throws IOException{
            byte[] bytes = sec.loadSectionData();
            fLE = isLittleEndian();
            if (isElf64()){
                fData = new int[bytes.length/(8*3)*3];
                for (int i = 0; i < fData.length; i += 3){
                    long offset = readLong(bytes,i*8,fLE);
                    long info = readLong(bytes,i*8+8,fLE);
                    long addend = readLong(bytes,i*8+16,fLE);
                    fData[i] = (int)offset;
                    fData[i+1] = ELF32_R_INFO(ELF64_R_SYM(info),ELF64_R_TYPE(info));
                    fData[i+2] = (int)addend;
                }
            }
            else {
                fData = new int[bytes.length/(4*3)*3];
                for (int i = 0; i < fData.length; i += 3){
                    int offset = readInt(bytes,i*4,fLE);
                    int info = readInt(bytes,i*4+4,fLE);
                    int addend = readInt(bytes,i*4+8,fLE);
                    fData[i] = offset;
                    fData[i+1] = info;
                    fData[i+2] = addend;
                }
            
            }
        }
        
        public void relocate(byte[] data, Section section, List<Section>list){
            for (int i = 0; i < fData.length; i += 3){
                int offset = fData[i+0];
                int info = fData[i+1];
                int addend = fData[i+2];
                int sym = ELF32_R_SYM(info);
                int type = ELF32_R_TYPE(info);
                if (sym > 0 && sym < fSymbols.length &&
                    fSymbols[sym-1].st_type() == Symbol.STT_SECTION)
                {
                    int targOffset = computeSectionOffset(fSections[fSymbols[sym-1].st_shndx],list);
                    if (targOffset != -1){
                        if (type == fREL32){
                            writeWord(data,offset,targOffset +addend);
                        }
                        else if (type == fDIFF32){
                            int sym2 = ELF32_R_SYM(fData[i+5]);
                            if (sym2 > 0 && sym2 < fSymbols.length &&
                                fSymbols[sym2-1].st_type() == Symbol.STT_SECTION)
                            {
                                int targ2 = computeSectionOffset(fSections[fSymbols[sym2-1].st_shndx],list);
                                if (targ2 != -1){
                                    writeWord(data,offset,targOffset+addend - targ2);
                                }
                            }
                        } else if (type == fNREL32){
                            int toff = readInt(data,offset,fLE);
                            writeWord(data,offset,toff-(targOffset+addend));
                        }
                    }
                }
            }
        }
        
        private void writeWord(byte[] data, int offset, int value){
            if (fLE){
                data[offset+0] = (byte)value;
                data[offset+1] = (byte)(value >> 8);
                data[offset+2] = (byte) (value >> 16);
                data[offset+3] = (byte) (value >> 24);
            }
            else {
                data[offset+3] = (byte)value;
                data[offset+2] = (byte)(value >> 8);
                data[offset+1] = (byte) (value >> 16);
                data[offset+0] = (byte) (value >> 24);
            }
        }
        
        private int computeSectionOffset(Section sec, List<Section> list){
            int off = 0;
            for (Section s: list){
                if (sec == s) return off;
                off += s.sh_size;
            }
            return 0;
        }
    }
    
    private static int ELF64_R_SYM(long info){
        return (int)(info >> 32);
    }
    
    private static int ELF64_R_TYPE(long info){
        return (int)(info);
    }
    
    private static int ELF32_R_TYPE(int info){
        return info & 0xFF;
    }
    
    private static int ELF32_R_SYM(int info){
        return info >> 8;
    }
    
    private static int ELF32_R_INFO(int sym,  int type){
        return (sym <<8) | (type&0xFF);
    }
    
    private int readInt(byte[] bytes, int offset, boolean le){
        if (le){
            return (bytes[offset] & 0xFF) | ((bytes[offset+1]&0xFF)<<8) |
                    ((bytes[offset+2]&0xFF) << 16) | ((bytes[offset+3]&0xFF)<<24);
        }
        else {
            return (bytes[offset+3] & 0xFF) | ((bytes[offset+2]&0xFF)<<8) |
            ((bytes[offset+1]&0xFF) << 16) | ((bytes[offset+0]&0xFF)<<24);
        }
    }
    
    private long readLong(byte[] bytes, int offset, boolean le){
        if (le){
            return (readInt(bytes,0,le) & 0xFFFFFFFFL) |
                   (readInt(bytes,4,le) << 32);
        }
        else {
            return (readInt(bytes,4,le) & 0xFFFFFFFFL) |
            (readInt(bytes,0,le) << 32);
        }
    }
    
    /**
     * Relocate the given section and returns its data.
     * We include the list of sections making up a composite (e.g., ".debug_info$Foo") that
     * we expect to be relocated against.
     * @param sec
     * @param others
     * @return
     * @throws IOException 
     */
    private byte[] loadSectionData(Section sec, List<Section> others) throws IOException{
        byte[] data = sec.loadSectionData();
        Section relSec = fRelMap.get(sec);
        if (relSec != null){
            Reloc rel = new Reloc(relSec);
            rel.relocate(data,sec,others);
        }
        return data;
    }

    @Override
    public boolean isLittleEndian () {
        try {
            return fElf.getELFhdr().e_ident[Elf.ELFhdr.EI_DATA] == Elf.ELFhdr.ELFDATA2LSB;
        }
        catch (IOException e) {
            return false;
        }
    }
    
    public boolean isElf64(){
        try {
            return fElf.getELFhdr().e_ident[Elf.ELFhdr.EI_CLASS] == Elf.ELFhdr.ELFCLASS64;
        }
        catch (IOException e) {
            return false;
        }
    }
}
