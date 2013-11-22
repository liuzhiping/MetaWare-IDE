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
package com.arc.cdt.importer.core;


/**
 * Denotes a formatting problem while reading a CodeWrite 
 * project space file (.psp).
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class PSPException extends Exception {

    /**
     * @todo davidp needs to add a constructor comment.
     * @param message
     */
    public PSPException(String message) {
        super(message);
    }

    /**
     * @todo davidp needs to add a constructor comment.
     * @param message
     * @param cause
     */
    public PSPException(String message, Throwable cause) {
        super(message, cause);
    }

}
