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

import java.util.List;

/**
 * Evaluates Guihili exprssions.
 * Expressions are of the form:
 * <pre>
 * "name"
 * "( action operands...)"
 * </pre>
 * where each operand can also be an expression.
 * <P>
 * Names and strings may have embedded macros, e.g. "foo$suffix"
 *
 * @author David Pickens
 * @version April 26, 2002
 */
public interface IEvaluator {
    /** 
     * @param expression expression to evaluate.
     * @param env symbol-lookup context
     * @return return boolean value of the expression.
     * @exception MalformedExpressionException occurs if expression can't be evaluated.
     */
    public boolean evaluateBoolean(String expression, IEnvironment env) throws MalformedExpressionException;
    /** 
     * @param expression expression to evaluate to an integer
     * @param env symbol-lookup context
     * @return value of inteer expression.
     * @exception MalformedExpressionException occurs if expression can't be evaluated.
     */
    public int evaluateInteger(String expression, IEnvironment env) throws MalformedExpressionException;
    /**
     * @param expression expression to evaluate to a string.
     * @param env symbol-lookup context
     * @return return the value of the string.
     * @exception MalformedExpressionException occurs if expression can't be evaluated.
     */
    public String evaluateStringExpression(String expression, IEnvironment env) throws MalformedExpressionException;
    /**
     * @param expression expression to evaluate to an arbitrary object.
     * @param env symbol-lookup context
     * @return return the value of the object.
     * @exception MalformedExpressionException occurs if expression can't be evaluated.
     */
    public Object evaluateExpression(String expression, IEnvironment env) throws MalformedExpressionException;
    /**
     * @param expression expression to evaluate to an list
     * @param env symbol-lookup context
     * @return return the value of the object.
     * @exception MalformedExpressionException occurs if expression can't be evaluated.
     */
    public List<Object> evaluateList(String expression, IEnvironment env) throws MalformedExpressionException;

    /**
     * Parse an expression that represents a deferred action. We do not evaluate
     * it if it is a Lisp procedure, but return it as an instance of List.
     */
    public Object parseAction(String expression) throws MalformedExpressionException;
    /**
     * Parse an expression that represents a list that will not
     * be evaluated as a lisp expression.
     */
    public Object parseList(String expression) throws MalformedExpressionException;

    /**
     *  Expand a string by back-substituting "$foo" references.
     * @param expression expression to evaluate to an arbitrary object.
     * @param env symbol-lookup context
     * @return the expanded string, or, for the case of "$foo", the object
     * associated with "foo".
     */
    public Object expandString(String expression, IEnvironment env) throws MalformedExpressionException;

     /**
      *Given a parsed expression, evaluate it.
      * If it is an instance of String, {@link #expandString(String,IEnvironment)} will be called on it.
      * If it is a List, then the Lisp processor will be invoked on it.
      * @param object the expression to be evaluated.
     * @param env symbol-lookup context
      */
    public Object evaluate(Object object, IEnvironment env) throws MalformedExpressionException;
    }
