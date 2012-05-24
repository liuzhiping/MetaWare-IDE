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

import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;

import com.arc.seecode.engine.AssemblyRecord;
import com.arc.seecode.engine.Location;


/**
 * A representation of a dissembled instruction.
 * @author David Pickens
 */
class AssemblyInstruction implements ICDIInstruction{

    private Location mLocation;
    private AssemblyRecord mRecord;
    private ICDITarget mTarget;

    /**
     * 
     */
    public AssemblyInstruction(ICDITarget target, Location loc, AssemblyRecord record) {
        mLocation = loc;
        mRecord = record;
        mTarget = target;
    }
    
    Location getLocation(){
        return mLocation;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction#getAdress()
     */
    @Override
    public BigInteger getAdress() {
        //Hmmm. Mispelled!
        return BigInteger.valueOf(mRecord.getAddress());
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction#getFuntionName()
     */
    @Override
    public String getFuntionName() {
        //mispelled again
        //Framework can't handle null
        String name = mLocation.getFunction();
        return name != null?name:"";
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction#getInstruction()
     */
    @Override
    public String getInstruction() {
        return getOpcode() + "\t" + getArgs();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction#getOpcode()
     */
    @Override
    public String getOpcode() {
        return mRecord.getOpcode();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction#getArgs()
     */
    @Override
    public String getArgs() {
        return mRecord.getOperands();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction#getOffset()
     */
    @Override
    public long getOffset() {
        // Accomodate the fact that null functions are
        // received as empty strings.
        if (mLocation.getFunction() == null)
            return mLocation.getAddress();
        return mLocation.getFunctionOffset();
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getTarget()
     */
    @Override
    public ICDITarget getTarget() {
        return mTarget;
    }

}
