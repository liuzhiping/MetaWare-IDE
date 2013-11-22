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
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIIntType;


/**
 * The "int" type.
 * @author David Pickens
 */
public class IntType extends IntegerType implements ICDIIntType {

    /**
     * @param name
     * @param isUnsigned
     * @param target
     */
    public IntType(String name,  boolean isUnsigned, ICDITarget target) {
        super(name != null?name:isUnsigned?"uint":"int", 4, isUnsigned, target);
    }

}
