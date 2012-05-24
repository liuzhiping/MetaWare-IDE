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
import com.arc.seecode.engine.Value;
import com.arc.seecode.engine.type.IType;


/**
 * Serializes a {@link Value} object.
 * @author David Pickens
 */
class ValueSerializer implements ISerializer {


    private ISerializer mTypeSerializer;

    ValueSerializer(JavaFactory f, ISerializer typeSer){
        mFactory = f;
        mTypeSerializer = typeSer;
        }
    
    private JavaFactory mFactory;
    
 
    private static final int MAGIC = 0x11011102; // arbitrary magic number

    /* (non-Javadoc)
     * @see com.arc.seecode.rmi.ISerializer#serialize(java.lang.Object, java.io.DataOutputStream)
     */
    @Override
    public void serialize(Object v, DataOutputStream output) throws IOException {
        output.writeInt(MAGIC);
       if (v == null) {
           output.writeBoolean(true);
       }
       else if (v instanceof Value){
           Value val = (Value)v;
           output.writeBoolean(false);
           StringSerializer.getInstance().serialize(val.getValue(),output);
           output.writeLong(val.getAddress());
           output.writeInt(val.getElementCount());
//           //TO BEREMOVED
//           if (val.getCookie() != 0){
//               System.out.println("Serialize value with cookie 0x" + Integer.toHexString(val.getCookie()));
//           }
           output.writeInt(val.getCookie());
           StringSerializer.getInstance().serialize(val.getFieldName(),output);
           mTypeSerializer.serialize(val.getType(),output);
       }
       else throw new IllegalArgumentException("argument isn't a Value");

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.rmi.ISerializer#deserialize(java.io.DataInputStream)
     */
    @Override
    public Object deserialize(DataInputStream input) throws IOException {
//        if (sFactory == null)
//            throw new IllegalStateException("ValueSerialize needs JavaFactory set!");
        if (input.readInt() != MAGIC)
            throw new IOException("Stream does not contain Value object");
        boolean isNull = input.readBoolean();
        if (isNull)
            return null;
        String value = (String)StringSerializer.getInstance().deserialize(input);
        long addr = input.readLong();
        int elementCount = input.readInt();
        int cookie = input.readInt();
//        //TO BEREMOVED
//        if (cookie != 0){
//            System.out.println("Deserialize value with cookie 0x" + Integer.toHexString(cookie));
//        }
        String fieldName = (String)StringSerializer.getInstance().deserialize(input);
        IType type = (IType)mTypeSerializer.deserialize(input);
        

        Value v = mFactory!=null?mFactory.newValue():new Value(null);
        v.setSimpleValue(value);
        v.setAddress(addr);
        v.setElements(cookie,elementCount);
        v.setFieldName(fieldName);
        v.setType(type);
        return v;        
    }
}
