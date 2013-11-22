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
package org.eclipse.cdt.debug.core.cdi;

/**
 * An exception to denote a failure in invoking a target
 * program. We provide this to distinguish between an ordinary
 * "CDIException" because the latter causes a CoreException.
 * @author David Pickens
 */
public class TargetInvocationException extends CDIException {

    /**
     * 
     */
    public TargetInvocationException() {
        super();
 
    }

    /**
     * @param t
     */
    public TargetInvocationException(Throwable t) {
        super(t);
 
    }

    /**
     * @param s
     */
    public TargetInvocationException(String s) {
        super(s);
  
    }

    /**
     * @param s
     * @param t
     */
    public TargetInvocationException(String s, Throwable t) {
        super(s, t);
  
    }

    /**
     * @param s
     * @param d
     */
    public TargetInvocationException(String s, String d) {
        super(s, d); 
    }

}
