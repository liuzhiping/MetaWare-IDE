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
class IntegerType extends AbstractType {

    private boolean mUnsigned;

    /**
     * @param name
     * @param size
     */
    public IntegerType(String name, int size, boolean isUnsigned) {
        super(name, size);
        mUnsigned = isUnsigned;
    }

    /* override */
    @Override
    public int getKind() {
        return INTEGER;
    }

    @Override
    public boolean isUnsigned() {
        return mUnsigned;
    }

    @Override
    public long getHighRange() {
        switch (getSize()) {
        case 1:
            if (isUnsigned())
                return 255;
            else
                return Byte.MAX_VALUE;
        case 2:
            if (isUnsigned())
                return 65535;
            else
                return Short.MAX_VALUE;

        case 4:
            if (isUnsigned())
                return 0xFFFFFFFFL;
            else
                return Integer.MAX_VALUE;
        case 8:
            if (isUnsigned())
                return -1L;
            else
                return Long.MAX_VALUE;
        }
        return Long.MAX_VALUE; // ???
    }

    @Override
    public long getLowRange() {
        if (isUnsigned()) return 0;
        switch (getSize()) {
        case 1:
            return Byte.MIN_VALUE;
        case 2:
            return Short.MIN_VALUE;
        case 4:
            return Integer.MIN_VALUE;
        case 8:
            return Long.MIN_VALUE;
        }
        return 0; //???
    }
    
    @Override
    public boolean equals(IType t){
        return t.getKind() == INTEGER &&
            t.getSize() == getSize() &&
            t.isUnsigned() == isUnsigned();
    }
}
