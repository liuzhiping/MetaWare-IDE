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
package com.arc.mw.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * This class wraps a <code>Process</code> object so as
 * to make it more palatable.
 * <P>
 * The <code>Writer</code> objects for <code>stderr</code> and
 * <code>stdout</code> are passed as arguments. The spawned process will write
 * to these.
 * <P>
 * If the process is to be invoked synchronously, then the
 * {@link #invoke(String[],Writer,Writer) invoke} method can invoked without
 * creating a <code>Command</code> object.
 * 
 * @author David Pickens
 */
public class Command {
    private String[] mCommand;
    private String[] mEnvp;
    private Writer mStdout;
    private Writer mStderr;
    private int mExitCode;
    private Process mProcess;
    private ReadingThread mStdOutReader;
    private ReadingThread mStdErrReader;
    private Thread mWaitThread;
    private Object mLock;
    /**
     * Create a command that can be invoked by calling {@link #invoke()}.
     * @param cmd the command and its arguments
     * @param stdout where stdout is to be sent, or <code>null</code>.
     * @param stderr where stdin is to be sent, or <code>null</code>.
     */
    public Command(String cmd[], Writer stdout, Writer stderr) {
    	this(cmd,stdout,stderr,null);
    }
    /**
     * Create a command that can be invoked by calling {@link #invoke()}.
     * @param cmd the command and its arguments
     * @param stdout where stdout is to be sent, or <code>null</code>.
     * @param stderr where stdin is to be sent, or <code>null</code>.
     * @param envp the environment to be passed to it, or <code>null</code> if the caller's
     * environment is to be used.
     */
    public Command(String cmd[], Writer stdout, Writer stderr, String[] envp) { 	
        mCommand = cmd;
        if (stdout == null)
            stdout = new PrintWriter(System.out, true);
        if (stderr == null)
            stderr = new PrintWriter(System.err, true);

        mStdout = stdout;
        mStderr = stderr;
        mLock = new Object();
        mExitCode = -1;
        mEnvp = envp;
    }

    /**
     * Create a command that will sends its output to stdout and stderr.
     * @param cmd
     */
    public Command(String cmd[]) {
        this(cmd, null, null);
    }
    
    /**
     * Given a list of environment strings, extract the PATH definition and return
     * the list of strings that define the PATH. If there is no PATH definition,
     * return <code>null</code>.
     * @param environment the environment strings.
     * @return the path strings, or <code>null</code>.
     */
    public static String[] extractPathsFromEnvironment(String environment[]){
        for (String s: environment){
            if (s.startsWith("PATH=") || s.startsWith("Path=") || s.startsWith("path=")) {
                String paths[] = s.substring(5).split(File.pathSeparator);
                return paths;
            }
        }
        return null;
    }
    /**
     * Given a command and a list of environment strings, perform the following:
     * <P>
     * If the environment array is non-null, and contains a definition of PATH, and
     * the command has no directory path, compute the absolute path of the command
     * from the PATH variable, if possible.
     * <P>
     * Otherwise, merely return the command.
     * @param command a path to a command that may or may not have a directory path.
     * @param environment a set of environment definitions of the form "symbol=value", or <code>null</code>.
     * @return the corresponding absolute path of the command if possible to compute, or else
     * returns <code>command</code>.
     */
    public static String computePathFromEnvironment(String command, String[] environment){
        if (environment == null) return command;
        String[] paths = extractPathsFromEnvironment(environment);
        if (paths != null)
            return computePathFromPaths(command,paths);
        return command;      
    }
    
    private static boolean isWindows(){
        String os = System.getProperty("os.name"); //$NON-NLS-1$
        return (os != null && os.startsWith("Win")); //$NON-NLS-1$       
    }
   
    /**
     * Given a command that may not have a directory path, compute the absolute path
     * from a list of paths. Or else, returns the command with modification.
     * @param command the command that may or may not have have directory path.
     * @param paths a list of directory paths.
     * @return the absolute path of the command if the original has no directory information,
     * or just the original content of the command otherwise.
     */
    public static String computePathFromPaths (String command, String[] paths) {
        if (command.indexOf('/') != -1 || command.indexOf('\\') != -1)
            return command;
        if (isWindows() && command.indexOf('.') < 0){
            command += ".exe";
        }
        for (String path: paths){
            File f = new File(path,command);
            if (f.isFile())
                return f.getAbsolutePath();
        }
        return command;
    }
    /**
     * Invoke the command sycnchrounously. Return its status code.
     * 
    
     * @return status code on exit.
     * @throws IOException when couldn't be invoked.
     * @throws IllegalStateException process is already invoked.
     */
    public int invokeAndWait() throws IOException {
        invoke();
        waitFor();
        return mExitCode;
    }
    
    public Process getProcess(){
        return mProcess;
    }
    
    /**
     * Invokes a command synchronously and captures
     * stdout and stderr appropriately. The method does
     * not return until the command is completed.
     * @param cmd the command and its arguments.
     * @param stdout an object to consume standard output
     *		text.
     * @param stderr an object to consume standard error
    		text.
     * @param envp the environment to be passed to the process, or <code>null</code> if the
     * caller's environment is to be used.
     * @return the exit code after the command completes.
     * @exception IOException if command can't be invoked.
     */
    public static int invoke(String cmd[], Writer stdout, Writer stderr, String[] envp)
        throws IOException {
        Command c = new Command(cmd, stdout, stderr, envp);
        return c.invokeAndWait();
    }
    
    /**
     * Invokes a command synchronously and captures
     * stdout and stderr appropriately. The method does
     * not return until the command is completed.
     * @param cmd the command and its arguments.
     * @param stdout an object to consume standard output
     *		text.
     * @param stderr an object to consume standard error
    		text.
     * @return the exit code after the command completes.
     * @exception IOException if command can't be invoked.
     */
    public static int invoke(String cmd[], Writer stdout, Writer stderr)
        throws IOException {
        return invoke(cmd,stdout,stderr,null);
    }
    /**
     * Start the command running, and then return immediately.
     * @throws IOException couldn't invoke command.
     * @throws IllegalStateException process is already invoked.
     */
    public void invoke() throws IOException {
        invokeIn(null,mEnvp);
    }
    
    /**
     * Start the command running, and then return immediately.
     * @param workingDir working directory for which to execute the process into.
     * @throws IOException couldn't invoke command.
     * @throws IllegalStateException process is already invoked.
     */
    public void invokeIn(File workingDir) throws IOException {   	
    	invokeIn(workingDir,mEnvp);
    }
    
    /**
     * Start the command running, and then return immediately.
     * @param workingDir working directory for which to execute the process into.
     * @param env the environment or <code>null</code> if the caller's environment is to be used.
     * @throws IOException couldn't invoke command.
     * @throws IllegalStateException process is already invoked.
     */
    public void invokeIn(File workingDir, String[]env) throws IOException {
        if (mProcess != null)
            throw new IllegalStateException("Process is running already");
        if(workingDir != null && !workingDir.exists())
            throw new IOException("Working directory does not exist");
        
        String[] command = mCommand;
        if (env != null){
            String cmd = computePathFromEnvironment(command[0],env);
            if (cmd != command[0]){
                // We've compute an absolute path to the command from the PATH environment
                // string. Use that.
                command = new String[mCommand.length];
                System.arraycopy(mCommand,1,command,1,mCommand.length-1);
                command[0] = cmd;
            }
        }
        mProcess = Runtime.getRuntime().exec(command, env, workingDir);

        assert mProcess != null;
        InputStream out = mProcess.getInputStream();
        InputStream err = mProcess.getErrorStream();

        mStdOutReader = new ReadingThread(out, mStdout,"Stdout Reader");
        mStdErrReader = new ReadingThread(err, mStderr,"Stderr Reader");
        mStdOutReader.start();
        mStdErrReader.start();
        mWaitThread = new Thread("Process waiter"){
            @Override
            public void run() {
                waitForProcessTermination();
            }
        };
        mWaitThread.start();
    }

    /**
     * Abort the command if it is running.
     */
    public void destroy() {
        if (mProcess != null) {
            mProcess.destroy();
            mExitCode = -1;
            mProcess = null;
        }
    }
    
    /**
     * Return whether or not command is completed.
     * @return whether or not command is completed.
     */
    public boolean isCompleted(){
        synchronized(mLock){
            return mProcess == null;
        }
    }

    /**
     * Wait for the process to terminate with a timeout value, if it isn't already,
     *  and  return its
     * exit code. 
     * <P>
     * Upon timemout, the function returns -1; but this is also a valid
     * exit value. Thus, the caller should invoke {@link #isCompleted()}
     * to determine if the process indeed terminated.
     * @param timeout milliseconds before we time out, or 0 if 
     * there is to be no timeout.
     * @return exit code of the process, or -1 if we timed out.
     */
    public int waitFor(long timeout) {
        synchronized(mLock){
            if (mProcess != null) {
                try {
                    mLock.wait(timeout);
                }
                catch (InterruptedException e) { }
            }
            return mExitCode;  
        }
    }
    
    /**
     * Wait indefinitely for the process to terminate.
     * @return the exit code of the process.
     */
    public int waitFor(){
        return waitFor(0);
    }
     
    private void waitForProcessTermination () {
        assert Thread.currentThread() == mWaitThread;
        try {
            mExitCode = mProcess.waitFor();
        }
        catch (InterruptedException x) {
            mExitCode = -1; // should never happen.
        }
        finally {
            mProcess = null;
            try {
                mStdOutReader.join();
                mStdErrReader.join();
            }
            catch (InterruptedException x) {
            }
            try {
                mStdout.flush();
                mStderr.flush();
            }
            catch (IOException x) {
            }
            synchronized (mLock) {
                mLock.notifyAll();
            }
        }
    }

    public static int invoke(String cmd[]) throws IOException {
        return invoke(cmd, null, null);
    }

    /**
     * Instances of this class are threads that monitor
     * the stdout and stderr of the command being invoked.
     * They are run as threads so that the TextConsumer
     * object can see lines immediately as they are produced.
     */
    static class ReadingThread extends Thread {
        ReadingThread(InputStream in, Writer consumer, String name) {
            super(name);
            _reader = new BufferedReader(new InputStreamReader(in));
            _consumer = consumer;
            // Prevent this thread from hogging the AWT EventQueue.
            setPriority(Math.max(getPriority() - 1,Thread.MIN_PRIORITY));
        }
        @Override
        public void run() {
            try {
                while (true) {
                    String line = _reader.readLine();
                    if (line == null) {
                        // Avoid "Pipe broken" IOException if we're writing to a pipe.
                        // The other end will receive such an exception if this thread
                        // terminates before closing it.
                        if (_consumer instanceof PipedWriter)
                            _consumer.close();
                        break;
                    }
                    _consumer.write(line);
                    _consumer.write("\n");
                    _consumer.flush();
                }
            } catch (IOException x) {
                System.err.println(">>>Input Error: " + x.getMessage() + '\n');
            }

        }
        private BufferedReader _reader;
        private Writer _consumer;
    }

    /**
     * Test this class by having it invoke commands
     */
    public static void main(String args[]) {
        if (args.length > 0) {
            try {
                invoke(args);
            } catch (IOException x) {
                System.err.println("Can't invoke " + args[0]);
            }
        }
    }
}
