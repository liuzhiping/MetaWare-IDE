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

import com.arc.seecode.engine.AssemblyRecord;


/**
 * Serializer for {@link AssemblyRecord}.
 * @author David Pickens
 */
class AssemblyRecordSerializer implements ISerializer {
    private AssemblyRecordSerializer(){}
    
    private static ISerializer sInstance = new AssemblyRecordSerializer();
    
    public static ISerializer getInstance(){
        return sInstance;
    }
    
    private static final int MAGIC = 0xaabbccdd; // arbitrary magic number
    /* (non-Javadoc)
     * @see com.arc.seecode.serialize.ISerializer#serialize(java.lang.Object, java.io.DataOutputStream)
     */
    @Override
    public void serialize(Object v, DataOutputStream output) throws IOException {
        output.writeInt(MAGIC);
        if (v == null){
            output.writeBoolean(true);
        }
        else 
        if (v instanceof AssemblyRecord){
            AssemblyRecord a = (AssemblyRecord)v;
            output.writeBoolean(false);
            output.writeLong(a.getAddress());
            ISerializer s = StringSerializer.getInstance();
            s.serialize(a.getOpcode(),output);
            s.serialize(a.getOperands(),output);
            s.serialize(a.getHex(),output);
            s.serialize(a.getComment(),output);
            
        }
        else throw new IllegalArgumentException("Not an assembly record");
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.serialize.ISerializer#deserialize(java.io.DataInputStream)
     */
    @Override
    public Object deserialize(DataInputStream input) throws IOException {
        if (input.readInt() != MAGIC)
            throw new IOException("Not an AssemblyRecord stream");
        if (input.readBoolean()){
            return null;
        }
        AssemblyRecord a = new AssemblyRecord();
        a.setAddress(input.readLong());
        ISerializer s = StringSerializer.getInstance();
        a.setOpcode((String)s.deserialize(input));
        a.setOperands((String)s.deserialize(input));
        a.setHex((String)s.deserialize(input));
        a.setComment((String)s.deserialize(input));
        return a;        
    }

}
