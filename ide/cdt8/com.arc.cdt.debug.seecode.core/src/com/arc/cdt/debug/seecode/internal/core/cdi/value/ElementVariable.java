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

import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.internal.core.cdi.ArrayPartition;
import com.arc.cdt.debug.seecode.internal.core.cdi.EventManager;
import com.arc.cdt.debug.seecode.internal.core.cdi.IMemoryBlockUpdater;
import com.arc.cdt.debug.seecode.internal.core.cdi.ISeeCodeVariable;
import com.arc.cdt.debug.seecode.internal.core.cdi.ISeeCodeVariableDescriptor;
import com.arc.cdt.debug.seecode.internal.core.cdi.event.ChangedEvent;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EvaluationException;
import com.arc.seecode.engine.Value;
import com.arc.seecode.engine.Variable;
import com.arc.seecode.engine.type.IType;

/**
 * We must make this implement ICDILocalVariable because CDT expects the elements
 * of a local variable to implement this interface.
 * @author David Pickens
 */
abstract class ElementVariable implements ICDILocalVariable, ISeeCodeVariable {

    private CDIValue mParent;

    private CDIValue mThisValue = null;

    private Value mSeeCodeValue = null;
    
    private String mCastType = null;

    ElementVariable(CDIValue parent) {
        mParent = parent;
    }

    @Override
    public ICDIValue getValue() throws CDIException {
        // Evaluate lazily
        if (mThisValue == null) {
            Value v = getSeeCodeValue();
            mThisValue = (CDIValue) ValueFactory.makeValue(this,v, getTarget(),null);
        }
        return mThisValue;
    }

    protected CDIValue getParent() {
        return mParent;
    }

    protected abstract int getChildIndex();
    
    private boolean mOutOfBoundsDiagnosed = false;

    /**
     * @return the seecode engine value that backs this object.
     */
    protected synchronized Value getSeeCodeValue() throws CDIException {
        if (mSeeCodeValue == null) {
            try {
                /*if (mCastType == null) */{
                	//NOTE: mParent.getSeeCodeValue() is not being updated when associated
                	// variable (if any) changes. Need to rethink this thru. Grab the
                	// value from the associated variable if there is one.
                	ICDIVariable var  = mParent.getAssociatedVariable();
                	Value value = null;
                	if (var != null) {
                		// May be array whose element values change.
                		// The logic here needs to be thought thru.
                		ICDIValue cdiValue = var.getValue();
                		if (cdiValue instanceof CDIValue){
                			value = ((CDIValue)cdiValue).getSeeCodeValue();
                		}
                	}
                	else value = mParent.getSeeCodeValue();
                	if (value != null)
                        mSeeCodeValue = value.getElement(getChildIndex());
                }
                /*
                else {
                    //???
                }
                */
            } catch (EngineException e) {
                SeeCodePlugin.log(e);
                throw new CDIException("can't retrieve value: "
                        + e.getMessage());

            } catch (IndexOutOfBoundsException e) {

                //Shouldn't get here, but it has happened.
                // Only print log once in case this problem is repeated
                // dozens of times. (Not yet able to reproduce
                // deterministically)
                if (!mOutOfBoundsDiagnosed){
                    SeeCodePlugin.log(e);
                    mOutOfBoundsDiagnosed = true;
                }
                mSeeCodeValue = new Value(null);
                mSeeCodeValue.setSimpleValue("<Bad index:" + getChildIndex() + ">");
            }
            if (mThisValue != null) {
                mThisValue.setValue(mSeeCodeValue);
            }
        }

        return mSeeCodeValue;
    }


    @Override
    public synchronized void setValue(String expression) throws CDIException {
        try {
            mParent.getSeeCodeValue().setElement(getChildIndex(), expression,
                    mParent.getStackFrame());
            mSeeCodeValue = null;
            getSeeCodeValue(); // reread to set everything.
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        } catch (EvaluationException e) {
            throw new CDIException(e.getMessage());
        }
        EventManager emgr = (EventManager) getTarget().getSession()
                .getEventManager();
        emgr.enqueueEvent(new ChangedEvent(this));
        //In case we have memory display that it
        // showing the variable's contents, update.
        if (getTarget() instanceof IMemoryBlockUpdater){
            ((IMemoryBlockUpdater)getTarget()).updateMemoryBlocks();
        }
    }

    @Override
    public void setValue(ICDIValue value) throws CDIException {
        setValue(value.getValueString());
    }

    public void setFormat(int format) {
        // TODO Auto-generated method stub
    }

    @Override
    public ICDIType getType() throws CDIException {
        // The type factory guarantees that all types implement
        // the ICDIType interface.
        return (ICDIType) getSeeCodeValue().getType();
    }

    @Override
    public String getTypeName() throws CDIException {
        if (mCastType != null) return mCastType;
        IType t = getSeeCodeValue().getType();
        if (t != null) return t.toString();
        return null;
    }

    @Override
    public int sizeof() throws CDIException {
        IType t = getSeeCodeValue().getType();
        if (t != null) return t.getSize();
        return 0;
    }

    @Override
    public ICDITarget getTarget() {
        return mParent.getTarget();
    }

    /**
     * We want to refresh this aggregate element and add it to the list if it
     * indeed has changed. The complicated thing is that we evaluate aggregate
     * elements lazily. So we must actually fetch the value to see if it
     * changes.
     */
    @Override
    public synchronized boolean refresh(List<ICDIObject> changedElements) throws CDIException {
        if (mThisValue != null) {
            Value old = mSeeCodeValue;
            mSeeCodeValue = null; // force regeneration
            Value newOne = getSeeCodeValue();
            //NOTE: if associated variable is out of scope, then the value will
            // come back
            // null. Out-of-scope variables are suppose to be filtered out, but
            // we may have
            // a stale display someplace trying to update them.
            boolean changed = false;
            if (newOne != null) {
                if (!newOne.equals(old)) {
                    changedElements.add(this);
                    changed = true;
                }
            } else {
                changed = true; // shouldn't get here
                newOne = mSeeCodeValue = old;
                newOne.setSimpleValue("<not available>");
            }
            mThisValue.setValue(newOne);
            if (mThisValue.refresh(changedElements)) changed = true;
            return changed;
        }
        mSeeCodeValue = null; // If this value is to be retrieved, force recomputation
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#dispose()
     */
    @Override
    public void dispose() throws CDIException {
       

    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#isEditable()
     */
    @Override
    public boolean isEditable() throws CDIException {
        return true;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#createVariable()
     */
    public ICDIVariable createVariable() {
        return this;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#getVariableDescriptorAsArray(int, int)
     */
    @Override
    public ICDIVariableDescriptor getVariableDescriptorAsArray(int start,
            int length) throws CDIException {
        return new ArrayPartition(this,start,length);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#getVariableDescriptorAsType(java.lang.String)
     */
    @Override
    public ICDIVariableDescriptor getVariableDescriptorAsType(String type)
            throws CDIException {
        ElementVariable clone = makeClone();
        clone.mCastType = type;
        return clone;
    }
    
    /**
     * Make a clone of this object.
     * @return a newly-created clone of this object.
     */
    abstract protected ElementVariable makeClone();

   
    @Override
    public ICDIStackFrame getStackFrame () throws CDIException {
        ICDIVariable parent = getParent().getAssociatedVariable();
        if (parent instanceof ISeeCodeVariable){
            return ((ISeeCodeVariable)parent).getStackFrame();
        }
        return null;
    }

  
    @Override
    public ISeeCodeVariable allocateVariable () {
        return this;
    }

    @Override
    public String getCastType () {
        return mCastType;
    }

    
    @Override
    public Variable getSeeCodeCookie () {
        return null;
    }

    @Override
    public boolean isOutOfScope () {
        CDIValue v = getParent();
        if (v != null && v.getAssociatedVariable() instanceof ISeeCodeVariable){
            return ((ISeeCodeVariable)v.getAssociatedVariable()).isOutOfScope();
        }
        return false;
    }

 
    @Override
    public void setSeeCodeCookie (Variable v) throws CDIException {
        if (v != null)
            throw new IllegalStateException("setSeeCodeCookie called on element"); // Don't thinks this will ever be called.
        
    }
    
    @Override
    public ISeeCodeVariableDescriptor getDescriptor(){
        return this;
    }
    
    @Override
    public boolean isArrayPartition(){
    	return false;
    }
}
