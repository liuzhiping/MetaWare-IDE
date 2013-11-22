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

import com.arc.seecode.engine.IRunner;
import com.arc.seecode.serialize.MethodSerializationHandler;

/**
 * A thread the reads commands (i.e., method invocations) from a connection and routes them to the 
 * appropriate target object.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface ICommandReceiverRouter {
    
    /**
     * Highest value for "objectID" so that it can be safely used as an array index.
     */
    public final int MAX_OBJECT_IDS = 512;
    
    public interface IErrorLogger{
        public void logException(String msg, Throwable t);
    }
    /**
     * Set the method handler that is to service received commands with the given object ID.
     * @param objectID the object ID to which this method handler is to apply.
     * @param handler the method handler.
     * @param inUIThread if true, then service the command in the UI thread.
     * @throws IllegalArgumentException if "objectID" is already being serviced.
     */
    public void setMethodHandler(int objectID, MethodSerializationHandler handler, boolean inUIThread);
    
    /**
     * Start reading commands from the given connection. Send errors to the given log.
     * If the handler is supposed to run in the UI thread, then use the given runner to invoke it.
     * @param connection
     * @param uiThreadRunner the callback to be used to invoke the target method if it has been
     * designated to run in the special UI thread.
     * @param log where exceptions are noted.
     * @param threadName name of the thread that will be created to served methods.
     * 
     */
    public void start(IConnection connection, IRunner uiThreadRunner, IErrorLogger log, String threadName);

}
