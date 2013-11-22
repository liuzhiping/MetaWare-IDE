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

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.arc.seecode.command.ICommandExecutor;
import com.arc.seecode.command.ICommandProcessor;
import com.arc.seecode.engine.StackFrameRef;


/**
 * The thing that processes arbitrary commands.
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
class CommandProcessor implements ICommandProcessor, ICommandInvoker {

    private Map<String,ICommandExecutor> mMap = new HashMap<String,ICommandExecutor>();
    private ICommandExecutor mUndefinedCommand;
    private ICommandExecutor mLastCommand;
    private Runnable mFlush; // called after every command to flush things
    private CommandService mService;
    
    public CommandProcessor(CommandService service, Runnable flush){
        mFlush = flush;
        setUndefinedCommand(new ICommandExecutor(){

            @Override
            public void execute (String arguments) throws Exception {
                throw new IllegalArgumentException("Unrecognized command: " + arguments);
                
            }

            @Override
            public boolean repeat () throws Exception {
                return false;
            }});
        mService = service;
        service.setCommandInvoker(this);
    }
    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     */
    @Override
    public void processCommand (String cmdLine) throws Exception {
        cmdLine = cmdLine.trim();
        if (cmdLine.length() == 0 || cmdLine.startsWith("#")) {
            // Ignore empty lines or comments
            return;
        }
        StringTokenizer each = new StringTokenizer(cmdLine);
        String cmd = each.nextToken();
        ICommandExecutor x = mMap.get(cmd);
        mLastCommand = x;
        try {
            if (x == null) {
                x = getUndefinedCommand();
                x.execute(cmdLine);
            }
            else {
                x.execute(each.hasMoreTokens()?each.nextToken("").trim():"");
            }
        }
        catch (CommandExitException e) {
            //Command preempted due to an error that was presumably diagnosed.
            // We continue from here.
        }     
        if (mFlush != null) mFlush.run();
    }

    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param x
     */
    @Override
    public void setUndefinedCommand (ICommandExecutor x) {
        if (x == null) throw new IllegalArgumentException("argument is null");
        mUndefinedCommand = x;      
    }

    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     */
    @Override
    public ICommandExecutor getUndefinedCommand () {      
        return mUndefinedCommand;
    }

    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param command
     * @param x
     */
    @Override
    public void addCommandExecutor (String command, ICommandExecutor x) {
        if (command == null)
            throw new IllegalArgumentException("command is null");
        if (x == null) mMap.remove(command);
        else
            mMap.put(command,x);      
    }

    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     */
    @Override
    public ICommandExecutor getCommandExecutor (String command) {
        return mMap.get(command);
    }
    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     */
    @Override
    public boolean repeatCommand () throws Exception {
        if (mLastCommand != null) {
            boolean result = mLastCommand.repeat();
            if (mFlush != null) mFlush.run();
            return result;
        }
        return false;
    }
    @Override
    public void setStackFrame (StackFrameRef stackFrame) {
        mService.setStackFrame(stackFrame);       
    }

}
