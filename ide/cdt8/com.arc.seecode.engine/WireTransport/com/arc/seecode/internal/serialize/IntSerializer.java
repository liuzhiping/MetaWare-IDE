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
 * Handle serialization of "int" objects.
 * @author David Pickens
 */
class IntSerializer implements ISerializer {
    
    private IntSerializer(){}
    private static ISerializer sInstance = new IntSerializer();
    public static ISerializer getInstance() {
        return sInstance;
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.rmi.IArgExtractor#extractArgument(java.io.DataInputStream)
     */
    @Override
    public Object deserialize(DataInputStream input) throws IOException {
        return new Integer(input.readInt());
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.rmi.ISerializer#serialize(java.lang.Object, java.io.DataOutputStream)
     */
    @Override
    public void serialize(Object v, DataOutputStream output) throws IOException {
        try {
            output.writeInt(((Number)v).intValue());
        }
        catch (ClassCastException x){
            throw new IllegalArgumentException("Argument must be Number: " + v);
        }
        catch (NullPointerException x){
            throw new IllegalArgumentException("Argument must be Number: " + v);
        }   
    }

}
