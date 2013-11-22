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

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIIntegralValue;

import com.arc.seecode.engine.Value;


/**
 * @author David Pickens
 */
class IntegerValue extends CDIValue implements ICDIIntegralValue {

    /**
     * @param target
     * @param value
     */
    public IntegerValue(Value value, ICDITarget target) {
        super(value,target);
    }

    /*override*/
    @Override
    public long longValue() throws CDIException {
        String v = getSeeCodeValue().getValue();
        try {
            if (v == null) 
                return 0xdeadbeef; // shouldn't happen
            else
            if (v.startsWith("0x")){
                return Long.parseLong(v.substring(2),16);
            }
            else if (v.startsWith("'")) { // a quoted ascii character
                if (v.length() >= 3) {
                    if (v.charAt(0) == '\'') {
                        if (v.charAt(1) == '\\' && v.length() > 3) {
                            int j = v.indexOf('\'', 3);
                            if (j >= 3) {
                                switch (v.charAt(2)) {
                                    case '\\':
                                        return '\\';
                                    case '\'':
                                        return '\'';
                                    case 'n':
                                        return '\n';
                                    case 'r':
                                        return '\r';
                                    case 't':
                                        return '\t';
                                    case '0':
                                    case '1':
                                    case '2':
                                    case '3':
                                    case '4':
                                    case '5':
                                    case '6':
                                    case '7':
                                        return Integer.parseInt(v.substring(2, j),8);
                                }
                            }
                        }
                        else if (v.charAt(2) == '\'') {
                            return v.charAt(1);
                        }
                    }
                }
            }
            return Long.parseLong(v);
        } catch (NumberFormatException e) {
            // No comment on the exception. The viewer will
            // simply display as is.
            throw new CDIException(v);
        }
    }

    /*override*/
    @Override
    public int intValue() throws CDIException {
        return (int)longValue();
    }

    /*override*/
    @Override
    public short shortValue() throws CDIException {
        return (short)longValue();
    }

    /*override*/
    @Override
    public int byteValue() throws CDIException {
        return (byte)longValue();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIIntegralValue#bigIntegerValue()
     */
    @Override
    public BigInteger bigIntegerValue() throws CDIException {
        return BigInteger.valueOf(longValue());
    }

}
