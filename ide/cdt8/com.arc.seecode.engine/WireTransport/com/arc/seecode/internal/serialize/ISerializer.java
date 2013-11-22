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
package com.arc.seecode.internal.serialize;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


/**
 * Interface for serializing and deserializing a value.
 * There is a concrete implementation for each type
 * that we're interested in.
 * @author David Pickens
 */
public interface ISerializer {
    /**
     * Given an object value, serialize it into an
     * output stream.
     * @param v the output value.
     * @param output the  output stream.
     */
    public void serialize(Object v, DataOutputStream output) throws IOException;
    
    /**
     * Given an input stream, deserialize an object and
     * return the object.
     * @param input the input stream.
     * @return the deserialized object.
     * @throws IOException
     */
    public Object deserialize(DataInputStream input) throws IOException;

}
