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
package com.arc.seecode.server;

import java.awt.EventQueue;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.eclipse.core.runtime.Platform;

import com.arc.mw.util.ILogger;
import com.arc.mw.util.Log;
import com.arc.mw.util.StringUtil;
import com.arc.mw.util.Toggle;
import com.arc.seecode.cmpd.CMPDFactory;
import com.arc.seecode.cmpd.ICMPDController;
import com.arc.seecode.connect.ConnectionFactory;
import com.arc.seecode.connect.IConnection;
import com.arc.seecode.connect.SocketTransport;
import com.arc.seecode.connect.TimeoutException;
import com.arc.seecode.connect.VMDisconnectedException;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;
import com.arc.seecode.engine.IBreakpointObserver;
import com.arc.seecode.engine.ICustomDisplayCallback;
import com.arc.seecode.engine.IEngineAPI;
import com.arc.seecode.engine.IEngineAPIObserver;
import com.arc.seecode.engine.IEnginePoller;
import com.arc.seecode.scwp.ScwpCommandPacket;
import com.arc.seecode.scwp.ScwpReplyPacket;
import com.arc.seecode.serialize.IMethodFilter;
import com.arc.seecode.serialize.IMethodSerializer;
import com.arc.seecode.serialize.MethodSerializationHandler;

/**
 * When the SeeCode engine is launched in a separate VM, this is the main
 * program that gains control.
 * 
 * @author David Pickens
 */
public class Server {

    private static final Toggle sTrace = Toggle.define("SERVER", false);
    
    private static int CALLBACK_QUEUE_SIZE_HIGH_WATER = 50;

    private MethodSerializationHandler[] mDispatchers; // indexed by CMPD index

    private SocketTransport mTransport;

    private boolean mShutdown = false;
    
    //private IEngineAPI mEngine;

    private IConnection mConnection;

    private EngineAPIObserver mEngineObserver;

    private CallbackThread mCallbackThread;

    private IEngineRunQueue mQueue;
    
    // Size of queue when we enqueued last command with timeout.
    // Make it large so that first one will be emitted.
    private int mQueueSizeForLastTimeoutCommand = 100;
    private int mCallbackQueueSizeForLastTimeoutComment = 1000;
    
    private IEngineAPI fEngine; // First or process reference.

    /**
     * We attach before loading the DLL so that we can properly
     * diagnose linkage errors.
     * <P>
     *  @param transport the connected TCP transport
     *  @param pid the CMPD process IDs to be assigned (or {1} if non-CMPD).
     *  
     */
    public Server(SocketTransport transport, int pid[], int highestPid) throws EngineException {    
        mTransport = transport;
 
//      trace("Server started; port=" + port);
        EngineInterface proxies[] = EngineInterface.CreateServer(pid);
        
        if (isTracing()) trace(proxies.length + " EngineInterface proxies created");
        
        IEnginePoller poller;
        if (proxies.length == 1) {
          //FIX: we assume "api" implements IEnginePoller
            poller = (IEnginePoller)proxies[0].getAPI();
        }
        else {
            poller = new CompositePoller(proxies);
        }
        fEngine = proxies[0].getAPI();
        
        mQueue = new EngineThread(poller,proxies[0],hasSimulator(proxies));
        
        // 0 is the CMPD controller
        mDispatchers = new MethodSerializationHandler[highestPid+1];

        // Pass toggle settings on to engine
        Toggle[] toggles = Toggle.getToggles();
        for (int i = 0; i < toggles.length; i++) {
            trace("setting toggle " + toggles[i].getName());
            // Toggles are session-wide; so have a single proxy read them.
            proxies[0].setToggle(toggles[i].getName(), toggles[i].on());
        }
        mConnection = ConnectionFactory.makeConnection(mTransport);
        
        mCallbackThread = new CallbackThread(mConnection);

        IMethodFilter filter = new IMethodFilter() {

            @Override
            public boolean includeMethod(Method method) {
                return !method.getName().endsWith("Observer")
                        && !method.getName().endsWith("Callback");
            }
        };
        
        // Object 0 is the CMPD controller. It only exists if we have a CMPD session
        // and the engine is late enough to have the new CMPD support. (version 16)
        if (pid.length > 1 && proxies[0].getAPI().getEngineInterfaceVersion() > 15){
            mDispatchers[0] = new MethodSerializationHandler(CMPDFactory.createLocal(),ICMPDController.class,filter,null,null);
        }      
        
        for (int i = 0; i < proxies.length; i++) {
            int processID = pid[i];
            IBreakpointObserver b = new BreakpointObserver(mCallbackThread,processID);
            ICustomDisplayCallback m = new CustomDisplayCallback(mCallbackThread,
                mConnection,processID);
            IEngineAPIObserver observer = new EngineAPIObserver(this,mCallbackThread,processID);
            IEngineAPI api = proxies[i].getAPI();
            api.setBreakpointObserver(b);
            api.setCustomDisplayCallback(m);
            api.setEngineObserver(observer);
            mDispatchers[processID] = new MethodSerializationHandler(api,
                IEngineAPI.class,filter,proxies[i].getJavaFactory(),proxies[i].getTypeFactory());
        }
        
        mEngineObserver = new EngineAPIObserver(this,mCallbackThread,1); // for Errors
    }
    
    private static boolean hasSimulator(EngineInterface engines[]){
        for (EngineInterface engine: engines){
            try {
                if (engine.isSimulator()) return true;
            }
            catch (EngineException e) {
               return false; //shouldn't happen
            }
        }
        return false;
    }

    /**
     * Called by {@link EngineAPIObserver#engineShutdown(IEngineAPI)}when the
     * engine is shutting down.
     *  
     */
    public void shutdown() {
    	if (isTracing()) trace("Shutdown called");
        mShutdown = true;
        if (mServerThread != null && mServerThread != Thread.currentThread()){
        	mServerThread.interrupt(); // Wake it from waiting for TCP/IP packet.
        }
    }
    
    /**
     * Return whether or not we're to continue reading commands from the GUI.
     * @return false if we're to shutdown the server.
     */
    private boolean isActive() {
        if (mConnection.isDisconnected()) return false;
        if (!mShutdown) return true;
        // if there is a command pending from the engine, go ahead and service it to avoid deadlock.
        if (mConnection.isCommandAvailable()) {
        	return true;
        }
        // Before shutting down, wait for callback queue to drain.
        // It may be stuck waiting to call back into the engine so we check for that.
        // If so, return true so that we can prevent deadlock.
        int lastQueueSize = mCallbackThread.getQueueSize();
        try {
            int changeCount = 0;
            int MAX_CHANGE_COUNT = 4;
            while (lastQueueSize > 0 && mCallbackThread.waitUntilChange(1000)){            	
                if (mConnection.isDisconnected()) return false;
                int thisQueueSize = mCallbackThread.getQueueSize();
                if (isTracing()){
            		trace("After shutdown, callback queue is of size " + thisQueueSize);
            	}
                if (thisQueueSize >= lastQueueSize) {
                    //Queue not shrinking; this means that the GUI is probably calling back
                    // into the engine.
                    if (++changeCount >= MAX_CHANGE_COUNT)
                        return mConnection.isCommandAvailable();
                }
                if (mConnection.isCommandAvailable()) return true;
                lastQueueSize = thisQueueSize;
            }
        }
        catch (InterruptedException e) {
        }
        if (mConnection.isDisconnected()) return false;
        return mConnection.isCommandAvailable(); //shutdown pending
    }
    
    private final static byte[] BOOLEAN_TRUE;
    private final static byte[] BOOLEAN_FALSE;
    static {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        DataOutputStream outStream = new DataOutputStream(bao);
        try {
            outStream.writeBoolean(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BOOLEAN_TRUE = bao.toByteArray();  
        outStream = new DataOutputStream(bao);
        try {
            outStream.writeBoolean(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BOOLEAN_FALSE = bao.toByteArray();       
    }
    
    private RuntimeException mPendingException = null;
    private Error mPendingError = null;

	private Thread mServerThread = null;
    
    private void terminate(){
    	trace("terminated called");
        mQueue.shutdown();
    }
    
    //private static final int CALLBACK_QUEUE_DRAIN_TIME = 30000;

    private void runReadAndDispatchLoop() throws Throwable{
    	mServerThread  = Thread.currentThread();
        trace("entering read loop");
        // Loop until shutdown and no more commands are queued up from the GUI.
        while (isActive()) {
        	if (mShutdown && isTracing()){
        		trace("Shutdown active but commands still pending from connection");
        	}
            if (mPendingException != null) throw mPendingException;
            if (mPendingError != null) throw mPendingError;
            try {
                ScwpCommandPacket cmd = mConnection.readCommand();
                final String methodName = cmd.getMethodName();
                final DataInputStream inStream = cmd.dataInStream();
                final ByteArrayOutputStream bao = new ByteArrayOutputStream();
                final DataOutputStream outStream = new DataOutputStream(bao);
                //Create reply packet.
                final ScwpReplyPacket reply = new ScwpReplyPacket(cmd.getId());
                final IMethodSerializer methodSerializer = mDispatchers[computeProcessID(cmd.getObject())].getSerializerFor(methodName);
                //If method is "asynchronous" we reply immeidately.
                // It may need to free up the main event thread
                // of the caller so that an engine's callback can
                // popup a GUI display.
                boolean isAsync = false;
                if (methodSerializer.isAsynchronous()){
                    mConnection.sendReply(reply);
                    isAsync = true;
                    if (isTracing())
                        trace("SERVER LOOP: ascync reply to " + methodName + "(pid=" + computeProcessID(cmd.getObject())+")");
                }
                if (isTracing())
                    trace("SERVER LOOP: invoke " + methodName + "(pid=" + computeProcessID(cmd.getObject())+")");
                //Engine is single-threaded. Enforce this.
                // However, "isActive()" is called when the
                // engine may be blocked in a "read-from-stdin".
                // So special case that to avoid locking the caller.

                if (methodName.equals("isActive")) {
                    reply.setData(BOOLEAN_TRUE);
                    mConnection.sendReply(reply);
                    // Can't call dispatcher without lock
                    // because of race conditions of
                    // callbacks. The "JNIEnv" pointer
                    // is set by each call!
                    //mDispatcher.invoke(methodName, inStream, outStream);
                }
                else {
                    
                    // "sendValueUpdate2" has a timeout. If non-zero, we don't want to emit
                    boolean hasTimeout = methodName.equals("sendValueUpdate2");
                    // The timeout is the last argument, but we don't want to go to the trouble
                    // of extracting it. So, wait just a small amount of time to avoid 
                    // slowing down the UI. Then notice if the queue is getting bigger since
                    // the last command with a time out.
                    boolean expired = false;

                    if (hasTimeout) {
                        if (!mQueue.waitUntilEmpty(100) && mQueueSizeForLastTimeoutCommand < mQueue.getQueueSize() ||
                            mCallbackThread.getQueueSize() > CALLBACK_QUEUE_SIZE_HIGH_WATER &&
                            mCallbackThread.getQueueSize() > mCallbackQueueSizeForLastTimeoutComment){                        
                            expired = true;
                        }
                        else {
                            mQueueSizeForLastTimeoutCommand = mQueue.getQueueSize();
                            mCallbackQueueSizeForLastTimeoutComment = mCallbackThread.getQueueSize();
                        }
                        if (!isAsync){
                            reply.setData(expired?BOOLEAN_FALSE:BOOLEAN_TRUE);
                            mConnection.sendReply(reply);
                            isAsync = true; // we already return the result  before enqueuing.
                        }   
                    }
                    if (!expired) {
                        final boolean isAsyncCopy = isAsync;
                        mQueue.enqueue(new IEngineInvocation() {

                            @Override
                            public void invoke () throws EngineException {
                                try {
                                    int startCallbackSize = mCallbackThread.getQueueSize();
                                    int startQueueSize = mQueue.getQueueSize();
                                    // System.out.printf(">>>Invoking %s\n",methodName);
                                    methodSerializer.invoke(inStream, outStream);
                                    if (!isAsyncCopy) {
                                        // NOTE: outStream writes to "bao" stream.
                                        byte[] result = bao.toByteArray();
                                        reply.setData(result);
                                        mConnection.sendReply(reply);
                                    }
                                    // EXPLANATION:
                                    // the call to "invoke" invokes a method in the SeeCode engine.
                                    // The engine may have replied by enqueuing callback requests.
                                    // In updating views, there could be scores of such requests.
                                    // We don't want the callback queue to overrun if the UI can't
                                    // service it fast enough.
                                    // If we don't insert stalls, the callback queue could get into
                                    // the hundreds or even thousands.
                                    //
                                    // Here is the rub: if we are naive in inserting stalls, we end
                                    // up stalling the UI. Here is the reason: the UI may be calling back
                                    // into the engine
                                    // as it services a callback request. Thus, the callback queue
                                    // won't start shrinking until such a recursive call can get
                                    // serviced.
                                    //
                                    // So, here is what we do. We remember the callback queue size
                                    // before the last call into the engine. Anything above that
                                    // was added by this last call. But, as we have said, the UI
                                    // callback handler may be making another call into the engine
                                    // which will eventually end up in this queue (mQueue).
                                    // If mQueue size is 0, and seems to stay that way, then
                                    // the UI doesn't appear to be waiting for an engine response.
                                    // So we stall the callback queue.
                                    // If the mQueue size is > 0, then we don't stall unless we
                                    // see the callback queue shrinking. We must be sensitive to
                                    // not locking the UI.
                                    if (mQueue.getQueueSize() == startQueueSize) {
                                        int highwater = startCallbackSize + CALLBACK_QUEUE_SIZE_HIGH_WATER;
                                        int cnt = 0;
                                        // Stall in 100 millisecond increments until UI enqueues a command
                                        // and the callback queue doesn't shrink or
                                        // the callback queue goes below the highwater.
                                        int lastCallbackQueueSize = mCallbackThread.getQueueSize();
                                        while (!mCallbackThread.stallUntilQueueSizeIsAt(highwater, 100) && cnt++ < 10) {
                                            int qs = mCallbackThread.getQueueSize();
                                            // if callback queue is not being consumed, and there
                                            // are other engine commands queued, then let things go.
                                            // We don't want to stall the UI.
                                            if (qs >= lastCallbackQueueSize && mQueue.getQueueSize() > startQueueSize)
                                                break;
                                            lastCallbackQueueSize = qs;
                                        }
                                    }
                                }
                                catch (TimeoutException e) {
                                    System.err.println("Reply to client timed out");
                                }
                                catch (VMDisconnectedException e) {
                                    // Will be presumably be caught later
                                }
                                catch (EngineException e) {
                                    mEngineObserver.displayError(fEngine, e.getMessage());
                                }
                                catch (RuntimeException e) {
                                    mPendingException = e;
                                }
                                catch (Error e) {
                                    mPendingError = e;
                                }
                                catch (Throwable e) {
                                    e.printStackTrace();
                                    displayError("Unexpected exception in seecode engine server: " + e.toString());
                                }

                            }
                        });
                    }
                    
                }
             

                trace("SERVER LOOP: reply sent");

                //Check if breakpoint or engine observers were
                // called and a timeout exception occurred.
                Exception e = mCallbackThread.getExceptionAndClear();
                if (e instanceof TimeoutException) {
                    throw (TimeoutException) e;
                }
                
              /*  //
                // If shutdown occurred, but there is callbacks that haven't been
                // serviced, then keep reading commands to avoid deadlock if the
                // callback itself is making a call back into the engine.
                if (mShutdown && !mConnection.isDisconnected()) {
                     // wait for things to drain
                    // There may be calls back into the engine that we need to service.
                    long start = System.currentTimeMillis();
                    while (!mCallbackThread.waitUntilEmpty(500)){
                        if (mConnection.isDisconnected() || mConnection.isCommandAvailable() ||
                            System.currentTimeMillis() - start > CALLBACK_QUEUE_DRAIN_TIME)
                            break;              
                    }
                }*/
            } catch (InterruptedException e) {
                //Someone apparently is shutting us down.
            } catch (TimeoutException e) {
                System.err.println("Reply to client timed out");

            } catch (VMDisconnectedException e) {
                trace("Socket dropped");
                return;
            } catch (EngineException e) {
                mEngineObserver.displayError(fEngine, e.getMessage());
            } catch (RuntimeException e) {
                throw e;
            } catch (Error e) {
                throw e;
            } catch (Throwable e) {
                // Shouldn't get here
                e.printStackTrace();
                displayError("Unexpected exception in seecode engine server: " + e.toString());
                throw e;
            }
        }
        trace("About to terminate server read thread");
        if (!mConnection.isDisconnected()) {
            mConnection.shutdown();
            mTransport.close();
        }
    }
    
    /**
     * Return the process ID corresponding to the object ID received from a packet.
     * @param objectID the object ID
     * @return the corresponding process ID
     */
    private static int computeProcessID(int objectID) throws EngineException{
        if (objectID % ScwpCommandPacket.REQUIRED_CHANNELS == ScwpCommandPacket.ENGINE){
            return objectID / ScwpCommandPacket.REQUIRED_CHANNELS;
        }
        throw new EngineException("Bad object ID received in server: " + objectID);
        
    }

    private static void setLogger(final JTextArea area) {
        Log.setLogger(new ILogger() {
            private int lineCount = 0;
            @Override
            public void log(String fromWhere, String message) {
                final String txt = ""+(++lineCount)+":[" + fromWhere + "]" + message + "\n";
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        area.append(txt);
                        Document doc = area.getDocument();
                        if (doc.getLength() > 200000){
                            try {
                                doc.remove(0,10000);
                            } catch (BadLocationException e) {
                            }
                        }
                        area.setCaretPosition(doc.getLength());
                    }
                });

            }
        });
    }
    
    private static void displayError(String msg){
        JOptionPane.showMessageDialog(null,msg,"Debugger Engine Error",JOptionPane.ERROR_MESSAGE);
    }
    
    private static boolean isWindows () {
        return Platform.getOS().startsWith("win");
    }

    /**
     * args:
     * 
     * <pre>
     * 
     *     [ -X<i>toggle</i> -X<i>toggle</i> ... ] [-pid=i-j,k-l,...] <i>port-number</i> <i>crout.dll</i>
     *  
     * </pre>
     * Where:<p>
     *<pre>
     * <i>toggle</i> is a toggle to set in the spawned process.
     * <p>
     * <i>CMPD-count</i> is a count of CMPD processes if this is a CMPD process; each will be
     * referenced starting with 0 to <i>CMPD-count</i>-1.
     * </pre>
     * @param args
     */
    public static void main(String args[]) {
        //        final FileWriter out;
        //        try {
        //            out = new FileWriter("/tmp/server.log");
        //        } catch (IOException e1) {
        //            // TODO Auto-generated catch block
        //            e1.printStackTrace();
        //            return;
        //        }
        //        Log.setLogger(new ILogger(){
        //
        //            public void log(String fromWhere, String message) {
        //                try {
        //                    out.write("[");
        //                    out.write(fromWhere);
        //                    out.write("]");
        //                    out.write(message);
        //                    out.write("\n");
        //                } catch (IOException e) {
        //                    // TODO Auto-generated catch block
        //                    e.printStackTrace();
        //                }
        //                
        //            }});

        Log.setPrefix("SERVER");
        boolean hasTracing = false;
        int argIndex = 0;
        List<Integer> pidList = new ArrayList<Integer>();
        for (argIndex = 0; argIndex < args.length; argIndex++) {
            if (args[argIndex].startsWith("-X")) {
                Toggle.set(args[argIndex].substring(2), true);
                // Don't bring up trace pane unless its something other than enabling
                // JIT debugging.
                if (!args[argIndex].equals("-Xdebugbreak"))
                    hasTracing = true;
            } else if (args[argIndex].startsWith("-pid=")){
                try {
                    String ranges[] = args[argIndex].substring(5).split(",");
                    for (String r: ranges){
                        int lo,hi;
                        if (r.indexOf('-') > 0 || r.indexOf(':') > 0){
                            String pair[] = r.split("[:-]");
                            if (pair.length != 2) {
                                System.err.println("[DEBUGGER SERVER] Bad pid specification: " + r + " in " + args[argIndex]);
                                continue;
                            }
                            else{
                                lo = Integer.parseInt(pair[0]);
                                hi = Integer.parseInt(pair[1]);
                            }
                        }
                        else {
                            lo = Integer.parseInt(r);
                            hi = lo;
                        }
                        if (lo > hi) System.err.println("[DEBUGGER SERVER] Bad pid specification: " + r + " in " + args[argIndex]);
                        for (int i = lo; i <= hi; i++) pidList.add(i);
                    }
                }
                catch (NumberFormatException e) {
                    System.err.println("Bad CMPD setting: " + args[argIndex]);
                }
            } else {
                break;
            }
        }
        
        if (pidList.size() == 0) pidList.add(1); // non-CMPD has PID=1

        boolean badArgCount = argIndex + 2 != args.length;
        JTextArea area = null;
        if (hasTracing || badArgCount) {
            JFrame frame = new JFrame("Server Trace Log");
            area = new JTextArea();
            setLogger(area);
            JScrollPane scroller = new JScrollPane(area);
            frame.setContentPane(scroller);
            frame.setSize(400, 500);
            frame.setVisible(true);
            Log.log("Server","Received Args: " + StringUtil.arrayToArgString(args));
        }
        if (argIndex + 2 != args.length) {
            System.out.println("Args are: [-Xtoggle ...] [-pid=i:j,k:l,...] <port> <crout.dll>");
            System.out.println("Received arguments: " + StringUtil.arrayToArgString(args));
            System.exit(1);
        }
        //        if (isTracing())
        //            trace(Server.class.getName() + " invoked with port " + args[0]);
        int port = Integer.parseInt(args[argIndex]);
        String crout = args[argIndex + 1];
        SocketTransport transport = null;
        int exitcode = 0;
        try {         
            transport = new SocketTransport();
            //trace("About to attach to port " + port);
            transport.attach("localhost", port);
            /*
             * Load crout.dll so that native calls are resolved.
             * Notice that we do this after connecting so that
             * the gui is free to capture stdout and stderr
             */
            System.load(crout);
            int pids[] = new int[pidList.size()];
            int highestPid = 1;
            for (int i = 0; i < pids.length; i++){
                pids[i] = pidList.get(i);
                if (pids[i] > highestPid) highestPid = pids[i];

            }
            trace("Server connected!!! process count=" + highestPid);

            Server server = new Server(transport,pids,highestPid);

            trace("Done making server object");
            server.runReadAndDispatchLoop();
            server.terminate();
        } catch (UnsatisfiedLinkError e) {
            writeExceptionTrace(e);
            if (e.getMessage() != null && e.getMessage().indexOf("init") >= 0){
                displayError("The DLL \"" + crout + "\" does not appear to be uptodate.\n" +
                        "It does not have the required IDE-integration code.\n");
            }
            else {
				String extra = "";
				String path = System.getenv("PATH");
				if (path == null) {
					extra = "\n\nPossible reason: the \"PATH\" environment variable is not set";
				} else {
					File dir = new File(crout).getParentFile();
					if (dir != null) {
						boolean found = false;
						for (String s : path.split(File.pathSeparator)) {
							if (dir.equals(new File(s))) {
								found = true;
								break;
							}
						}
						if (!found) {
							extra = "\n\nPossible reason: the MetaWare Debugger's installation directory\n"
									+ "\""
									+ dir
									+ "\" is not in the search path."
									+ "\nPerhaps the PATH environment variable is not set correctly.";
						}
					}
				}
                displayError("Can't load DLL " + crout + ": " + e.getMessage() + extra);
            }         
            System.err.println("Can't load DLL \"" + crout + "\"");
            trace("Can't load DLL \"" + crout + "\"");
            exitcode = 1;
        } catch (IOException e) {
            writeExceptionTrace(e);
            System.out.flush();
            System.err.println("Server could not connect at port " + port);
            e.printStackTrace(System.err);
            String extra = "";
            if (isWindows() && System.getenv("SystemRoot") == null){
            	extra = "\n\nPossible reason: \"SystemRoot\" environment variable is not set";
            }
            displayError("Debugger engine could not connect to " + crout + "\nat port " + port + ":\n" +
            		e.getMessage() + extra);
            trace("Debugger engine could not connect at port " + port);
            exitcode = 1;
        } catch (IllegalStateException t){
            // Version mismatch
            writeExceptionTrace(t);
            displayError(t.getMessage());
            trace(t.getMessage());
            exitcode = 1;
        } catch (Throwable t) {           
            System.out.flush();
            String msg;
            // An exception that has newlines in its message is assume to be be self-contained message.
            // No callstack trace is necessary. E.g. callback queue overflowing.
            if (t instanceof Error && t.getMessage() != null && t.getMessage().indexOf("\n") >= 0){
                msg = t.getMessage();
            }
            else {
                CharArrayWriter w = new CharArrayWriter(2000);
                t.printStackTrace(new PrintWriter(w));
                writeExceptionTrace(t);
                msg = w.toString();
            }
            String s = msg.replaceAll("\n","<br>");
            displayError("<html>Debugger engine server failed: <br>" + s); 
            trace(t.getMessage());
            exitcode = 1;
        }
        finally {      
            if (area != null){
                try {
                    FileWriter out = new FileWriter("server.log");
                    out.write(area.getText());
                    out.close();
                }          
                catch (IOException e) {
                    // @todo Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            if (transport != null && transport.isOpen()){
                transport.close();
            }
        }
        System.out.println("Server terminating");
        System.exit(exitcode);
    }
    
    private static void writeExceptionTrace(Throwable t){
        try {
            PrintWriter out = new PrintWriter(new FileWriter("server_exception.log"));
            t.printStackTrace(out);
            out.close();
        }
        catch (IOException e) {
            t.printStackTrace();
            e.printStackTrace();
            e.printStackTrace();
        }
    }

    private static boolean isTracing() {
        return sTrace.on();
    }

    private static void trace(String msg) {
        if (isTracing()) {
            Log.log("SERVER", msg);
        }
    }
}
