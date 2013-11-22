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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.arc.dwarf2.model.IArange;


class Arange implements IArange {

    private int fLength;
    private int fVersion;
    private int fCompUnitOffset;
    private int fSegSize;
    private List<Range> fRanges = new ArrayList<Range>();
    private int fAddressSize;

    Arange(int length, int version, int compUnitOffset, int addressSize, int segSize){
        fLength = length;
        fVersion = version;
        fCompUnitOffset = compUnitOffset;
        fAddressSize = addressSize;
        fSegSize = segSize;
    }
    @Override
    public int getCompilationUnitOffset () {
        return fCompUnitOffset;
    }

    @Override
    public int getLength () {
        return fLength;
    }
    
    void addRange(Range range){
        fRanges.add(range);
    }

    @Override
    public Collection<Range> getRanges () {
        return Collections.unmodifiableCollection(fRanges);
    }

    @Override
    public int getVersion () {
        return fVersion;
    }
    
    @Override
    public int getAddressSize () {
        return fAddressSize;
    }
    @Override
    public int getSegmentSize () {
        return fSegSize;
    }

}
