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
package com.arc.seecode.engine;


/**
 * Thrown when a error occurs while the seecode engine
 * is evaluating something.
 * @author David Pickens
 */
public class EvaluationException extends Exception {

    /**
     * @param arg0
     */
    public EvaluationException(String arg0) {
        super(arg0);
    }
    /**
     * @param arg0
     * @param arg1
     */
    public EvaluationException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }
    /**
     * @param arg0
     */
    public EvaluationException(Throwable arg0) {
        super(arg0);
    }
}
