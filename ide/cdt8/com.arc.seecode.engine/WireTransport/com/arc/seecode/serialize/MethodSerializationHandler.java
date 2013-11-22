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
package com.arc.seecode.serialize;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.arc.seecode.engine.JavaFactory;
import com.arc.seecode.engine.Location;
import com.arc.seecode.engine.Variable;
import com.arc.seecode.engine.type.IType;
import com.arc.seecode.engine.type.ITypeFactory;
import com.arc.seecode.internal.serialize.MethodSerializer;
import com.arc.seecode.internal.serialize.SerializerFactory;

/**
 * Given the name of a method within an object, and a byte stream, deserializes
 * the byte stream to form arguments, and invokes the method. The result, if
 * any, is returned as a serialize byte stream.
 * 
 * @author David Pickens
 */
public class MethodSerializationHandler {

    private Map<String,MethodSerializer> mMethodMap;

    /**
     * Construct on behalf of an object.
     * @param obj the object whose methods are being invoked.
     * @param iNterface the interface that "obj" implements that we're
     * interested in, or <code>null</code> if we want all methods of the
     * object.
     * @param filter a filter for excluding some methods that won't be
     * called remotely, or <code>null</code> if all methods to be included.
     * @param jFactory factory from where we create {@link Variable},
     * {@link Location} objects, etc.
     * @param typeFactory if a {@link IType} object needs 
     * to be made, this is the factory that will do it.
     */
    public MethodSerializationHandler(Object obj, Class<?> iNterface, IMethodFilter filter, JavaFactory jFactory, ITypeFactory typeFactory) {
        SerializerFactory sf = new SerializerFactory(jFactory,typeFactory);
        if (filter == null) filter = new IMethodFilter(){
            @Override
            public boolean includeMethod(Method method) {
                return true;
            }};
        mMethodMap = new HashMap<String,MethodSerializer>();
        Method methods[] = iNterface!=null?iNterface.getDeclaredMethods():obj.getClass().getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            if (filter.includeMethod(methods[i]) && mMethodMap.put(methods[i].getName(), new MethodSerializer(obj,
                    methods[i],sf)) != null) {
                System.err.println("Method name " + methods[i].getName()
                        + " is not unique!");
            }
        }
    }

    /**
     * Invoke the method with a given name. Arguments are
     * extracted from a serialized representation and
     * the result, if any, is serialized into an output stream.
     * @param method the name of the method.
     * @param args the input stream from which to extract arguments.
     * @param result the output stream to which result is written.
     * @throws Throwable
     */
    public void invoke(String method, DataInputStream args,
            DataOutputStream result) throws Throwable {
        IMethodSerializer invoker = getInvoker(method);
        invoker.invoke(args,result);
    }
    
    /**
     * @param method name of method for which an invoker is desired.
     * @return the object for invoking the method.
     * @throws IllegalArgumentException if the method is unknown.
     */
    private IMethodSerializer getInvoker(String method) throws IllegalArgumentException{
        IMethodSerializer invoker = mMethodMap.get(method);
        if (invoker == null){
            throw new IllegalArgumentException("Unrecognized method: " + method);
        }
        return invoker;
    }

    public byte[] serializeArguments(String method,Object[]args){
        IMethodSerializer invoker = getInvoker(method);
        return invoker.serializeArguments(args);
    }
    
    public IMethodSerializer getSerializerFor(String method){
        IMethodSerializer serializer = getInvoker(method);
        return serializer;
    }

}
