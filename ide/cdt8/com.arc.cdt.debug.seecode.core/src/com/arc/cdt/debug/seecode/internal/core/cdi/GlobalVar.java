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
import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariableDescriptor2;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;

import com.arc.symbols.ISymbol;


class GlobalVar implements ICDIGlobalVariableDescriptor2 {

    private ISymbol fSym;
    private ICDITarget fTarget;

    GlobalVar(ISymbol sym, ICDITarget target){
        fSym = sym;
        fTarget = target;
    }
    @Override
    public boolean equals (ICDIVariableDescriptor varDesc) {
        try {
            return varDesc instanceof ICDIGlobalVariableDescriptor &&
                getQualifiedName().equals(varDesc.getQualifiedName());
        }
        catch (CDIException e) {
            return false;
        }
    }
    
    @Override
    public String getPath() {
        return fSym.getSourceFile();
    }

    @Override
    public String getName () {
        return fSym.getName();
    }

    @Override
    public String getQualifiedName () throws CDIException {
        return fSym.getLinkageName();
    }

    @Override
    public ICDIType getType () throws CDIException {
        return null;
    }

    @Override
    public String getTypeName () throws CDIException {
        // @todo Auto-generated method stub
        return null;
    }

    @Override
    public ICDIVariableDescriptor getVariableDescriptorAsArray (int start, int length) throws CDIException {
        // @todo Auto-generated method stub
        return null;
    }

    @Override
    public ICDIVariableDescriptor getVariableDescriptorAsType (String type) throws CDIException {
        // @todo Auto-generated method stub
        return null;
    }

    @Override
    public int sizeof () throws CDIException {
        // @todo Auto-generated method stub
        return 0;
    }

    @Override
    public ICDITarget getTarget () {
        return fTarget;
    }

}
