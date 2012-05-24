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
import java.io.InterruptedIOException;

/**
 * We wrap the stdout and stderr stream of SeeCode engine process
 * so that we can simulate a close when the debuggee terminates.
 * Otherwise, consumers will wait forever since the streams are
 * really tied to the engine process itself.
 * <P>
 * Also, during program startup, a SeeCode plugin may spew out
 * massive amounts of debug information before the GUI can start
 * reading it. If we don't consume it, it will hang. So we do it here.
 * @author David Pickens
 */
class EngineInputStreamWrapper extends InputStream {

    private InputStream _input;
    private Thread _thread;
    private byte[] _buffer;
    private int _bufIndex = 0;
    private int _bufEnd = 0;
    private boolean _closed = false;
    private int _bytesRead = 0;
    private IOException mException = null;
    private static final int MAX_BUFFER_SIZE = 256000;
    private static final int INIT_BUFFER_SIZE = 4096;
    EngineInputStreamWrapper(InputStream in, String name){
        _input = in;
        _buffer = new byte[INIT_BUFFER_SIZE];
        _thread = new Thread(new Runnable(){

            @Override
            public void run() {
                doReadLoop();
                
            }},"DebuggeeInputStream:" +name);
        _thread.start();
    }
    /* (non-Javadoc)
     * @see java.io.InputStream#read()
     */
    @Override
    public synchronized int read() throws IOException {
        if (!waitForBytes()) return -1;
        int result = _buffer[_bufIndex++] & 0xFF;
        notifyAll();
        _bytesRead++;
        return result;
    }
    /**
     * @throws IOException
     */
    private boolean waitForBytes() throws IOException {
        while (_bufIndex >= _bufEnd && !_closed){
            try {
                //NOTE: if we wait forever, we can get in a deadlock
                // because the calling thread, OutputStreamMonitor, closes by
                // simply terminating its run loop without interrupting it!
                // You could argue that this is a bug, but the OutputStreamMonitor code
                // is within the core Eclipse and we can't fix it.
                // Thus, we wake up every few seconds to see if we've been closed.
                //
                // ADDENDUM: we fixed things so that when the pipe is dropped or
                // returns EOF, it calls "close()", which wakes things up.
                // But we check things every 10 seconds just in case.
                wait(10000);
            } catch (InterruptedException e) {
                if (_closed){
                    return false;
                }
            }
            if (mException != null){
                IOException x = mException;
                mException = null;
                throw x;
            }
        }
        return !_closed;
    }
    /* (non-Javadoc)
     * @see java.io.InputStream#available()
     */
    @Override
    public synchronized int available() throws IOException {
         return _bufEnd-_bufIndex;
    }
    /* (non-Javadoc)
     * @see java.io.InputStream#close()
     */
    @Override
    public synchronized void close() {
        if (!_closed){
            _closed = true;
            notifyAll();
            if (_thread != Thread.currentThread())
                _thread.interrupt();
        }
    }
    /* (non-Javadoc)
     * @see java.io.InputStream#read(byte[], int, int)
     */
    @Override
    public synchronized int read(byte[] buf, int start, int length) throws IOException {
        if (!waitForBytes()) return -1;
        // To avoid deadlock in the calling thread, we don't hang indefinitely waiting
        // for input. Instead, we return 0 bytes after so-many seconds so that the
        // calling thread (instance of OutputStreamMonitor) doesn't hang when it
        // is being forced to close.
        // See comment about in watiForBytes.
        int amount = Math.min(_bufEnd - _bufIndex,length);
        if (amount > 0){
            System.arraycopy(_buffer,_bufIndex,buf,start,amount);
            _bufIndex += amount;
            notifyAll(); // wakeup thread that is reading.
        }
        _bytesRead += amount;
        return amount;
    }
    
    private void doReadLoop(){
        while (!_closed){
            synchronized(this){
                while (!_closed){
                    if (_bufIndex >= _bufEnd){
                        _bufIndex = _bufEnd = 0;
                        break;
                    }
                    else if (_bufIndex > 200 && _buffer.length-_bufEnd < 200){
                        //Shift things down to make room.
                        int len = _bufEnd-_bufIndex;
                        System.arraycopy(_buffer,_bufIndex,_buffer,0,len);
                        _bufIndex = 0;
                        _bufEnd = len;
                        break;
                    }
                    else if (_bufEnd == _buffer.length){
                        //NOTE: if we have overflowed out buffer
                        // and nothing has yet been read, then
                        // the debugger is likely emitting debug
                        // information before the process is instantiated.
                        // If we don't consume it, the debugger will hang.
                        // So, if nothing has yet been read, then expand
                        // the buffer until we reach its limit, then start
                        // truncating
                        if (_bytesRead == 0) {
                            int newLength = Math.min(_buffer.length*2,MAX_BUFFER_SIZE);
                            if (newLength > _buffer.length){
                                byte[] newBuffer = new byte[newLength];
                                System.arraycopy(_buffer,0,newBuffer,0,_buffer.length);
                                _buffer = newBuffer;
                            } else {
                                //Truncate off the top
                                int truncCount = MAX_BUFFER_SIZE/10;
                                _bufEnd -= truncCount;
                                System.arraycopy(_buffer,truncCount,_buffer,0,_bufEnd);
                            }
                            break;
                        }
                        else {
                            try {
                                wait();
                            }
                            catch (InterruptedException e1) {
                            }
                        }
                        if (_closed) return;
                    }
                    else break;
                }
            }
            int cnt = 0;
            try {
                cnt = _input.read(_buffer,_bufEnd, _buffer.length-_bufEnd);
            } catch (InterruptedIOException e){
                continue;
            } catch (IOException e) {
                //Don't clutter error log with "dropped pipe" exceptions
                // as we close things.
                if (!_closed){
                   mException = e;
                   close();
                }
                break;
            }
            if (cnt <= 0){
                close();
                break;
            }
            synchronized(this){
                _bufEnd += cnt;
                this.notifyAll();
            }
            
        }
    }
}
