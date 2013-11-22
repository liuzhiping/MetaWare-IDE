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
import com.metaware.guihili.IEnvironment;
import com.metaware.guihili.IEvaluator;
import com.metaware.guihili.MalformedExpressionException;

import java.util.List;

/**
 *  LISP expression processor.
 *
 * @author David Pickens
 * @version May 9, 2002
 */
public interface ILisp {
    /** 
     * Evaluate the given expression (a List of expressions).
     * @param list the list to evaluate.
     * @param evaluator where to evaluate symbols and calls.
     * @param env symbol-lookup context
     * @return return an object that denotes the result.
     * @exception MalformedExpressionException occurs if expression can't be evaluated.
     */
    public Object lispEval(List<Object> list, IEvaluator evaluator, IEnvironment env) throws MalformedExpressionException;
    /** 
     * Define a new Lisp function. This is how we extend our lisp
     * iterpreter.
     * @param name name of function.
     * @param function the callback to execute the function; it receives the list
     * as an argument.
     */
    public void lispDefine(String name, ILispFunction function);

    }
