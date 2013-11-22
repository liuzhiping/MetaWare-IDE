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
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIPointerType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;

import com.arc.seecode.engine.Variable;
import com.arc.seecode.engine.type.IType;
import com.arc.seecode.engine.type.ITypeFactory;

/**
 * A subset of a variable array. Perhaps we need to reorganize this?
 * @author David Pickens
 */
class ArrayPartitionDescriptor implements ISeeCodeVariableDescriptor{
    protected ISeeCodeVariableDescriptor mVar;
    protected int mStart;
    protected int mLength;
    private String mCastType = null;
	private Variable mCookie = null;
	protected VariableManager mVarMgr;
	private ICDIType mType;
    /**
     * @throws CDIException 
     * 
     */
    public ArrayPartitionDescriptor(ISeeCodeVariableDescriptor var, int start, int length,
    		VariableManager vmgr) throws CDIException{
        mVar = var;
        mStart = start;
        mLength = length;
        mVarMgr = vmgr;
        ICDIType type = var.getType();
        if (type instanceof ICDIPointerType){
        	ICDIType base = ((ICDIPointerType)type).getComponentType();
        	ITypeFactory typeFactory = ((Target)getTarget()).getEngineInterface().getTypeFactory();
        	IType array = typeFactory.createArray(null,(IType)base,length);
        	mType = (ICDIType)typeFactory.createPointer(null,array,4/*???*/);
        }
        else if (type instanceof ICDIArrayType){
        	ICDIType base = ((ICDIArrayType)type).getComponentType();
        	ITypeFactory typeFactory = ((Target)getTarget()).getEngineInterface().getTypeFactory();
        	mType = (ICDIType) typeFactory.createArray(null,(IType)base,length);
        }
        else mType = type; // Shouldn't get here
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#getName()
     */
    @Override
    public String getName() {
        return mVar.getName(); /* + "[" + mStart + ".." +
         (mStart+mLength) + "]"; */
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#getType()
     */
    @Override
    public ICDIType getType() throws CDIException {
        return mType;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#getTypeName()
     */
    @Override
    public String getTypeName() throws CDIException {
    	if (mCastType != null) return mCastType;
    	return mType.getTypeName();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#sizeof()
     */
    @Override
    public int sizeof() throws CDIException {
        return mVar.sizeof(); // todo
    }
    
    private boolean isSimpleName(String name){
    	return name.indexOf('.') < 0 && name.indexOf('[') < 0 && name.indexOf('*') < 0;
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#getQualifiedName()
     */
    @Override
    public String getQualifiedName() throws CDIException {
    	ICDIType type = mVar.getType();
    	if (type instanceof ICDIPointerType){
    		ICDIPointerType ptype = ((ICDIPointerType)type);
    		String baseTypeName = ptype.getComponentType().getTypeName();
    		if (!isSimpleName(baseTypeName))
    			baseTypeName = "(" + baseTypeName + ")";
    		String e =  "*(" + baseTypeName + "(*)[" + mLength + "])(";
    		if (mStart != 0){
    			e += "(" + mVar.getQualifiedName() + ")+" + mStart;
    		}
    		else e += mVar.getQualifiedName();
    		return e+")";
    	}
    	else if (type instanceof ICDIArrayType){
    		// Can we really cast an array??? Perhaps not.
    		ICDIArrayType ptype = ((ICDIArrayType)type);
    		String baseTypeName = ptype.getComponentType().getTypeName();
    		if (!isSimpleName(baseTypeName))
    			baseTypeName = "(" + baseTypeName + ")";
    		String e =  "*(" + baseTypeName + "(*)[" + mLength + "])(";
    		if (mStart != 0){
    			e += "(" + mVar.getQualifiedName() + ")+" + mStart;
    		}
    		else e += mVar.getQualifiedName();
    		return e+")";
    	}
        return mVar.getQualifiedName(); // we punt
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#equals(org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject)
     */
    @Override
    public boolean equals(ICDIVariableDescriptor varObject) {
        if (varObject == this) return true;
        if (!(varObject instanceof ArrayPartitionDescriptor))
            return false;
        ArrayPartitionDescriptor ap = (ArrayPartitionDescriptor)varObject;
        return ap.mVar.equals(mVar) && ap.mStart == mStart &&
            ap.mLength == mLength;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getTarget()
     */
    @Override
    public ICDITarget getTarget() {
        return mVar.getTarget();
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#getVariableDescriptorAsArray(int, int)
     */
    @Override
    public ICDIVariableDescriptor getVariableDescriptorAsArray(int start, int length) throws CDIException {
        return new ArrayPartitionDescriptor(this,start,length,mVarMgr);
    }

  
    @Override
    public ICDIVariableDescriptor getVariableDescriptorAsType(String type) throws CDIException {
		ArrayPartitionDescriptor clone = makeClone();
		clone.setCastType(type);
		return clone;
	}
    
    private void setCastType(String type) { mCastType = type; }
    
	protected ArrayPartitionDescriptor makeClone() throws CDIException{
    	return new ArrayPartitionDescriptor(mVar,mStart,mLength,mVarMgr);
    }
    
    int getStart(){ return mStart; }
    int getLength() { return mLength; }


    @Override
    public ISeeCodeVariable allocateVariable(){
        return new CDIVariable(this,mVarMgr);
    }

	@Override
    public String getCastType() {
		return mCastType;
	}


	@Override
    public Variable getSeeCodeCookie() {
		if (mCookie  != null) return mCookie;
		return mVar.getSeeCodeCookie();
	}


	@Override
    public ICDIStackFrame getStackFrame() throws CDIException {
		return mVar.getStackFrame();
	}


	@Override
    public boolean isOutOfScope() {
		return mVar.isOutOfScope();
	}


	@Override
    public void setSeeCodeCookie(Variable v) throws CDIException {
		mCookie = v;		
	}


	@Override
    public boolean isArrayPartition() {
		return true;
	}
}
