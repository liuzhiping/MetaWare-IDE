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
package com.arc.dwarf2.model;

/**
 * A callback interface by which Dwarf2 format errors can be
 * reported back to the client.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface IDwarf2ErrorReporter {
    /**
     * Reports an error in the Dwarf2 data structures.
     * <P>
     * <B>NOTE:</b> Dwarf 2 can be read lazily on demand. This means that
     * errors may be reported at arbitrary times. The implementation of this
     * interface must take this fact into account.
     * 
     * @param section the name of the Dwarf section that has the problem.
     * @param offset the byte offset into the section where the problem is located.
     * @param message text of the error message.
     */
    public void error(String section, int offset,String message);

}
