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
package org.eclipse.cdt.debug.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;


/**
 * A class implements this interface by which a command string can be built to
 * invoke an executable that may require a simulator.
 * <P>
 * CUSTOMIZATION
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface IRunCommand {
    /**
     * Given an executable that is to be invoked with the given arguments, return
     * a command array that will be invoked as an external process. The first element
     * of the array is the executable to be invoked. If the program requires a simulator,
     * then the first element will be the path of the simulator and the rest of the
     * elements will be arguments to be passed to the simulator.
     * <P>
     * Returns null if the exePath is to be interpreted as an ordinary native application.
     * <P>
     * @param config the corresponding launch configuration.
     * @param exePath the path of the executable to be invoked.
     * @param arguments arguments to be passed to the executable.
     * @return a command array to be invoked as an external process.
     * @throw CoreException of some sort of screw up occurred.
     */
    String[] createCommand(ILaunchConfiguration config, String exePath, String[] arguments) throws CoreException;

}
