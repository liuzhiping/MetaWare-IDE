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
import org.eclipse.cdt.debug.core.cdi.event.ICDIDestroyedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIDisconnectedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.event.ICDIExitedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIRestartedEvent;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;


/**
 * This is a contrived "Process" that respresents the
 * process that the debugger is debugging.
 * It uses the stdout and stderr of the engine process.
 * @author David Pickens
 */
class DebuggeeProcess extends Process {

    private Process mEngineProcess;
    private Target mTarget;
    private boolean mTerminated = false;
    private PipedOutputStream mStdoutStream = null;
    private PipedInputStream mInput = null;
    private PipedOutputStream mStderrStream = null;
    private PipedInputStream mErrorInput = null;
    private OutputStream mOutputStream;

    /**
     * 
     */
    public DebuggeeProcess(Target target, Process engineProcess) {
        mEngineProcess = engineProcess;
        mTarget = target;
        target.getSession().getEventManager().addEventListener(new ICDIEventListener(){

            @Override
            public void handleDebugEvents(ICDIEvent[] event) {
                for (int i = 0; i < event.length; i++){
                    ICDIEvent e = event[i];
                    if (e.getSource() == mTarget){
                        if (e instanceof ICDIDestroyedEvent || e instanceof ICDIExitedEvent || e instanceof ICDIDisconnectedEvent){
                            onTerminated();
                        }
                        else if (e instanceof ICDIRestartedEvent){
                            synchronized(DebuggeeProcess.this){
                                mTerminated = false;
                            }
                        }
                    }                   
                }
                
            }});
        //      NOTE: we want to read the stderr or stdout stream from the
        // engine process as the stderr/stdoiut stream for the debuggee.
        // But when the debuggee is terminated, we must terminate the
        // stream or else we get stream consumers waiting forever for
        // the stream to close.
        // So we create a wrapper.
        
    }
    
    private synchronized void onTerminated() {
        // We could be called more than once due to
        // a race condition in EventManager's firing of
        // target-terminated event, and the call to
        // "isTerminated" from another thread after
        // the target has terminated but before 
        // the event has fired.
        //
        if (!mTerminated) {
            try {
                if (mStdoutStream != null) mStdoutStream.close();
                if (mStderrStream != null) mStderrStream.close();
            }
            catch (IOException e) {
            }
            mTerminated = true;
            //No! we must not terminate threads that read process stdout and stderr
            // streams until the engine itself dies. Otherwise, the engine may hang
            // while flushing stdout. (The Microsoft C runtime will go into a spin loop!)
            // CR1860
            //closeInputThreads();
            notifyAll();
        }
    }
    

    /*
     * (non-Javadoc)
     * @see java.lang.Process#exitValue()
     */
    @Override
    public int exitValue() {
        if (!isTerminated())
            throw new IllegalThreadStateException("Process not terminated");
        return mTarget.getEngineInterface().getProcessExitCode();
    }
    
    private boolean isTerminated(){
        // During sudden shutdown of eclipse, plugins shutdown
        // in random order. We could have the target process terminate
        // after the event manager has shutdown. Thus, we would not
        // see the termination event. Check for this case.
        synchronized(this){
            if (!mTerminated && mTarget.isTerminated()){
                onTerminated();
                assert mTerminated;
            }
        return mTerminated;
        }
        
    }

    /* (non-Javadoc)
     * @see java.lang.Process#waitFor()
     */
    @Override
    public int waitFor() throws InterruptedException {
        synchronized(this){
            while (!mTerminated){
                // Will awaken when DestroyEvent is
                // encountered for the process.
                wait();
            }
            return mTarget.getEngineInterface().getProcessExitCode();
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Process#destroy()
     */
    @Override
    public void destroy() {
        try {            
            if (!isTerminated())
                mTarget.terminate();
        } catch (CDIException e) {
            SeeCodePlugin.log(e);
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

    /* (non-Javadoc)
     * @see java.lang.Process#getOutputStream()
     */
    @Override
    public OutputStream getOutputStream () {
        if (mTarget.getEngineInterface().supportsWritingStdin()) {
            if (mOutputStream == null) {
                mOutputStream = new OurOutputStream(mTarget.getEngineInterface());
            }
            return mOutputStream;
        }
        else
            return mEngineProcess.getOutputStream();
    }
    
    static class OurOutputStream extends OutputStream {
        private EngineInterface _engine;
        private byte[] _buffer = null;
        private int _bufferPointer = 0;
        OurOutputStream(EngineInterface engine){
            _engine = engine;
        }

        @Override
        public void close () throws IOException {
            flush();
        }

        @Override
        public void flush () throws IOException {
            if (_bufferPointer > 0){
                byte[] data = _buffer;
                if (_bufferPointer < _buffer.length){
                    data = new byte[_bufferPointer];
                    System.arraycopy(_buffer,0,data,0,_bufferPointer);
                }
                try {
                    _engine.writeStdin(data);
                }
                catch (EngineException e) {
                    throw new IOException(e.getMessage()/*,e*/);
                }
                _bufferPointer = 0;
            }
        }

        @Override
        public void write (byte[] b, int off, int len) throws IOException {
            flush();
            byte[] data = b;
            if (off != 0 || len != b.length){
                data = new byte[len];
                System.arraycopy(b,off,data,0,len);
            }
            try {
                _engine.writeStdin(data);
            }
            catch (EngineException e) {
                throw new IOException(e.getMessage()/*,e*/);
            }
        }

        @Override
        public void write (int b) throws IOException {
            if (_buffer == null) {
                _buffer = new byte[4096];
            }
            if (_bufferPointer < _buffer.length){
                _buffer[_bufferPointer++] = (byte)b;
            }
            if (b == '\n'){
                flush();
            }
            
        }
        
    }
}
