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
 * An exception that is thrown when the engine (as a separate process)
 * terminates; i.e., the socket connection drops.
 * @author David Pickens
 */
public class EngineDisconnectedException extends EngineException {

    /**
     * @param message
     */
    public EngineDisconnectedException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public EngineDisconnectedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public EngineDisconnectedException(Throwable cause) {
        super(cause);
    }

}
