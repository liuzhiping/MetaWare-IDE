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
package com.arc.mw.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Functions to cast a non-typed collection to one that is typed.
 * This is necessary to get around Eclipse's warning when we attempt to do
 * something like:
 * <pre>
 *  List l = ...
 *  List<String> stringList = (List<String>)l;
 *  </pre>
 *  Thus, all of the warning are in this class only. We can compile all other code cleanly.
 * @author David Pickens
 */
public class Cast {
    private Cast(){}
    
    
    @SuppressWarnings("unchecked")
    public static <T> List<T> toType(Object l){
        return (List<T>)l;
    }
    
    @SuppressWarnings({ "unchecked", "cast", "rawtypes" })
    public static <T> List<T> toType(List l){
        return (List<T>)l;
    }
    
    @SuppressWarnings({ "unchecked", "cast", "rawtypes" })
    public static <T> Collection<T> toType(Collection l){
        return (Collection<T>)l;
    }
    
    @SuppressWarnings({ "unchecked", "cast", "rawtypes" })
    public static <K,V> Map<K,V> toType(Map m){
        return (Map<K,V>)m;
    }

}
