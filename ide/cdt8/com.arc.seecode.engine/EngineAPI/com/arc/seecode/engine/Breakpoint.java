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
 * A location breakpoint or a data watchpoint. There
 * are two subclasses: {@link LocationBreakpoint} and
 * {@link Watchpoint}.
 * @author David Pickens
 */
public abstract class Breakpoint {
    private static final int[] NO_THREADS = new int[0];

    /**
    * @param e the associated engine interface.
    * @param condition if not null, a conditional expression to be
    * evaluated at the location to determine if the breakpoint it to take.
    * @param tid if not zero, a thread id to be tied to the breakpoint.
    * @param breakID the ID returned by the engine that denotes this
    * break- or watchpoint.
    * @param flags breakpoint flags.
    * @param enabled if false, then breakpoint was created disabled.
    * */
    Breakpoint(EngineInterface e, int hitCount, String condition, int tid, int breakID, int flags,
                boolean enabled){
        mEngine = e;
        mCondition = condition;
        if (tid != 0){
            mThreads = new int[]{tid};
        }
        else mThreads = NO_THREADS;
        mBreakID = breakID;
        mFlags = flags;
        mEnabled = enabled;
        fIgnoreCount = hitCount;
    }

    /**
     * The breakpoint needs to be re-applied to the engine.
     * Return true if successful.
     */
    abstract boolean reapply() throws EngineException;

    /**
     * Return the break ID through which this breakpoint is referenced
     * via the {@link EngineInterface}.
     * @return break ID number.
     */
    public int getBreakID() {
        return mBreakID;
    }
    
    /**
     * Called from sub-class when need to re-apply ID after
     * re-generating it. The engine may have deleted it when restarting
     * a changed exe.
     * @param id the new breakid
     */
    protected void setBreakID(int id){
        mBreakID = id;
    }

    public String getCondition() {
        return mCondition;
    }

    public int[] getThreads() {
        return mThreads;
    }

    /**
     * Enable or disable this breakpoint.
     * @param v
     * @pre mEngine.isValidBreakpoint(mBreakID)
     * @post mEngine.
     */
    public void setEnabled(boolean v) throws EngineException {
        if (v != mEnabled){
            mEnabled = v;
            mEngine.setBreakpointEnabled(mBreakID,v);
        }
    }
    
    /**
     * Called to make the "enabled" state reflect the engine's assumed state.
     * @param v
     */
    void setEnabledProperty(boolean v){
        mEnabled = v;
    }

    public boolean isEnabled() {
        return mEnabled;
    }
    
    public boolean isTemporary(){
        return (getFlags() & IEngineAPI.BP_TEMPORARY) != 0;
    }
    
    /**
     * Adjust flags after engine has actually set the breakpoint.
     * @param flags reflects that actual attributes that the engine applied to the breakpoint.
     */
    public void setFlags(int flags){
        this.mFlags = flags;
    }
    
    /**
     * Return the number of times that this action point has been hit.
     * @return the number of times that this action point has been hit.
     * @throws EngineException 
     */
    public int getHitCount() throws EngineException {
        return mEngine.getBreakpointHitCount(mBreakID);       
    }

    protected void setRemoved() throws EngineException {
        // called during garbage-collection to free breakpoint.
        if (mBreakID != 0){
            try {
                mEngine.freeBreakpoint(mBreakID);
            } catch(EngineDisconnectedException x){
                // We may be called after the engine terminated as
                // the UI is doing stuff. Ignore disconnection errors.
            }
            mBreakID = 0;
        }
        
    }
    
    /**
     * Indicate that the engine has removed this breakpoint, so no need
     * to send a redundant removeBreakpoint call to the engine.
     */
    public void setDeletedByEngine(){
        mDeletedByEngine = true;
    }

    public boolean isRemoved() {
        return mBreakID == 0;
    }

    public boolean isDeletedByEngine(){
        return mDeletedByEngine;
    }
    @Override
    protected void finalize() {
        if (!isRemoved()) {
            mEngine.enqueue(new Runnable() {
                @Override
                public void run() {
                    try {
                        setRemoved(); // free up breakpoint
                    } catch (EngineException e) {
                    }
                }
            },null);
        }
    }

    /**
     * @return whether or not this is a hardware breakpoint
     */
    public boolean isHardware() {
        return (mFlags & IEngineAPI.BP_HARDWARE) != 0;
    }

    /**
     * @param expression
     */
    public void setCondition(String expression) throws EngineException {
        if (expression != null && expression.length() == 0) expression = null;
        if ( expression != null && !expression.equals(mCondition) || expression == null && mCondition != null){
            mCondition = expression;
            if (!isRemoved()) // avoid race condition ?
                mEngine.setBreakpointCondition(mBreakID,expression);       
        }
    }
    
    private static boolean threadListCompares(int t1[], int t2[]){
        if (t1.length != t2.length) return false;
        for (int i = 0; i < t1.length; i++){
            int tid = t1[i];
            boolean found = false;
            for (int j = 0; j < t2.length; j++){
                if (t2[j] == tid) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }
    
    public void setThreads(int threads[]) throws EngineException{
        if (! isRemoved() && !threadListCompares(mThreads,threads)){
            mThreads = threads;
            mEngine.setBreakpointThreads(mBreakID,threads);
        }
    }
    
    protected EngineInterface getEngine(){
        return mEngine;
    }
    
    protected int getFlags(){
        return mFlags;
    }
    
    /**
     * @param count
     */
    public void setIgnoreCount(int count) throws EngineException {
        if (isRemoved()) return; // prevent race condition
        if (count < 0) throw new IllegalArgumentException("Count is negative: " + count);
        if (fIgnoreCount != count) {
            fIgnoreCount = count;
            getEngine().setBreakpointHitCount(getBreakID(),count);  
        }
    }
    
    public int getIngoreCount(){
        return fIgnoreCount;
    }
    
    /**
     * Called as part of "toString()" operation to display this object
     * as a human-readable string.
     * @param buf  string builder to append to.
     */
    protected void appendStringSuffix(StringBuilder buf){
        String condition = getCondition();
        if (condition != null && condition.length() > 0){
            buf.append(", eval ");
            buf.append(condition);
        }
        int hitCount = this.getIngoreCount();
        if (hitCount > 1){
            buf.append(", count ");
            buf.append(hitCount);
        }
        int thread[] = this.getThreads();
        if (thread != null && thread.length > 0){
            buf.append(", thread");
            for (int t: thread){
                buf.append(' ');
                buf.append(t);
            }
        }
        int cnt;
        try {
            cnt = getHitCount();
        }
        catch (EngineException e) {
            cnt = 0;
        }
        if (cnt > 0){
            buf.append(" [hit:");
            buf.append(cnt);
            buf.append(" times]");
        }
    }
    
    public String toDisplayString(){
        return toString();
    }

    private String mCondition;
    private int mThreads[];
    private int mBreakID;
    private EngineInterface mEngine;
    private boolean mEnabled = true;
    private int mFlags;
    private int fIgnoreCount;
    private boolean mDeletedByEngine = false;

}
