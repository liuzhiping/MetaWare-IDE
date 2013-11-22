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
package com.arc.seecode.server;

import com.arc.seecode.engine.EngineException;

/**
 * A queue for running commands in the main engine thread.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */

public interface IEngineRunQueue {
    /**
     * Enqueue an operation that will access the engine so that it is
     * done in the engine's sole operating thread.
     * @param run
     * @throws EngineException if the engine appears to be hung.
     */
    public void enqueue(IEngineInvocation run) throws EngineException;
    
    /**
     * Return the number of ending operations that are enqueued.
     * @return the number of ending operations that are enqueued.
     */
    public int getQueueSize();
    
    /**
     * Wait until the queue is empty. Return true if successful.
     * @param timeout time out value in milliseconds.
     * @return true if the queue emptied prior to timeout.
     */
    public boolean waitUntilEmpty(int timeout);
    
    /**
     * Shutdown the queue and any associated thread.
     */
    public void shutdown();
    
    /** 
     * Return associated thread (for error checking).
     * @return the only thread that talks to engine.
     */
    public Thread getThread();

}
