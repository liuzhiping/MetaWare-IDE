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
package com.metaware.guihili;


import java.io.File;


/**
 * An environment which can be used to retrieve symbol values and Lisp procedures.
 */
public interface IEnvironment {

    /**
     * Return the value of a symbol, or null if the symbol isn't defined.
     * <P>
     * Note: the name space for symbols is separate from procedures.
     * <P>
     * @param symbol the symbol whose value is requested.
     * @return the value of a symbol, or null if the symbol isn't defined.
     */
    Object getSymbolValue (String symbol);

    /**
     * Define a symbol. Subsequent calls to {@link #getSymbolValue(String)} will return the value. Any preceeding value
     * associated with the symbol is overridden.
     * @param symbol the symbol being defined.
     * @param value the value to be associated with the symbol.
     */
    void putSymbolValue (String symbol, Object value);

    /**
     * return the Lisp procedure assocated with a symbol, or null if there is no such procedure.
     * @param name the name of a Lisp procedure.
     * @return the Lisp procedure in the form of a List, or null.
     */
    ProcBody getProcedure (String name);

    /**
     * Define a Lisp procedure by a given name. Subsequent calls to {@link #getProcedure(String)} will return the body.
     * <P>
     * Any preceeding definition of the name is overridden.
     * <P>
     * Note: the name space for procedures is separate for "symbols".
     * @param name name of the procedure to define.
     * @param body the Lisp procedure to be associate with the name.
     */
    void putProcedure (String name, ProcBody body);

    /**
     * Transitively evaluate a string that evaluates into a an object. We follow the old guihili semantics of first
     * transitively evaluating everything in one context before taking the result to the parent context.
     * @param string string to be evaluated, transitively.
     * @param eval the evaluator to use.
     * @return the evaluated string, or null if there is no name for in the environment.
     */
    Object expand (String string, IEvaluator eval) throws MalformedExpressionException;

    /**
     * Return the working directory when computing the location of relative files paths.
     * @return the working directory. Shouldn't be <code>null</code>.
     */
    File getWorkingDirectory ();

    /**
     * Return the value of an "environment" variable from the OS. The result does not necessarily match
     * <code>System.getenv(name)</code> because the client may be overriding the OS environment when spawning a
     * relavent process.
     * @param name the environment variable to look up.
     * @return the value of the symbol or <code>null</code> if it isn't defined.
     */
    String getenv (String name);
}
