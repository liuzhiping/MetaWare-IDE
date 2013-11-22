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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.arc.mw.util.Log;
import com.arc.mw.util.Toggle;
import com.arc.seecode.serialize.IMethodSerializer;

/**
 * Class for invoking a method when its arguments need to be deserialized from a
 * datastream. The result, if any, must be reserialized.
 * <P>
 * Also used to serialize the arguments of a call to the method.
 * 
 * @author David Pickens
 */
public class MethodSerializer implements IMethodSerializer {
    
    private static final Toggle sTrace = Toggle.define("SERIALIZER",false);

    private ISerializer mArgExtractors[];

    private ISerializer mResultMaker;

    private Method mMethod;

    private Object mObject;
    
    /**
     *  
     */
    public MethodSerializer(Object object, Method method, SerializerFactory serializerFactory) {
        mMethod = method;
        mObject = object;
        Class<?>[] parms = method.getParameterTypes();
        mArgExtractors = new ISerializer[parms.length];
        for (int i = 0; i < parms.length; i++) {
            mArgExtractors[i] = serializerFactory.findSerializer(parms[i]);
        }
        mResultMaker = serializerFactory.findSerializer(method.getReturnType());
    }
       
    @Override
    public String getName(){
        return mMethod.getName();
    }
    
    @Override
    public boolean isAsynchronous(){
        return mResultMaker instanceof VoidSerializer;       
    }


    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param input
     * @param output
     * @throws Throwable
     */
    @Override
    public void invoke(DataInputStream input, DataOutputStream output) throws Throwable{
        Object[] args = new Object[mArgExtractors.length];
        try {
            for (int i = 0; i < args.length; i++) {
                args[i] = mArgExtractors[i].deserialize(input);
            }
            if (isTracing()) {
                StringBuffer buf = new StringBuffer();
                buf.append("Server invoking  " + mMethod.getName() + "(");
                for (int i = 0; i < args.length; i++) {
                    if (i > 0) buf.append(',');
                    buf.append(args[i]);
                }
               buf.append(")");
               trace(buf.toString());
            }
            Object result = mMethod.invoke(mObject, args);
            if (isTracing()){
                trace("...server result is " + result);
            }
            mResultMaker.serialize(result, output);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (IOException e) {
            throw e;
        }

    }

    @Override
    public byte[] serializeArguments(Object[] args) {
        if (args.length != mArgExtractors.length) { throw new IllegalArgumentException(
                "Args don't match"); }
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(ba);
        try {
            for (int i = 0; i < args.length; i++) {
                mArgExtractors[i].serialize(args[i], out);
            }
            out.flush();
        } catch (IOException e) {
            //Shouldn't happen
            e.printStackTrace();
        }
        return ba.toByteArray();
    }
    
    @Override
    public Object deserializeResult(DataInputStream input) throws IOException{
        return mResultMaker.deserialize(input);
    }
    
    private static boolean isTracing(){
        return sTrace.on();
    }
    
    private static void trace(String msg){
        if (isTracing()){
            Log.log("SERIALIZE",msg);
        }
    }

}
