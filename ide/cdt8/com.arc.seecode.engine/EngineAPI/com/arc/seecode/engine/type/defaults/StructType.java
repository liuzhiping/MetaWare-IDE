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

/**
 * @author David Pickens
 */
class StructType extends AbstractType {
    private int mKind;
    /**
     * @param name
     * @param size
     */
    public StructType(String name, int size, int kind) {
        super(name, size);
        mKind = kind;
        if (kind != STRUCT && kind != UNION && kind != CLASS) { throw new IllegalArgumentException(
                "Bad kind: " + kind); }
    }

    /* override */
    @Override
    public int getKind() {
        return mKind;
    }
    
    @Override
    public String toString(){
        if (getName() != null) return getName();
        switch(getKind()){
        case STRUCT: return "struct{}";
        case UNION: return "union{}";
        case CLASS: return "class{}";
        }
        return "???";
    }

}
