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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import com.arc.seecode.engine.IBreakpointObserver;
import com.arc.seecode.engine.ICustomDisplayCallback;
import com.arc.seecode.engine.IEngineAPI;
import com.arc.seecode.engine.IEngineAPIObserver;

/**
 * A command-type packet. This code was adapted from JdwpCommandPacket.
 * 
 * @author David Pickens
 */
public class ScwpCommandPacket extends ScwpPacket {

    /**
     *  
     */
    public ScwpCommandPacket() {

    }

    /** Mapping of command codes to strings. */
    private static Map<Integer,String> fgCommandMap = null;

    /** Next id to be assigned. */
    private static int fgNextId = 1;

    
    /**
     * Which object the method is to be applied to:
     * 
     */
    private int fObject;
    /**
     * Method being invoked.
     */
    private String fMethodName;
    /**
     * We preserved 0 to denote the CMPD channel.
     * The first engine interface starts at 1.
     */
    public static final int CMPD = 0;
    /**
     * Object ID to denote a method call channel relative to
     * the {@link IEngineAPI} object.
     */
    public static final int ENGINE = 0;
    
    /**
     * Object ID to denote a method call channel relative to
     * the {@link IEngineAPIObserver} object.
     */
    public static final int ENGINE_OBSERVER = 1;
    
    /**
     * Object ID to denote a method call channel relative to
     * the {@link IBreakpointObserver} object.
     */
    public static final int BREAKPOINT_OBSERVER = 2;
    
    /**
     * Object ID to denote a method call channel relative to
     * the {@link ICustomDisplayCallback} object.
     */
    public static final int CUSTOM_DISPLAY_MONITOR = 3;
    
    /**
     * Total number of channels per target connection.
     */
    public static final int REQUIRED_CHANNELS = 4;

    /**
     * Creates new ScwpCommandPacket.
     * @param object one of {@link #ENGINE},
     * {@link #ENGINE_OBSERVER}, {@link #BREAKPOINT_OBSERVER},
     * {@link #CUSTOM_DISPLAY_MONITOR}.
     * @param methodName the method name to invoke.
     */
    public ScwpCommandPacket(int object, String methodName) {
        setObject(object);
        setMethodName(methodName);
        setId(getNewId());
    }

    /**
     * @return Returns unique id for command packet.
     */
    public static synchronized int getNewId() {
        return fgNextId++;
    }


    /**
     * @return Return return the command.
     */
    public int getObject() {
        return fObject;
    }
    /**
     * Return the method name to be invoked.
     * @return the method name to be invoked.
     */
    public String getMethodName(){
        return fMethodName;
    }

    /**
     * Assigns object to which this applies
     */
    public void setObject(int object) {
        fObject = object;
    }
    
    public void setMethodName(String name){
        fMethodName = name;
    }

    /**
     * Reads header fields that are specific for this type of packet.
     */
    @Override
    protected void readSpecificHeaderFields(DataInputStream dataInStream)
            throws IOException {
        fObject = dataInStream.readInt();
        fMethodName = dataInStream.readUTF();
    }

    /**
     * Writes header fields that are specific for this type of packet.
     */
    @Override
    protected void writeSpecificHeaderFields(DataOutputStream dataOutStream)
            throws IOException {
        dataOutStream.writeInt(fObject);
        dataOutStream.writeUTF(fMethodName);
    }
    
    @Override
    protected int getSpecificHeaderFieldsLength() {
        // UTF length is 2-byte length followed by 
        return 1 + (2+fMethodName.length());
    }

    /**
     * Retrieves constant mappings.
     */
    public static void getConstantMaps() {
        if (fgCommandMap != null) { return; }

        Field[] fields = ScwpCommandPacket.class.getDeclaredFields();

 

        // Get the commands.
        fgCommandMap = new HashMap<Integer,String>();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if ((field.getModifiers() & Modifier.PUBLIC) == 0
                    || (field.getModifiers() & Modifier.STATIC) == 0
                    || (field.getModifiers() & Modifier.FINAL) == 0) {
                continue;
            }

            try {
                String name = field.getName();

                // If it is a set, continue.
                if (name.startsWith("CMD_")) { //$NON-NLS-1$
                    continue;
                }
                Integer val = (Integer) field.get(null);
                String entryName = removePrefix(name); 
                fgCommandMap.put(val, entryName);

            } catch (IllegalAccessException e) {
                // Will not occur for own class.
            }
        }
    }

    /**
     * @return Returns a map with string representations of error codes.
     */
    public static Map<Integer,String> commandMap() {
        getConstantMaps();
        return fgCommandMap;
    }

    /**
     * @return Returns string without XXX_ prefix.
     */
    public static String removePrefix(String str) {
        int i = str.indexOf('_');
        if (i < 0) {
            return str;
        }
		return str.substring(i + 1);
    }
    
    @Override
    public String toString(){
        return "command(obj=" + fObject/4 + "*4+"+fObject%4+",ID=" + getId() + ",method=\"" + getMethodName()+"\")";
    }



}
