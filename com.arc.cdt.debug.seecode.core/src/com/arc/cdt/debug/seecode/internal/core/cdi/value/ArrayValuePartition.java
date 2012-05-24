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

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;


/**
 * @author David Pickens
 */
public class ArrayValuePartition implements ICDIValue {
    private ICDIValue mValue;
    private int mStart;
    private int mLength;
    /**
     * 
     */
    public ArrayValuePartition(ICDIValue value, int start, int length) {
        mValue = value;
        mStart = start;
        mLength = length;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getTypeName()
     */
    @Override
    public String getTypeName() throws CDIException {
       return mValue.getTypeName();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getValueString()
     */
    @Override
    public String getValueString() throws CDIException {
        return "...";
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getChildrenNumber()
     */
    @Override
    public int getChildrenNumber() throws CDIException {
        return mLength;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#hasChildren()
     */
    @Override
    public boolean hasChildren() throws CDIException {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getVariables()
     */
    @Override
    public ICDIVariable[] getVariables() throws CDIException {
        if (mValue instanceof CDIValue){
            return ((CDIValue)mValue).getVariables(mStart,mLength);
        }
        ICDIVariable v[] = mValue.getVariables();
        ICDIVariable result[] = new ICDIVariable[mLength];
        System.arraycopy(v,mStart,result,0,mLength);
        return result;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getTarget()
     */
    @Override
    public ICDITarget getTarget() {
        return mValue.getTarget();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getType()
     */
    @Override
    public ICDIType getType() throws CDIException {
        return mValue.getType();
    }

}
