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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * A class for instantiating a method by name.
 * @author David Pickens, MetaWare Incorporated
 * @version 1.0
 */
public class Caller {
    /**
     * Given an object, a method  name, and a list of arguments,
     * invoke the method on behalf of the object.
     * <p>
     * @param object the object on behalf that the method is being called.
     * @param method the name of the method.
     * @param args the arguments to be passed to the constructor.
     * @return the instantiated object.
     */
    public static Object invoke(Object object, String method, Object args[])
            throws NoSuchMethodException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException,
            SecurityException {
        Method m = findMethodFor(object.getClass(), method, args);
        if (m == null)
            throw new NoSuchMethodException("Class "
                    + object.getClass().getName() + " has no method named "
                    + method);
        return m.invoke(object, args);
    }

    /**
     * Given a class, a static method  name, and a list of arguments,
     * invoke the method.
     * <p>
     * @param klass the class in which the method is defined.
     * @param method the name of the method.
     * @param args the arguments to be passed to the constructor.
     * @return the instantiated object.
     */
    public static Object invokeStatic(Class<?> klass, String method, Object args[])
            throws NoSuchMethodException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException,
            SecurityException {
        Method m = findMethodFor(klass, method, args);
        if (m == null || !Modifier.isStatic(m.getModifiers()))
            throw new NoSuchMethodException("Class " + klass.getName()
                    + " has no static method named " + method);
        return m.invoke(null, args);
    }

    /**
     * Given a class, method name,  and a list of method arguments,
     * find a method that will accept the arguments.
     */
    private static Method findMethodFor(Class<?> klass, String method,
            Object args[]) {
        Method methods[] = klass.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            if (m.getName().equals(method) && argumentsConsistentWith(args, m))
                return m;
        }
        return null;
    }

    /**
     * Return true if the given constructor can accept the given
     * argument list.
     */
    private static boolean argumentsConsistentWith(Object args[], Method m) {
        Class<?>[] parmTypes = m.getParameterTypes();
        // Arguments must match in number
        if (parmTypes.length != args.length)
            return false;
        for (int i = 0; i < parmTypes.length; i++) {
            if (args[i] == null) {
                // can't pass "null" to something expecting a
                // primitive type.
                if (parmTypes[i].isPrimitive())
                    return false;
            } else if (!isAssignableFrom(parmTypes[i], args[i].getClass()))
                return false;
        }
        return true;
    }

    static boolean isAssignableFrom(Class<?> dest, Class<?> src) {
        if (dest.isAssignableFrom(src))
            return true;
        if (dest.isPrimitive()) {
            if (src == Float.class)
                src = Float.TYPE;
            if (src == Integer.class)
                src = Integer.TYPE;
            if (src == Boolean.class)
                src = Boolean.TYPE;
            return dest == src;
        }
        return false;
    }

}
