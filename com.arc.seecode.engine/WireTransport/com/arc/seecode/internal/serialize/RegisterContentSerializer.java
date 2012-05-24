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

import com.arc.seecode.engine.RegisterContent;


/**
 * @author David Pickens
 */
class RegisterContentSerializer implements ISerializer {

    private RegisterContentSerializer() {
    }

    private static ISerializer sInstance = new RegisterContentSerializer();

    public static ISerializer getInstance () {
        return sInstance;
    }

    private static final int MAGIC = 0x10CA7104; // arbitrary magic number

    /*
     * (non-Javadoc)
     * @see com.arc.seecode.rmi.ISerializer#serialize(java.lang.Object, java.io.DataOutputStream)
     */
    @Override
    public void serialize (Object v, DataOutputStream output) throws IOException {
        output.writeInt(MAGIC);
        if (v == null) {
            output.writeByte(0);
        }
        else if (v instanceof RegisterContent) {
            RegisterContent r = (RegisterContent) v;
            if (r.isSpecial()) {
                output.writeByte(3);
                output.writeShort(r.getRegister());
                int len = r.getLength();
                output.writeShort(len);
                StringSerializer.getInstance().serialize(r.getSpecialValue(), output);
            }
            else {
                int b = r.isValid()?(r.isScalar()?1:2):4;
                output.writeByte(b);
                output.writeShort(r.getRegister());
                int len = r.getLength();
                output.writeShort(len);
                if (!r.isValid()){
                    //Write nothing
                }
                else
                if (r.isScalar()) {
                    if (len <= 4) {
                        output.writeInt((int) r.getValue());
                    }
                    else
                        output.writeLong(r.getValue());
                }
                else {
                    Object c = r.getValueAsObject();
                    output.writeByte(r.getUnitSize());
                    switch (r.getUnitSize()) {
                        case 1: {
                            byte bytes[] = (byte[]) c;
                            output.write(bytes);
                            break;
                        }
                        case 2: {
                            short shorts[] = (short[]) c;
                            if (shorts.length * 2 != len)
                                throw new IllegalStateException("Length wrong");
                            for (short s : shorts) {
                                output.writeShort(s);
                            }
                            break;
                        }
                        case 4: {
                            int ints[] = (int[]) c;
                            if (ints.length * 4 != len)
                                throw new IllegalStateException("Length wrong");
                            for (int i : ints) {
                                output.writeInt(i);
                            }
                            break;
                        }
                        case 8: {
                            long longs[] = (long[]) c;
                            if (longs.length * 8 != len)
                                throw new IllegalStateException("Length wrong");
                            for (long l : longs) {
                                output.writeLong(l);
                            }
                            break;
                        }
                        default:
                            throw new IllegalStateException("Not valid unit size");
                    }
                }
            }
        }
        else
            throw new IllegalArgumentException("Argument isn't RegisterContent");

    }

    /*
     * (non-Javadoc)
     * @see com.arc.seecode.rmi.ISerializer#deserialize(java.io.DataInputStream)
     */
    @Override
    public Object deserialize (DataInputStream input) throws IOException {
        if (input.readInt() != MAGIC)
            throw new IOException("Stream does not contain RegisterContent object");
        int kind = input.readByte();
        if (kind == 0)
            return null;
        int regID = input.readShort();
        int len = input.readShort();
        switch (kind) {
            case 1: { // Simple scalar that is 8 bytes or less?
                long v;
                if (len <= 4)
                    v = input.readInt();
                else
                    v = input.readLong();
                return new RegisterContent(regID, v, len);
            }
            case 2: { // Aggregate
                int unitSize = input.readByte();
                switch (unitSize) {
                    case 1: {
                        byte[] buf = new byte[len]; 
                        input.read(buf);
                        return new RegisterContent(regID,buf);
                    }
                    case 2: {
                        short[] buf = new short[len/2];
                        for (int i = 0; i < buf.length; i++){
                            buf[i] = input.readShort();
                        }
                        return new RegisterContent(regID,buf);
                    }
                    case 4: {
                        int[] buf = new int[len/4];
                        for (int i = 0; i < buf.length; i++){
                            buf[i] = input.readInt();
                        }
                        return new RegisterContent(regID,buf);
                    }
                    case 8: {
                        long[] buf = new long[len/8];
                        for (int i = 0; i < buf.length; i++){
                            buf[i] = input.readLong();
                        }
                        return new RegisterContent(regID,buf);
                    }
                    default: throw new IllegalStateException("Bad unit size on reg content: " + unitSize);
                }
            }
            case 3: { // special
                String s = (String)StringSerializer.getInstance().deserialize(input);
                return new RegisterContent(regID,s);
            }
            case 4: { // invalid register
                RegisterContent r = new RegisterContent();
                r.setInvalid(regID,len);
                return r;
            }
            default:
                throw new IllegalStateException("Not valid kind: " + kind);
        }

    }

}
