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
package com.arc.seecode.internal.command;


import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import com.arc.seecode.command.CommandProcessorAugmenter;
import com.arc.seecode.command.ICommandExecutor;
import com.arc.seecode.command.ICommandProcessor;
import com.arc.seecode.command.IErrorHandler;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;


/**
 * Factory for producing a command processor for seecode.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp </a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class Factory {

    /**
     * Create a command processor that talks to a particular instance of the engine proxy.
     * @param engine the SeeCode engine proxy.
     * @param errorHandler where errors are sent.
     * @param output where commands display their results.
     * @return the new command processor.
     */
    public static ICommandProcessor create (final EngineInterface engine, final IErrorHandler errorHandler, OutputStream output)
            throws EngineException {
        final PrintWriter writer = new PrintWriter(new OutputStreamWriter(output));
        CommandService service = new CommandService(engine, writer, errorHandler);
        SeeCodeCommandHandler handler = new SeeCodeCommandHandler(engine,errorHandler,service);
        CommandProcessor p = new CommandProcessor(service,new Runnable(){
            @Override
            public void run () {
                writer.flush();   // flush after each command            
            }});
        CommandProcessorAugmenter.augmentProcessor(p,handler);
        p.setUndefinedCommand(new ICommandExecutor(){

            @Override
            public void execute (String arguments) throws Exception {
                engine.invokeCommand(arguments);
//                StringTokenizer t = new StringTokenizer(arguments);
//                String cmd = arguments;
//                if (t.hasMoreTokens()){
//                    cmd = t.nextToken();
//                }
//                errorHandler.error("Unrecognized command: " + cmd);
                
            }

            @Override
            public boolean repeat () throws Exception {
                return false;
            }});
        return p;
    }

}
