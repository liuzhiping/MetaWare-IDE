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
 * A description of a value that is associated with a variable.
 * <P>
 * NOTE: instances of this class are created by JNI calls from the
 * C++ engine. Any changes must be reflected in the C++ code that
 * accesses it.
 * 
 * @author David Pickens
 */
public class Value {
    static final int MAX_VALUE_LENGTH = 500;
    public final int NO_ADDRESS = 0xDEADBEEF;
    private String mSimpleValue; // simple value
    private String mFieldName; // field name if applicable.
    // A cookie that is set by the engine to the "ExprObj" that
    // backs this value. It permits us to do lazy evaluation
    // of elements.
    private int mCookie = 0;
    private IAggregateAccessor mEngine;
    private int mElementCount = 0;
    private IType mType = null;
    private long mAddress = NO_ADDRESS;
    
    public Value(IAggregateAccessor engine){
        mEngine = engine;
    }
    
    public void setType(IType type){
        mType = type;
    }
    
    /**
     * Serialization needs this method.
     * @return the "cookie" from which aggregate elements
     * are lazily extracted.
     */
    public int getCookie(){
        return mCookie;
    }
    
    public IType getType(){
        return mType;
    }
    
    public boolean hasAddress(){
        return mAddress != NO_ADDRESS;
    }
    
    /**
     * If the content of this value is somehow tied to an address,
     * return that address. The GUI uses this to determine if two
     * element references from one snapshot to the next are
     * actually referencing the same element.
     * @return the address.
     */
    public long getAddress(){
        return mAddress;
    }
    
    /**
     * Called from the engine to set the address if there is one.
     * @param addr
     */
    public void setAddress(long addr){
        mAddress = addr;
    }
    /**
     * Set the value if it is a simple scalar expression.
     * @param expression scalar expression (e.g., "100").
     */
    public void setSimpleValue(String expression) {
        mSimpleValue = expression;
    }
    
    /**
     * Number of elements if this is an aggregate; otherwise,
     * returns 0.
     * @return the number of elements if this is an aggregate;
     * otherwise 0.
     */
    public int getElementCount() {
        return mElementCount;
    }
    
    /**
     * Get an element value, lazily, by calling back through the
     * engine. See {@link EngineInterface#getValueElement(int,int)}.
     * @param i the element number.
     * @return the element value.
     *      * @throws IndexOutOfBoundsException if index out of bounds.
     */
    public Value getElement(int i) throws EngineException{
        if (i >= 0 && i < mElementCount){           
            assert mCookie != 0;
            return mEngine.getValueElement(mCookie,i);
        }
        else
            throw new IndexOutOfBoundsException("index out of bounds: " + i);
    }
    
    /**
     * Set the value of a scalar element.
     * @param index the index of the lement.
     * @param value the new value.
     * @param sf the stackframe in which to evaluate.
     * @throws EngineException
     * @throws EvaluationException
     * @throws IndexOutOfBoundsException if index out of bounds.
     * @nojni
     */
    public void setElement(int index, String value, StackFrameRef sf) throws EngineException, EvaluationException {
        if (index >= 0 && index < mElementCount){
            assert mCookie != 0;
            mEngine.setValueElement(mCookie,index,value,sf!=null?sf.getFrameID():0);
        }
        else throw new IndexOutOfBoundsException("bad index: " + index);
    }
    
    /**
     * Set by the engine so that it can be used
     * in a callback to get elements.
     * @param cookie engine object from which elements
     * can be accessed.
     */
    public void setElements(int cookie, int elementCount){
        mCookie = cookie;
        mElementCount = elementCount;
    }
    
    /**
     * Set the associated field name if there is one.
     * @param name associated field name.
     */
    public void setFieldName(String name){
        mFieldName = name;
    }
    
    public String getFieldName(){
    	return mFieldName;
    }
    
    /**
     * Return the value as a string.
     * @return the value as a string.
     * @pre mSimpleValue != null || mAggregate != null &&
     *     mAggregate.$forall(Value v, v != null)
     * @post $result != null
     * @nojni
     */
    public String getValue(){
        return mSimpleValue;
    }
    
    public boolean isAggregate(){
        return mElementCount > 0;
    }
   
    /**
     * @nojni
     */
    @Override
    public String toString(){
        StringBuffer buf  = new StringBuffer(100);
        stringize(buf);
        return buf.toString();
    }
    
    
    private void stringize(StringBuffer buf) {

        if (mSimpleValue != null && mSimpleValue.length()>0){
            buf.append(mSimpleValue);
        }
        else
        if (mElementCount != 0){
            buf.append('{');
            for (int i = 0; i < mElementCount; i++){
                if (buf.length() >= MAX_VALUE_LENGTH){
                    buf.append("...");
                    break;
                }
                Value v;
                try {
                    v = getElement(i);
                    if (v.mFieldName != null){
                        buf.append(v.mFieldName);
                        buf.append(':');
                    }
                    v.stringize(buf);  
                } catch (EngineException e) {
                    buf.append("???");
                    // Engine failure.
                }
              
            }
            buf.append('}');
        }
    }
    
    /**
     * @nojni
     */
    @Override
    protected void finalize(){
        if (mCookie != 0 && mEngine instanceof IEnqueue) {
            final int cookie = mCookie;
            mCookie = 0;
            mElementCount = 0;
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        // Free the engine's "cookie". This is suppose to be
                        // the only reference.
                        mEngine.freeValueCookie(cookie);
                    } catch (EngineException e) {
                        //Assume a  subsequent exception will occur outside
                        // the garbage-collection thread.
                    }                  
                }
            };
            ((IEnqueue)mEngine).enqueue(run,null);
        }
    }
    
    /**
     * @nojni
     */
    @Override
    public boolean equals(Object v){
        if (!(v instanceof Value)) return false;
        if (v == this) return true;
        Value value = (Value)v;
        if (value.getAddress() != getAddress())
            return false;
        if (getElementCount() != value.getElementCount())
            return false;
        String s = getValue();
        if (s != null && !s.equals(value.getValue()))
            return false;
        // If the values are based on the same address,
        // they will be the same, even though the contents
        // may have changed. How do we detect this
        // since we lazily evaluate elements?
        if (value.getAddress() != NO_ADDRESS)
            return true;
        return false;
    }
    
    /**
     * @nojni
     * 
     */
    @Override
    public int hashCode() {
    	String s = getValue();
    	if (s != null) return s.hashCode() ^ 0xDEADBEEF;
    	return (int)mAddress ^ (mElementCount<<10);
    }


}
