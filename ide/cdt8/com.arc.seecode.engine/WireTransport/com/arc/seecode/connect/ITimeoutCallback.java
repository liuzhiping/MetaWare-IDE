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
package com.arc.seecode.connect;

/**
 * A callback that is invoked when the engine is about to timeout.
 * It gives the client an opportunity to extend the time.
 * 
 */
public interface ITimeoutCallback {
    
    /**
     * This is called when a timeout has occurred while waiting for the response to
     * an engine command across a TCP/IP line.
     *<P>
     * Its a hook for giving a client the opportunity to try again.
     * If this method returns 0, then a TimeOutException will be thrown immediately.
     * <P>
     * If -1, then the caller will wait indefinitely for the engine to reply.
     * <P>
     * Otherewise, its the number of milliseconds to wait further.
     * @param oldTimeout
     * @return 0, -1, or number of addition milliseconds to wait.
     */
    int getNewTimeout(int oldTimeout);

}
