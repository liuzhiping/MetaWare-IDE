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
package com.arc.cdt.debug.seecode.internal.core.cdi.types;

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIStructType;


/**
 * @author David Pickens
 */
class StructType extends AbstractType implements ICDIStructType {
    private int mKind;
    /**
     * @param name
     * @param size
     * @param target
     */
    public StructType(String name, int size, int kind, ICDITarget target) {
        super(name, size, target);
        if (kind != STRUCT && kind != UNION && kind != CLASS)
            throw new IllegalArgumentException("Bad kind for struct");
        mKind = kind;
    }

    /*override*/
    @Override
    public boolean isClass() {
        return mKind == CLASS;
    }

    /*override*/
    @Override
    public boolean isStruct() {
        return mKind == STRUCT;
    }

    /*override*/
    @Override
    public boolean isUnion() {
        return mKind == UNION;
    }
    
    @Override
	public String getTypeName(){
    	final String name = getName();
    	return name != null?name:mKind==STRUCT?"<struct>":mKind==UNION?"<union>":"<class>";
    }

    /*override*/
    @Override
    public int getKind() {
        return mKind;
    }

}
