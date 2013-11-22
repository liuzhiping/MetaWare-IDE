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

import java.io.DataOutput;
import java.io.IOException;

/**
 * A wrapper around a DataOutput that swaps endian.
 * @author David Pickens
 */
public class DataOutputEndianSwapper implements DataOutput {
    private DataOutput output;
    public DataOutputEndianSwapper(DataOutput output){
        this.output = output;
    }
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        output.write(b, off, len);
    }
    @Override
    public void write(byte[] b) throws IOException {
        output.write(b);
    }
    @Override
    public void write(int b) throws IOException {
        output.write(b);
    }
    @Override
    public void writeBoolean(boolean v) throws IOException {
        output.writeBoolean(v);
    }
    @Override
    public void writeByte(int v) throws IOException {
        output.writeByte(v);
    }
    @Override
    public void writeBytes(String s) throws IOException {
        output.writeBytes(s);
    }
    @Override
    public void writeChar(int v) throws IOException {
        output.writeChar(v);
    }
    @Override
    public void writeChars(String s) throws IOException {
        output.writeChars(s);
    }
    @Override
    public void writeDouble(double v) throws IOException {
        output.writeLong(Double.doubleToLongBits(v));
    }
    @Override
    public void writeFloat(float v) throws IOException {
        output.writeInt(Float.floatToIntBits(v));
    }
    @Override
    public void writeInt(int v) throws IOException {
        output.writeInt(ByteSwapper.swapInt(v));
    }
    @Override
    public void writeLong(long v) throws IOException {
        output.writeLong(ByteSwapper.swapLong(v));
    }
    @Override
    public void writeShort(int v) throws IOException {
        output.writeShort(ByteSwapper.swapShort(v));
    }
    @Override
    public void writeUTF(String str) throws IOException {
        output.writeUTF(str);
    }

}
