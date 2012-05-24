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
import org.eclipse.cdt.debug.core.cdi.ICDIFormat;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.core.runtime.IAdaptable;

import com.arc.seecode.engine.StackFrameRef;
import com.arc.seecode.engine.Value;
import com.arc.seecode.engine.type.IType;


/**
 * @author David Pickens
 */
public class ValueFactory {

    public static ICDIValue makeValue (Value v, ICDITarget target) {
        return makeValue(null, v, target, null,0);
    }
    
    /**
     * Given a SeeCode value object, create a CDI value appropriately.
     * @param var the associated variable if there is one, otherwise null.
     * @param v the seecode value
     * @param target the target that every CDI object needs
     * @param sf the stackframe in case we need to change the value of an element.
     * @return the CDI value.
     */
    public static ICDIValue makeValue (ICDIVariable var, Value v, ICDITarget target, StackFrameRef sf){
        return makeValue(var,v,target,sf,0);
    }

    /**
     * Given a SeeCode value object, create a CDI value appropriately. Permit default
     * format to be specified.
     * @param var the associated variable if there is one, otherwise null.
     * @param v the seecode value
     * @param target the target that every CDI object needs
     * @param sf the stackframe in case we need to change the value of an element.
     * @param defaultFormat one of the ICDIFormat constants.
     * @return the CDI value.
     */
    public static ICDIValue makeValue (ICDIVariable var, Value v, ICDITarget target, StackFrameRef sf, int defaultFormat) {
        IType t = null;
        try {
        	if (v.getType() != null){
        		t = v.getType();
        	}
        	else
            if (var != null && var.getType() instanceof IAdaptable){
                t = (IType)((IAdaptable)var.getType()).getAdapter(IType.class);            
            }
        }
        catch (CDIException e) {
            // Can't access variable type for some reason. Just use value type.
        }
        if (t == null)
            t = v.getType();
        CDIValue value;
        // If the value wasn't computable, it will have
        // a text like "<not current>". Treat these as special.
        String sv = v.getValue();
        if (sv == null) sv = "<not current>";
        if (sv.startsWith("<"))
            value = new CDIValue(v, target);
        else
        // no type defaults to int
        if (t == null)
            value = new CDIValue(var, t, v, target);
        else {
            switch (t.getKind()) {
                case IType.INTEGER:
                    if (t.getSize() == 1) {
                        // CR2238: if the type actually appears to be byte integer instead of
                        // a char, then treat it as such so that it displays as expected.
                        String typeName = t.getName();
                        if (typeName.indexOf("byte") >= 0|| typeName.indexOf("int") >= 0)
                            return new IntValue(v,target);
                        return new CharValue(v, target);
                    }
                    if (t.getSize() == 2)
                        return new ShortValue(v, target);
                    if (t.getSize() == 4) {
                        if (t.getName().indexOf("long") >= 0)
                            return new LongValue(v, target);
                        value = new IntValue(v, target);
                    }
                    else
                        value = new LongLongValue(v, target);
                    break;
                case IType.POINTER:
                    value = new PointerValue(var, t, v, target, sf);
                    if (defaultFormat == 0)
                        defaultFormat = ICDIFormat.HEXADECIMAL;
                    break;
                case IType.REF:
                    value = new RefValue(var, t, v, target, sf);
                    if (defaultFormat == 0)
                        defaultFormat = ICDIFormat.HEXADECIMAL;
                    break;
                case IType.ENUM:
                    value = new EnumValue(v, target);
                    break;
                case IType.FLOAT:
                    if (t.getSize() == 4)
                        value = new FloatValue(v, target);
                    else
                        value = new DoubleValue(v, target);
                    if (defaultFormat == 0)
                        defaultFormat = ICDIFormat.HEXADECIMAL;
                    break;
                case IType.ARRAY:
                    value = new ArrayValue(var, t, v, target, sf);
                    break;
                case IType.STRUCT:
                case IType.UNION:
                case IType.CLASS:
                    value = new StructValue(var, t, v, target, sf);
                    break;
                default: // Shouldn't get here
                    value = new CDIValue(var, t, v, target);
                    break;
            }
        }
        value.setDefaultFormat(defaultFormat);
        return value;
    }
}
