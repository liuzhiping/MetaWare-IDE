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
 *  An interface for adding new function to our Lisp interpreter.
 *
 * @author David Pickens
 * @version May 9, 2002
 */
public interface ILispFunction {
    /**
     * Given a list, the first element of which reference
     * the name of this function, evaluate it.
     * <P>
     * None of the operands have been evaluated.
     * <P>
     * @param list the Lisp expression being evaluated.
     * @param eval evaluator to evaluate atomic operands.
     * @param env symbol-lookup environment for the evaluator
     * @return the result, or null if there is no result.
     * @exception MalformedExpressionException wrong number of operands, etc.
     */
    public Object evaluate(List<Object> list, IEvaluator eval, IEnvironment env) throws MalformedExpressionException;
    }
