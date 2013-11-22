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


/**
 * A composite of consecutive sections that are read as a unit. This is how
 * we dump, say, a ".debug_info" section that is made up of multiple sections,
 * e.g., ".debug_info$foo", ".debug_info$bar".
 * @author davidp
 *
 */
public class CompositeSectionReader implements ISectionReader {

    private ISectionReader[] fReaders;
    private int fOffset[];
    private int fLength;

    public CompositeSectionReader(ISectionReader readers[]){
        if (readers == null || readers.length == 0){
            throw new IllegalArgumentException("No readeres");
        }
        fReaders = new ISectionReader[readers.length];
        System.arraycopy(readers,0,fReaders,0,readers.length);
        fOffset = new int[readers.length+1];
        int offset = 0;
        for (int i = 0; i < readers.length; i++){
            fOffset[i] = offset;
            offset += readers[i].getLength();
        }
        fOffset[readers.length] = offset;
        fLength = offset;
    }
    @Override
    public int getByteAt (int offset) throws IOException {
        for (int i = 0; i < fOffset.length-1; i++){
            if (fOffset[i+1] > offset){
                return fReaders[i].getByteAt(offset-fOffset[i]);
            }
        }
        throw new IOException("Offset " + offset + " > max " + getLength());
    }

    /* (non-Javadoc)
     * @see com.arc.dwarf2.model.ISectionReader#getLength()
     */
    @Override
    public int getLength () {
        return fLength;
    }

    /* (non-Javadoc)
     * @see com.arc.dwarf2.model.ISectionReader#getName()
     */
    @Override
    public String getName () {
        return fReaders[0].getName() + "*";
    }
    /* (non-Javadoc)
     * @see com.arc.dwarf2.model.ISectionReader#getActualName()
     */
    @Override
    public String getActualName (int offset) {
        for (int i = 0; i < fOffset.length-1; i++){
            if (fOffset[i+1] > offset){
                return fReaders[i].getActualName(offset-fOffset[i]);
            }
        }
        return "???";
    }
    /* (non-Javadoc)
     * @see com.arc.dwarf2.model.ISectionReader#getSectionOffset(int)
     */
    @Override
    public int getSectionOffset (int offset) {
        for (int i = 0; i < fOffset.length-1; i++){
            if (fOffset[i+1] > offset){
                return fReaders[i].getSectionOffset(offset-fOffset[i]);
            }
        }
        return fOffset[fOffset.length-1]; // shouldn't get here.
    }

}
