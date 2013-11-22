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
package org.eclipse.cdt.debug.core.cdi;

/**
 * Extend session configuration to include session process name.
 *<P>
 *CUSTOMIZATION
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface ICDISessionConfiguration2 extends ICDISessionConfiguration {
    /**
     * Return the name of the session process (e.g, "GDB Debugger Engine").
     * <P>
     * @return the name of the debugger process.
     * @throws CDIException
     */
    String getSessionProcessName();
}
