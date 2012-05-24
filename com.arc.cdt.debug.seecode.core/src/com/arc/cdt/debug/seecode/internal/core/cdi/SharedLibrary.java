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

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;


/**
 * @author David Pickens
 */
public class SharedLibrary implements ICDISharedLibrary {
    private EngineInterface mEngine;
    private ICDITarget mTarget;
    private int mID;
    private String mName = null;
    private long mStartAddress = -1;
    private long mEndAddress = -1;
    /**
     * @param target the target.
     * @param id the module id for this library.
     */
    public SharedLibrary(Target target, int id) {
        mEngine = target.getEngineInterface();
        mID = id;
        mTarget = target;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary#getFileName()
     */
    @Override
    public String getFileName() {
        if (mName == null){
            try {
                mName = mEngine.getModuleName(mID);
            } catch (EngineException e) {
                SeeCodePlugin.log(e);
                mName = "???";
            }
        }
        return mName;
    }
    
    int getModuleID() {
        return mID;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary#getStartAddress()
     */
    @Override
    public BigInteger getStartAddress() {
        if (mStartAddress == -1){
            try {
                mStartAddress = mEngine.getModuleBaseAddress(mID);
            } catch (EngineException e) {
                SeeCodePlugin.log(e);
                mStartAddress = 0xDeadBeef;
            }
        }
        return BigInteger.valueOf(mStartAddress);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary#getEndAddress()
     */
    @Override
    public BigInteger getEndAddress() {
        if (mEndAddress == -1){
            try {
                if (mStartAddress == -1) getStartAddress();
                mEndAddress = mStartAddress+mEngine.getModuleSize(mID);
            } catch (EngineException e) {
                SeeCodePlugin.log(e);
                mEndAddress = 0xDeadBeef;
            }
        }
        return BigInteger.valueOf(mEndAddress);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary#areSymbolsLoaded()
     */
    @Override
    public boolean areSymbolsLoaded() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary#loadSymbols()
     */
    @Override
    public void loadSymbols() throws CDIException {
        // already loaded implicitly by engine
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getTarget()
     */
    @Override
    public ICDITarget getTarget() {
        return mTarget;
    }

}
