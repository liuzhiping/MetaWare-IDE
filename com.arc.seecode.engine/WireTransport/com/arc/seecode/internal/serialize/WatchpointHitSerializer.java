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

import com.arc.seecode.engine.JavaFactory;
import com.arc.seecode.engine.WatchpointHit;


/**
 * Serializes a {@link WatchpointHit} object.
 * NOTE: to deserialize, we must have the {@link #setFactory(JavaFactory)
 * JavaFactory} object set so that we can instantiate the Variable.
 * @author David Pickens
 */
class WatchpointHitSerializer implements ISerializer {

    private WatchpointHitSerializer(){}
    
    private static ISerializer sInstance = new WatchpointHitSerializer();
    
    public static ISerializer getInstance(){
        return sInstance;
    }
    
    public static void setFactory(JavaFactory f){
        sFactory = f;
    }
    
    private static JavaFactory sFactory;
    
    private static final int MAGIC = 0x80707050; // arbitrary magic number

    /* (non-Javadoc)
     * @see com.arc.seecode.rmi.ISerializer#serialize(java.lang.Object, java.io.DataOutputStream)
     */
    @Override
    public void serialize(Object v, DataOutputStream output) throws IOException {
        output.writeInt(MAGIC);
       if (v == null) {
           output.writeBoolean(true);
       }
       else if (v instanceof WatchpointHit){
           WatchpointHit wp = (WatchpointHit)v;
           output.writeBoolean(false);
           output.writeInt(wp.getWatchpointID());
           output.writeLong(wp.getAddress());
           ISerializer s = StringSerializer.getInstance();
           s.serialize(wp.getOldValue(),output);
           s.serialize(wp.getNewValue(),output);
       }
       else throw new IllegalArgumentException("argument isn't a WatchpointHit");

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.rmi.ISerializer#deserialize(java.io.DataInputStream)
     */
    @Override
    public Object deserialize(DataInputStream input) throws IOException {
//        if (sFactory == null)
//            throw new IllegalStateException("VariableSerialize needs JavaFactory set!");
        if (input.readInt() != MAGIC)
            throw new IOException("Stream does not contain WatchpointHit object");
        boolean isNull = input.readBoolean();
        if (isNull)
            return null;
        int id = input.readInt();
        long addr = input.readLong();
        String oldValue = (String)StringSerializer.getInstance().deserialize(input);
        String newValue = (String)StringSerializer.getInstance().deserialize(input);

        WatchpointHit w = sFactory!=null?sFactory.newWatchpointHit():new WatchpointHit();
        w.setWatchpointID(id);
        w.setAddress(addr);
        w.setOldValue(oldValue);
        w.setNewValue(newValue);
        return w;        
    }
}
