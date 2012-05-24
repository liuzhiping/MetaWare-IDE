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
class LocationSerializer implements ISerializer {
    private LocationSerializer(){}
    
    private static ISerializer sInstance = new LocationSerializer();
    
    public static ISerializer getInstance(){
        return sInstance;
    }
    
    private static final int MAGIC = 0x10CA7103; // arbitrary magic number

    /* (non-Javadoc)
     * @see com.arc.seecode.rmi.ISerializer#serialize(java.lang.Object, java.io.DataOutputStream)
     */
    @Override
    public void serialize(Object v, DataOutputStream output) throws IOException {
        output.writeInt(MAGIC);
       if (v == null) {
           output.writeBoolean(true);
       }
       else if (v instanceof Location){
           Location loc = (Location)v;
           output.writeBoolean(false);
           output.writeLong(loc.getAddress());
           StringSerializer.getInstance().serialize(loc.getFunction(),output);
           output.writeInt(loc.getFunctionOffset());
           StringSerializer.getInstance().serialize(loc.getSource(),output);
           output.writeInt(loc.getSourceLine());
           output.writeInt(loc.getSourceLineOffset());
           output.writeInt(loc.getModule());
           output.writeLong(loc.getLogicalAddress());
           output.writeBoolean(loc.isAmbiguous());
       }
       else throw new IllegalArgumentException("Argument isn't Location");

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.rmi.ISerializer#deserialize(java.io.DataInputStream)
     */
    @Override
    public Object deserialize(DataInputStream input) throws IOException {
        if (input.readInt() != MAGIC)
            throw new IOException("Stream does not contain Location object");
        boolean isNull = input.readBoolean();
        if (isNull)
            return null;
        long addr = input.readLong();
        String function = (String)StringSerializer.getInstance().deserialize(input);
        int funcOffset = input.readInt();
        String source = (String)StringSerializer.getInstance().deserialize(input);
        int line = input.readInt();
        int lineOffset = input.readInt();
        int module = input.readInt();
        long laddr = input.readLong();
        boolean ambiguous = input.readBoolean();
        Location loc = new Location();
        if (function != null && function.length() > 0)
            loc.setFunction(function,funcOffset);
        if (source != null && source.length() > 0) {
            loc.setSource(source);
            loc.setSourceLine(line,lineOffset);
        }
        if (module != 0){
            loc.setLogicalAddress(module,laddr,addr);
        }
        else loc.setAddress(addr);
        loc.setAmbiguous(ambiguous);
        return loc;        
    }

}
