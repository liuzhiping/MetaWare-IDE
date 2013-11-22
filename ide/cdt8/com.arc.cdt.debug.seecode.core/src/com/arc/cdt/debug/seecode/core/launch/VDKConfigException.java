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
package com.arc.cdt.debug.seecode.core.launch;

/**
 * Exception that indicates an error in VDK config file for configurating CMPD.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class VDKConfigException extends Exception {

    public VDKConfigException() {
        // @todo Auto-generated constructor stub
    }

    public VDKConfigException(String message) {
        super(message);
        // @todo Auto-generated constructor stub
    }

    public VDKConfigException(Throwable cause) {
        super(cause);
        // @todo Auto-generated constructor stub
    }

    public VDKConfigException(String message, Throwable cause) {
        super(message, cause);
        // @todo Auto-generated constructor stub
    }

}
