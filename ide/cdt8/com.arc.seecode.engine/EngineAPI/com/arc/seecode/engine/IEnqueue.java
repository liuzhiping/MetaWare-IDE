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
package com.arc.seecode.engine;

/**
 * Enqueue a method for running asynchronously on another thread.
 * 
 */
public interface IEnqueue {
    /**
     * Enqueue a run object to be invoked asynchronously at a later time.
     * <P>
     * If "key" is not null, then replace any existing run object with that key.
     * @param run the method to be invoked ascynchronously in the future.
     * @param key if not null, a key to identify any pending queued run object that
     * is to be replaced.
     */
    void enqueue(Runnable run, Object key);
}
