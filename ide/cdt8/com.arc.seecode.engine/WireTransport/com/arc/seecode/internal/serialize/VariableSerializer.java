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
import com.arc.seecode.engine.Variable;
import com.arc.seecode.engine.type.IType;


/**
 * Serializes a {@link Variable} object.
 * @author David Pickens
 */
class VariableSerializer implements ISerializer {
    private ISerializer mValueSerializer;

    private ISerializer mTypeSerializer;

    /**
     * 
     * @param f Factory for creating {@link Variable} instances.
     * @param valueSerializer serializer for {@link Value} objects.
     * @param typeSerializer serializer for {@link IType} objects.
     */
    VariableSerializer(JavaFactory f, ISerializer valueSerializer, ISerializer typeSerializer){
        mFactory = f;
        mValueSerializer = valueSerializer;
        mTypeSerializer = typeSerializer;
        }
    
    private JavaFactory mFactory;
    
    private static final int MAGIC = 0x88778877; // arbitrary magic number

    /* (non-Javadoc)
     * @see com.arc.seecode.rmi.ISerializer#serialize(java.lang.Object, java.io.DataOutputStream)
     */
    @Override
    public void serialize(Object v, DataOutputStream output) throws IOException {
        output.writeInt(MAGIC);
       if (v == null) {
           output.writeBoolean(true);
       }
       else if (v instanceof Variable){
           Variable var = (Variable)v;
           output.writeBoolean(false);
           ISerializer s = StringSerializer.getInstance();
           s.serialize(var.getName(),output);
           s.serialize(var.getActualName(),output);
           output.writeByte(var.getKind());
           output.writeLong(var.getAddress());
           mValueSerializer.serialize(var.getValue(),output);
           mTypeSerializer.serialize(var.getType(),output);
           output.writeBoolean(var.isActive());
           output.writeInt(var.getRegister());
           output.writeInt(var.getModule());
           output.writeLong(var.getLogicalAddress());
       }
       else throw new IllegalArgumentException("argument isn't a Variable");

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.rmi.ISerializer#deserialize(java.io.DataInputStream)
     */
    @Override
    public Object deserialize(DataInputStream input) throws IOException {
//        if (sFactory == null)
//            throw new IllegalStateException("VariableSerialize needs JavaFactory set!");
        if (input.readInt() != MAGIC)
            throw new IOException("Stream does not contain Variable object");
        boolean isNull = input.readBoolean();
        if (isNull)
            return null;
        String name = (String)StringSerializer.getInstance().deserialize(input);
        String actualName = (String)StringSerializer.getInstance().deserialize(input);
        int kind = input.readByte();
        long addr = input.readLong();
        Value value = (Value)mValueSerializer.deserialize(input);
        IType type = (IType)mTypeSerializer.deserialize(input);
        boolean isActive = input.readBoolean();
        int register = input.readInt();
        int module = input.readInt();
        long laddr = input.readLong();
        Variable v = mFactory!=null?mFactory.newVariable():new Variable();
        v.setName(name);
        v.setActualName(actualName);
        v.setKind(kind);
        v.setValue(value);
        v.setType(type);
        v.setActive(isActive);
        v.setRegister(register);
        if (module != 0){
            v.setLogicalAddress(module,laddr,addr);
        }
        else
            v.setAddress(addr);
        return v;        
    }
}
