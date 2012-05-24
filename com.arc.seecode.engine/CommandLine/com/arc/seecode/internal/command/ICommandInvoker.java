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
package com.arc.seecode.internal.command;

/**
 * 
 * A callback that is used in {@link CommandService} to invoke a command
 * that is associated with a breakpoint when the breakpoint his hit.
 */
public interface ICommandInvoker {
    void processCommand(String cmd) throws Exception;
}
