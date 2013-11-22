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
package com.arc.seecode.engine.internal.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import com.arc.mw.util.Command;
import com.arc.mw.util.StringUtil;
import com.arc.seecode.engine.config.ArgProcessorFactory;
import com.arc.seecode.engine.config.ConfigException;
import com.arc.seecode.engine.config.IArgProcessor;


/**
 * This class invokes the Swahili processor on a script against
 * an array of command-line arguments. It returns the
 * expanded arguments.
 * @author David Pickens
 */
public class Swahili implements IArgProcessor {
    
//    /**
//     * Where startup sets the value of "-install" argument.
//     */
//    private static final String INSTALL_PROP = "osgi.install.area";
    
    static abstract class MyWriter extends Writer{
    	protected IOutput mOutput;
        MyWriter(IOutput output){
        	mOutput = output;
        }
        @Override
		public void close() throws IOException {
		}
        @Override
		public void flush() throws IOException {
		}
        @Override
		public void write(char[] buf, int index, int len) throws IOException {
			String s = new String(buf,index,len);
			writeString(s);			
		}
        @Override
		public void write(String s){
			writeString(s);			
		}
		protected abstract void writeString(String s);
    }
    
    static class MyStdoutWriter extends MyWriter{
    	MyStdoutWriter(IOutput output){
    		super(output);
    	}
        @Override
    	protected void writeString(String s){
    		mOutput.printOutput(s);
    	}
    }
    
    static class MyStderrWriter extends MyWriter{
    	MyStderrWriter(IOutput output){
    		super(output);
    	}
        @Override
    	protected void writeString(String s){
    		mOutput.printError(s);
    	}
    }
    
    /**
     * Compute the path to the "sc???.exe" command so as to
     * be able to invoke swahili script.
     * <P>
     * We make it public so that it can be accessed
     * by {@link ArgProcessorFactory#getSCEXE(String)}.
     * @param target the target processor (e.g., "arc")
     * @return path to swahili processor, or null if can't be located.
     */
    public static String computeSCEXE(String target) {
        if (target == null) target = "ac"; // punt
        String arcInstall = System.getProperty("arc.install");
        String sccmd = "sc" + target;
        String hc = "hc" + target;
        if (arcInstall == null){
            return sccmd;
//            Location location = Platform.getInstallLocation();
//            String eclipseInstall = null;
//            if (location != null){
//                try {
//                    eclipseInstall = Platform.asLocalURL(location.getURL()).getFile();
//                } catch (IOException e) {
//                }
//            }
//            if (eclipseInstall != null){
//                File f = new File(eclipseInstall);
//                if (new File(f,"../metaware/" + hc).isDirectory()){
//                    arcInstall = f.getParentFile().getPath();
//                }
//                else if (f.getParentFile() != null && new File(f.getParentFile(), hc).isDirectory() && f.getParentFile().getParentFile() != null){
//                    arcInstall = f.getParentFile().getParentFile().getPath();
//                }    
//                else if (new File("C:/ARC").isDirectory()){
//                    // An act of desparation...
//                    arcInstall = "C:/ARC";
//                }
//            }         
        }
        String result = arcInstall + "/metaware/" + hc + "/bin/" + sccmd;
        if (!new File(result).exists())
            result = sccmd;
       
        return result;       
    }
    
    private static String findTarget(String args[]){
        for (int i = 0; i < args.length; i++){
            if (args[i].startsWith("-targs=")){
                String s = args[i].substring(7);
                int comma = s.indexOf(',');
                if (comma >= 0){
                    s = s.substring(0,comma);
                }
                return s.toLowerCase();
            }
        }
        return null;
    }
    
    /*override*/
    @Override
    public String expandArgs(String[] args, IOutput output, String[]envp) throws ConfigException {
        String[] cmd = new String[args.length+4];
        System.arraycopy(args,0,cmd,4,args.length);
        String target = findTarget(args);
        cmd[0] = computeSCEXE(target);
        if (cmd[0] == null){
            throw new ConfigException("Configuration Error: Can't locate sc" + target + " path");
        }
        
        if (output == null){
            output = new IOutput(){
                @Override
                public void printError(String message) {
                    System.err.print(message);                   
                }

                @Override
                public void printOutput(String message) {
                    System.out.print(message);                   
                }              
            };
        }
        
        try {
            File tmpFile = File.createTempFile("tmp",".args");
            cmd[1] = "-cnftmp="+tmpFile.getPath();
            cmd[2] = "-norun";
            cmd[3] = "-nooptions"; // don't read .sc.project!!!
            
            int rc = Command.invoke(cmd, new MyStdoutWriter(output),
            				new MyStderrWriter(output),envp);
            if (rc < 0){
                throw new ConfigException(cmd[0] + " failed: exit code=" + rc);                
            }
            if (rc != 0){
                throw new ConfigException("MetaWare debugger driver terminated with exit code " + rc);
            }
            if (!tmpFile.exists() || tmpFile.length() == 0){
                throw new ConfigException("Arg file is empty or non-existent: " + tmpFile);
            }
            //String result[] = extractContents(tmpFile);
            tmpFile.deleteOnExit();
            return "@" + tmpFile.getPath();
            
        } catch (IOException e) {
            throw new ConfigException("I/O error in invoking " + cmd[0] + "; possible search path problem?",e);
        }
    }
    
    private static String[] extractContents(File argFile) throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(argFile));
        ArrayList<String> list = new ArrayList<String>(40);
        String line = reader.readLine();
        while (line != null){
            line = line.trim();
            if (line.length() > 0) // replace \ with /. Property files don't like backslashes
                StringUtil.stringToList(list,line.replace('\\','/'));
            line = reader.readLine();
        }
        return list.toArray(new String[list.size()]);
    }
    
    public static void main(String args[]) throws ConfigException, IOException{
        Swahili s = new Swahili();
        String r = s.expandArgs(args,null,null);
        System.out.println("result is: \"" + r +"\"");
        String results[];
        if (r.startsWith("@")) {
            results = extractContents(new File(r.substring(1)));
        }
        else
            results = new String[]{r};
        for (int i = 0; i < results.length;i++){
            if (results[i].startsWith("-")) {
                System.out.println();
                System.out.print(results[i]);
            }
            else {
                System.out.print(" \"" + results[i] + '"');
            }
        }
    }
    

}
