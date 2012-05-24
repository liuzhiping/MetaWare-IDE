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

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIAggregateType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIPointerType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;

import com.arc.seecode.engine.Variable;
import com.arc.seecode.engine.type.IType;

/**
 * @author David Pickens
 */
class CDIVariableDescriptor implements ISeeCodeVariableDescriptor{

    private Target mTarget;

    private Variable mSeeCodeCookie;

    private StackFrame mStackFrame;

    private VariableManager mVariableManager;
    
    private String mCastType = null;

    CDIVariableDescriptor(Target target, Variable var, StackFrame sf, VariableManager vmgr) {
        mTarget = target;
        mSeeCodeCookie = var;
        mStackFrame = sf;
        mVariableManager = vmgr;
    }

    @Override
    public void setSeeCodeCookie(Variable v) throws CDIException {
        mSeeCodeCookie = v;
    }

    @Override
    public Variable getSeeCodeCookie() {
        return mSeeCodeCookie;
    }
    
    /**
     * Indicate that this is a cast type.
     * @param type the cast type name.
     */
    protected void setCastType(String type){
    	mCastType = type;
    }
    
    /**
     * Return the type that this variable is being cast to, or null if it
     * isn't a cast (which is the normal case).
     * @return the type that this variable is being cast to, or null if it
     * isn't a cast (which is the normal case).
     */
    @Override
    public String getCastType(){
    	return mCastType;
    }

    @Override
    public boolean isOutOfScope() {
        return !mSeeCodeCookie.isActive();
    }
    
    VariableManager getVariableManager(){
        return mVariableManager;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#getName()
     */
    @Override
    public String getName() {
        return mSeeCodeCookie.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#getStackFrame()
     */
    @Override
    public ICDIStackFrame getStackFrame() {
        return mStackFrame;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#getType()
     */
    @Override
    public ICDIType getType() throws CDIException {
        //Our type factory that the engine uses was supplied by
        // us, and always produces types that implement ICDIType.
        return (ICDIType) mSeeCodeCookie.getType();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#getTypeName()
     */
    @Override
    public String getTypeName() throws CDIException {
    	if (mCastType != null) return mCastType;
        IType t = mSeeCodeCookie.getType();
        if (t != null) return t.toString();
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#sizeof()
     */
    @Override
    public int sizeof() throws CDIException {
        IType t = mSeeCodeCookie.getType();
        if (t != null) return t.getSize();
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#isEditable()
     */
    public boolean isEditable() throws CDIException {
        // can only change scalar variables.
        // If aggregate, we can only change elements.
        // Can't change register variables unless it
        // topmost frame.
        return !(getType() instanceof ICDIAggregateType)
                && (mSeeCodeCookie.getRegister() < 0 || mSeeCodeCookie.getStackFrame() == null || mSeeCodeCookie
                        .getStackFrame().isTopMostFrame());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#getQualifiedName()
     */
    @Override
    public String getQualifiedName() throws CDIException {
    	if (mCastType == null)
            return mSeeCodeCookie.getActualName();
    	String cast = mCastType;
    	String deref = "";
    	if (mCastType.endsWith("]") && getType() instanceof ICDIPointerType) {
			int i = mCastType.indexOf("[");
			// Convert cast to "Foo[]" to "Foo(*)[]". That's what
			// SeeCode expects.
			if (i > 0) {
				cast = mCastType.substring(0, i) + "(*)" + mCastType.substring(i);
				deref = "*";
			}
		} 
    	return deref + "(" + cast + ")(" + mSeeCodeCookie.getActualName() + ")";
    }
    
    private static boolean compareEqual(Object a, Object b){
    	if (a == null) return b == null;
    	return a.equals(b);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#equals(org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor)
     */
    @Override
    public boolean equals(ICDIVariableDescriptor varObject) {
        if (!(varObject instanceof ISeeCodeVariableDescriptor)) return false;
        return getSeeCodeCookie().equals(((ISeeCodeVariableDescriptor) varObject).getSeeCodeCookie()) &&
              compareEqual(getCastType(),((ISeeCodeVariableDescriptor)varObject).getCastType()) &&
              // SeeCode returns global variables when requesting "locals". Don't confuse them.
              (varObject instanceof ICDIGlobalVariableDescriptor) == (this instanceof ICDIGlobalVariableDescriptor);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CDIVariableDescriptor) { return equals((ICDIVariableDescriptor) obj); }
        return false;
    }

    @Override
    public int hashCode() {
        return mSeeCodeCookie.hashCode();
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


    /**
     * Make a clone of this descriptor so that we may apply a
     * cast type.
     * @return a clone of this descriptor.
     */
    protected  CDIVariableDescriptor makeClone() {
    	return new CDIVariableDescriptor(mTarget,mSeeCodeCookie,mStackFrame,mVariableManager);
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#getVariableDescriptorAsArray(int, int)
     */
    @Override
    public ICDIVariableDescriptor getVariableDescriptorAsArray(int start, int length) throws CDIException {
          return new ArrayPartitionDescriptor(this,start,length,mVariableManager);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#getVariableDescriptorAsType(java.lang.String)
     */
    @Override
    public ICDIVariableDescriptor getVariableDescriptorAsType(String type) throws CDIException {
        CDIVariableDescriptor clone = makeClone();
        clone.setCastType(type);
        return clone;
    }

//    /* (non-Javadoc)
//     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#createVariable()
//     */
//    public ICDIVariable createVariable() throws CDIException {
//        mVariable = mVariableManager.createVariable(this);
//        return mVariable;
//    }
    
    /**
     * Called by {@link VariableManager#createVariable}.
     * @return the newly allocated variable.
     */
    @Override
    public ISeeCodeVariable allocateVariable(){
        return new CDIVariable(this,mVariableManager);
    }

	@Override
    public boolean isArrayPartition() {
		return false;
	}
    
}
