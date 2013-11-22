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

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIDoubleValue;

import com.arc.seecode.engine.Value;


/**
 * @author David Pickens
 */
class DoubleValue extends FloatingPointValue implements ICDIDoubleValue {

    /**
     * @param value
     * @param target
     */
    public DoubleValue(Value value, ICDITarget target) {
        super(value, target);
    }

}
