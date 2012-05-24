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
 * Exception to note that the engine response timed out.
 * Clients may want to pop up a different message than for a normal failure.

 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class EngineTimeoutException extends EngineException {

    public EngineTimeoutException(String message) {
        super(message);
        // @todo Auto-generated constructor stub
    }

    public EngineTimeoutException(String message, Throwable cause) {
        super(message, cause);
        // @todo Auto-generated constructor stub
    }

    public EngineTimeoutException(Throwable cause) {
        super(cause);
        // @todo Auto-generated constructor stub
    }

}
