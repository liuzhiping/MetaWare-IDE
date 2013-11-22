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
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIAggregateType;

/**
 * This represents an element of an aggregate value. That is, an instance of
 * this class is returned as the {@linkplain ICDIValue#getVariables() variables}
 * of a value.
 * 
 * @author David Pickens
 */
class ValueField extends ElementVariable{

    private int mChildIndex;

    //private Value mField; // evaluate lazily

    /**
     * Create a reference to an aggregate element. Since SeeCode API permits
     * lazy evaluation, we only record the child number. It isn't actually
     * retrieved until needed.
     * 
     * @param parent
     * @param childIndex
     */
    ValueField(CDIValue parent, int childIndex) {
        super(parent);
        mChildIndex = childIndex;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#getName()
     */
    @Override
    public String getName() {
        try {
            return getName(false);
        } catch (CDIException e) {
            return "???";
        }
    }



    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#getQualifiedName()
     */
    @Override
    public String getQualifiedName() throws CDIException { 
        return getName(true);
    }
    
    private String getName(boolean qualified) throws CDIException{
        String name = getSeeCodeValue().getFieldName();
        if (name != null && getParent() != null) {
            ICDIVariable parentVar = getParent().getAssociatedVariable();
            String parentName = null;
            if (parentVar != null) {
                parentName = qualified?parentVar.getQualifiedName():parentVar.getName();
            }
            else {
                parentName = getParent().getValueString();
            }
            if (parentName != null) {
                if (parentName.startsWith("*"))
                    parentName = "(" + parentName + ")";
                if (name.length() > 0 && Character.isDigit(name.charAt(0))) {
                    return parentName + "[" + name + "]";
                } 
                else
                    return parentName + "." + name;
            }          
        }
        return name;
    }
    
    @Override
    public boolean isEditable() throws CDIException {
        return !(getType() instanceof ICDIAggregateType) &&
                   getSeeCodeValue().hasAddress();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#equals(org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject)
     */
    @Override
    public boolean equals(ICDIVariableDescriptor varObject) {
        if (!(varObject instanceof ValueField)) return false;
        ValueField vf = (ValueField) varObject;
        if (vf == this) return true;
        try {
            if (!vf.getSeeCodeValue().hasAddress()
                    || !getSeeCodeValue().hasAddress()) return false;
            return vf.getSeeCodeValue().getAddress() == getSeeCodeValue()
                    .getAddress() && isEqual(vf.getSeeCodeValue().getFieldName(),getSeeCodeValue().getFieldName());
        } catch (CDIException e) {
            return false;
        }
    }

    private static boolean isEqual(String a, String b){
        if (a == null) return b == null;
        return a.equals(b);
    }
    /* (non-Javadoc)
     * @see com.arc.cdt.debug.seecode.core.cdi.IRefresh#isOutOfScope()
     */
    @Override
    public boolean isOutOfScope() {
        return false;
    }

    /* (non-Javadoc)
     * @see com.arc.cdt.debug.seecode.core.cdi.value.ElementVariable#getChildIndex()
     */
    @Override
    protected int getChildIndex() {
        return mChildIndex;
    }
    
    @Override
    public boolean equals(ICDIVariable variable) {
        return equals((ICDIVariableDescriptor)variable);
    }

    @Override
    protected ElementVariable makeClone () {
        return new ValueField(getParent(),getChildIndex());
    }
}
