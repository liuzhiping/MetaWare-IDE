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
package com.arc.debugger;

/**
 * An exception to denote that a debugger path could not be constructed.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class EngineLocatorException extends Exception {

    public EngineLocatorException() {
        // @todo Auto-generated constructor stub
    }

    public EngineLocatorException(String message) {
        super(message);
        // @todo Auto-generated constructor stub
    }

    public EngineLocatorException(Throwable cause) {
        super(cause);
        // @todo Auto-generated constructor stub
    }

    public EngineLocatorException(String message, Throwable cause) {
        super(message, cause);
        // @todo Auto-generated constructor stub
    }

}
