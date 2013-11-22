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
package com.arc.dwarf2.model;

import java.util.Collection;


public interface IArange {
    /**
     * Return the version number (typically "2").
     * @return the version number.
     */
    public int getVersion();
    
    /**
     * Return the length of this entry (for display purposes).
     * @return the length of this entry in bytes.
     */
    public int getLength();
    
    /**
     * Return the size of an address (typically 4).
     * @return the size of an address.
     */
    public int getAddressSize();
    
    /**
     * The size of the "segment" portion of an address. Zero for all
     * architecture for which we have an interest.
     * @return size of the segment portion of the address, typically 0.
     */
    public int getSegmentSize();
    
    /**
     * Return the offset of the corresponding compilation unit.
     * @return byte offset of the compilation unit to which these ranges apply.
     */
    public int getCompilationUnitOffset();
    
    static public class Range{
        public Range(long address, int length, int aoff){
            this.address = address;
            this.length = length;
            this.aoff = aoff;
        }
        /**
         * Start of an address range.
         */
        public long address;
        /**
         * Length of the range (in bytes).
         * <P>
         * Should this be a long? Is is possible to have a range exceed 4GB
         * in 64-bit dwarf?
         */
        public int length;
        /**
         * Offset of the address in the .debug_aranges section 
         * (in case the client needs to match it to relocation entry).
         */
        public int aoff;
    }
    
    /**
     * Return (immutable) collection of ranges
     * @return collection of ranges.
     */
    Collection<Range> getRanges();
}
