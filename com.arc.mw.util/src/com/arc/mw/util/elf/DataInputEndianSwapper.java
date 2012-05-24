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
package com.arc.mw.util.elf;

import java.io.DataInput;
import java.io.IOException;

/**
 * Wraps a DataInput object and swaps endian.
 * @author David Pickens
 */
public class DataInputEndianSwapper implements DataInput {
    private DataInput input;
    public DataInputEndianSwapper(DataInput input){
        this.input = input;
    }
    @Override
    public void readFully(byte[] b) throws IOException {
        input.readFully(b);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        input.readFully(b,off,len);

    }

    @Override
    public int skipBytes(int n) throws IOException {
        return input.skipBytes(n);
    }

    @Override
    public boolean readBoolean() throws IOException {
        return input.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return input.readByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return input.readUnsignedByte();
    }

    @Override
    public short readShort() throws IOException {
        return (short)ByteSwapper.swapShort(input.readShort());
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return ByteSwapper.swapUShort(input.readUnsignedShort());
    }

    @Override
    public char readChar() throws IOException {
        return input.readChar();
    }

    @Override
    public int readInt() throws IOException {
        return ByteSwapper.swapInt(input.readInt());
    }

    @Override
    public long readLong() throws IOException {
        return ByteSwapper.swapLong(input.readLong());
    }

    @Override
    public float readFloat() throws IOException {
        int i = readInt();
        return Float.intBitsToFloat(i);
    }

    @Override
    public double readDouble() throws IOException {
        long l = readLong();
        return Double.longBitsToDouble(l);
    }

    @Override
    public String readLine() throws IOException {
        return input.readLine();
    }

    @Override
    public String readUTF() throws IOException {
        return input.readUTF();
    }

}
