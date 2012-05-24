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
package com.arc.cdt.debug.seecode.options;


/**
 * An exception that is thrown if the SeeCode
 * argument list cannot be computed.
 * @author David Pickens
 */
public class ConfigurationException extends Exception {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public ConfigurationException() {
        super();
    }

    /**
     * @param arg0
     */
    public ConfigurationException(String arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     */
    public ConfigurationException(Throwable arg0) {
        super(arg0);

    }

    /**
     * @param arg0
     * @param arg1
     */
    public ConfigurationException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
