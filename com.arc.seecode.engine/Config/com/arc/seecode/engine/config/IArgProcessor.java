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
package com.arc.seecode.engine.config;


/**
 * @author David Pickens
 */
public interface IArgProcessor {
    
    public interface IOutput {
    	/**
    	 * Messages that are considered errors (such as
    	 * those that would be written to stderr.
    	 * @param message the message to be printed as
    	 * an error; newlines must be explicit.
    	 */
        void printError(String message);
        /**
         * Messages that are information (such as those that
         * would be written to stdouer).
         * @param message the message to be printed; newlines
         * must be explicit.
         */
        void printOutput(String message);
    }
    /**
     * Given an array of arguments as specified on the command line,
     * expand them into the arguments that the SeeCode engine
     * wants to see: a string of tokens separated by whitespace;
     * tokens with whitespace are quoted.
     * <P>
     * A callback is passed to receive errors. If any error occurs
     * the result is undefined.
     * 
     * @param args the arguments from the command line.
     * @param output callback for messages.
     * @param envp the environment strings, or <code>null</code> if the caller's environment is
     * to be used.
     * @return expanded arguments.
     * @throws ConfigException when an internal configuration
     * error is detected.
     */
    String expandArgs(String args[], IOutput output, String envp[])
        throws ConfigException;

}
