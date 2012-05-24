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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * A class for instantiating a class by name.
 * 
 * @author David Pickens, MetaWare Incorporated
 * @version 1.0
 */
public class Instantiator {
    /**
     * Given a class by name, and a list of arguments, search for a compatible
     * constructor and instantiate it.
     * <p>
     * If any of the constructor arguments are of primitive types (e.g.,
     * <code><b>int</b></code>), they must be wrapped as objects.
     * <p>
     * 
     * @param className
     *            the name of the class to be invoked.
     * @param args
     *            the arguments to be passed to the constructor.
     * @return the instantiated object.
     */
    public static Object instantiate(String className, Object args[])
            throws NoSuchMethodException, InstantiationException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, SecurityException,
            ClassNotFoundException {
        return instantiate(Class.forName(className), args);
    }

    /**
     * Given a class object, and a list of arguments, search for a compatible
     * constructor and instantiate it.
     * <p>
     * If any of the constructor arguments are of primitive types (e.g.,
     * <code><b>int</b></code>), they must be wrapped as objects.
     * <p>
     * 
     * @param klass
     *            the name of the class to be invoked.
     * @param args
     *            the arguments to be passed to the constructor.
     * @return the instantiated object.
     */
    public static Object instantiate(Class<?> klass, Object args[])
            throws NoSuchMethodException, InstantiationException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, SecurityException {
        Constructor<?> c = findConstructorFor(klass, args);
        if (c == null)
            throw new NoSuchMethodException(
                    "No appropriate public constructor for " + klass.getName());
        return c.newInstance(args);
    }

    /**
     * Given a class and a list of constructor arguments, find a construct that
     * will accept the arguments.
     */
    private static Constructor<?> findConstructorFor(Class<?> klass, Object args[]) {
        Constructor<?> constructors[] = klass.getConstructors();
        for (int i = 0; i < constructors.length; i++) {
            Constructor<?> c = constructors[i];
            if (argumentsConsistentWith(args, c))
                return c;
        }
        return null;
    }

    /**
     * Return true if the given constructor can accept the given argument list.
     */
    private static boolean argumentsConsistentWith(Object args[], Constructor<?> c) {
        Class<?>[] parmTypes = c.getParameterTypes();
        // Arguments must match in number
        if (parmTypes.length != args.length)
            return false;
        for (int i = 0; i < parmTypes.length; i++) {
            if (args[i] == null) {
                // can't pass "null" to something expecting a
                // primitive type.
                if (parmTypes[i].isPrimitive())
                    return false;
            } else if (!parmTypes[i].isAssignableFrom(args[i].getClass()))
                return false;
        }
        return true;
    }

    private Instantiator() {
    } // Prevent instantiating this class
}
