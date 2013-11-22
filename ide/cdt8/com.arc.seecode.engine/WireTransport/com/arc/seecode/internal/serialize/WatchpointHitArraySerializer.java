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
import com.arc.seecode.engine.WatchpointHit;


/**
 * Serializer for array of {@link Variable}.
 * @author David Pickens
 */
class WatchpointHitArraySerializer implements ISerializer{
    private WatchpointHitArraySerializer(){}
    private static ISerializer sInstance = new WatchpointHitArraySerializer();
    public static ISerializer getInstance() {
        return sInstance;
    }
    
    private static final int MAGIC = 0x34455667; //arbitrary magic number
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
           WatchpointHit[] array = (WatchpointHit[])v;
           output.writeInt(array.length);
           for (int i = 0; i < array.length;i++){
               WatchpointHitSerializer.getInstance().serialize(array[i],output);
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
        WatchpointHit array[] = new WatchpointHit[dimension];
        for (int i = 0; i < array.length; i++){
            array[i] = (WatchpointHit)WatchpointHitSerializer.getInstance().deserialize(input);
        }
        return array;
    }

}
