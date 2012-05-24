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
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;


/**
 * A register description.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
class RegisterDescriptor implements ICDIRegisterDescriptor {
    
    RegisterDescriptor(ICDITarget target, String name, int id){
        mTarget = target;
        mName = name;
        mID = id;
    }

    @Override
    public ICDIVariableDescriptor getVariableDescriptorAsType (String type) throws CDIException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ICDIVariableDescriptor getVariableDescriptorAsArray (int start, int length) throws CDIException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ICDITarget getTarget () {
        return mTarget;
    }

    @Override
    public boolean equals (ICDIVariableDescriptor varObject) {
        if (!(varObject instanceof Register)) return false;
        Register r = (Register) varObject;
        return r.getID() == getID();
    }

    @Override
    public String getQualifiedName () throws CDIException {
        return mName;
    }

    @Override
    public int sizeof () throws CDIException {
        return 4; //TODO
    }

    @Override
    public String getTypeName () throws CDIException {
        ICDIType type = getType();
        if (type != null) return type.getTypeName();
        return null;
    }

    @Override
    public ICDIType getType () throws CDIException {
        return null;
    }

    int getID () {
        return mID;
    }

    @Override
    public String getName () {
        return mName;
    }
    
    private int mID;
    private ICDITarget mTarget;
    private String mName;

}
