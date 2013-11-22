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
package com.arc.cdt.debug.seecode.internal.core.cdi.event;

import org.eclipse.cdt.debug.core.cdi.event.ICDIRestartedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;


/** An event fired when a program is restarted.
 * @author David Pickens
 */
public class RestartedEvent extends Event implements ICDIRestartedEvent {


    /**
     * @param source
     */
    public RestartedEvent(ICDITarget source) {
        super(source);
    }
    
    @Override
    public String toString(){
        return "RestartedEvent(" + getSource() + ")";
    }
}
