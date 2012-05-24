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

import java.util.IdentityHashMap;
import java.util.Map;

import com.arc.seecode.engine.AssemblyRecord;
import com.arc.seecode.engine.IEngineAPI;
import com.arc.seecode.engine.JavaFactory;
import com.arc.seecode.engine.Location;
import com.arc.seecode.engine.RegisterContent;
import com.arc.seecode.engine.Value;
import com.arc.seecode.engine.Variable;
import com.arc.seecode.engine.WatchpointHit;
import com.arc.seecode.engine.type.IType;
import com.arc.seecode.engine.type.ITypeFactory;

/**
 * @author David Pickens
 */
public class SerializerFactory {
    private Map<Class<?>,ISerializer> mMap = new IdentityHashMap<Class<?>,ISerializer>();
    private Map<Class<?>,ISerializer> mArrayMap = new IdentityHashMap<Class<?>,ISerializer>();
    
    /**
     * A serializer that doesn't expect any arguments of type "Variable", "Value", or "Type".
     */
    public SerializerFactory(){
        this(null,null);
    }
    /**
     * A serializer of method arguments.
     * @param jfactory factory for creating Value and Variable objects; may be <code>null</code>
     * if no such types are expected.
     * @param typeFactory factory for creating debugger Type objects; may be <code>null</code> if
     * such types are not expected.
     */
    public SerializerFactory(JavaFactory jfactory, ITypeFactory typeFactory){
        mMap.put(Integer.TYPE,IntSerializer.getInstance());
        mMap.put(Void.TYPE,VoidSerializer.getInstance());
        mMap.put(Boolean.TYPE,BooleanSerializer.getInstance());
        mMap.put(String.class,StringSerializer.getInstance());
        mMap.put(Location.class,LocationSerializer.getInstance());
        mMap.put(Long.TYPE,LongSerializer.getInstance());
        if (typeFactory != null && jfactory != null) {
            TypeSerializer typeSerializer = new TypeSerializer(typeFactory);
            ValueSerializer valueSerializer = new ValueSerializer(jfactory,typeSerializer);
            VariableSerializer variableSerializer = new VariableSerializer(jfactory,valueSerializer,typeSerializer);
            mMap.put(Variable.class,variableSerializer);  
            mMap.put(IType.class,typeSerializer);
            mMap.put(Value.class,valueSerializer);
            mArrayMap.put(Variable.class,new VariableArraySerializer(variableSerializer));
        }
        mMap.put(RegisterContent.class,RegisterContentSerializer.getInstance());

        //Handles EngineAPIObserver argument, which we
        // pass across the wire as null.
        mMap.put(IEngineAPI.class, NullObjectSerializer.getInstance());
        mMap.put(Throwable.class,NullObjectSerializer.getInstance());
        
        mArrayMap.put(Integer.TYPE,IntArraySerializer.getInstance());
        mArrayMap.put(Byte.TYPE,ByteArraySerializer.getInstance());
        mArrayMap.put(AssemblyRecord.class,AssemblyRecordArraySerializer.getInstance());
        mArrayMap.put(Location.class,LocationArraySerializer.getInstance());       
        mArrayMap.put(WatchpointHit.class,WatchpointHitArraySerializer.getInstance());
        mArrayMap.put(String.class,StringArraySerializer.getInstance());
        mArrayMap.put(RegisterContent.class,RegisterContentArraySerializer.getInstance());
        
    }
    public ISerializer findSerializer(Class<?> c) {
        ISerializer result;
        if (!c.isArray()){
            result = mMap.get(c);          
        }
        else {
            result = mArrayMap.get(c.getComponentType());
        }
        if (result == null) {
            throw new IllegalArgumentException("Unrecognized argument type: " + c.getName());
        }
        return result;
    }
}
