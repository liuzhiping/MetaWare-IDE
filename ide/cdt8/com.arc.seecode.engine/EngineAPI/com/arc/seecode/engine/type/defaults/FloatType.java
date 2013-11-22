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
package com.arc.seecode.engine.type.defaults;

import com.arc.seecode.engine.type.IType;


/**
 * @author David Pickens
 */
class FloatType extends AbstractType {

    /**
     * @param name
     * @param size
     */
    public FloatType(String name, int size) {
        super(name, size);
    }

    /*override*/
    @Override
    public int getKind() {
        return FLOAT;
    }
    
    @Override
    public boolean equals(IType t){
        return t.getKind() == FLOAT && t.getSize() == getSize();
    }

}
