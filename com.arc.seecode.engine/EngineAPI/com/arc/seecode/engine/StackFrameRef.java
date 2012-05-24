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

import java.util.HashMap;
import java.util.Map;

/**
 * Local variable lookupss, register access, etc. are done relative to
 * stackframes. Stackframe, however, are very transitory, and, thus, need to be
 * subject to garbage collection.
 * <P>
 * Therefore, we maintain stackframe references on the Java side.
 * <P>
 * The engine maintains a collection of <i>register set snapshots <i>which are
 * referenced by <i>reg set IDs </i>. The class is the <i>only </I> reference to
 * these IDs. Then this object is garbage collected, its corresponding
 * reg-set-ID is freed by calling {@link IEngineAPI#freeStackFrameID(int)}.
 * <P>
 * Within the engine, the <code>RegSetID's</code> can become invalid as the
 * associated thread runs. When a regset becomes invalid, it will never become
 * invalid again. This state can be access by calling {@link #isValid()}.
 * 
 * @author David Pickens
 * @nojni
 */
public class StackFrameRef {

    private static final int UNDEFINED = 0xDEADBEEF;

    private int mFrameID;

    private int mTID;

    private EngineInterface mEngine;

    private StackFrameRef mCaller;

    private boolean mCallerComputed = false;

    private boolean mIsTopMost = true;

    private int mLevel = 0;

    private boolean mValid = true;

    private Map<String,Variable> mVarCache = null;

    private Variable[] mLocals = null;

    private Map<Integer,RegisterContent> mRegCache = new HashMap<Integer,RegisterContent>();

    private long mPC = UNDEFINED;

    private long mFramePointer = UNDEFINED;

    private long mStackPointer = UNDEFINED;

    /**
     * Create a stackframe reference.
     * <P>
     * The <code>regSetID</code> argument is used by the engine to record
     * register-set contents. <I>This is the only class that references this
     * integer! </I> When this object is garbage-collected,
     * {@link IEngineAPI#freeStackFrameID(int)}is called to free the underlying
     * storage for the register set.
     * <P>
     * 
     * @param e
     *            the engine interface. ALso serves as a lock for accessing the
     *            {@link IEngineAPI}object.
     * @param tid
     *            the associated thread.
     * @param frameID
     *            an internal identifier that the engine.
     */
    public StackFrameRef(EngineInterface e, int tid, int frameID) {
        mTID = tid;
        mFrameID = frameID;
        mEngine = e;
    }

    public boolean isTopMostFrame() {
        return mIsTopMost;
    }

    void invalidate() {
        mValid = false;
        synchronized(this) {
            mVarCache = null;
            mLocals = null;
        }
        synchronized(mRegCache) {
            mRegCache.clear();
        }
        //cr99138: Don't kill PC. We need to be able to compare source file
        // references between an old invalidated frame and a new active one.
        //mPC = UNDEFINED;
    }
    
    void invalidateRegister(int reg){
        Integer regObject = new Integer(reg);
        mRegCache.remove(regObject);
    }

    /**
     * Lookup a variable from within this stack frame.
     * 
     * @param name
     *            the name of the variable.
     * @return the variable, or <code>null</code>.
     */
    public Variable lookupVariable(String name) throws EngineException,
            EvaluationException {
        if (name == null) // Happened during validation testing
            throw new IllegalArgumentException("Name is null");
        synchronized (this) {
            if (mVarCache != null) {
                Variable var =  mVarCache.get(name);
                if (var != null) return var;
            }
        }

        // Note that a competing thread could get in here, and 
        // call "mEngine.lookupVariable()" on the same name ahead of us.
        // In practice, it shouldn't hurt anything because the resulting
        // objects should compare equal.
        Variable v;
        /*synchronized(mEngine) */{
            v = mEngine.lookupVariable(name, mFrameID);
        }
       
        if (v != null) {
            v.setStackFrame(this);
            synchronized(this) {
                // NOte that we could have a subtle race condition in which
                // in which a competing thread may have already inserted
                // the variable into the cache. But shouldn't matter.
                if (mVarCache == null) mVarCache = new HashMap<String,Variable>();
                mVarCache.put(name, v);
            }
        }
        return v;
    }
    
    /**
     * Lookup a variable but bypass our local cache.
     * @param name
     * @return the variable with the given name.
     * @throws EngineException
     * @throws EvaluationException
     */
    Variable lookupVariableUncached(String name) throws EngineException, EvaluationException {
        return mEngine.lookupVariable(name,mFrameID);
    }

    /**
     * Evaluate an expression in the context of this stack frame.
     * 
     * @param expression
     *            the contents of the expression.
     * @return the value, or <code>null</code> if an error occurred.
     */
    public Value evaluate(String expression) throws EngineException,
            EvaluationException {
        return mEngine.evaluate(expression, mFrameID);
    }

    public boolean isValid() {
        return mValid;
    }

    public Variable[] getLocals () throws EngineException {
        //NOTE:we forgo synchronizing to avoid locking the GUI if the engine hangs, or take awhile. Though a non UI thread may be
        // calling this method, there may a UI thread that is attempting to gain the lock.
        /*synchronized (this)*/ {
            if (mLocals == null) {
                Variable v[] = mEngine.getAPI().getLocals(mFrameID);
                if (v != null) {
                    for (int i = 0; i < v.length; i++) {
                        v[i].setStackFrame(this);
                    }
                }
                mLocals = v;
            }
            return mLocals;
        }
    }
    
    private static final int OUTRAGEOUS_STACKFRAME_DEPTH = 200;
    private Object mCallerLock = new Object();

    public StackFrameRef getCallerFrame () throws EngineException {
        synchronized (mCallerLock) {
            if (mCallerComputed)
                return mCaller;
            mCallerComputed = true;
            // Due to engine bug, we can end up never terminating the callstack
            // chain. If the levels are getting too deep, then stop.
            if (isValid() && mLevel < OUTRAGEOUS_STACKFRAME_DEPTH) {
                int callerID = mEngine.getAPI().computeCallerFrame(mFrameID);
                if (callerID != 0) {
                    mCaller = new StackFrameRef(mEngine, mTID, callerID);
                    mCaller.mIsTopMost = false;
                    mCaller.mLevel = mLevel + 1;
                }
            }
            return mCaller;
        }
    }
    
    public RegisterContent getRegisterContent (int regID) throws EngineException {
        Integer reg = new Integer(regID);
        synchronized (mRegCache) {
            RegisterContent content = mRegCache.get(reg);
            if (content == null) {
                content = mEngine.getRegisterContent(mFrameID,regID);
                if (content != null) { // should always be non null
                    mRegCache.put(reg, content);
                }
            }
            return content;
        }
    }
    
    private RegisterContent[] readRegsFromEngine(int regs[]) throws EngineException{
        RegisterContent[] result = mEngine.getRegisterContent(mFrameID,regs);
        for (RegisterContent r: result){
            mRegCache.put(r.getRegister(), r);
        }
        return result;
    }
    
    public RegisterContent[] getRegisterContent (int regIDs[]) throws EngineException {
        RegisterContent result[] = null;
        // If a significant subset are not in the cache, then refresh all of them from
        // the engine. Otherwise, we can save bandwidth time by reducing engine traffic.
        synchronized (mRegCache) {
            if (mRegCache.size() >= regIDs.length * 3 / 4) {
                int uncachedRegs[] = new int[regIDs.length];
                int last = 0;
                for (int i = 0; i < regIDs.length; i++) {
                    if (!mRegCache.containsKey(regIDs[i])) {
                        uncachedRegs[last++] = regIDs[i];
                    }
                }
                if (last > 0) {
                    if (last > regIDs.length / 2 && regIDs.length - last < 10) { // If most are not cached, reread
                        result = readRegsFromEngine(regIDs);
                    }
                    else {
                        int newList[] = new int[last];
                        System.arraycopy(uncachedRegs, 0, newList, 0, last);
                        readRegsFromEngine(newList);
                    }
                }
                if (result == null) {
                    result = new RegisterContent[regIDs.length];
                    for (int i = 0; i < regIDs.length; i++) {
                        result[i] = mRegCache.get(regIDs[i]);
                        assert result[i] != null;
                    }
                }
            }
            else
                result = readRegsFromEngine(regIDs);
        }
        return result;
    }

    public String getRegisterValue (int regID, Format format) throws EngineException {
        RegisterContent content = getRegisterContent(regID);
        if (content != null) return content.toString(format);
        return "???";
    }

    public boolean setRegisterValue (int regID, String value) throws EvaluationException, EngineException {
        boolean result = mEngine.setRegisterValue(mFrameID, regID, value);
        synchronized (mRegCache) {
            // Just invalidate; we want to see the value as the engine has it stored.
            mRegCache.remove(new Integer(regID));
        }
        return result;
    }
    

    public Location evaluateLocation(String expression) throws EngineException,
            EvaluationException {
        return mEngine.evaluateLocation(expression, mFrameID);
    }

    public int getLevel() {
        return mLevel;
    }

    public int getThreadID() {
        return mTID;
    }

    public long getPC() throws EngineException {
        synchronized(this) {
            if (mPC != UNDEFINED) return mPC;
        }
        long pc = mEngine.getAPI().getPC(mFrameID);
        synchronized(this){
            mPC = pc;
        }
        return pc;
    }

    public long getStackPointer() throws EngineException {
        // Note: we don't want to block on mEngine unless we have to.
        // There may be competing threads that have the engine lock
        // which could stall the caller significantly.
        synchronized(this) {
            if (mStackPointer != UNDEFINED) return mStackPointer;
        }
        long sp = mEngine.getAPI().getStackPointer(mFrameID);
        synchronized(this){
            mStackPointer = sp;
        }
        return sp;
    }

    public synchronized long getFramePointer() throws EngineException {
        // Note: we don't want to block on mEngine unless we have to.
        // There may be competing threads that have the engine lock
        // which could stall the caller significantly.
        synchronized(this){
            if (mFramePointer != UNDEFINED) return mFramePointer;
        }
        long fp = mEngine.getFramePointer(mFrameID);
        synchronized(this){
            mFramePointer = fp;
        }
        return fp;
    }

    /**
     * Return the frame ID that denotes this frame from within the engine.
     * 
     * @return the frame ID that denotes this frame from within the engine.
     * @nojni
     */
    int getFrameID() {
        return mFrameID;
    }

    /**
     * @nojni
     */
    @Override
    public String toString(){
        try {
            return "SeeCodeFrame(fp=0x"
                + Long.toHexString(getFramePointer())
                + ",pc=0x"
                + Long.toHexString(getPC())
                + ")";
        }
        catch (Exception e) {
            return "SeeCodeFrame(" + e  +")";
        }        
    }
    /**
     * Free the stackframe ID. This is suppose to be the only reference.
     * @nojni
     */
    @Override
    protected void finalize() {
        if (!mEngine.isShutdown()) {
            
            Runnable runner = new Runnable() {
                @Override
                public void run() {
                    try {
                        mEngine.getAPI().freeStackFrameID(mFrameID);
                    } catch (EngineException e) {
                        // What else to do?
                        //Ignore, and assume subsequent resume or step
                        // will reproduce error.
                    }
                }
            };
            mEngine.enqueue(runner,null);
        }
    }

    /**
     * 
     * Return true if this frame is the same as another.
     * @param ref the frame to compare to.
     * @return true if this frame is the same as the argument frame.
     * @nojni
     */
    public boolean isEqual(StackFrameRef ref){
        try {
            //if (ref.getLevel() != getLevel()) return false;
            //UI logic considers two stackframes to be "equal" if
            // they represent the same function invocation, regardless of
            // PC value.
            //if (ref.getPC() != getPC()) return false;
            if (ref.getFramePointer() != getFramePointer()) return false;
            if (this.mCallerComputed && ref.mCallerComputed){
                StackFrameRef thisCaller = this.getCallerFrame();
                StackFrameRef thatCaller = ref.getCallerFrame();
                if (thisCaller != null && thatCaller != null){
                    if (thisCaller.getPC() != thatCaller.getPC() ||
                        thisCaller.getFramePointer() != thatCaller.getFramePointer())
                        return false;
                }
            }
            return true;
        }
        catch (EngineException e) {
            return false; //if this or ref is invalid
        }
    }

    /**
     * This method is only defined because SeeCode engines built prior to Oct 16, 2006
     * have a reference to it. However, it is never called.
     * To be deleted at some point.
     * @deprecated
     */
    @Deprecated
	public String getRegisterValue(int regID, int format){
        throw new IllegalStateException("Deprecated call");
    }
    
    /**
     * 
     * @return the associated engine interface proxy.
     * @nojni
     */
    public EngineInterface getEngine(){ return mEngine; }
}
