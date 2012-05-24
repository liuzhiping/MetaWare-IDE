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
package com.arc.cdt.debug.seecode.core.cdi;

import com.arc.seecode.engine.EngineInterface;


/**
 * Interface for the error log to receive entries.
 *
 */
public interface IEngineErrorLog {
    

    
    /**
     * Write message to log on behalf of a particular engine.
     * @param engine the engine on behalf of this message.
     * @param message the message to write.
     */
    public void write(EngineInterface engine, String message);
}
