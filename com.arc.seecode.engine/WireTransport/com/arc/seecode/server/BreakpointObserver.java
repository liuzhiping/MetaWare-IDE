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
package com.arc.seecode.server;

import com.arc.seecode.engine.IBreakpointObserver;
import com.arc.seecode.engine.Location;
import com.arc.seecode.scwp.ScwpCommandPacket;


/**
 * Handle callbacks for breakpoint events.
 * @author David Pickens
 */
class BreakpointObserver extends AbstractObserver implements IBreakpointObserver {

    BreakpointObserver(CallbackThread callbackThread, int cmpdProcessID){
        super(callbackThread,cmpdProcessID * ScwpCommandPacket.REQUIRED_CHANNELS+ScwpCommandPacket.BREAKPOINT_OBSERVER);
   }
    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IBreakpointObserver#breakpointStateChange(int, boolean)
     */
    @Override
    public void breakpointStateChanged(int breakID, boolean enabled) {
        dispatch("breakpointStateChanged",new Object[]{new Integer(breakID), Boolean.valueOf(enabled)});
    }
    
    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IBreakpointObserver#breakpointRemoved(int)
     */
    @Override
    public void breakpointRemoved(int breakID) {
        dispatch("breakpointRemoved",new Object[]{new Integer(breakID)});

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IBreakpointObserver#breakpointConditionChanged(int)
     */
    @Override
    public void breakpointConditionChanged(int breakID) {
        dispatch("breakpointConditionChanged",new Object[]{new Integer(breakID)});

    }
    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IBreakpointObserver#breakpointAdded(int, com.arc.seecode.engine.Location, int, java.lang.String, int)
     */
    @Override
    public void breakpointAdded(int breakID, Location location, int hitCount, String conditional, int tid) {
        dispatch("breakpointAdded",new Object[]{new Integer(breakID), location, new Integer(hitCount), conditional, new Integer(tid)});
        
    }
    @Override
    public void watchpointAdded (int breakID, String var, int length, int hitCount, String condition, int flags) {
        dispatch("watchpointAdded",new Object[]{new Integer(breakID), var, new Integer(length),new Integer(hitCount), condition, new Integer(flags)});
        
    }
    @Override
    public void watchpointAdded2 (
        int breakID,
        String var,
        int length,
        String condition,
        int flags,
        int threadID,
        String[] attributes) {
        dispatch("watchpointAdded2",new Object[]{new Integer(breakID), var, new Integer(length), condition, new Integer(flags),
            new Integer(threadID),attributes});
        
    }
    @Override
    public void breakpointAdded2 (
        int breakID,
        Location location,
        int ignoreCount,
        String conditional,
        int tid,
        int flags,
        String[] attributes) {
        dispatch("breakpointAdded2",new Object[]{new Integer(breakID), location, new Integer(ignoreCount), conditional, new Integer(tid),
            new Integer(flags),attributes});
        
    }

}
