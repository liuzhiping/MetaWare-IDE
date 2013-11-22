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

import com.arc.seecode.engine.Location;


/**
 * @author David Pickens
 */
class LocationArraySerializer implements ISerializer {
    private LocationArraySerializer(){}
    private static ISerializer sInstance = new LocationArraySerializer();
    public static ISerializer getInstance() {
        return sInstance;
    }
    
    private static final int MAGIC = 0x12121212; //arbitrary magic number
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
           Location[] array = (Location[])v;
           output.writeInt(array.length);
           for (int i = 0; i < array.length;i++){
               LocationSerializer.getInstance().serialize(array[i],output);
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
        Location array[] = new Location[dimension];
        for (int i = 0; i < array.length; i++){
            array[i] = (Location)LocationSerializer.getInstance().deserialize(input);
        }
        return array;
    }

}
