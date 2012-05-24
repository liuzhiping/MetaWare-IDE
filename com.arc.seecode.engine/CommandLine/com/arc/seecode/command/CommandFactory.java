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
package com.arc.seecode.command;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;
import com.arc.seecode.internal.command.Factory;

/**
 * @author David Pickens
 */
public class CommandFactory {

    /**
     * Create a SeeCode command processor
     * 
     * @param e
     *            the engine interface that is used to service the command.
     * @param stdout
     *            where output messages are sent.
     * @param stderr
     *            where error messages sent.
     * @return a new command processor.
     * @throws EngineException
     *             if error occurred.
     */
    public static ICommandProcessor createCommandProcessor(EngineInterface e,
            OutputStream stdout, OutputStream stderr) throws EngineException {
        if (stdout == null || stderr == null) {
            throw new IllegalArgumentException("stdout or stderr is null");
        }
        final PrintWriter err = new PrintWriter(new OutputStreamWriter(stderr));
        final IErrorHandler eh = new IErrorHandler() {

            @Override
            public void error(String msg) {
                err.println(msg);
                err.flush();
            }

            @Override
            public void warning(String msg) {
                err.print("(Warning)");
                err.println(msg);
                err.flush();
            }
        };
        
        return Factory.create(e,eh,stdout);
    }
}
