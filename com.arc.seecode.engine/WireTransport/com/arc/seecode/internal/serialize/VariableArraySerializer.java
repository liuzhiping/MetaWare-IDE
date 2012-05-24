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

import com.arc.seecode.engine.Variable;


/**
 * Serializer for array of {@link Variable}.
 * @author David Pickens
 */
class VariableArraySerializer implements ISerializer{
    private ISerializer mVarSerializer;
    VariableArraySerializer(ISerializer varSerializer){
        mVarSerializer = varSerializer;
    }
    
    private static final int MAGIC = 0x33445566; //arbitrary magic number
    /* (non-Javadoc)
     * @see com.arc.seecode.serialize.ISerializer#serialize(java.lang.Object, java.io.DataOutputStream)
     */
    @Override
    public void serialize(Object v, DataOutputStream output) throws IOException {
        output.writeInt(MAGIC);
       if (v == null){
           output.writeInt(-1);
       }
       else {
           Variable[] array = (Variable[])v;
           output.writeInt(array.length);
           for (int i = 0; i < array.length;i++){
               mVarSerializer.serialize(array[i],output);
           }
       }

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.serialize.ISerializer#deserialize(java.io.DataInputStream)
     */
    @Override
    public Object deserialize(DataInputStream input) throws IOException {
        if (input.readInt() != MAGIC)
            throw new IOException("Stream does not contain Location array");
        int dimension = input.readInt();
        // negative dimension means null.
        if (dimension < 0) return null; 
        Variable array[] = new Variable[dimension];
        for (int i = 0; i < array.length; i++){
            array[i] = (Variable)mVarSerializer.deserialize(input);
        }
        return array;
    }

}
