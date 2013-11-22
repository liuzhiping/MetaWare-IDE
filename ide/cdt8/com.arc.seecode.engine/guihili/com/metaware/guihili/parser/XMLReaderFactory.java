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
package com.metaware.guihili.parser;


import org.xml.sax.XMLReader;

import com.metaware.guihili.IEnvironment;
import com.metaware.guihili.IEvaluator;
import com.metaware.guihili.IFileResolver;
import com.metaware.guihili.builder.Environment;
import com.metaware.guihili.builder.FileResolver;
import com.metaware.guihili.eval.Evaluator;


/**
 * This class creates an instance of {@link XMLReader}which actually parses "guihili". With this instance, an XML-based
 * document can be constructed.
 * <P>
 * Guihili has the following syntax:
 * <P>
 * 
 * <pre>
 * 
 *     Specification -&gt; Component
 *     Component -&gt; '(' Property* Component* ')'
 *     Component -&gt; '&lt;' Property* Component* '&gt;
 *     Property -&gt; Identifier '=' Value
 *     Value -&gt; Identifier | Number | String | '{' Value* '}'
 *  
 * </pre>
 * 
 * For the property name "list", the Value can be a list of values separated by white space. They are interpreted as a
 * list of String.
 * @author David Pickens
 * @version April 22, 2002
 */

public class XMLReaderFactory {

    /**
     * Create an {@link XMLReader}instance.
     * @param resolver an object for opening a Guihili file, given its unqualified name, or null.
     * @param eval expression evaluator; used in processing of #include specifier.
     * @param env symbol-lookup environment
     */
    public static XMLReader makeReader (IFileResolver resolver, IEvaluator eval, IEnvironment env) {
        if (eval == null || resolver == null)
            throw new IllegalArgumentException("Null argument not permitted");
        return new Parser(resolver, eval, env);
    }

    /**
     * Create an {@link XMLReader}instance.
     * @param eval expression evaluator; used in processing of #include specifier.
     */
    public static XMLReader makeReader (IEvaluator eval) {
        return makeReader(new FileResolver(), eval, Environment.create());
    }

    /**
     * Create an {@link XMLReader}instance.
     */
    public static XMLReader makeReader () {
        return makeReader(new Evaluator());
    }

    /**
     * Create an {@link XMLReader}instance.
     * @param resolver an object for opening a Guihili file, given its unqualified name, or null.
     */
    public static XMLReader makeReader (IFileResolver resolver) {
        return makeReader(resolver, new Evaluator(), Environment.create());
    }
}
