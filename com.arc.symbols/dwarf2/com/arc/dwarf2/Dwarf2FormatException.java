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
package com.arc.dwarf2;

/**
 * An exception to denote some sort of corruption within Dwarf2.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class Dwarf2FormatException extends Exception {

    public Dwarf2FormatException() {
        // @todo Auto-generated constructor stub
    }

    public Dwarf2FormatException(String message) {
        super(message);
        // @todo Auto-generated constructor stub
    }

    public Dwarf2FormatException(Throwable cause) {
        super(cause);
        // @todo Auto-generated constructor stub
    }

    public Dwarf2FormatException(String message, Throwable cause) {
        super(message, cause);
        // @todo Auto-generated constructor stub
    }

}
