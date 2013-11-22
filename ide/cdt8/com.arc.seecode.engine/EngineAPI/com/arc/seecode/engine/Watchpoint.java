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
package com.arc.seecode.engine;

/**
 * @author David Pickens
 */
public class Watchpoint extends Breakpoint {

    private String mVar;
    private int mLength;
    private String[] mAttributes;
    private WatchpointHit mPendingHit = null;

    /**
     * @param e
     *            the associated engine interface.
     * @param var
     *            the variable that is being watched.
     * 
     * @param length
     * 			   the number of bytes to watch or 0
     * 				if the size is to be derived from the
     * 				variable's size.
     * 
     * @param condition
     *            if not null, a conditional expression to be evaluated at the
     *            location to determine if the breakpoint it to take.
     * @param tid
     *            if not zero, a thread id to be tied to the breakpoint.
     * @param breakID
     *            the ID returned by the engine that denotes this 
     *            watchpoint.
     * @param flags
     *            breakpoint flags.
     * @param enabled if false, then watchpoint was created in a disabled state.
     * @param attributes
     *            esoteric attributes (e.g., "mask=0x...") or <code>null</code>.
     */
    public Watchpoint(EngineInterface e, String var, int length, 
            String condition, int tid,
            int breakID, int flags, boolean enabled, String attributes[]) {
        super(e, 0,condition, tid, breakID, flags,enabled);
        mVar = var;
        mLength = length;
        mAttributes = attributes;
    }

    public String getVariable() {
        return mVar;
    }
    
    public int getLength(){
        return mLength;
    }
    
    public boolean isWrite(){
        return (getFlags() & IEngineAPI.WP_WRITE) != 0;
    }
    
    public boolean isRead(){
        return (getFlags() & IEngineAPI.WP_READ) != 0;
    }
    
    @Override
    public String toString(){
        StringBuilder buf = new StringBuilder();
        buf.append("watch ");
        return appendRest(buf);
    }
    
    @Override
    public String toDisplayString(){
        StringBuilder buf = new StringBuilder();
        return appendRest(buf);
    }

    private String appendRest (StringBuilder buf) {
        buf.append(getVariable());
        if (getLength() > 0) {
            buf.append(", length ");
            buf.append(getLength());
        }
        if (mAttributes != null){
            for (String s: mAttributes){
                buf.append(",");
                buf.append(s.replace('=',' '));
            }
        }
        appendStringSuffix(buf);
        return buf.toString();
    }
    
    @Override
    boolean reapply() throws EngineException {
        //Only reset for global variables!
        int id = getEngine().createWatchpoint(mVar, mLength, getCondition(), 0, getFlags(),0,
                    isEnabled() || !getEngine().canCreateDisabledActionPoints(),mAttributes);
        if (id > 0){
            setBreakID(id);
            if (!isEnabled() && !getEngine().canCreateDisabledActionPoints())
                getEngine().setBreakpointEnabled(id,isEnabled());
            return true;
        }
        return false;
    }
    
    /**
     * If this watch point was responsible for the stop that just took place, then
     * return the information as to what caused the stop.
     * Turns null if this watch point was not responsible for the last stop.
     * @return the watchpoint that caused the stop.
     */
    public WatchpointHit getPendingHit(){
        return mPendingHit;
    }
    
    /**
     * Set the watch "hit" information if this watch point caused the last stop.
     * This method is also called with <code>null</code> to clear things when resuming.
     * @param hit the watch point hit information if this watchpoint caused a hit, or <code>null</code>
     * to clear the pending hit.
     */
    void setPendingHit(WatchpointHit hit){
        mPendingHit = hit;
    }

}
