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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.internal.core.cdi.event.MemoryChangedEvent;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;


/**
 * A representation of a block of memory being displayed.
 * @author David Pickens
 */
class MemoryBlock implements ICDIMemoryBlock {
    private Target mTarget;
    private boolean mFrozen;
    private long mAddress;
    private int mLength;
    private byte[] mBuffer = null;
    private int mWordsize;
    /**
     * 
     */
    public MemoryBlock(Target target, long address, int units, int wordsize) {
        mTarget = target;
        mFrozen = false;
        mAddress = address;
        mLength = units*wordsize;
        mWordsize = wordsize;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock#getStartAddress()
     */
    @Override
    public BigInteger getStartAddress() {
        return BigInteger.valueOf(mAddress);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock#getLength()
     */
    @Override
    public long getLength() {
        return mBuffer == null?mLength: mBuffer.length;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock#getBytes()
     */
    @Override
    public byte[] getBytes() throws CDIException {
        if (mBuffer == null) {
            refresh();
        }
        return mBuffer;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock#setValue(long, byte[])
     */
    @Override
    public void setValue (long offset, byte[] bytes) throws CDIException {
        EngineInterface engine = mTarget.getEngineInterface();
        if (mBuffer == null)
            refresh();
        if (offset < 0 || offset + bytes.length > mLength)
            throw new CDIException("Bad memory range");
        List<BigInteger> changedAddresses = new ArrayList<BigInteger>();
        int lowest = -1;
        int highest = -1;
        for (int i = 0; i < bytes.length; i++) {
            if (mBuffer[(int) offset + i] != bytes[i]) {
                if (lowest < 0)
                    lowest = (int) offset + i;
                highest = (int) offset + i;
                mBuffer[(int) offset + i] = bytes[i];
                changedAddresses.add(BigInteger.valueOf(mAddress + offset + i));
            }
        }
        if (lowest >= 0) {
            try {
                engine.setMemoryBytes(mAddress + lowest, mBuffer, lowest, highest - lowest + 1, 0);
            }
            catch (EngineException e) {
                SeeCodePlugin.log(e);
                throw new CDIException(e.getMessage());
            }
            if (changedAddresses.size() > 0) {
                fireChanged(changedAddresses);
            }
        }
    }

    /**
     * @param changedAddresses list of addresses that changed.
     */
    private void fireChanged(List<BigInteger> changedAddresses) {
        EventManager emgr = (EventManager)mTarget.getSession().getEventManager();
        emgr.enqueueEvent(new MemoryChangedEvent(this,changedAddresses.toArray(new BigInteger[changedAddresses.size()])));
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock#isFrozen()
     */
    @Override
    public boolean isFrozen() {
        return mFrozen;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock#setFrozen(boolean)
     */
    @Override
    public void setFrozen(boolean frozen) {
        mFrozen = frozen;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock#refresh()
     */
    @Override
    public void refresh() throws CDIException {
        EngineInterface engine = mTarget.getEngineInterface();
        byte[] bytes = null;
        try {
            bytes = engine.getMemoryBytes(mAddress,mLength,0);
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        }
        if (mBuffer != null){
            List<BigInteger> changedAddresses = new ArrayList<BigInteger>();
            int cnt = Math.min(bytes.length,mBuffer.length);
            for (int i = 0; i < cnt; i++){
                if (bytes[i] != mBuffer[i]){
                    changedAddresses.add(BigInteger.valueOf(mAddress+i));
                }
            }
            if (changedAddresses.size() > 0){
                fireChanged(changedAddresses);
            }
        }
        mBuffer = bytes;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getTarget()
     */
    @Override
    public ICDITarget getTarget() {
        return mTarget;
    }

 
    @Override
    public byte getFlags (int offset) {
       return VALID; // need more work here
    }

    @Override
    public int getWordSize () {
        return mWordsize;
    }

}
