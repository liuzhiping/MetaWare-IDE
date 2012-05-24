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
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.internal.core.cdi.event.DestroyedEvent;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EvaluationException;
import com.arc.seecode.engine.Location;
import com.arc.seecode.engine.StackFrameRef;

/**
 * Manages blocks of memory being displayed.
 * 
 * @author David Pickens
 */
class MemoryManager extends Manager {

    private List<ICDIMemoryBlock> mList = new ArrayList<ICDIMemoryBlock>();

    /**
     * @param target the assoicated target.
     */
    public MemoryManager(Target target) {
        super(target, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager#createMemoryBlock(java.lang.String,
     *      int)
     */
    public ICDIMemoryBlock createMemoryBlock(String address, int unit, int wordsize)
            throws CDIException {
        Target target = getTarget();
        StackFrameRef sf = null;
        try {
            sf = target.getStackFrame();
        } catch(CDIException e){} // If thread isn't stopped, then no stack frame.
        Location loc;
        try {
            loc = sf != null?sf.evaluateLocation(address):target.getEngineInterface().evaluateLocation(address,0);
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        } catch (EvaluationException e) {
            throw new CDIException(e.getMessage());
        }
        if (loc == null || !loc.isValid())
                throw new CDIException("Cannot evaluate " + address);
        //        IType type = v.getType();
        //        if (type != null && type.getKind() != IType.INTEGER &&
        //                	type.getKind() != IType.POINTER)
        //            throw new CDIException("\"" + address + "\" is not of a valid type");
        //        String a = v.getValue();
        //        long startAddress;
        //        try {
        //            if (a.startsWith("0x")){
        //                startAddress = Long.parseLong(a.substring(2),16);
        //            }
        //            else startAddress = Long.parseLong(a);
        //        } catch (NumberFormatException e) {
        //            throw new CDIException("Not a recognizable address value: " + a);
        //        }
        return createMemoryBlock(BigInteger.valueOf(loc.getAddress()), unit,wordsize);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager#createMemoryBlock(long,
     *      int)
     */
    public ICDIMemoryBlock createMemoryBlock(BigInteger address, int units, int wordsize) {
        MemoryBlock m = new MemoryBlock(getTarget(), address.longValue(), units, wordsize);
        mList.add(m);
        return m;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager#removeBlock(org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock)
     */
    public void removeBlock(ICDIMemoryBlock memoryBlock) {
        if (mList.remove(memoryBlock)){
            EventManager emgr = (EventManager)getSession().getEventManager();
            if (emgr != null){
                emgr.enqueueEvent(new DestroyedEvent(memoryBlock));
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager#removeBlocks(org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock[])
     */
    public void removeBlocks(ICDIMemoryBlock[] memoryBlocks) {
        List<ICDIEvent> list = new ArrayList<ICDIEvent>(memoryBlocks.length);
        for (int i = 0; i < memoryBlocks.length; i++) {
            if (mList.remove(memoryBlocks[i])){
                list.add(new DestroyedEvent(memoryBlocks[i]));
            }
        }
        if (list.size() > 0){
            EventManager emgr = (EventManager)getSession().getEventManager();
            if (emgr != null){
                emgr.enqueueEvents(list.toArray(new ICDIEvent[list.size()]));
            }          
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager#removeAllBlocks()
     */
    public void removeAllBlocks() {
        removeBlocks(mList.toArray(new ICDIMemoryBlock[mList.size()]));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager#getMemoryBlocks()
     */
    public ICDIMemoryBlock[] getMemoryBlocks() {
        return mList.toArray(new ICDIMemoryBlock[mList.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDIManager#update()
     */
    @Override
    public void update(Target target) throws CDIException {
        ICDIMemoryBlock m[] = getMemoryBlocks();
        for (int i = 0; i < m.length; i++) {
            if (!m[i].isFrozen()) m[i].refresh();
        }
    }
}
