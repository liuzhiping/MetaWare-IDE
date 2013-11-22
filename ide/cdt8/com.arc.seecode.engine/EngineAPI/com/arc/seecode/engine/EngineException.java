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
package com.arc.seecode.engine;


/**
 * Exception occurred in engine.
 * @author David Pickens
 */
public class EngineException extends Exception {

    /**
     * @param message
     */
    public EngineException(String message) {
        super(message);
    }
    /**
     * @param message
     * @param cause
     */
    public EngineException(String message, Throwable cause) {
        super(message, cause);

    }
    /**
     * @param cause
     */
    public EngineException(Throwable cause) {
        super(cause);
    }
}
