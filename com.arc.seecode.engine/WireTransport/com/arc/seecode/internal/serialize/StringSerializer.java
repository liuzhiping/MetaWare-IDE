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
import java.io.UTFDataFormatException;


/**
 * Extract a serialized representation of a string.
 * 
 * @author David Pickens
 */
class StringSerializer implements ISerializer {
    private StringSerializer(){}
    private static ISerializer sInstance = new StringSerializer();
    public static ISerializer getInstance() {
        return sInstance;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.rmi.IArgExtractor#extractArgument(java.io.DataInputStream)
     */
    @Override
    public Object deserialize(DataInputStream input) throws IOException {
        boolean isNull = input.readBoolean();
        if (isNull) return null;
        // UTF strings cannot exceed 64K
        // If we have a string that is longer, then
        // we resort to breaking it up into
        // parts. A boolean appears after each part
        // to indicate that an additional part
        // continues.
        String s = input.readUTF();
        boolean isContinued = input.readBoolean();
        if (isContinued){
            // Very long string; recontruct
            StringBuffer buf = new StringBuffer(200000);
            buf.append(s);
            while (isContinued){
                buf.append(input.readUTF());
                isContinued = input.readBoolean();
            }
            return buf.toString();
        }
        return s;
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.rmi.ISerializer#serialize(java.lang.Object,
     *      java.io.DataOutputStream)
     */
    @Override
    public void serialize(Object v, DataOutputStream output) throws IOException {
        if (v == null)
            output.writeBoolean(true);
        else {
            output.writeBoolean(false);
            if (!(v instanceof String))
                    throw new IllegalArgumentException("Must be string: " + v);
            // The "writeUTF" method limits the strings to 64K bytes!!!
            // So if it likely exceeds 64K, then we break it up
            String s = (String)v;
            try {
                output.writeUTF(s);
                output.writeBoolean(false); // end
            }
            catch(UTFDataFormatException x){
                // Must break things up into substrings
                // A substring of 20000 should be safe since
                // it couldn't exceed 60000 as a UTF string
                int len = s.length();
                final int PART_SIZE = 20000;
                for (int i = 0; i < len; i+=PART_SIZE){
                    if (i > 0) output.writeBoolean(true);
                    String substring = s.substring(i,Math.min(i+PART_SIZE,len));
                    output.writeUTF(substring);
                }
                output.writeBoolean(false);
            }
        }
    }
}
