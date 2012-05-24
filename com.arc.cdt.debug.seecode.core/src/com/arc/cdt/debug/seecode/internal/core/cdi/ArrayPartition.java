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

import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;

import com.arc.cdt.debug.seecode.internal.core.cdi.value.ArrayValuePartition;


/**
 * An array partition within an array variable.
 * @author David Pickens
 */
public class ArrayPartition implements ICDIVariable, IRefresh {
    private ICDIVariable mVar;
    private int mStart;
    private int mLength;
    private ICDIValue mValue;
    /**
     * 
     */
    public ArrayPartition(ICDIVariable var, int start, int length) throws CDIException{
        mVar = var;
        ICDIValue value = var.getValue();
        int cnt = value==null?0:value.getChildrenNumber();
        if (cnt < start + length)
                throw new CDIException("Not a valid array slice");
        mStart = start;
        mLength = length;
        mValue = new ArrayValuePartition(var.getValue(),start,length);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getValue()
     */
    @Override
    public ICDIValue getValue() throws CDIException {
        return mValue;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#setValue(java.lang.String)
     */
    @Override
    public void setValue(String expression) throws CDIException {
       throw new CDIException("Can't assign to array as a whole");

    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#setValue(org.eclipse.cdt.debug.core.cdi.model.ICDIValue)
     */
    @Override
    public void setValue(ICDIValue value) throws CDIException {
        throw new CDIException("Can't assign to array as a whole");
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#setFormat(int)
     */
    public void setFormat(int format) {
        //TODO

    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#getName()
     */
    @Override
    public String getName() {
        return mVar.getName() + "[" + mStart + ".." +
         (mStart+mLength) + "]";
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#getType()
     */
    @Override
    public ICDIType getType() throws CDIException {
        return mVar.getType();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#getTypeName()
     */
    @Override
    public String getTypeName() throws CDIException {
        return mVar.getTypeName();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#sizeof()
     */
    @Override
    public int sizeof() throws CDIException {
        return mVar.sizeof(); // todo
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#isEditable()
     */
    @Override
    public boolean isEditable() throws CDIException {
        return mVar.isEditable();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#getQualifiedName()
     */
    @Override
    public String getQualifiedName() throws CDIException {
        return mVar.getQualifiedName() + "[" +
        mStart + ".." + (mStart + mLength) + "]";
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#equals(org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject)
     */
    @Override
    public boolean equals(ICDIVariableDescriptor varObject) {
        if (varObject == this) return true;
        if (!(varObject instanceof ArrayPartition))
            return false;
        ArrayPartition ap = (ArrayPartition)varObject;
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
     * @see com.arc.cdt.debug.seecode.core.cdi.IRefresh#refresh()
     */
    @Override
    public boolean refresh(List<ICDIObject> listToUpdate) throws CDIException {
        if (mVar instanceof IRefresh){
            return ((IRefresh)mVar).refresh(listToUpdate);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#dispose()
     */
    @Override
    public void dispose() throws CDIException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#getVariableDescriptorAsArray(int, int)
     */
    @Override
    public ICDIVariableDescriptor getVariableDescriptorAsArray(int start, int length) throws CDIException {
        return new ArrayPartition(this,start,length);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#getVariableDescriptorAsType(java.lang.String)
     */
    @Override
    public ICDIVariableDescriptor getVariableDescriptorAsType(String type) throws CDIException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#createVariable()
     */
    public ICDIVariable createVariable() {
        return this;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#equals(org.eclipse.cdt.debug.core.cdi.model.ICDIVariable)
     */
    @Override
    public boolean equals(ICDIVariable variable) {
        return equals((ICDIVariableDescriptor)variable);
    }
}
