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
package com.metaware.guihili.eval;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import com.metaware.guihili.IEnvironment;
import com.metaware.guihili.IEvaluator;
import com.metaware.guihili.MalformedExpressionException;


/**
 * LISP expression processor. It is instantiated by calling {@link #create Lisp.create()}.
 * @author David Pickens
 * @version May 9, 2002
 */
public class Lisp implements ILisp {

    private Lisp() {
    }

    public static ILisp create () {
        return new Lisp();
    }

    /**
     * Evaluate the given expression (a List of expressions).
     * @param list the list to evaluate.
     * @param evaluator where to evaluate symbols and calls.
     * @param env symbol-lookup environment
     * @return return an object that denotes the result.
     * @exception MalformedExpressionException occurs if expression can't be evaluated.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object lispEval (List<Object> list, IEvaluator evaluator, IEnvironment env) throws MalformedExpressionException {
        if (list.size() == 0)
            return null;
        Object funcName = list.get(0);
        if (funcName instanceof String) {
            ILispFunction func = mFuncMap.get(funcName);
            if (func == null)
                throw new MalformedExpressionException("Not recognized function: " + funcName);
            Object o = func.evaluate(list, evaluator, env);
            // System.out.println("[LISP] " + funcName + " returns " + o);
            return o;
        }
        else if (funcName instanceof List) {
            // If our first element is a list, then we assume a sequence of
            // lisp expressions. The last has the result.
            int cnt = list.size();
            Object result = null;
            for (int i = 0; i < cnt; i++) {
                if (list.get(i) instanceof List) {
                    result = lispEval((List<Object>) list.get(i), evaluator, env);
                }
                else
                    throw new MalformedExpressionException("Element " + i + " must be list within " + list);

            }
            return result;
        }
        else {
            throw new MalformedExpressionException("Not valid LISP function: " + list);
        }
    }

    /**
     * Define a new Lisp function. This is how we extend our lisp iterpreter.
     * @param name name of function.
     * @param function the callback to execute the function; it receives the list as an argument.
     */
    @Override
    public void lispDefine (String name, ILispFunction function) {
        // System.out.println("lispDefine " + name);
        mFuncMap.put(name, function);
    }

    /**
     * The object, o, is inspected by reflection. Every method that has the following signature is assumed to be a lisp
     * function by the same name:
     * 
     * <pre>
     * 
     *    (List list, IEvaluator eval, IEnviroment env)
     *  
     * </pre>
     * 
     * @param lisp the interpreter that we're adding methods to.
     * @param o the object that contains the functions.
     */
    public static void addFunctions (ILisp lisp, Object o) {
        Method methods[] = o.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            if (m.getName().startsWith("do_") && isProperSignature(m))
                lisp.lispDefine(m.getName().substring(3), new Function(o, m));
        }
    }

    static class Function implements ILispFunction {

        private Object mObject;

        private Method mMethod;

        Function(Object o, Method m) {
            mObject = o;
            mMethod = m;
        }

        @Override
        public Object evaluate (List<Object> list, IEvaluator eval, IEnvironment env) throws MalformedExpressionException {
            try {
                return mMethod.invoke(mObject, new Object[] { list, eval, env });
            }
            catch (InvocationTargetException x) {
                Throwable t = x.getTargetException();
                if (t instanceof MalformedExpressionException) {
                    throw (MalformedExpressionException) t;
                }
                else if (t instanceof RuntimeException)
                    throw (RuntimeException) t;
                throw new MalformedExpressionException("(" + mMethod.getName() + ") " + x.getMessage(), x);
            }
            catch (Exception x) {
                throw new MalformedExpressionException("(" + mMethod.getName() + ") ", x);
            }
        }
    }

    /**
     * return true if method has the signature: List, IEvaluator
     */
    static boolean isProperSignature (Method m) {
        Class<?> parms[] = m.getParameterTypes();
        if (parms.length != 3)
            return false;
        if (parms[0] != List.class)
            return false;
        if (parms[1] != IEvaluator.class)
            return false;
        if (parms[2] != IEnvironment.class)
            return false;
        return true;
    }

    private HashMap<String,ILispFunction>mFuncMap = new HashMap<String,ILispFunction>();
}
