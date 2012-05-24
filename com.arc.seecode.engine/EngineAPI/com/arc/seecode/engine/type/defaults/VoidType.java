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
 * The "void" type.
 * @author David Pickens
 */
public class VoidType extends AbstractType {

    /**
     * @param name
     */
    public VoidType(String name) {
        super(name, 0);
    }

    @Override
    public int getKind() {
        return VOID;
    }
    
    @Override
    public boolean equals(IType type){
        return type.getKind() == VOID;
    }

}
