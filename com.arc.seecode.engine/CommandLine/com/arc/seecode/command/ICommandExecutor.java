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
package com.arc.seecode.command;


/**
 * An instance of this object is called to process a command.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface ICommandExecutor {
    /**
     * Execute a command with a given set of arguments.
     * @param arguments the arguments to the command.
     * @throws Exception
     */
    void execute(String arguments) throws Exception;
    
    /**
     * If this command is of variety that can be repeated, then
     * repeat it with whatever state information that was stored by
     * the last call to {@link #execute}.
     * <P>
     * Such commands as "step" are repeatable. Commands like "load" are not.
     * @return true if command was indeed repeated; false otherwise.
     * @throws Exception
     */
    boolean repeat() throws Exception;
}
