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
package com.arc.cdt.debug.seecode.internal.core.launch;


import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.runtime.CoreException;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.core.launch.ICMPDInfo;
import com.arc.debugger.EngineLocator;
import com.arc.mw.util.Command;
import com.arc.seecode.engine.config.ArgProcessorFactory;
import com.arc.seecode.engine.config.ConfigException;
import com.arc.seecode.engine.config.IArgProcessor;


public class EngineConfiguration {

    /**
     * Given the so-called "swahili" arguments to be passed to the engine (for a non-CMPD session), create the necessary
     * temp file for configuring the engine. Return a string that is used to read the temp file.
     * <P>
     * The resulting string typically looks something like "@tmpfile".
     * @param swahiliArgs the arguments to be passed to the engine as generated from the Launch Configuration dialog.
     * @param console where to emit messages as the debugger driver is invoked to compute engine configuration.
     * @return the argument string to be passed to the engine to have it configure itself.
     * @throws CoreException
     */
    public static String createArgument (String[] swahiliArgs, IConsole console, String env[]) throws CoreException, ConfigException {
        final Writer stdOut = new OutputStreamWriter(console.getOutputStream());
        final Writer errorWriter = new OutputStreamWriter(console.getErrorStream());
        final StringBuilder[] stderrBuilder = new StringBuilder[1];

        // We want to cache stderr in case the driver emits an error message so that we
        // can place it in an error box.
        stderrBuilder[0] = new StringBuilder();
        final Writer stdErr = new Writer(){

            
            @Override
            public void close () throws IOException {
                             
            }

            @Override
            public void flush () throws IOException {
                errorWriter.flush();
                
            }

            @Override
            public void write (char[] cbuf, int off, int len) throws IOException {
                stderrBuilder[0].append(new String(cbuf,off,len));
                while (stderrBuilder[0].length() > 1000){
                    for (int i = 0; i < stderrBuilder[0].length(); i++){
                        if (stderrBuilder[0].charAt(i) == '\n'){
                            stderrBuilder[0] = new StringBuilder(stderrBuilder[0].substring(i+1));
                            break;
                        }
                    }
                }
                errorWriter.write(cbuf,off,len);
                
            }};
        //
        // Invoke the swahili script to expand the
        // arguments into a string to pass to the engine, checking
        // for errors.
        IArgProcessor argProc = ArgProcessorFactory.makeArgProcessor();
        try {
            return expandArgs(swahiliArgs, env, stdOut, stdErr, argProc);
        }
        catch (ConfigException e) {
            if (stderrBuilder[0].length() > 0){
                // NOTE: each line of the driver message are prefix with: 
                // "(C:\arc\metaware\arc\bin\scac:)" which doesn't leave much room
                // for the message in the error box. Fix them up.
                String lines[] = stderrBuilder[0].toString().split("\\n");
                if (lines.length == 1)
                    throw new ConfigException(stderrBuilder[0].toString(),e);
                Pattern p = Pattern.compile("(\\(.*sc...?: ?\\)) (.*)");
                String fromWhere = null;
                for (int i = lines.length-1; i >= 0; i--){
                    Matcher m = p.matcher(lines[i]);
                    if (m.matches()){
                        fromWhere = m.group(1);
                        lines[i] = m.group(2);
                    }                   
                }
                if (fromWhere != null){
                    StringBuilder b = new StringBuilder();
                    b.append(fromWhere);
                    b.append('\n');
                    for (String l: lines){
                        b.append(l);
                        b.append('\n');
                    }
                    throw new ConfigException(b.toString(),e);
                }
                throw new ConfigException(stderrBuilder[0].toString(),e);
            }
            throw e;
        }
    }

    /**
     * @todo davidp needs to add a method comment.
     * @param swahiliArgs
     * @param env
     * @param stdOut
     * @param stdErr
     * @param argProc
     * @return
     * @throws ConfigException
     */
    private static String expandArgs (
        String[] swahiliArgs,
        String[] env,
        final Writer stdOut,
        final Writer stdErr,
        IArgProcessor argProc) throws ConfigException {
        String args;
        args = argProc.expandArgs(swahiliArgs, new IArgProcessor.IOutput() {

            @Override
            public void printError (String message) {
                try {
                    stdErr.write(message);
                    stdErr.flush();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void printOutput (String message) {
                try {
                    stdOut.write(message);
                    stdOut.flush();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, env);
        return args;
    }

    /**
     * Given a CMPD description of a session, create the necessary temp file(s) be calling into the driver. Return an
     * argument string to be passed to the engine to read the generated temp files. The argument string typically looks
     * something like "@tmpfile".
     * @param cmpdInfo a CMPD session description.
     * @param console where to emit messages as the debugger driver is invoked to compute engine configuration.
     * @return the argument string to be passed to the engine to have it configure itself.
     */
    public static String createCMPDArgument (ICMPDInfo cmpdInfo, IConsole console, String[] env) throws CoreException {
        final Writer stdOut = new OutputStreamWriter(console.getOutputStream());
        final Writer stdErr = new OutputStreamWriter(console.getErrorStream());
        List<File> tempFiles = new ArrayList<File>(cmpdInfo.getProcesses().length);
        try {
            String scac;
            scac = EngineLocator.computeSCCommand("ac", env); // assume CMPD is based on ARCompact

            StringBuilder multifiles = new StringBuilder(100);
            multifiles.append("-multifiles=");
            String comma = "";
            for (ICMPDInfo.IProcess info : cmpdInfo.getProcesses()) {

                // Emit:
                // -no_mangle_multi -pset=pid[:pid+cnt] -psetname=processName -optfile=tmpfile
                List<String> argList = new ArrayList<String>();
                argList.add(scac);
                argList.add("-no_mangle_multi");
                argList.add("-pset=" + info.getIDList().getEncoding());
                argList.add("-psetname=" + info.getProcessName());
                File tmpFile;
                tmpFile = File.createTempFile("CMPD", ".txt");
                tmpFile.deleteOnExit();

                argList.add("-optfile=" + tmpFile);
                argList.add("-nogoifmain");
                argList.add("-off=initial_load");
                multifiles.append(comma);
                comma = ",";
                multifiles.append(tmpFile);
                tempFiles.add(tmpFile);
                argList.addAll(Arrays.asList(info.getSwahiliArgs()));

                argList.add("--");
                for (String a : info.getCommand()) {
                    argList.add(a);
                }

                String cmd[] = argList.toArray(new String[argList.size()]);
                Command.invoke(cmd, stdOut, stdErr, env);
            }
            List<String> finalArgList = new ArrayList<String>();
            finalArgList.add(scac);
            finalArgList.add(multifiles.toString());
            File multiFile = File.createTempFile("multi", ".txt");
            multiFile.deleteOnExit();
            File configFile = File.createTempFile("config",".txt");
            finalArgList.add("-multifile_path=" + multiFile);
            finalArgList.add("-generate_config=" + configFile);
            finalArgList.add("-no_mangle_multi");
            finalArgList.add("-on=cmpd");
            finalArgList.add("-nogoifmain");
            finalArgList.add("-off=initial_load");
            finalArgList.add("-norun");
            for (String a: cmpdInfo.getLaunchArgs()){
                finalArgList.add(a);
            }
            //Emit:
            //   -multifile=tmp1,tmp2,... -generate_config=multifile -norun
            Command.invoke(finalArgList.toArray(new String[finalArgList.size()]),stdOut,stdErr,env);
            return "@" + configFile.getPath();
        }
        catch (Exception e) {
            throw new CoreException(SeeCodePlugin.makeErrorStatus(e.getMessage(), e));
        }
    }
}
