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
package com.metaware.guihili.builder;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import com.metaware.guihili.IEnvironment;
import com.metaware.guihili.IEvaluator;
import com.metaware.guihili.MalformedExpressionException;
import com.metaware.guihili.ProcBody;

/**
 * An environment which can be used to retrieve symbol values and Lisp
 * procedures.
 * <P>
 * We allow a hierarchy of environments by providing a "parent" environment.
 */
public class Environment implements IEnvironment {
    private static int sWhich;
    
    private HashMap<String,Object> mSymbols = new HashMap<String,Object>();

    private HashMap<String,ProcBody> mProcs = new HashMap<String,ProcBody>();
    
    private HashMap<String,String> mEnv = null;

    private int mWhich;
    
    private File mWorkingDir;

    private Environment(File workingDir, String env[]) {
        if (workingDir == null) workingDir = new File(".");
        mWorkingDir = workingDir;
        if (env != null){
            mEnv = new HashMap<String,String>();  
            for (String s: env){
                int i = s.indexOf('=');
                if (i > 0){
                    mEnv.put(s.substring(0,i),s.substring(i+1));
                }
            }
        }
        mWhich = ++sWhich; // for debugging
    }

    public static IEnvironment create() {
        return new Environment(null,null);
    }
    
    public static IEnvironment create(File workingDir, String env[]) {
        return new Environment(workingDir,env);
    }

    /**
     * Create an environment with a parent; we access them as a hierarchy.
     */
    public static IEnvironment create(IEnvironment parent) {
        return new CompoundEnvironment(new Environment(parent.getWorkingDirectory(),null), parent);
    }

    /**
     * Create an environment with a parent; we access them as a hierarchy.
     */
    public static IEnvironment create(IEnvironment child, IEnvironment parent) {
        return new CompoundEnvironment(child, parent);
    }

    /**
     * Return the value of a symbol, or null if the symbol isn't defined.
     * <P>
     * Note: the name space for symbols is separate from procedures.
     * <P>
     * 
     * @param symbol
     *            the symbol whose value is requested.
     * @return the value of a symbol, or null if the symbol isn't defined.
     */
    @Override
    public Object getSymbolValue(String symbol) {
        Object o = mSymbols.get(symbol);
        // System.out.print(""+mWhich + ":getSymbolValue(" + symbol+ ") returns"
        // );
        // if (o == null) System.out.println("<null>");
        // else System.out.println(" instanceof " + o.getClass().getName());
        return o;
    }

    /**
     * Define a symbol. Subsequent calls to {@link #getSymbolValue(String)} will
     * return the value. Any preceeding value associated with the symbol is
     * overridden.
     * 
     * @param symbol
     *            the symbol being defined.
     * @param value
     *            the value to be associated with the symbol.
     */
    @Override
    public void putSymbolValue(String symbol, Object value) {
        if (value == null)
            value = "";
        // System.out.println(""+mWhich + ":putSymbolValue(\"" + symbol + "\","
        // + value + ")");
        // System.out.println(" instanceof " + value.getClass().getName());
        mSymbols.put(symbol, value);
    }

    /**
     * return the Lisp procedure assocated with a symbol, or null if there is no
     * such procedure.
     * 
     * @param name
     *            the name of a Lisp procedure.
     * @return the Lisp procedure in the form of a List, or null.
     */
    @Override
    public ProcBody getProcedure(String name) {
        ProcBody o = mProcs.get(name);
        return o;
    }

    /**
     * Define a Lisp procedure by a given name. Subsequent calls to
     * {@link #getProcedure(String)} will return the body.
     * <P>
     * Any preceeding definition of the name is overridden.
     * <P>
     * Note: the name space for procedures is separate for "symbols".
     * 
     * @param name
     *            name of the procedure to define.
     * @param body
     *            the Lisp procedure to be associate with the name.
     */
    @Override
    public void putProcedure(String name, ProcBody body) {
        mProcs.put(name, body);
    }

    // see comments in interface
    @Override
    public Object expand(String name, IEvaluator eval)
            throws MalformedExpressionException {
        Object v = getSymbolValue(name);
        if (v instanceof String) {
        	Object o = eval.expandString((String) v, this);
            return o;
        }
        // Convert integers to strings so that such things as "foo$i" works when
        // i is an integer
        // (e.g. as index of "upto" node)
        if (v instanceof Integer) {
            // System.out.println("[ENV:" + mWhich + "] expand(" + name + ")=" +
            // v);
            return "" + v;
        }
        // System.out.println("[ENV:" + mWhich + "] expand(" + name +
        // ")=<null>");
        // System.out.println(" contents: " + this);
        return v;
    }
    
    @Override
    public File getWorkingDirectory () {
        return mWorkingDir;
    }

    @Override
    public String getenv (String name) {
        if (mEnv != null){
            return mEnv.get(name);
        }
        return System.getenv(name);
    }

    @Override
    public String toString() {
        StringWriter sw = new StringWriter(300);
        PrintWriter out = new PrintWriter(sw);
        out.print("Environment#" + mWhich + "[");
        out.print(mSymbols);
        out.print(']');
        return sw.toString();
    }

    /**
     * A compound environment that encapsolates a pair of enviornments. This is
     * required so that the semantics of {@link #expand(String,IEvaluator)} work
     * correctly
     */
    static class CompoundEnvironment implements IEnvironment {
        CompoundEnvironment(IEnvironment first, IEnvironment second) {
            mFirst = first;
            mSecond = second;
        }

        @Override
        public Object expand(String name, IEvaluator eval)
                throws MalformedExpressionException {
            // System.out.println("Compound.expand(" + name +" )");
            Object o = mFirst.expand(name, eval);
            if (o == null) {
                // System.out.println(" first was null");
                o = mSecond.expand(name, eval);
            }
            if (o instanceof String) {
                // System.out.println(" " + name + " expands into " + o);
                // System.out.println(" now try expanding it");
                o = eval.expandString((String) o, this);
                // System.out.println(" " + name + " transitively expands into "
                // + o);
            }
            return o;
        }

        @Override
        public Object getSymbolValue(String name) {
            Object o = mFirst.getSymbolValue(name);
            if (o == null)
                o = mSecond.getSymbolValue(name);
            return o;
        }

        @Override
        public void putSymbolValue(String name, Object value) {
            mFirst.putSymbolValue(name, value);
        }

        @Override
        public ProcBody getProcedure(String name) {
            ProcBody b = mFirst.getProcedure(name);
            if (b == null)
                b = mSecond.getProcedure(name);
            return b;
        }

        @Override
        public void putProcedure(String name, ProcBody body) {
            mFirst.putProcedure(name, body);
        }

        @Override
        public String toString() {
            return "CompoundEnvironment(" + mFirst + " " + mSecond + " )";
        }
        
        @Override
        public File getWorkingDirectory () {
            return mFirst.getWorkingDirectory();
        }

        @Override
        public String getenv (String name) {
            String s = mFirst.getenv(name);
            if (s == null) s = mSecond.getenv(name);
            return s;
        }

        private IEnvironment mFirst;

        private IEnvironment mSecond;

      
    }


}
