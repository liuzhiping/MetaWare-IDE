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
 * Data describing a single breakpoint.
 * @author David Pickens
 */
public class LocationBreakpoint extends Breakpoint {
    /**
     * Create a description of  breakpoint.
     * <b>NOTE:</b>
     * This should not be instantiated outside the context of the {@Link BreapointManager}
     * because the engine's breakpoint ID is deleted when this instance 
     * is garbage-collected.
     * @param e the associated engine interface.
     * @param loc the location of the breakpoint
     * @param hitCount the number of hits before breakpoint stops
     * @param condition if not null, a conditional expression to be
     * evaluated at the location to determine if the breakpoint it to take.
     * @param tid if not zero, a thread id to be tied to the breakpoint.
     * @param flags breakpoint flags.
     * @param enabled whether or not the breakpoint was created enabled or disabled.
     * 
     * @pre loc != null 
     * @pre hitCount >= 0
     * @pre e.isValidBreakpoing(breakID)
     * @post $none
     */
     LocationBreakpoint(EngineInterface e, Location loc, int hitCount, String condition, int tid,
            int breakID, int flags, boolean enabled){
         super(e,hitCount, condition,tid,breakID,flags, enabled);
        mLocation = loc;

    }
    
    public Location getLocation(){
        return mLocation;
    }
    
    private static long computeAddress(EngineInterface engine, Location loc)throws EngineException{
        if (loc.getSource() != null && loc.getSourceLine() > 0){
            try {
                Location newLoc = engine.lookupSource(loc.getSource(),loc.getSourceLine());
                if (newLoc != null)
                   return newLoc.getAddress() + loc.getSourceLineOffset();
            } catch (EvaluationException e) {
            }    
        }
        else if (loc.getFunction() != null) {
            Location newLoc;
            try {
                newLoc = engine.evaluateLocation(loc.getFunction(),0);
                if (newLoc != null)
                    return newLoc.getAddress() + loc.getFunctionOffset();
            } catch (EvaluationException e) {             
            }
            
        }
        return loc.getAddress();
    }
    
    @Override
    boolean reapply() throws EngineException{
        //Hex address may have changed.
        mLocation.setAddress(computeAddress(getEngine(),mLocation));
        int tid = 0;
        if (this.getThreads().length > 0){
            tid = this.getThreads()[0];
        }
        int id = getEngine().createBreakpoint(mLocation, getIngoreCount(), getCondition(), 
                    tid, getFlags(), isEnabled() || !getEngine().canCreateDisabledActionPoints());
        if (id >= 0) {
            setBreakID(id);
            if (!isEnabled() && !getEngine().canCreateDisabledActionPoints())
                getEngine().setBreakpointEnabled(id,isEnabled());
            return true;
        }
        return false;      
    }
    

    /**
     * @return whether or not this breakpoint is a temporary one.
     */
    @Override
    public boolean isTemporary() {
        return (getFlags() & IEngineAPI.BP_TEMPORARY) != 0;
    }
    
    static String toString(Location loc){
        StringBuilder buf = new StringBuilder();
        String src = loc.getSource();
        String func = loc.getFunction();
        if (src != null){
            buf.append(src);
            buf.append('!');
            buf.append(loc.getSourceLine());
            int off = loc.getSourceLineOffset();
            if (off != 0){
                buf.append("[+0x");
                buf.append(Integer.toHexString(off));
                buf.append("]");
            }
            buf.append(" @0x");
            buf.append(Long.toHexString(loc.getAddress()));
        }
        else if (func != null){
            buf.append(func);
            int off = loc.getFunctionOffset();
            if (off != 0){
                buf.append("+0x");
                buf.append(Integer.toHexString(off));
            }
            buf.append(" @0x");
            buf.append(Long.toHexString(loc.getAddress()));
        }
        else {
            buf.append("0x");
            buf.append(Long.toHexString(loc.getAddress()));
        }
        return buf.toString();
    }
    
    @Override
    public String toDisplayString(){
        StringBuilder buf = new StringBuilder();
        buf.append(toString(mLocation));
        appendStringSuffix(buf);
        return buf.toString();
    }
    
    @Override
    public String toString(){
        StringBuilder buf = new StringBuilder();
        buf.append("break ");
        buf.append(toString(mLocation));
        appendStringSuffix(buf);
        return buf.toString();
    }
    
    private Location mLocation;

}
