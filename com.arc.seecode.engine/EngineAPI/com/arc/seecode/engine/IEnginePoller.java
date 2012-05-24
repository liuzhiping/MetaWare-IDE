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


public interface IEnginePoller {

    /**
     * Poll the engine to see if anything is running.
     * If the engine is stopped, this method will return immediately with the value false.
     * <P>
     * If the engine is running, then the caller will sleep for whatever time the target
     * requires (0 for a simulator), and then returns true.
     * <p>
     * 
     * @param delayIfRunning if false, then no delay will be performed.
     * @return true if running; false if stopped.
     */
    public boolean poll (boolean delayIfRunning);
    
    /**
     * If another thread is being delayed within {@link #poll} method, this
     * method interrupts it.
     */
    public void interruptPollDelay();

}
