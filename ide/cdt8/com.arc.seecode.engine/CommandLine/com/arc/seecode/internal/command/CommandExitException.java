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
package com.arc.seecode.internal.command;
/**
 * Thrown when parsing a command in which an error was detected and diagnosed.
 * It merely terminates the command.
 * @author David Pickens
 */

public class CommandExitException extends RuntimeException {

    public CommandExitException() {
        super();
       
    }

    public CommandExitException(String message) {
        super(message);
     
    }

    public CommandExitException(String message, Throwable cause) {
        super(message, cause);
        // @todo Auto-generated constructor stub
    }

    public CommandExitException(Throwable cause) {
        super(cause);
       
    }

}
