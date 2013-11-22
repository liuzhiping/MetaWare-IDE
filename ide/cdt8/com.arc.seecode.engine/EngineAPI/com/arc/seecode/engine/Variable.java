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

import com.arc.seecode.engine.type.IType;

/**
 * A variable description.
 * <P>
 * Instances of this class are created from the C++-based engine
 * via JNI calls.
 * 
 * @author David Pickens
 */
public class Variable {
    private String mName = "??"; // don't permit name to be null
    private String mActualName;
    private int mRegister = -1;
    private long mAddress = -1; //machine address if applicable
    private boolean mActive = true;
    private StackFrameRef mStackFrame = null; // the stack frame if applicable
    private int mKind;
    private IType mType;
    private EngineInterface mEngine;

    private Value mValue;
    
    private int mModule; // Id of associated module, or 0
    private long mLogicalAddress; // logical address within module if module is not zer0.
    
    /**
     * Deserialize uses this one.
     *
     */
    public Variable(){
        this(null);
    }
    Variable(EngineInterface engine){
        mEngine = engine;
    }

    /**
     * Return "user-friendly" name; I.e., the name as the
     * user would expect to see it.
     * @return the "user-friendly" name for this variable.
     * 
     * @pre $none
     * @post $result != null
     */
    public String getName(){
        return mName;
    }
    
    /**
     * The stackframe object becomes "invalid" when
     * the PC is changed. But the actual stack frame
     * may be active, so we want to find the stackframe
     * object with the same frame address.
     * @return stackframe with same address.
     * @nojni
     */
    private StackFrameRef findStackFrame() throws EngineException {
		if (mStackFrame == null)
			return null;
		if (mStackFrame.isValid()) {
			// Current one is still valid.
			return mStackFrame;
		}

		StackFrameRef sf = mEngine.getTopStackFrame(mStackFrame.getThreadID());
		StackFrameRef top = sf;
//		if (false) { // CR2108: a downwind frame may appear to have same FP,
//						// but it may be due to
//			// being position in prolog.
//			long frameAddr = mStackFrame.getFramePointer();
//			while (sf != null && sf.getFramePointer() != frameAddr) {
//				sf = sf.getCallerFrame();
//			}
//			if (sf != null)
//				return sf;
//		}
		// If we're not stackframe based, then punt and
		// return the top frame.
		return top;
	}
    
    /**
	 * Re-sync the value of this variable with reality. If the variable has gone
	 * out of scope, mark it as invalid.
	 * 
	 * @return true if a change is made.
	 * 
	 */
    public boolean update() throws EngineException{
        StackFrameRef sf = findStackFrame();
        if (sf == null){
            if (isActive() && isStackFrameBased()) {
               setActive(false);
               return true;
            }
            // Globals may become active again
            //if (!isActive()) return true;
        }
        Variable vp;
        try {
            if (sf != null)
                vp = sf.lookupVariableUncached(getActualName());
            else 
                vp = this.mEngine.lookupGlobalVariable(getActualName());
        } catch (EvaluationException e) {
            vp = null;
        }
        // If variable no longer exits, or if the variable associated with the name is
        // different, then we went out of scope.
        // However, for globals (no stack frame), we just update the values.
        if (vp == null || !vp.equals(this) /* && sf != null */){
            if (isActive()){
                setActive(false);
                return true;
            }
            return false;
        }
        setStackFrame(sf);
        Value newValue = vp.getValue();
        if (newValue != null && !newValue.equals(getValue())){
            setValue(newValue);
            setActive(true);
            return true;
        }
        if (!isActive()){
            setActive(true);
            return true;
        }
        return false;
    }
    
    /**
     * Return the type of the variable.
     * 
     * @return the type  or null.
     * 
     * @pre $none
     * @post $none
     */
    public IType getType(){
        return mType;
    }
       
    /**
     * Return whether or not this variable is active. A variable
     * goes inactive when its associated stack frame disappears.
     * @return whether or not this variable is active.
     */
    public boolean isActive(){
        return mActive;
    }
    
    /**
     * A value returned from {@link #getKind()} that denotes
     * a stackframe-based, or register-based variable that is
     * not a parameter.
     */
    public static final int AUTO = 1;
    /**
     * A value returned from {@link #getKind()} that denotes
     * a stackframe-based, or register-based variable that also
     * is a parameter.
     */
    public static final int PARM = 2;
    /**
     * A value returned from {@link #getKind()} that denotes
     * a static variable (whether global or local).
     */
    public static final int STATIC = 3;
    /**
     * A value returned from {@link #getKind()} that denotes
     * a thread-local variable.
     */
    public static final int THREADLOCAL = 4;
    
    /**
     * Return the kind of variable this is:
     * <dl>
     * <dt> {@link #AUTO}
     * <dt> {@link #PARM}
     * <dt> {@link #STATIC}
     * <dt> {@link #THREADLOCAL}
     * </dl>
     * @return a manifest constant to denote the kind of variable.
     */
    public int getKind(){
        return mKind;
    }
    
    boolean isStackFrameBased(){
        return mKind == AUTO || mKind == PARM;
    }
    
    /**
     * Return the stack frame if this variable is associated with
     * a stack frame.
     * @return stack frame reference, or <code>null</code>.
     * @nojni
     */
    public StackFrameRef getStackFrame(){
        return mStackFrame;
    }
    
    /**
     * Set the associated stack frame.
     * <P>
     * NOTE: the engine doesn't do this because it doesn't
     * have access to the StackFrameRef object. The 
     * {@link StackFrameRef#getLocals()} method fills this in.
     * @param sf the associated stackframe.
     * @nojni Prevents JNI call being emitted.
     */
    public void setStackFrame(StackFrameRef sf){
       mStackFrame = sf;
    }
    
 
    
    /**
     * Return the name as the evaluator would need to see it.
     * @return the name as the evaluator would need to see it.
     * 
     * @pre $none
     * @post $result != null
     */
    public String getActualName(){
        return mActualName != null? mActualName: getName();        
    }
    
    /**
     * If the variable is mapped to a machine register, return
     * the register id number.
     * @return ID number of the register that the variable is mapped to,
     * or -1 if not mapped to a register;
     * 
     * @pre $none
     * @post $none
     */
    public int getRegister(){
        return mRegister;        
    }
    
    /**
     * Return the value of this variable, if known.
     * 
     * @return the value of the variable, if known; otherwise <code>null</code>.
     */
    public Value getValue(){
        return mValue;
    }
    
    /**
     * Return the machine address if known. Returns -1 if the
     * address isn't known, or if the variable is register-based.
     * @return the variable's machine address, or -1.
     */
    public long getAddress(){
        return mAddress;     
    }
    
    /**
     * If there is an associated logical address, return the index of the"module" that is associated
     * with the logical address.
     * @return the module associated with the logical address, or 0 if there is no associated module.
     * Method added as of version 5 of interface.
     * @new
     */
    public int getModule(){
        return mModule;
    }
    
    /**
     * Return the associated logical address if known. Otherwise, returns 0.
     * <B>NOTE:</b> a logical address is assumed to exist if {@link #getModule()} return an index > 0.
     * @return the associated logical address if there is an associated module.
     * Method added as of version 5 of interface.
     * @new
     */
    public long getLogicalAddress(){
        return mLogicalAddress;
    }
    
    /**
     * Set the name of this variable.
     * <b>NOTE:</b> this method is provided for the benefit of
     * the engine to record the name.
     * @param n
     */
    public void setName(String n){
        mName = n;
    }
    /**
     * Set value when it is something simple.
     * <P>
     * If the value is simple, then we allow the caller to
     * merely set this field so as not to suffer the overhead
     * of constructing a {@link Value Value} object.
     * <P>
     * <B>NOTE:</B> this method is intended to be called from the
     * engine to make the variable's value correspond to reality. 
     * When called from Java, it does <em>not</em>
     * cause the underlying value to be set.
     */
    public void setValue(String expression){
        mValue = new Value(null);
        mValue.setSimpleValue(expression);
    }
    
    /**
     * Set the value of this variable.
     * <P>
     * <B>NOTE:</B> this method is intended to be called from the
     * engine to make the variable's value correspond to reality. 
     * When called from Java, it does <em>not</em>
     * cause the underlying value to be set.
     * @param value the value of this variable.
     */
    public void setValue(Value value){
        mValue = value;
    }
    
    /**
     * Set the type of this variable.
     * @param type type of this variable.
     */
    public void setType(IType type){
        mType = type;
    }
    
    /**
     * Set the actual name as an expression evaluator would like to 
     * see it.
     * @param name actual name of the variable.
     */
    public void setActualName(String name){
        mActualName = name;
    }
    
    /**
     * Set the machine address of the variable if it is memory-based.
     * @param adr machine address of the variable
     */
    public void setAddress(long adr){
        mAddress = adr;
    }
   
    /** 
     * Set the number of the register to which this variable is mapped
     * if applicable.
     * @param r register number to which this variable is mapped.
     */
    public void setRegister(int r) {
        mRegister = r;
    }
    
    /**
     * Set whether or not this variable is active; that is, its
     * associated stack frame is live.
     * @param v 
     */
    public void setActive(boolean v){
        mActive = v;
    }
    /**
     * Set the kind of variable this is:
     * <dl>
     * <dt> {@link #AUTO}
     * <dt> {@link #PARM}
     * <dt> {@link #STATIC}
     * <dt> {@link #THREADLOCAL}
     * </dl>
     */
    public void setKind(int kind){
        mKind = kind;
    }
    
    private static boolean isTypesCompare(IType type1, IType type2){
    	if (type1 == null) return type2 == null;
    	if (type2 == null) return false;
    	return type1.equals(type2);
    }
    
    /**
     * Determine if two Variable objects are indeed referencing
     * the same variable. This will be true if their actual names
     * match, and they have the same address.
     * @param v
     * @return true if variable matches another.
     * @nojni
     */
    public boolean equals(Variable v){
        return v != null && v.getActualName().equals(getActualName()) && v.getKind() == getKind()
        &&  v.getAddress() == getAddress() &&
        v.isActive() == isActive() && 
        // Check type: we may have gone out of scope but have a variable with same name but
        // different type. Causes havic if one is an array and the other isn't (CR2267)
        isTypesCompare(this.getType(),v.getType());
    }  
    
    /**
     * 
     * @param module
     * @param logAddress
     * @param phyAddress
     * Method added as of version 5 of interface
     * @new
     */
    public void setLogicalAddress(int module, long logAddress, long phyAddress){
        if (module <= 0) throw new IllegalArgumentException("Invalid module index");
        mModule = module;
        mLogicalAddress = logAddress;
        mAddress = phyAddress;
    }
    
    /**
     * @nojni
     */
    @Override
    public boolean equals(Object obj){
        if (obj instanceof Variable)
            return equals((Variable)obj);
        return false;
    }
    
    /**
     * @nojni
     */
    @Override
    public int hashCode(){
        return getActualName().hashCode() ^ (int)getAddress();
    }
}
