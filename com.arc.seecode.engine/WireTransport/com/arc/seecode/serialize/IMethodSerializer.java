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
package com.arc.seecode.serialize;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public interface IMethodSerializer {

    /**
     * Return name of associated method.
     * @return name of associated method.
     */
    public String getName ();

    /**
     * Return whether or not this method can be invoked
     * asynchronously. Such methods can issue a "reply" prior
     * to actually executing.
     * <P>
     * An asynchronous method must return "void", and
     * not throw any exceptions that must be caught immediately.
     * @return true if this method can be called without
     * waiting for the a result.
     */
    public boolean isAsynchronous ();

    /**
     * Invoke the method by extracting its arguments from a serialized input
     * stream. The result is serialized to an output stream.
     * 
     * @param input
     *            stream from which to extract arguments.
     * @param output
     *            stream to which result is written.
     */
    public void invoke (DataInputStream input, DataOutputStream output) throws Throwable;

    /**
     * Given the arguments of this this function. Serialize and return the
     * bytes.
     * 
     * @param args
     *            the arguments.
     * @return serialized representation.
     */
    public byte[] serializeArguments (Object[] args);

    public Object deserializeResult (DataInputStream input) throws IOException;

}
