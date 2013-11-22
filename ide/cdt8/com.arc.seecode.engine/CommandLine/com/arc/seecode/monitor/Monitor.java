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
package com.arc.seecode.monitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.arc.mw.util.Log;
import com.arc.seecode.command.CommandFactory;
import com.arc.seecode.command.ICommandProcessor;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;
import com.arc.seecode.engine.config.ArgProcessorFactory;
import com.arc.seecode.engine.config.ConfigException;
import com.arc.seecode.engine.config.IArgProcessor;


/**
 * Command monitor for SeeCode.
 * It prompts for the commands on an input stream, and
 * sends results on output stream.
 * 
 * @author David Pickens
 */
public class Monitor {

    private BufferedReader mReader;
    private OutputStream mOutput;
    private OutputStream mErr;
    private ICommandProcessor  mProcessor;
    private EngineInterface mEngine;
    
    public Monitor(EngineObserver d, InputStream input, OutputStream out, OutputStream err) throws IOException, EngineException {

        mReader = new BufferedReader(new InputStreamReader(input));
        mOutput = out;
        mErr = err;
        mEngine = EngineInterface.CreateRemote(
        "C:\\Program Files\\Java\\jdk1.5.0\\bin\\java",
        new String[]{"-classpath","C:/Documents and Settings/davidp/My Documents/My Development/workspace/com.arc.seecode.engine/bin"+
                File.pathSeparator +
                "C:/Documents and Settings/davidp/My Documents/My Development/workspace/com.arc.mw.util/bin", 
                /*"-Xdebug",
                "-Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y",*/"-Xcheck:jni"},
        null,60000,120000,null
        );
        //EngineInterface engine = EngineInterface.CreateDirect();
        IArgProcessor args = ArgProcessorFactory.makeArgProcessor();
        mProcessor = CommandFactory.createCommandProcessor(mEngine,out,err);
        try {
            mEngine.setEngineArguments(args.expandArgs(new String[]{"-targ","ARC"},null,null));
        } catch (ConfigException e) {          
            throw new EngineException("config failure",e);
        }
    }

    public void enterReadLoop() throws Exception {

        while (!mEngine.isShutdown()) {
           
            String cmd;
            try {
                mOutput.write("SC> ".getBytes());
                mOutput.flush();
                cmd = readLine();
            }
            catch (IOException e) {
                mErr.write(("Unexpected I/O exception: " + e.getMessage()+"\n").getBytes());
                break;
            }
            if (cmd == null)
                break;
            cmd = cmd.trim();
            if (cmd.startsWith("#")) //comment
                continue;
            if (cmd.length() == 0){
                mProcessor.repeatCommand();
            }
            mProcessor.processCommand(cmd);
        }
    }

    private String readLine() throws IOException {
        String line = mReader.readLine();
        if (line != null && line.endsWith("\\")) {
            StringBuffer buf = new StringBuffer();
            while (true) {
                buf.append(line.substring(0, line.length() - 1));
                line = mReader.readLine();
                if (line != null) {
                    if (line.endsWith("\\")) {
                        buf.append(line.substring(0, line.length() - 1));
                    }
                    else {
                        buf.append(line);
                        break;
                    }
                }
                else
                    break;
            }
            return buf.toString();
        }
        return line;
    }

    public static void main(String args[]) {
        Log.setPrefix("CLIENT");
//        Toggle.set("CLIENT",true);
//        Toggle.set("SERVER",true);
//        Toggle.set("PACKETSENDER",true);
//        Toggle.set("PACKETRECEIVER",true);
        
        String lib = args.length > 0?args[0]:"C:/working/sc/bin/crout.dll";
        System.load(lib);

       
        EngineObserver d = new EngineObserver();
        try {
            Monitor m = new Monitor(d, System.in, System.out, System.err );
            m.enterReadLoop();
        } catch (Exception e) {
            System.out.flush();
            e.printStackTrace(System.err);
        } 
    }

}
