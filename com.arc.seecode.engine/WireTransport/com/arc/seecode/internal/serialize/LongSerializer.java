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
 * Extracts an "long" argument from a serialize respresentation.
 * @author David Pickens
 */
class LongSerializer implements ISerializer {
    private LongSerializer(){}
    private static ISerializer sInstance = new LongSerializer();
    
    public static ISerializer getInstance(){return sInstance;}

    /* (non-Javadoc)
     * @see com.arc.seecode.rmi.IArgExtractor#extractArgument(java.io.DataInputStream)
     */
    @Override
    public Object deserialize(DataInputStream input) throws IOException {
        return new Long(input.readLong());
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.rmi.ISerializer#serialize(java.lang.Object, java.io.DataOutputStream)
     */
    @Override
    public void serialize(Object v, DataOutputStream output) throws IOException {
       try {
           output.writeLong(((Number)v).longValue());
       }
       catch (ClassCastException x){
           throw new IllegalArgumentException("Argument must be Number: " + v);
       }
       catch (NullPointerException x){
           throw new IllegalArgumentException("Argument must be Number: " + v);
       }
        
    }
}

