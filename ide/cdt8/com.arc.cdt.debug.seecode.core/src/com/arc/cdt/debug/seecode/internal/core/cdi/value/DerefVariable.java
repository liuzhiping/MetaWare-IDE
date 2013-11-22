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
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;

import com.arc.seecode.engine.type.IType;


/**
 * A contrived variable to denote a pointer dereference.
 * @author David Pickens
 */
class DerefVariable extends ElementVariable  {

    private IType mType;
    DerefVariable(CDIValue pointer, IType type){
        super(pointer);
        mType = type;
    }
 
    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#getName()
     */
    @Override
    public String getName() {
        ICDIVariable v = getParent().getAssociatedVariable();
        if (v != null){
            return "*" + v.getName();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#getStackFrame()
     */
    @Override
    public ICDIStackFrame getStackFrame() throws CDIException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#getType()
     */
    @Override
    public ICDIType getType() throws CDIException {
       return (ICDIType)mType;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#getTypeName()
     */
    @Override
    public String getTypeName() throws CDIException {
        return mType!=null?mType.toString():"pointer";
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#sizeof()
     */
    @Override
    public int sizeof() throws CDIException {
        return mType != null? mType.getSize():4;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#isEditable()
     */
    @Override
    public boolean isEditable() throws CDIException {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#getQualifiedName()
     */
    @Override
    public String getQualifiedName() throws CDIException {
        ICDIVariable v = getParentVariable();
        if (v != null){
            return "(*" + v.getQualifiedName() + ")";
        }
        return "(*(" + getTypeName() + ")" + getParent().getValueString() + ")";
    }
    
    ICDIVariable getParentVariable(){
        return getParent().getAssociatedVariable();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#equals(org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject)
     */
    @Override
    public boolean equals(ICDIVariableDescriptor varObject) {
        if (varObject == this) return true;
        if (varObject instanceof DerefVariable){
            DerefVariable d = (DerefVariable)varObject;
            if (getParentVariable() != null)
                return getParentVariable().equals(d.getParentVariable());
        }
        return false;
    }

    /* (non-Javadoc)
     * @see com.arc.cdt.debug.seecode.core.cdi.value.ElementVariable#getChildIndex()
     */
    @Override
    protected int getChildIndex() {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#equals(org.eclipse.cdt.debug.core.cdi.model.ICDIVariable)
     */
    @Override
    public boolean equals(ICDIVariable variable) {
        return equals((ICDIVariableDescriptor)variable);
    }

    @Override
    protected ElementVariable makeClone () {
        return new DerefVariable(getParent(),mType);
    }

}
