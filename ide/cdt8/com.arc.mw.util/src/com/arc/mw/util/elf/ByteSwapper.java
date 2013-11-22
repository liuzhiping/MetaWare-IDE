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


/**
 * Performs endian swapping on various scalar types.
 * This should eventually be moved to a more generic package.
 * @author David Pickens
 */
public class ByteSwapper {
    
    public static int swapShort(int i) {
        int byte0 = i & 0xFF;
        int byte1 = (i >> 8) & 0xFF;
        return (short)((byte0 << 8) | byte1);
    }
    
    public static int swapUShort(int i) {
        int byte0 = i & 0xFF;
        int byte1 = (i >> 8) & 0xFF;
        return (byte0 << 8) | byte1;
    }
    
    public static int swapInt(int i)  {
        int byte0 = i & 0xFF;
        int byte1 = (i >> 8) & 0xFF;
        int byte2 = (i >> 16) & 0xFF;
        int byte3 = (i >> 24) & 0xFF;
        return (byte0 << 24) + (byte1 << 16) + (byte2<<8) + byte3;
    }
    
    public static long swapLong(long i) {
        long byte0 = i & 0xFF;
        long byte1 = (i >> 8) & 0xFF;
        long byte2 = (i >> 16) & 0xFF;
        long byte3 = (i >> 24) & 0xFF;
        long byte4 = (i >> 32) & 0xFF;
        long byte5 = (i >> 40) & 0xFF;
        long byte6 = (i >> 48) & 0xFF;
        long byte7 = (i >> 56) & 0xFF;
        return (byte0 << 56) + (byte1 << 48) + (byte2<<40) + (byte3<<32) +
                (byte4<<24) + (byte5<<16) + (byte6<<8) + byte7;
    }

}
