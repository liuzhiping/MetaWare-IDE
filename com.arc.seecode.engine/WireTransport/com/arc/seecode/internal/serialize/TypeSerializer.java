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

import com.arc.seecode.engine.type.IType;
import com.arc.seecode.engine.type.ITypeFactory;
import com.arc.seecode.engine.type.defaults.TypeFactory;

/**
 * Handles the serialization of the "IType" object.
 * 
 * @author David Pickens
 */
class TypeSerializer implements ISerializer {

    private ITypeFactory mFactory;

    TypeSerializer(ITypeFactory f) {
        mFactory = f;
        if (f == null) mFactory = new TypeFactory();
    }

    private static final int MAGIC = 0x1746E; // arbitrary magic number

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.serialize.ISerializer#serialize(java.lang.Object,
     *      java.io.DataOutputStream)
     */
    @Override
    public void serialize(Object v, DataOutputStream output) throws IOException {
        output.writeInt(MAGIC);
        if (v == null) {
            output.writeBoolean(true);
        } else {
            IType t = (IType) v;
            output.writeBoolean(false);
            output.writeByte(t.getKind());
            StringSerializer.getInstance().serialize(t.getName(), output);
            switch (t.getKind()) {
            case IType.ARRAY:
                //TO BEREMOVED
                //System.out.println("Serializing " + t); System.out.flush();
                if (t.getBaseType() == null)
                        throw new IllegalStateException("Bad array type");
                serialize(t.getBaseType(), output);
                output.writeInt(t.getDimension());
                break;
            case IType.POINTER:
            case IType.REF:
                output.writeInt(t.getSize());
                serialize(t.getBaseType(), output);
                break;
            case IType.VOID:
                break;
            case IType.ENUM:
            //TO BEREMOVED
            //System.out.println("Serializing enum " + t); System.out.flush();
            /* FALLTHRU */
            case IType.CLASS:
            case IType.STRUCT:
            case IType.UNION:

            case IType.FLOAT:
                output.writeInt(t.getSize());
                break;
            case IType.INTEGER:
                output.writeInt(t.getSize());
                output.writeBoolean(t.isUnsigned());
                break;
            default:
                throw new IllegalArgumentException("Bad type to serialize: "
                        + t);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.serialize.ISerializer#deserialize(java.io.DataInputStream)
     */
    @Override
    public Object deserialize(DataInputStream input) throws IOException {
        if (input.readInt() != MAGIC)
                throw new IOException("Stream for IType object is corrupted");
        boolean isNull = input.readBoolean();
        if (isNull) { return null; }
        IType t = null;
        int kind = input.readByte();
        String name = (String) StringSerializer.getInstance()
                .deserialize(input);
        switch (kind) {
        case IType.INTEGER: {
            int size = input.readInt();
            boolean isUnsigned = input.readBoolean();
            t = mFactory.createInteger(name, size, isUnsigned);
            break;
        }
        case IType.ARRAY: {
            IType base = (IType) deserialize(input);
            int dimension = input.readInt();
            //TO BEREMOVED
            //System.out.println("Deerializing an array[" + dimension+"] of " +
            // base);
            //System.out.flush();

            t = mFactory.createArray(name, base, dimension);
            break;
        }
        case IType.CLASS: {
            int size = input.readInt();
            t = mFactory.createClass(name, size);
            break;
        }
        case IType.STRUCT: {
            int size = input.readInt();
            t = mFactory.createStruct(name, size);
            break;
        }
        case IType.UNION: {
            int size = input.readInt();
            t = mFactory.createStruct(name, size);
            break;
        }
        case IType.ENUM: {
            //TO BEREMOVED
            //System.out.println("Deserializing enum " + name);
            // System.out.flush();
            int size = input.readInt();
            t = mFactory.createEnum(name, size);
            break;
        }
        case IType.FLOAT: {
            int size = input.readInt();
            t = mFactory.createFloatingPoint(name, size);
            break;
        }
        case IType.VOID:
            t = mFactory.createVoidType(name);
            break;
        case IType.POINTER: {
            int size = input.readInt();
            IType base = (IType) deserialize(input);
            t = mFactory.createPointer(name, base, size);
            break;
        }
        case IType.REF: {
            int size = input.readInt();
            IType base = (IType) deserialize(input);
            t = mFactory.createRef(name, base, size);
            break;

        }
        default:
            throw new IllegalStateException(
                    "Type is corrupted within serialization stream");
        }
        return t;
    }
}
