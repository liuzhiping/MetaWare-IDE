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
package com.arc.cdt.debug.seecode.internal.core.cdi.value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIFormattable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;

import com.arc.cdt.debug.seecode.internal.core.cdi.IRefresh;
import com.arc.seecode.engine.StackFrameRef;
import com.arc.seecode.engine.Value;
import com.arc.seecode.engine.type.IType;

/**
 * @author David Pickens
 */
class CDIValue implements ICDIValue, IValueChangeable, IRefresh, ICDIFormattable {

    private ICDITarget mTarget;

    private Value mValue;
    
    private ICDIVariable mVar;

    private Map<Integer,ICDIVariable> mElements = null;

    private IType mType;

    private StackFrameRef mStackFrame;

    private int mDefaultFormat = 0;
    /**
     * We need the SeeCode stack frame in case we're
     * changing an element.
     * @param var
     * @param type
     * @param value
     * @param target
     * @param sf
     */
    CDIValue(ICDIVariable var, IType type, Value value, ICDITarget target,
            StackFrameRef sf) {
        mTarget = target;
        mValue = value;
        mVar = var; // associated variable if any.
        mType = type;
        mStackFrame = sf;
    }
    
    CDIValue(Value value, ICDITarget target){
        this(null,value != null?value.getType():null,value,target,null);
    }
    
    CDIValue(ICDIVariable var, IType type, Value value, ICDITarget target){
        this(var,type,value,target,null);
    }
    
    Value getSeeCodeValue(){
        return mValue;
    }
    
    IType getSeeCodeType(){
        return mType;
    }
    
    @Override
    public ICDIType getType(){
        return (ICDIType)mType;
    }
    
    ICDIVariable getAssociatedVariable(){
        return mVar;
    }
    
    StackFrameRef getStackFrame(){
        return mStackFrame;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getTypeName()
     */
    @Override
    public String getTypeName() throws CDIException {
        IType t = mValue.getType();
        if (t != null) return t.toString();
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getValueString()
     */
    @Override
    public String getValueString() throws CDIException {
        return mValue.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getChildrenNumber()
     */
    @Override
    public int getChildrenNumber() throws CDIException {
        return mValue.getElementCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#hasChildren()
     */
    @Override
    public boolean hasChildren() throws CDIException {
        return getChildrenNumber() > 0;
    }

    private static ICDIVariable[] NO_VARIABLES = new ICDIVariable[0];
    
    public ICDIVariable[] getVariables(int lo, int length) throws CDIException {
        int hi = lo + length-1;
        if (lo < 0 || hi >= mValue.getElementCount() || hi < lo) {
        	if (length == 0) return NO_VARIABLES;
            throw new CDIException("bad index range: " + lo + ".. " + hi);
        }
        if (mElements == null)
            mElements = new HashMap<Integer,ICDIVariable>();
        ICDIVariable results[] = new ICDIVariable[length];
        for (int i = lo; i <= hi; i++){
            Integer key = new Integer(i);
            ICDIVariable v = mElements.get(key);
            if (v == null) {
                v = createChild(i);
                mElements.put(key,v);
            }
            results[i-lo] = v;
        }       
        return results;
    }
    
    /**
     * @param i the element to return.
     * @return element variable
     */
    protected ICDIVariable createChild(int i) {
        return new ValueField(this,i);
    }

    @Override
    public ICDIVariable[] getVariables() throws CDIException{
        return getVariables(0,getChildrenNumber());
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getTarget()
     */
    @Override
    public ICDITarget getTarget() {
        return mTarget;
    }
    
    public boolean equals(CDIValue v){
        return v.mValue.equals(mValue);
    }
    
    @Override
    public boolean equals(Object obj){
        if (obj instanceof CDIValue)
            return equals((CDIValue)obj);
        return false;
    }
    
    @Override
    public int hashCode(){
    	return mValue != null?mValue.hashCode():0;
    }

    /* (non-Javadoc)
     * @see com.arc.cdt.debug.seecode.core.cdi.value.IValueChangeable#setValue(com.arc.seecode.engine.Value)
     */
    @Override
    public void setValue(Value v) throws CDIException{
        mValue = v;     
    }

    /**
     * @throws CDIException
     */
    @Override
    public boolean refresh(List<ICDIObject> listToBeUpdated) throws CDIException {
        boolean change = false;
        if (mElements != null){
            for (ICDIVariable v: mElements.values()){
                if (v instanceof IRefresh){
                    if (((IRefresh)v).refresh(listToBeUpdated))
                        change = true;
                }               
            }
        }
        return change;
    }
    
    /**
     * Set default format.
     * @param format one of the ICDIFormat constants.
     */
    public void setDefaultFormat(int format){
        mDefaultFormat = format;
    }

    @Override
    public int getNaturalFormat () throws CDIException {
        return mDefaultFormat ;
    }
}
