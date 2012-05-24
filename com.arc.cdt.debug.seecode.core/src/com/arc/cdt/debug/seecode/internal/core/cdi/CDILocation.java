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
package com.arc.cdt.debug.seecode.internal.core.cdi;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation2;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation2;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocator;

import com.arc.seecode.engine.Location;

/**
 * Our implementation of {@link ICDILocator}. We extend
 * our own {@link Location} class because it has more
 * information.
 * @author David Pickens
 */
class CDILocation  implements ICDILocator, ICDILineLocation2, ICDIFunctionLocation2 {
    private Location mLoc;
    private String mSource = null;
	CDILocation(long addr){
		mLoc = new Location();
        mLoc.setAddress(addr);
	}
	CDILocation(Location l){
		this(l,null);
	}
	CDILocation(Location l, String source){
        if (l == null) {
            // Engine will return null location if process is terminated, etc.
            l = new Location();
            //throw new IllegalArgumentException("Argument is null");
        }
        mSource = source != null?source:l.getSource();
        mLoc = l;
	}
    
    Location getLocation(){
        return mLoc;
    }
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDILocation#getFile()
	 */
	@Override
    public String getFile() {
		return mSource;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDILocation#getLineNumber()
	 */
	@Override
    public int getLineNumber() {
		return mLoc.getSourceLine();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDILocation#equals(org.eclipse.cdt.debug.core.cdi.ICDILocation)
	 */
	@Override
    public boolean equals(ICDILocation location) {
        if (location instanceof CDILocation){
            return mLoc.getAddress() == ((CDILocation)location).mLoc.getAddress();
        }
		return false;
	}
    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.ICDILocation#getAddress()
     */
    @Override
    public BigInteger getAddress() {
        return BigInteger.valueOf(mLoc.getAddress());
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.ICDILocation#getFunction()
     */
    @Override
    public String getFunction() {
        return mLoc.getFunction();
    }
    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @return the byte offset from start of function.
     */
    @Override
    public int getOffsetFromFunction () {
        return mLoc.getFunctionOffset();
    }
    
    @Override
    public int getOffsetFromLine(){
        return mLoc.getSourceLineOffset();
    }
    
    @Override
    public String toString(){
        return mLoc.toString();
    }
}
