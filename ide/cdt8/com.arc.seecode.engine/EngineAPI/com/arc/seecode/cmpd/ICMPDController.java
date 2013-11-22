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
package com.arc.seecode.cmpd;

import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;

/**
 * This object manages CMPD operations. 
 * <P>
 * Any operations invoked on this object affect all target processes.
 * <P>
 * There are two reasons why this object is necessary, as opposed to invoking
 * methods in a sequence of {@link EngineInterface} objects:
 * <nl>
 * <li> The engine itself will broadcast the operation to all processes before returning.
 * Thus, a collection of processes can be resumed, stopped, or stepped as a single operation
 * without the overhead of having to send operations to each individual process.
 * <li> Commands issued here are compositely invoked in each process simultaneously.
 * </nl>
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface ICMPDController {
    /**
     * Resume all processes that are currently suspended.
     */
    public void resume() throws EngineException;
    /**
     * Restart all processes.
     */
    public void restart() throws EngineException;

    /**
     * Suspend all processes.
     */
    public void suspend() throws EngineException;
    
    /**
     * Have each process run until it returns from the current function.
     */
    public void stepOut() throws EngineException;
    /**
     * Perform a statement step operation on each process.
     * @param over if true, step over calls; otherwise, step into them.
     */
    public void statementStep(boolean over) throws EngineException;
    
    /**
     * Perform an instruction step operation on each process.
     * @param over if true, step over calls; otherwise, step into them.
     */
    public void instructionStep(boolean over) throws EngineException;
    
    /**
     * Invoke a command on behalf of all processes.
     * A command may be prefixed with &quot;[<i>focus</i>]&quot; to indicate that the
     * command is to be invoked on behalf of a subset of processes.
     */
    public void invokeCommand(String command) throws EngineException;

}
