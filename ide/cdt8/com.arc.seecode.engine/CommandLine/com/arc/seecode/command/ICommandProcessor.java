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

import com.arc.seecode.engine.StackFrameRef;

/**
 * @author David Pickens
 */
public interface ICommandProcessor {
    /**
     * Process a command to the seecode engine. It extracts the
     * command portion from the arguments then invokes an appropriate
     * {@linkplain ICommandExecutor executor} to process the command.
     * @param cmd the command.
     */
    public void processCommand(String cmd) throws Exception;
    
    /**
     * If the last command is repeatable (e.g., a step command), then
     * repeat it and return true. Otherwise, just return false.
     * @return true if last command was repeated; false otherwise.
     * @throws Exception
     */
    public boolean repeatCommand() throws Exception;
    /**
     * Set the executor to be invoked for an unrecognized command.
     * The arguments passed to the executor is the entire command string.
     * @param x the executor to be invoked to handle an unrecognized command.
     */
    public void setUndefinedCommand(ICommandExecutor x);
    /**
     * Return the executor that handles unrecognized commands.
     * @return the executor that handles unrecognized commands.
     */
    public ICommandExecutor getUndefinedCommand();
    
    /**
     * Add an executor to handle a command.
     * @param command the command's name.
     * @param x the thing to be invoked when the command is encountered.
     */
    public void addCommandExecutor(String command, ICommandExecutor x);
    
    /**
     * Return the command executor for a given command, or <code>null</code>
     * if no such command exists.
     * @param command the name of the command.
     * @return the executor for the command, or <code>null</code>.
     */
    public ICommandExecutor getCommandExecutor(String command);
    
    /**
     * Set the current stack frame against which commands are to be
     * executed.
     */
    public void setStackFrame(StackFrameRef stackFrame);
}
