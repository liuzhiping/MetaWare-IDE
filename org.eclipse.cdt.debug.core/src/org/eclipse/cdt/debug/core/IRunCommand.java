/*
 * IRunCommand
 * $Revision$
 * $Date$
 *
 * CONFIDENTIAL AND PROPRIETARY INFORMATION.
 * Copyright 2006 ARC International (Unpublished).
 * All Rights Reserved.
 * This document, material and/or software contains confidential and
 * proprietary information of ARC International and is protected by copyright,
 * trade secret and other state, federal, and international laws, and may be
 * embodied in patents issued or pending. Its receipt or possession does not
 * convey any rights to use, reproduce, disclose its contents, or to
 * manufacture, or sell anything it may describe.  Reverse engineering is
 * prohibited, and reproduction, disclosure or use without specific written
 * authorization of ARC International is strictly forbidden.  ARC and the ARC
 * logotype are trademarks of ARC International.
 */
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
     * @param config the corresponding launch configuration.
     * @param exePath the path of the executable to be invoked.
     * @param arguments arguments to be passed to the executable.
     * @return a command array to be invoked as an external process.
     * @throw CoreException of some sort of screw up occurred.
     */
    String[] createCommand(ILaunchConfiguration config, String exePath, String[] arguments) throws CoreException;

}
