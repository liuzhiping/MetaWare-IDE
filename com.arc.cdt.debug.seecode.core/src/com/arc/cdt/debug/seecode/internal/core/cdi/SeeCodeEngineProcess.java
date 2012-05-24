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
package com.arc.cdt.debug.seecode.internal.core.cdi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.core.runtime.IAdaptable;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.seecode.command.CommandFactory;
import com.arc.seecode.command.ICommandProcessor;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;

/**
 * The debugger's session process. Its output and error streams are those
 * produced by the debugger engine itself. The {@link DebuggeeProcess}is
 * similar but its output and error streams are the stdout and stderr of the
 * program being debugged.
 * 
 * @author David Pickens
 */
public class SeeCodeEngineProcess extends Process implements IAdaptable{

    private Process mEngineProcess;

    private PipedOutputStream mStdoutStream;

    private PipedInputStream mInput = null;

    private PipedOutputStream mStderrStream;

    private PipedInputStream mErrorInput = null;

    private OutputStream mOutput = null;

    //private ICommandProcessor mCommandParser = null;

    private Session mSession;

    /**
     *  
     */
    public SeeCodeEngineProcess(Process engineProcess, Session session) {
        mEngineProcess = engineProcess;
        mSession = session;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Process#exitValue()
     */
    @Override
    public int exitValue() {
        return mEngineProcess.exitValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Process#waitFor()
     */
    @Override
    public int waitFor() throws InterruptedException {
        return mEngineProcess.waitFor();
    }

    public boolean isShutdown() {
        return mSession.isShutdown();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Process#destroy()
     */
    @Override
    public synchronized void destroy() {
        //NOTE: there is a timing bug in the infrastructure.
        // When a launch is terminated, this method is
        // ordinarily called prior to the engine having a chance
        // to shut itself down!
        // To fix this, we added our own ProcessFactory
        // and arrange to call "shutdown()" first.
        try {
            closeStreams();
        } catch (IOException e) {
            SeeCodePlugin.log(e);
        }
        mEngineProcess.destroy();
    }

    synchronized void closeStreams() throws IOException {
		if (mInput != null) {
		    mInput.close();
		    mInput = null;
		}

		if (mErrorInput != null) {
		    mErrorInput.close();
		    mErrorInput = null;
		}
		if (mStderrStream != null) {
		    mStderrStream.close();
		    mStderrStream = null;
		}
		if (mStdoutStream != null) {
		    mStdoutStream.close();
		    mStdoutStream = null;
		}
	}

    /**
     * Wait for the process to die as a result of an ordinary shutdown.
     * 
     * @param timeout
     *            timeout in milliseconds.
     * @return true if process terminated before timeout expired.
     */
    public boolean waitForProcessTermination(int timeout) {

        return mSession.waitForProcessDeath(timeout);
    }

    /**
     *  
     */
    public void shutdown() {

        if (!mSession.isShutdown()) {
            try {
                mSession.terminate();
            } catch (CDIException e) {
                SeeCodePlugin.log(e);
            }
        }
    }

    /**
     * Return the stream to which the engine writes output messages.
     * 
     * @return the stream to which the engine writes output messages.
     */
    OutputStream getProcessOutputStream() {
        getInputStream(); // make sure initialized.
        assert mStdoutStream != null;
        return mStdoutStream;
    }

    /**
     * Return the stream to which the engine writes error messages.
     * 
     * @return the stream to which the error writes output messages.
     */
    OutputStream getProcessErrorStream() {
        getErrorStream(); // make sure initialized.
        assert mStderrStream != null;
        return mStderrStream;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Process#getErrorStream()
     */
    @Override
    public InputStream getErrorStream() {
        if (mErrorInput == null) {
            try {
                mStderrStream = new PipedOutputStream();
                mErrorInput = new OurPipedInputStream(mStderrStream);
            } catch (IOException e) {
            }
        }
        return mErrorInput;
    }

    /**
     * Arrange to read the log output of the engine.
     * 
     * @see java.lang.Process#getInputStream()
     */
    @Override
    public InputStream getInputStream() {
        if (mInput == null) {
            try {
                mStdoutStream = new PipedOutputStream();
                mInput = new OurPipedInputStream(mStdoutStream);
            } catch (IOException e) {
            }
        }
        return mInput;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Process#getOutputStream()
     */
    @Override
    public OutputStream getOutputStream() {
        if (mOutput == null) {
            EngineInterface e = ((Target) mSession.getTargets()[0])
                    .getEngineInterface();
            ICommandProcessor cp;
            try {
                cp = CommandFactory.createCommandProcessor(e,
                        getProcessOutputStream(), getProcessErrorStream());
            } catch (EngineException e1) {
                SeeCodePlugin.log(e1);
                cp = null;
            }
            final ICommandProcessor cp_ = cp;
            mOutput = new OutputStream() {
                private StringBuffer _buf = new StringBuffer(256);

                private boolean _backslashPending = false;
                
                @Override
                public void write(int c) throws IOException {
                    if (cp_ != null) {
                        if (c == '\n' || c == '\r') {
                            if (_backslashPending) {
                                // do nothing
                            } else if (_buf.length() > 0) {
                                String cmd = _buf.toString().trim();
                                try {
                                    cp_.processCommand(cmd);
                                    mStdoutStream.flush();
                                    mStderrStream.flush();
                                }
                                catch (Exception x) {
                                    SeeCodePlugin.log(x);
                                }
                                _buf.setLength(0);
                            }
                        } else {
                            if (_backslashPending) {
                                _buf.append('\\');
                                _backslashPending = false;
                            }
                            if (c == '\\') {
                                _backslashPending = true;
                            } else
                                _buf.append((char) c);
                        }
                    }
                }
            };
        }
        return mOutput;
    }

	@SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class adapter) {
		if (adapter.isInstance(this)) return this;
		return mSession.getAdapter(adapter);
	}

}
