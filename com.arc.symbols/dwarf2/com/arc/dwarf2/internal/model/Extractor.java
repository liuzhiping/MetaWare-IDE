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

import com.arc.dwarf2.model.ISectionReader;


/**
 * Extracts data from a section.
 * @todo davidp needs to add a class description.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
abstract class Extractor {
	protected ISectionReader fSectionReader;
    protected int fStart;
    private int fOffset;
    protected int fEnd;

    protected Extractor(ISectionReader sectionReader, int startOffset, int length){
		fSectionReader = sectionReader;
		fStart = startOffset;
		fOffset = startOffset;
		fEnd = startOffset + length;
	}
    
    public static Extractor create(ISectionReader sectionReader, int startOffset, int length, boolean littleEndian){
    	return littleEndian?createLittleEndian(sectionReader,startOffset,length):
    		createBigEndian(sectionReader,startOffset,length);
    }
    
    public int getOffsetFromStart(){
    	return fOffset - fStart;
    }
    
    public void skipBytes(int amount){
        setOffset(fOffset+amount);
    }
    
    public int getOffset() {
    	return fOffset;
    }
    
    public int getLength() {
    	return fEnd-fStart;
    }
    
    public void setOffset(int offset){
    	if (offset < fStart || offset > fEnd){
    		throw new IllegalArgumentException("Offset out of range");
    	}
    	fOffset = offset;
    }
    
    public String getName() {
    	return this.fSectionReader.getName();
    }
    
    public void setOffsetFromStart(int offset){
    	setOffset(fStart + offset);
    }
    
    public int readULEB() throws IOException {
    	int result = 0;
    	int shift = 0;
    	while (true){
    		int v = readByte();
    		result |= (0x7F & v) << shift;
    		if ((v & 0x80) == 0) break;
    		shift += 7; 		
    	}
    	return result;
    }
    
    public int getStart() {
    	return fStart;
    }
    
    public int readSLEB() throws IOException {
    	int result = 0;
    	int shift = 0;
    	int v = 0;
    	while (true){
    		v = readByte();
    		result |= (0x7F & v) << shift;  		
    		shift += 7; 
            if ((v & 0x80) == 0) break;
    	}
    	if ( shift < 32 && (v & 0x40) != 0){
    		result |= -(1 << shift);
    	}
    	return result;
    }
    
    /**
     * Read the next byte and bump the offset pointer.
     * @return the next byte in the section.
     * @throws IOException
     */
    public int readByte()throws IOException{
    	if (fOffset < fEnd)
    	    return fSectionReader.getByteAt(fOffset++) & 0xFF;
    	throw new IOException("reading passed end of section data");
    }
    
    public abstract Extractor createNew(int offsetFromStart, int length);
    /**
     * Read the next unsigned short, taking into account endianess.
     * @return the next unsigned short, taking into account endianess.
     * @throws IOException
     */
    public abstract int readUShort() throws IOException;
    
    public abstract int readWord() throws IOException;
    
    public abstract long readLong() throws IOException;
    
    /**
     * Read a null-terminated ASCIZ string.
     * @return null-termianted ASCIZ string.
     * @throws IOException
     */
    public String readString() throws IOException {
    	StringBuilder buf = new StringBuilder();
    	int c = readByte();
    	while (c != 0){
    		buf.append((char)(c & 0x7F));
    		c = readByte();
    	}
    	return buf.toString();
    }
    
    /**
     * Return a big-endian version.
     * @param reader
     * @param startOffset
     * @return a big-endian version.
     */
    static Extractor createBigEndian(ISectionReader reader, int startOffset, int length){
    	return new BigEndianExtractor(reader,startOffset, length);
    }
    
    /**
     * Return a big-endian version.
     * @param reader
     * @param startOffset
     * @return a big-endian version.
     */
    static Extractor createLittleEndian(ISectionReader reader, int startOffset, int length){
    	return new LittleEndianExtractor(reader,startOffset,length);
    }
    
    static class BigEndianExtractor extends Extractor {

        /**
         * @todo davidp needs to add a constructor comment.
         * @param sectionReader
         * @param startOffset
         * @param length
         */
        BigEndianExtractor(ISectionReader sectionReader, int startOffset, int length) {
            super(sectionReader, startOffset, length);
        }

        @Override
        public int readUShort () throws IOException {
            int ub = readByte();
            int lb = readByte();
            return (ub << 8) | lb;
        }

        @Override
        public int readWord () throws IOException {
            int b0 = readByte();
            int b1 = readByte();
            int b2 = readByte();
            int b3 = readByte();
            return (b0 << 24) + (b1 << 16) + (b2 << 8) + b3;
        }
        
        @Override
        public long readLong () throws IOException {
            long w0 = readWord() & 0xFFFFFFFFL;
            long w1 = readWord() & 0xFFFFFFFFL;
            return (w0 << 32) + w1;
        }
        

        @Override
        public Extractor createNew (int offsetFromStart, int length) {
        	if (offsetFromStart < 0 || offsetFromStart + length > fEnd)
        		throw new IllegalArgumentException("Arguments out of range");
            return new BigEndianExtractor(this.fSectionReader,fStart+offsetFromStart,length);
        }
    	
    }
    
    static class LittleEndianExtractor extends Extractor {

        /**
         * @todo davidp needs to add a constructor comment.
         * @param sectionReader
         * @param startOffset
         * @param length
         */
        LittleEndianExtractor(ISectionReader sectionReader, int startOffset, int length) {
            super(sectionReader, startOffset, length);
        }

        @Override
        public int readUShort () throws IOException {
            int lb = readByte();
            int ub = readByte();
            return (ub << 8) | lb;
        }

        @Override
        public int readWord () throws IOException {
            int b3 = readByte();
            int b2 = readByte();
            int b1 = readByte();
            int b0 = readByte();
            return (b0 << 24) + (b1 << 16) + (b2 << 8) + b3;
        }
        
        @Override
        public long readLong () throws IOException {
            long w1 = readWord() & 0xFFFFFFFFL;
            long w0 = readWord() & 0xFFFFFFFFL;
            return (w0 << 32) + w1;
        }
        
        @Override
        public Extractor createNew (int offsetFromStart, int length) {
        	if (offsetFromStart < 0 || offsetFromStart + length > fEnd)
        		throw new IllegalArgumentException("Arguments out of range");
            return new LittleEndianExtractor(this.fSectionReader,fStart+offsetFromStart,length);
        }
    	
    }

}
