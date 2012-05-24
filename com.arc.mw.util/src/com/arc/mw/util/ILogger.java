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
package com.arc.mw.util;

/**
 * An interface for loggin messages (as in debug tracing).
 */
public interface ILogger {
	/**
	 * Log a message.
	 * @param fromWhere originator.
	 * @param message the message.
	 */
    public void log(String fromWhere, String message);
    }
