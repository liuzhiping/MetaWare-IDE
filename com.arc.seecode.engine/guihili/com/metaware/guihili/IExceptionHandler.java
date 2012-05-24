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
package com.metaware.guihili;

/**
 * A callback to handle internal exceptions.
 * Typically the implementor will popup a dialog of some sort.
 *
 * @author David Pickens
 * @version May 14, 2002
 */
public interface IExceptionHandler {
    /**
     * Handle an exception
     * @param x the exception
     */
    public void handleException(Throwable x);
    /**
     *  Handle an exception with an accompanying message.
     * @param msg the accompanying exception
     * @param x the exception
     */
    public void handleException(String msg, Throwable x);
    }
