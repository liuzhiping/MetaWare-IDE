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
package com.arc.seecode.scwp;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Base class of the two kinds of packets: command and reply. Patterned after
 * Java's JDWP packets.
 * 
 * @author David Pickens
 */
public abstract class ScwpPacket {

    /**
     *  
     */
    protected ScwpPacket() {
    }

    /** General SCWP constants. */
    public static final byte FLAG_REPLY_PACKET = (byte) 0x80;

    protected static final int MIN_PACKET_LENGTH = 11;

    /** Map with Strings for flag bits. */
    private static String[] fgFlagStrings = null;

    /** Header fields. */
    protected int fId = 0;

    protected byte fFlags = 0;
    
    private boolean fWritten = false;

    protected byte[] fDataBuf = null;

    /**
     * Set Id.
     */
    /* package */void setId(int id) {
        fId = id;
    }

    /**
     * @return Returns Id.
     */
    public int getId() {
        return fId;
    }

    /**
     * Set Flags.
     */
    /* package */void setFlags(byte flags) {
        fFlags = flags;
    }

    /**
     * @return Returns Flags.
     */
    public byte getFlags() {
        return fFlags;
    }

    /**
     * @return Returns total length of packet.
     */
    public int getLength() {
        return getHeaderLength() + getDataLength() + 4/*size word*/;
    }

    /**
     * @return Returns length of data in packet.
     */
    public int getDataLength() {
        return fDataBuf == null ? 0 : fDataBuf.length;
    }

    /**
     * @return Returns data of packet.
     */
    public byte[] getData() {
        return fDataBuf;
    }

    /**
     * @return Returns DataInputStream with reply data, or an empty stream if
     *         there is none.
     */
    public DataInputStream dataInStream() {
        if (fDataBuf != null)
            return new DataInputStream(new ByteArrayInputStream(fDataBuf));
        else
            return new DataInputStream(new ByteArrayInputStream(new byte[0]));
    }

    /**
     * Assigns data to packet.
     */
    public void setData(byte[] data) {
        fDataBuf = data;
    }
    
    /**
     * Assuming that this is an outgoing packet that hasn't yet been written, replace the data.
     * Return true if the data was replaced before the packet was sent.
     * Returns false if the packet has already been sent.
     * @param data
     * @return false if the packet has already been sent; true if update was successful.
     */
    public synchronized boolean updateDataIfPossible(byte[] data){
        if (!fWritten){
            fDataBuf = data;
            return true;
        }
        return false;      
    }

    /**
     * Reads header fields that are specific for a type of packet.
     */
    protected abstract void readSpecificHeaderFields(
            DataInputStream dataInStream) throws IOException;

    /**
     * Writes header fields that are specific for a type of packet.
     */
    protected abstract void writeSpecificHeaderFields(
            DataOutputStream dataOutStream) throws IOException;

    /**
     * Reads complete packet.
     */
    public static ScwpPacket read(InputStream inStream) throws IOException {
        DataInputStream dataInStream = new DataInputStream(inStream);

        // Read header.
        int id = dataInStream.readInt();
        byte flags = dataInStream.readByte();

        // Determine type: command or reply.
        ScwpPacket packet;
        if ((flags & FLAG_REPLY_PACKET) != 0)
            packet = new ScwpReplyPacket(id);
        else
            packet = new ScwpCommandPacket();

        // Assign generic header fields.
        packet.setId(id);
        packet.setFlags(flags);

        // Read specific header fields and data.
        packet.readSpecificHeaderFields(dataInStream);
        int dataLen = dataInStream.readInt();
        if (dataLen > 0) {
            packet.fDataBuf = new byte[dataLen];
            dataInStream.readFully(packet.fDataBuf);
        }

        return packet;
    }

    /**
     * Writes complete packet.
     * Synchronized because the data can be updated in an out-going packet while it is
     * still enqueued. See {@link #updateDataIfPossible(byte[])}.
     */
    public synchronized void write(OutputStream outStream) throws IOException {
        fWritten = true;
        DataOutputStream dataOutStream = new DataOutputStream(outStream);

        writeHeader(dataOutStream);
        writeData(dataOutStream);
    }
    
    /**
     * Return whether or not this (out-going) packet has been sent. Returns false, if it
     * is (possibly) enqueued but not yet sent.
     * @return true if the packet has been written.
     */
    public boolean isSent(){
        return fWritten;
    }
    
    protected int getHeaderLength(){
        return 4+4+1+getSpecificHeaderFieldsLength();
    }
    abstract protected int getSpecificHeaderFieldsLength();

    /**
     * Writes header of packet.
     */
    protected void writeHeader(DataOutputStream dataOutStream)
            throws IOException {
        dataOutStream.writeInt(getId());
        dataOutStream.writeByte(getFlags());
        writeSpecificHeaderFields(dataOutStream);
    }

    /**
     * Writes data of packet.
     */
    protected void writeData(DataOutputStream dataOutStream) throws IOException {
        if (fDataBuf != null) {
            dataOutStream.writeInt(fDataBuf.length);
            dataOutStream.write(fDataBuf);
        }
        else dataOutStream.writeInt(0);
    }

    /**
     * Retrieves constant mappings.
     */
    public static void getConstantMaps() {
        if (fgFlagStrings != null) { return; }

        Field[] fields = ScwpPacket.class.getDeclaredFields();
        fgFlagStrings = new String[8];

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if ((field.getModifiers() & Modifier.PUBLIC) == 0
                    || (field.getModifiers() & Modifier.STATIC) == 0
                    || (field.getModifiers() & Modifier.FINAL) == 0) {
                continue;
            }

            String name = field.getName();
            if (!name.startsWith("FLAG_")) {//$NON-NLS-1$
                continue;
            }

            name = name.substring(5);

            try {
                byte value = field.getByte(null);

                for (int j = 0; j < fgFlagStrings.length; j++) {
                    if ((1 << j & value) != 0) {
                        fgFlagStrings[j] = name;
                        break;
                    }
                }
            } catch (IllegalAccessException e) {
                // Will not occur for own class.
            } catch (IllegalArgumentException e) {
                // Should not occur.
                // We should take care that all public static final constants
                // in this class are bytes.
            }
        }
    }

    /**
     * @return Returns a mapping with string representations of flags.
     */
    public static String[] getFlagMap() {
        getConstantMaps();
        return fgFlagStrings;
    }

}
