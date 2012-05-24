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


/**
 * Exception that is thrown from expression evaluators.
 * @author David Pickens
 * @version April 26, 2002
 */
public class MalformedExpressionException extends Exception {

    public MalformedExpressionException(String msg) {
        super(msg);
    }

    public MalformedExpressionException(String msg, Exception x) {
        super(msg);
        initCause(x);
        mException = x;
    }

    public Exception getException () {
        return mException;
    }

    private Exception mException;
}
