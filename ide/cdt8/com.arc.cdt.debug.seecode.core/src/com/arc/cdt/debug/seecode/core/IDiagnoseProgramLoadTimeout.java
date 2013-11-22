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
package com.arc.cdt.debug.seecode.core;

/**
 * Callback into the UI to diagnose a timeout exception while waiting for the program
 * to load.
 *
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface IDiagnoseProgramLoadTimeout {
    /**
     * 
     * Inform the user that the engine timed out and what he can do to remedy it.
     * @param exeName the name of the executable.
     * @param timeout current timeout that may need to be increased.
     */
    void diagnoseTimeout(String exeName, int timeout);

}
