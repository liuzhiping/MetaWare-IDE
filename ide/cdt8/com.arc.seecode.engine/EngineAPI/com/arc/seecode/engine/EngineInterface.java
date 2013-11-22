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
package com.arc.seecode.engine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.arc.mw.util.Command;
import com.arc.mw.util.Log;
import com.arc.mw.util.Toggle;
import com.arc.seecode.client.Client;
import com.arc.seecode.connect.ConnectionFactory;
import com.arc.seecode.connect.ICommandReceiverRouter;
import com.arc.seecode.connect.IConnection;
import com.arc.seecode.connect.SocketTransport;
import com.arc.seecode.connect.ICommandReceiverRouter.IErrorLogger;
import com.arc.seecode.engine.internal.EngineAPI;
import com.arc.seecode.engine.type.ITypeFactory;
import com.arc.seecode.engine.type.defaults.TypeFactory;
import com.arc.seecode.server.Server;

/**
 * Wraps the API connection so that we can talk directly to the engine (from a
 * command-line debugger) or reroute through a serialized wire protocol (GUI
 * debugger).
 * <P>
 * Note that we synchronize most methods because the SeeCode engine is
 * single-threaded. Trying to inforce single-threadness elsewhere hasn't proved
 * reliable.
 * <P>
 * Such methods as {@link #stop}are not synchronized because it doesn't
 * directly call into the engine.
 * <P>
 * For Javadoc on each of these methods, see {@link IEngineAPI}.
 * <P>
 * ADDENDUM:<br>
 * When talking to the engine via a serialized wire protocol, the engine
 * invokes "callbacks" that are serviced by another thread. If the callback
 * makes a call to one of these engine methods before it the originating
 * method returns, there will be a deadlock.
 * 
 * Therefore, we have removed the synchronization lock on most of the methods.
 * This is okay when the engine is running as another process because the
 * "remote method call" serialization protocol inherently synchronizes 
 * the requests. <b>But we can no longer use this interface to directly connect
 * to the engine.</b>
 * 
 * @author David Pickens
 */
public class EngineInterface implements IAggregateAccessor, IEnqueue {
      
    private static final int MAX_LOCATION_CACHE_SIZE = 500;
    
    private static final Toggle sTrace = Toggle.define("ENGINE",false);

    private IEngineAPI mAPI;

    private IEngineObserver mObserver;

    private JavaFactory mJavaFactory;

    private ITypeFactory mTypeFactory;

    private Map<Integer,StackFrameRef> mStackFrameCache = new HashMap<Integer,StackFrameRef>();

    private Map<Object,Location> mLocationCache = new HashMap<Object,Location>();
    private int[] mThreadsCache = null;
    /**
     * Cache for the state of a thread until the thread is resumed.
     */
    private Map<Integer,Integer> mQueryState = new HashMap<Integer,Integer>();

    private boolean mServerMode = false;

    // The following will be set to SHUTDOWN or DISCONNECTED
    private int mTerminatedState = 0;

    private int mExitCode;

    private Properties mMessageMap;

    private int[] mExceptionIDs;

    private BreakpointManager mBreakpointManager = null;

    private ICustomDisplayCallback mDisplayCallback;

    private transient boolean mRestartPending = false;

    private OutputStream mStdout = null;
    private OutputStream mStderr = null;

    private String[] mDisplaySelectorsCache = null;

    private int mVersion;

    private int mBankCount = 0;

    private int[][] mBank = null;

    /**
     *  
     */
    private EngineInterface(JavaFactory jFactory, ITypeFactory typeFactory) {
        mJavaFactory = jFactory;
        mTypeFactory = typeFactory;
        InputStream in = this.getClass().getResourceAsStream("messages.properties");
        mMessageMap = new Properties();
        if (in != null){
            try {
                mMessageMap.load(in);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void init(IEngineAPI api) {
        mAPI = api;
        try {
            mVersion = api.getEngineInterfaceVersion();
        }
        catch (EngineException e) {
            mVersion = 2; // Shouldn't happen unless things are a mess.
        }
    }
    
    /**
     * Called each time engine stops so that we
     * can invalidate cached information.
     * <P>
     * Also called if engine is informing us
     * to update our views.
     *
     */
    public void invalidateCache(){
        invalidateStackFrameCache();
        mThreadsCache = null;
        // We assume that engine sends events if it implicitly stops or
        // resumes threads. If we must call into the engine to query the
        // state of things, we can hang if the engine is occupied.
//        invalidateState();
    }
    
/*    private void invalidateState(){
        synchronized(mQueryState){
            mQueryState.clear();
        }
    }*/

    private void invalidateStackFrameCache () {
        //System.out.println(">>>INVALIDATE ALL STACKFRAMES");
        for (StackFrameRef sf: mStackFrameCache.values()){
            sf.invalidate();
        }
        synchronized(mStackFrameCache) {
            mStackFrameCache.clear();
        }
    }
    
    public void invalidateCacheForThread(int tid){
        //System.out.println(">>>INVALIDATE STACKFRAMES FOR " + tid);
        synchronized(mStackFrameCache) {
            StackFrameRef sf = mStackFrameCache.remove(new Integer(tid));
            if (sf != null) {
                sf.invalidate();
            }
        }
        synchronized(mQueryState) {
            mQueryState.remove(new Integer(tid));
        }
        synchronized(mThreadsCacheLock) {
            mThreadsCache = null;
        }
    }
    
    public synchronized BreakpointManager getBreakpointManager() throws EngineException{
        if (mBreakpointManager == null){
            mBreakpointManager = new BreakpointManager(this);
        }
        return mBreakpointManager;
    }

    public ITypeFactory getTypeFactory() {
        return mTypeFactory;
    }
    
    /**
     * Permit client to insert his own type factory. For Eclipse CDT, it needs to have
     * access to the associated "ICDITarget" instance.
     * <P>
     * Presumably, this method is called soon after construction.
     * @param typeFactory new type factory to used.
     */
    public void setTypeFactory(ITypeFactory typeFactory){
        mTypeFactory = typeFactory;
    }

    public JavaFactory getJavaFactory() {
        return mJavaFactory;
    }

    /**
     * Our seecode engine server needs access to this.
     * 
     * @return the underlying engine object that 
     * calls are being delegated to.
     */
    public IEngineAPI getAPI() {
        return mAPI;
    }
    
    /**
     * Called from observer when engine shuts down.
     *
     */
    void setShutdown(){
        mTerminatedState = IEngineAPI.SHUTDOWN;
    }
    
    /**
     * Called to formally shutdown any sockets, threads, etc.
     * that are related to talking to the engine.
     *
     */
    public void onShutdown(){
        mAPI.onShutdown();
        setShutdown(); // if not already.
        if (mQueuedRunner != null) {
            mQueuedRunner.interrupt();
            mQueuedRunner = null;
        }
    }
    /**
     * Called when process terminated from observer.
     * @param code
     */
    void setExitCode(int code) {
        mExitCode = code;
    }

    /**
     * Create an instance that directly communicates with the SeeCode engine in
     * this JVM. One instance per target process (to handle CMPD).
     * 
     * @param typeFact
     *            type factory for making types.
     * @param processCount number of target processes;
     * @return the new instance.
     */
    public static EngineInterface[] CreateDirect(ITypeFactory typeFact, int pid[]) {
        EngineInterface engines[] = new EngineInterface[pid.length];
        for (int i = 0; i < engines.length; i++) {
            JavaFactory f = new JavaFactory();
            EngineInterface engine = new EngineInterface(f, typeFact);
            EngineAPI api = EngineAPI.Create(f, typeFact,pid[i]);
            engine.init(api);
            f.setEngineInterface(engine);
            engines[i] = engine;
        }
        return engines;
    }

    /**
     * Create an instance that directly communicates with the SeeCode engine in
     * this JVM. Use default type factory.
     * 
     * @return the new instance.
     */
    public static EngineInterface CreateDirect() {
        return CreateDirect(new TypeFactory(),new int[]{1})[0];
    }
    
    /**
     * Create an instance that directly communicates with the SeeCode engine in
     * this JVM but uses a UI that is connected by
     * sockets. The difference is that it it doesn't
     * garbage collect {@link #freeValueCookie(int) value cookies}
     * when the {@link Value} object is garbage collected.
     * Instead, the client UI must free them.
     * 
     * @param pid The CMPD process numbers to be assigned ({1} for non-CMPD)
     * @return the new instance.
     */
    public static EngineInterface[] CreateServer(int pid[]) {
        EngineInterface engine[] = CreateDirect(new TypeFactory(),pid);
        for (EngineInterface e: engine){
            e.mServerMode = true;
        }
        return engine;
    }

    /**
     * Create an instance that comminicates with the engine that is to run in a
     * separate JVM.
     * 
     * @param jvmPath
     *            the path of the JVM to be invoked on behalf of the engine.
     * @param jvmArguments
     *            arguments to be passed to the java command, including
     *            "-classpath".
     * @param workingDir
     *            the working directory to use.
     * @param typeFactory
     *            the type factory (so that we can use Eclipse CDI-based types).
     * @param timeout milliseconds to wait for engine response.
     * @param loadTimeout milliseconds to wait for engine to load a program.
     * @param loadTimeoutCallback a hook by which the client has an opportunity to extend the
     * time if a program load operation exceeds its timeout value; may be <code>null</code>.
     * @return the new instance.
     * @throws IOException if remote process could not be spawned, or socket
     * connection could not be made.
     */
    public static EngineInterface CreateRemote(String jvmPath,
            String[] jvmArguments, File workingDir, ITypeFactory typeFactory, int timeout,
            int loadTimeout, ITimeoutCallback loadTimeoutCallback)
            throws IOException {

        SocketTransport transport = new SocketTransport();
        transport.listen(0);

        int port = transport.listeningPort();
        int toggleCnt = 0;
        if (Toggle.lookup("SERVER",true).on()){
            toggleCnt++;
        }
        String[] cmd = new String[1 + jvmArguments.length + 1 + 1+toggleCnt];
        cmd[0] = jvmPath;
        System.arraycopy(jvmArguments, 0, cmd, 1, jvmArguments.length);
        cmd[1 + jvmArguments.length] = Server.class.getName();
        if (toggleCnt>0)
            cmd[1+jvmArguments.length+1] = "-XSERVER";
        cmd[1 + jvmArguments.length + toggleCnt+1] = port + "";
        final Command remoteCommand = new Command(cmd);
        remoteCommand.invokeIn(workingDir);
        transport.setAcceptTimeout(timeout);
        transport.accept();
        trace("Client connected!!!");
        return CreateRemote(transport,typeFactory,null,timeout,loadTimeout,loadTimeoutCallback);
    }
    

    /**
     * Create an instance that comminicates with a remote engine that already
     * has a socket connection.
     * 
     * @param transport
     *            the connected socket.
     * @param typeFactory the type factory for constructing types. For Eclipse/CDT it
     * has a reference to the associated ICDITarget instance.
     * @param displayCallbackRunner if not null, the 
     * {@link ICustomDisplayCallback display callback}
     * will be invoked via this interface. Under Eclipse,
     * this how we force it to be in the main GUI thread.
     * 
     * @param timeout milliseconds to wait for engine response.
     * @param loadTimeout milliseconds to wait for engine to load a program.
     * @param loadTimeoutCallback a hook by which the client has an opportunity to extend the
     * time if a program load operation exceeds its timeout value; may be <code>null</code>.
     * @return the new instance.
     */
    public static EngineInterface CreateRemote(SocketTransport transport,
            final ITypeFactory typeFactory,
            IRunner displayCallbackRunner,  
            int timeout,
            int loadTimeout,
            ITimeoutCallback loadTimeoutCallback)
    {
        return CreateRemote(transport,new ITypeFactoryFactory(){

            @Override
            public ITypeFactory makeTypeFactory () {
                return typeFactory;
            }},displayCallbackRunner,timeout,loadTimeout,
            loadTimeoutCallback,new int[]{1})[0];
    }
    
    public static EngineInterface[] CreateRemote(SocketTransport transport,
        ITypeFactoryFactory typeFactoryFactory,
        IRunner displayCallbackRunner,  
        int timeout,
        int loadTimeout,
        ITimeoutCallback loadTimeoutCallback,
        int pid[])


    {
        final IConnection connection = ConnectionFactory.makeConnection(transport, timeout);
        return CreateRemote(connection,typeFactoryFactory,displayCallbackRunner,timeout,loadTimeout,loadTimeoutCallback,pid);
    }
        
    public static EngineInterface[] CreateRemote (
        final IConnection connection,
        ITypeFactoryFactory typeFactoryFactory,
        IRunner displayCallbackRunner,
        int timeout,
        int loadTimeout,
        ITimeoutCallback loadTimeoutCallback,
        int pid[])

    {
        ICommandReceiverRouter router = ConnectionFactory.makeCommandReceiverRouter();
        QueuedRunner queuedRunner = new QueuedRunner("EngineRunQueue", new QueuedRunner.ITerminateQuery() {

            @Override
            public boolean isTerminated () {
                return connection.isDisconnected();
            }
        });
        queuedRunner.start();

        final EngineInterface engines[] = new EngineInterface[pid.length];
        for (int i = 0; i < engines.length; i++) {
            JavaFactory f = new JavaFactory();
            ITypeFactory typeFactory = typeFactoryFactory.makeTypeFactory();
            final EngineInterface engine = new EngineInterface(f, typeFactory);
            f.setEngineInterface(engine);
            engines[i] = engine;
            final Client client = new Client(connection, router, f, typeFactory, displayCallbackRunner, timeout,
                loadTimeout, loadTimeoutCallback, pid[i]);
            engine.setQueuedRunner(queuedRunner);
            engine.init(client);
        }
        router.start(connection, displayCallbackRunner, new IErrorLogger() {

            @Override
            public void logException (String msg, Throwable t) {
                engines[0].getObserver().logError(engines[0], msg, t);

            }
        }, "EngineEventThread");
        return engines;
    }

    /**
     * Create an instance that comminicates with the engine that is to run in a separate JVM. A default type factory
     * will be used.
     * @param jvmPath the path of the JVM to be invoked on behalf of the engine.
     * @param jvmArguments arguments to be passed to the java command, including "-classpath".
     * @param workingDir the working directory to use.
     * @param timeout milliseconds to wait for engine response.
     * @param loadTimeout milliseconds to wait for engine to load a program.
     * @param timeoutCallback a hook by which the client has an opportunity to extend the time if a program load
     * operation exceeds its timeout value; may be <code>null</code>.
     * @return the new instance.
     */
    public static EngineInterface CreateRemote(String jvmPath,
            String[] jvmArguments, File workingDir, int timeout,
            int loadTimeout, ITimeoutCallback timeoutCallback) throws IOException {
        return CreateRemote(jvmPath, jvmArguments, workingDir,
                new TypeFactory(),timeout,loadTimeout,timeoutCallback);
    }

    /**
     * Return a reference to the topmost stackframe of a (stopped) thread.
     * 
     * @param tid
     *            the thread whose stack frame is required.
     * @return stackframe reference, or <code>null</code> if one cannot be computed.
     */
    public StackFrameRef getTopStackFrame (int tid) throws EngineException {
        Integer tidKey = new Integer(tid);
        synchronized (mStackFrameCache) {
            StackFrameRef sf = mStackFrameCache.get(tidKey);
            if (sf == null) {
                int stackFrameID = mAPI.makeStackFrameID(tid);
                if (stackFrameID != 0) {
                    sf = new StackFrameRef(this, tid, stackFrameID);
                    mStackFrameCache.put(tidKey, sf);
                }
            }
            return sf;
        }
    }
    
    /**
     * Return whether or not the engine has been shutdown.
     * @return whether or not the engine has been shutdown.
     */
    public boolean isShutdown(){
        return mTerminatedState != 0;
    }

    /**
     * @param cmpdID
     * @param pid
     * @return true if successful.
     */
    public /*synchronized*/ boolean attach(int cmpdID, String pid)
            throws EngineException {
        return mAPI.attach(cmpdID, pid);
    }

    /**
     * @param address
     * @return location object corresponding to the address.
     */
    public Location computeLocation(long address)
            throws EngineException {
        Long key = new Long(address);
        Location loc;
        synchronized(mLocationCache) {
            loc = mLocationCache.get(key);
            if (loc != null)
                return loc;
            /*synchronized(this) */
            loc = mAPI.computeLocation(address);
            if (loc == null) {
                loc = this.mJavaFactory.newLocation();
                loc.setAddress(address);
                loc.setValid(false);
            }
            if (mLocationCache.size() > MAX_LOCATION_CACHE_SIZE)
                    mLocationCache.clear();
            mLocationCache.put(key,loc);
        }
        return loc;
    }

    /**
     * Create a location breakpoint.
     * @param location
     * @param ignoreCount
     * @param conditional
     * @param tid
     * @param flags
     * @return the breakpoint ID.
     */
    public int createBreakpoint(Location location, int ignoreCount,
            String conditional, int tid, int flags, boolean enabled) throws EngineException {
        if (!enabled) {
            flags |= IEngineAPI.BP_DISABLED;
            assert mVersion >= 13;
        }
        int id = mAPI.createBreakpoint(location, ignoreCount, conditional, tid,
                flags);
        return id;
    }
    
//    /**
//     * Create a watchpoint.
//     * @param var
//     * @param conditional
//     * @param tid
//     * @param flags
//     * @param stackFrameID
//     * @return
//     */
//    public synchronized int createWatchpoint(String var, 
//            String conditional, int tid, int flags, int stackFrameID) throws EngineException {
//        return mAPI.createWatchpoint(var,  conditional, tid,
//                flags,stackFrameID);
//    }

    /**
     * @param addr
     * @param instructionCount
     * @return an array of assembly instructions.
     */
    public /*synchronized*/ AssemblyRecord[] disassemble(long addr,
            int instructionCount) throws EngineException {
        return mAPI.disassemble(addr, instructionCount);
    }
    
    protected String formMessage(String msg[]){
        if (msg == null || msg.length == 0)
            throw new IllegalArgumentException("Bad argument");
        String msgString = mMessageMap.getProperty(msg[0]);
        // If not found, assume its English
        if (msgString == null) return msg[0];
        if (msg.length == 1) return msgString;
        String args[] = new String[msg.length-1];
        System.arraycopy(msg,1,args,0,args.length);
        return MessageFormat.format(msgString,(Object[])args);       
    }
    
    void evaluationError() throws EvaluationException, EngineException{
        String[] msg = mAPI.getErrorMessage();
        if (msg != null && msg.length > 0){
            throw new EvaluationException(formMessage(msg));
        }
    }

    /**
     * Evaluate an expression by calling through the
     * {@linkplain IEngineAPI#evaluate(String,int) delegate}.
     * @param expression the expression to be evaluated.
     * @param frameID stackframe ID.
     * @return the value of the expression.
     * @exception EvaluationException if the expression could not be evaluated.
     */
    public /*synchronized*/ Value evaluate(String expression, int frameID)
            throws EngineException, EvaluationException {
        Value v = mAPI.evaluate(expression, frameID);
        if (v == null){
            evaluationError();
        }
        return v;
    }

    /**
     * Evaluate a location expression by calling through the
     * {@linkplain IEngineAPI#evaluateLocation(String,int) delegate}.
     * @param expression the expression to be evaluated.
     * @param frameID stackframe ID.
     * @return the value of the expression.
     * @exception EvaluationException if the expression could not be evaluated.
     */
    public /*synchronized*/ Location evaluateLocation(String expression,
            int frameID) throws EngineException, EvaluationException {
        Location l = mAPI.evaluateLocation(expression, frameID);
        if (l == null){
            evaluationError();
        }
        return l;
    }

    /**
     * @param breakID
     */
    public /*synchronized*/ void freeBreakpoint(int breakID) throws EngineException {
        mAPI.freeBreakpoint(breakID);
    }

    //    /**
    //     * Called by "finalize" method of {@link StackFrameRef} to
    //     * free a stackframe ID.
    //     * @param stackframeID
    //     */
    //    public synchronized void freeStackFrameID(int stackframeID) throws
    // EngineException {
    //        mAPI.freeStackFrameID(stackframeID);
    //    }
    /**
     * Free value "cookie" that was used to lazily retrieve aggregate elements.
     * It doesn't need to be guarded because the implementation enqueues. Called
     * from the garbage collector via {@link Value#finalize()}.
     * 
     * @param cookie
     */
    @Override
    public void freeValueCookie(int cookie) throws EngineException {
        //If we're running as a server, then every
        // "Value" has a corresponding client proxy.
        // Let the client free this by calling 
        // this method itself.
        if (!mServerMode && !isShutdown())      
            mAPI.freeValueCookie(cookie);
    }


    /**
     * @return the breakpoint that caused the process to stop.
     */
    public /*synchronized*/ int getBreakpointHit(int tid) throws EngineException {
        return mAPI.getBreakpointHit(tid);
    }

    /**
     * @param source
     * @param rangeLo
     * @param rangeHi
     * @return the array of lines that on which a breakpoint can be set.
     */
    public /*synchronized*/ int[] getExecutableLines(String source, int rangeLo,
            int rangeHi) throws EngineException {
        return mAPI.getExecutableLines(source, rangeLo, rangeHi);
    }

    /**
     * @param regSetSnapshot
     * @return the value of the frame pointer.
     */
    public /*synchronized*/ long getFramePointer(int regSetSnapshot)
            throws EngineException {
        return mAPI.getFramePointer(regSetSnapshot);
    }

    /**
     * @param moduleID
     * @return all functions within a module.
     */
    public /*synchronized*/ Location[] getFunctionsWithinModule(int moduleID)
            throws EngineException {
        return mAPI.getFunctionsWithinModule(moduleID);
    }

    /**
     * @param address
     * @param length
     * @param flags
     * @return bytes from memory.
     */
    public /*synchronized*/ byte[] getMemoryBytes(long address, int length,
            int flags) throws EngineException {
        return mAPI.getMemoryBytes(address, length, flags);
    }

    /**
     * @param moduleID
     * @return base address of module.
     */
    public /*synchronized*/ long getModuleBaseAddress(int moduleID)
            throws EngineException {
        return mAPI.getModuleBaseAddress(moduleID);
    }

    /**
     * @param moduleID
     * @return name of module.
     */
    public /*synchronized*/ String getModuleName(int moduleID)
            throws EngineException {
        return mAPI.getModuleName(moduleID);
    }

    /**
     * @return array of all module IDs.
     */
    public /*synchronized */int[] getModules() throws EngineException {
        return mAPI.getModules();
    }

    /**
     * @param moduleID
     * @return the size of the module with the given ID.
     */
    public /*synchronized*/ long getModuleSize(int moduleID) throws EngineException {
        return mAPI.getModuleSize(moduleID);
    }

    /**
     * @param pc
     * @return all non-local variables visible from the given PC.
     */
    public /*synchronized*/ Variable[] getNonlocals(long pc) throws EngineException {
        return mAPI.getNonlocals(pc);
    }

    /**
     * @return the process ID.
     */
    public /*synchronized*/ int getPID() throws EngineException {
        if (mTerminatedState != 0) return 0;
        return mAPI.getPID();
    }

    /**
     * @return the exit code of the process, if it has
     * terminated.
     */
    public int getProcessExitCode() {
        return mExitCode;
    }
    

    /**
     * @return the number of register banks.
     */
    public /*synchronized*/ int getRegisterBankCount() throws EngineException {
        if (mBankCount == 0)
            mBankCount  = mAPI.getRegisterBankCount();
        return mBankCount;
    }

    /**
     * @param bank
     * @return the name of a particular register bank
     */
    public /*synchronized*/ String getRegisterBankName(int bank)
            throws EngineException {
        return mAPI.getRegisterBankName(bank);
    }

    /**
     * @param bank
     * @return the register IDs for a bank.
     */
    public /*synchronized*/ int[] getRegisterIDsFromBank(int bank)
            throws EngineException {
        if (mBank == null){
            mBank  = new int[getRegisterBankCount()][];
        }
        if (mBank[bank] == null){
            mBank[bank] = mAPI.getRegisterIDsFromBank(bank);
        }
        return mBank[bank];
    }

    /**
     * @param regID
     * @return the name of a register given its id.
     */
    public /*synchronized*/ String getRegisterName(int regID)
            throws EngineException {
        return mAPI.getRegisterName(regID);
    }

    /**
     * @param tid
     * @return the name of a thread.
     */
    public /*synchronized*/ String getThreadName(int tid) throws EngineException {
        return mAPI.getThreadName(tid);
    }
    
    private Object mThreadsCacheLock = new Object();

	private boolean mProgramLoaded = false;

	private List<Object[]> mDisplaysToCreateAtProgramLoad = null;

    private int mCurrentThreadID;

    private int mPendingWatchpointTID;

    private WatchpointHit[] mHits = null; // cache of pending watchpoint hits

    /**
     * @return the active thread IDs.
     */
    public int[] getThreads() throws EngineException {
        synchronized(mThreadsCacheLock) {
            if (mThreadsCache == null || mThreadsCache.length == 0)
                mThreadsCache = mAPI.getThreads();
            return mThreadsCache;
        }
    }

    /**
     * @param cookie
     * @param elementIndex
     * @return the value of the element of an aggregate variable.
     */
    @Override
    public Value getValueElement(int cookie, int elementIndex)
            throws EngineException {
        Value v = mAPI.getValueElement(cookie, elementIndex);
        // cr91306: don't ever return null. Make it a bad value if null
        if (v == null){
            v = new Value(this);
            v.setSimpleValue("<Element " + elementIndex + "not retrievable>");
        }
        return v;
    }

    /**
     * Perform an instruction step.
     * @param tid the thread to step.
     * @param over if true, step over calls.
     * @param cnt the number of steps to take (must be >0).
     */
    public  void instructionStep(int tid, boolean over, int cnt)
            throws EngineException {
        synchronized(mQueryState) {
            if (tid == 0)
                mQueryState.clear();
            mQueryState.put(tid,IEngineAPI.STEP_PENDING);
        }
        mAPI.instructionStep(tid, over,cnt);
    }
    
    /**
     * Invoked when the engine has gone into a resumed state.
     * @param tid thread ID or 0 if applies to whole process.
     */
    void onResume(int tid){
        synchronized(mQueryState){
            mQueryState.put(tid,IEngineAPI.RUNNING);
            if (tid == 0) {
                mCurrentThreadID = 0;
                for (Map.Entry<Integer,Integer>e: mQueryState.entrySet()){
                    if (e.getKey().intValue() != 0){
                        e.setValue(IEngineAPI.RUNNING);
                    }
                }
            }
            else if (tid == mCurrentThreadID){
                mCurrentThreadID = 0;
            }
        }       
    }
    
    /**
     * Invoked when the engine has gone into a stopped state.
     * @param tid thread ID that caused the stop, or 0 if the user explicitly
     * stopped all threads.
     * @param reason the reason for the stop.
     */
    void onStopped (int tid, int reason) {
        invalidateStackFrameCache();
        mCurrentThreadID = tid;
        synchronized (mQueryState) {
            mQueryState.put(tid, reason);
            boolean threadControl = false;
            if (tid != 0) {
                try {
                    threadControl = hasThreadControl();
                }
                catch (EngineException e1) {
                    threadControl = false;
                }
            }
            if (tid == 0 || !threadControl) {
                for (Map.Entry<Integer, Integer> e : mQueryState.entrySet()) {
                    if (e.getKey().intValue() != tid) {
                        mQueryState.put(e.getKey().intValue(), IEngineAPI.STOPPED_BY_USER);
                    }
                }

            }
        }
    }

    /**
     * Do not make it synchronized or else we'll deadlock
     * if the client is in a run state.
     * @return whether or not the engine is active.
     */
    public boolean isActive() throws EngineException {
        if (mTerminatedState != 0 || mAPI == null) return false;
        return mAPI.isActive();
    }

    /**
     * @param tid
     * @return whether or not the given thread exists.
     */
    public /*synchronized*/ boolean isActiveThread(int tid) throws EngineException {
        if (mTerminatedState != 0) return false;
        return mAPI.isActiveThread(tid);
    }

    /**
     * @param bank
     * @return whether or not a register bank is active.
     */
    public /*synchronized*/ boolean isRegisterBankActive(int bank)
            throws EngineException {
        return mAPI.isRegisterBankActive(bank);
    }

    /**
     * @return whether or not the engine is running
     * as a simluator.
     */
    public /*synchronized*/ boolean isSimulator() throws EngineException {
        return mAPI.isSimulator();
    }

    /**
     * @param breakID
     * @return whether or not the given breakpoint ID is valid.
     */
    public /*synchronized*/ boolean isValidBreakpoint(int breakID)
            throws EngineException {
        return mAPI.isValidBreakpoint(breakID);
    }

    /**
     * @param address
     * @param flags
     * @return whether or not the given memory address is valid.
     */
    public /*synchronized*/ boolean isValidMemory(long address, int flags)
            throws EngineException {
        return mAPI.isValidMemory(address, flags);
    }

    /**
     * @param frameID
     * @return whether or not the given frameID is valid.
     */
    public /*synchronized*/ boolean isValidStackFrameID(int frameID)
            throws EngineException {
        return mAPI.isValidStackFrameID(frameID);
    }


    /**
     * @param command
     * @return true if successful in loading the program.
     */
    public boolean loadProgram(String command[]) throws EngineException {
		synchronized (mLocationCache) {
			mLocationCache.clear();
		}
		if (mAPI.loadProgram(command)) {
			//Synchronized because "createDisplay()" is called from UI thread.
			synchronized (this) {

				mProgramLoaded = true;
				// If there is any cached commands to populate displays, then
				// populate
				// them now, since
				// the program is loaded. The engine can't handle it before the
				// program
				// is loaded.
				if (mDisplaysToCreateAtProgramLoad != null) {
					for (int i = 0; i < mDisplaysToCreateAtProgramLoad.size(); i++) {
						try {
							Object[] d = mDisplaysToCreateAtProgramLoad.get(i);
							createDisplay(((Integer) d[0]).intValue(), (String) d[1]);
						} catch (EngineException x) {
							// An exception occur while creating a display.
							// The rest, if any, will remain uncreated.
							for (int j = 0; j <= i; j++) {
								mDisplaysToCreateAtProgramLoad.remove(j);
							}
							throw x;
						}
					}
					mDisplaysToCreateAtProgramLoad = null;
				}
				return true;
			}
		}
		return false;
	}
    /**
	 * Key for location cache to store source/line.
	 * 
	 * @author David Pickens
	 */
    static class SourceRef {
        private String _source;
        private int _line;
        SourceRef(String source,int line){
            this._source = source;
            this._line = line;
        }
        @Override
        public int hashCode(){
            return _source.hashCode() + _line;
        }
        @Override
        public boolean equals(Object o){
            if (!(o instanceof SourceRef))
                return false;
            SourceRef sr = (SourceRef)o;
            return sr._line == _line &&
            	sr._source.equals(_source);
        }
    }

    /**
     * Lookup a source reference by calling through the
     * {@linkplain IEngineAPI#lookupSource(String,int) delegate}.
     * @param source source file name.
     * @param line line number.
     * @return the location or null.
     * @exception EvaluationException if the source location is not valid.
     */
    public Location lookupSource (String source, int line) throws EngineException, EvaluationException {
        SourceRef key = new SourceRef(source, line);
        Location loc;
        synchronized (mLocationCache) {
            loc = mLocationCache.get(key);
            if (loc == null) {
                loc = mAPI.lookupSource(source, line);
                if (loc != null) {
                    synchronized (mLocationCache) {
                        if (mLocationCache.size() >= MAX_LOCATION_CACHE_SIZE)
                            mLocationCache.clear();
                        mLocationCache.put(key, loc);
                    }
                }
                else
                    evaluationError();
            }
        }
        return loc;
    }

    /**
     * Lookup a vaiable by calling through the
     * {@linkplain IEngineAPI#lookupVariable(String,int) delegate}.
     * @param name the name of the variable.
     * @param frameID stackframe ID.
     * @return the value of the expression.
     * @exception EvaluationException if the expression could not be evaluated.
     */
    public /*synchronized */ Variable lookupVariable(String name, int frameID)
            throws EngineException, EvaluationException {
        if (name == null) throw new IllegalArgumentException("Name is null");
        Variable v = mAPI.lookupVariable(name, frameID);
        if (v == null) evaluationError();
        return v;
    }
    
    /**
     * Lookup a global variable by calling through the
     * {@linkplain IEngineAPI#lookupGlobalVariable(String) delegate}.
     * @param name the name of the variable.
     * @return the variable or <code>null</code>
     */
    public /*synchronized */  Variable lookupGlobalVariable(String name)
            throws EngineException {
        if (name == null) throw new IllegalArgumentException("Name is null");
        return mAPI.lookupGlobalVariable(name);
    }

    /**
     * @return the state of the engine.
     */
    public int queryState(int tid) throws EngineException {
        if (mTerminatedState != 0) return mTerminatedState;
        Integer key = new Integer(tid);
        synchronized(mQueryState){
            Integer result = mQueryState.get(key);
            if (result == null || result.intValue() == IEngineAPI.UNDEFINED){
                try {
                    result = mAPI.queryState(key);
                }
                catch (EngineDisconnectedException e) {
                    mTerminatedState = IEngineAPI.DISCONNECTED;
                    return mTerminatedState;
                }
                mQueryState.put(tid,result);
            }
            return result.intValue();
        }
    }

    public boolean isStopped(int tid) throws EngineException {
        return queryState(tid) != IEngineAPI.RUNNING;
    }

    /**
     * @param breakID
     */
    public /*synchronized */  void removeBreakpoint(int breakID)
            throws EngineException {
        mAPI.removeBreakpoint(breakID);
    }


    /**
     *  
     */
    public void resume(int tid) throws EngineException {
        mHits = null; // clear watchpoint hit cache.
        synchronized(mQueryState) {
            if (tid == 0) 
                mQueryState.clear();
            mQueryState.put(tid,IEngineAPI.RESUME_PENDING);
        }
        mAPI.resume(tid);
    }

    /**
     * @param tid
     * @param location
     */
    public /*synchronized */  void runToAddress(int tid, Location location)
            throws EngineException {
        mAPI.runToAddress(tid, location);
    }

    /**
     * @param displayID the associted display ID
     * @param property the property to be updated.
     * @param value the new value.
     */
    public void sendValueUpdate(final int displayID, final String property,
            final String value) throws EngineException {
        if (value == null || property == null) 
            throw new IllegalArgumentException("Argument is null");
        // Displays sometime try to update themselves as engine is shutting down. So, check for this...
        if (mTerminatedState == 0) {
            /*synchronized(this)*/ {
                mAPI.sendValueUpdate(displayID,property,value);
            }
        }
    }
    
    /**
     * @param displayID the associted display ID
     * @param property the property to be updated.
     * @param value the new value.
     */
    public boolean sendValueUpdate2(final int displayID, final String property,
            final String value, int timeout) throws EngineException {
        // Displays sometime try to update themselves as engine is shutting down. So, check for this...
        if (mTerminatedState == 0) {
            if (mVersion >= 18)
                return mAPI.sendValueUpdate2(displayID,property,value,timeout);
            else {
                mAPI.sendValueUpdate(displayID,property,value);
                return true;
            }
        }
        return false;
    }

    /**
     * @param breakID
     * @param expression
     */
    public /*synchronized */  void setBreakpointCondition(int breakID,
            String expression) throws EngineException {
        mAPI.setBreakpointCondition(breakID, expression);
    }

    /**
     * @param id
     * @param v
     */
    public /*synchronized */  void setBreakpointEnabled(int id, boolean v)
            throws EngineException {
        mAPI.setBreakpointEnabled(id, v);
    }

    /**
     * @param breakID
     * @param count
     */
    public /*synchronized */  void setBreakpointHitCount(int breakID, int count)
            throws EngineException {
        mAPI.setBreakpointHitCount(breakID, count);
    }
    
    /**
     * @param breakID
     * @param threads
     */
    public /*synchronized */  void setBreakpointThreads(int breakID, int threads[])
            throws EngineException {
        mAPI.setBreakpointThreads(breakID, threads);
    }

    /**
     * @param observer
     */
    public /*synchronized */  void setBreakpointObserver(IBreakpointObserver observer)
            throws EngineException {
        mAPI.setBreakpointObserver(observer);
    }

    /**
     * @param callback
     */
    public /*synchronized */  void setCustomDisplayCallback(
            ICustomDisplayCallback callback) throws EngineException {
        mDisplayCallback = callback;
        mAPI.setCustomDisplayCallback(callback);
    }
    
    public ICustomDisplayCallback getCustomDisplayCallback(){
        return mDisplayCallback;
    }

    /**
     * @param args
     */
    public /*synchronized */  void setEngineArguments(String args)
            throws EngineException {
        mAPI.setEngineArguments(args);
    }

    /**
     * @param observer
     */
    public /*synchronized */  void setEngineObserver(IEngineObserver observer)
            throws EngineException {
        mObserver = observer;
        mAPI.setEngineObserver(new EngineAPIObserver(this, observer));
    }

    public IEngineObserver getObserver()  {
        return mObserver;
    }

    /**
     * @param address
     * @param value
     * @param flags
     * @return true if successful
     */
    public /*synchronized */  boolean setMemoryByte(long address, int value, int flags)
            throws EngineException {
        return mAPI.setMemoryByte(address, value, flags);
    }
    
    public void setMemoryBytes (long address, byte[] buffer, int offset, int length, int flags) throws EngineException {
        if (mVersion >= 4) {
            // setMemoryBytes wasn't defined until version 4
            // Avoid sending the whole buffer across if only a small subset is being
            // retrieved.
            if (offset > 50 || offset+length < buffer.length-50) {
                byte[] newBuf = new byte[length];
                System.arraycopy(buffer,offset,newBuf,0,length);
                buffer = newBuf;
                offset = 0;
            }
            int actual = mAPI.setMemoryBytes(address,buffer,offset,length,flags);
            if (actual < length){
                throw new EngineException("Write of "
                    + length
                    + " bytes at address "
                    + Long.toHexString(address)
                    + (actual > 0?
                       " only succeeded in writing "
                       + actual
                       + " bytes":
                       "failed."));
            }
        }
        else {
            int end = offset + length;
            if (offset < 0 || end > buffer.length)
                throw new EngineException("Bad offset and/or length");
            for (int i = offset; i < end; i++) {
                if (!mAPI.setMemoryByte(address++, buffer[i], flags))
                    throw new EngineException("Write of "
                        + length
                        + " bytes at address "
                        + Long.toHexString(address)
                        + " only succeeded in writing "
                        + (i - offset)
                        + " bytes");
            }
        }
    }

    /**
     * @param address
     * @param value
     * @param flags
     * @return true if successful
     */
    public /*synchronized */  boolean setMemoryWord(long address, int value, int flags)
            throws EngineException {
        return mAPI.setMemoryWord(address, value, flags);
    }

    /**
     * @param tid
     * @param regID
     * @param value
     * @return true if successful
     * @throws EngineException
     * @throws EvaluationException
     */
    public /*synchronized */  boolean setRegisterValue(int tid, int regID,
            String value) throws EvaluationException, EngineException {
        boolean v = mAPI.setRegisterValue(tid, regID, value);
        if (!v){
            evaluationError();
        }
        return v;
    }

    /**
     * @param dirs
     */
    public /*synchronized */  void setSourceDirectories(String[] dirs)
            throws EngineException {
        mAPI.setSourceDirectories(dirs);
    }

    /**
     * @param var
     * @param value
     * @return true if successful
     * @throws EngineException
     * @throws EvaluationException
     */
    public /*synchronized */  boolean setVariableValue(Variable var, String value)
            throws EvaluationException, EngineException {
//        boolean v = mAPI.setVariable(var, value);
//        if (!v){
//            evaluationError();
//        }
//        return v;
          StackFrameRef sf = var.getStackFrame();
          Value v;
          if (sf != null){
              v = sf.evaluate(var.getActualName() + "=" + value);
              int reg = var.getRegister();
              if (reg >= 0){
                  sf.invalidateRegister(reg);
              }
          }
          else v = mAPI.evaluate(var.getActualName() + "=" + value,0);
          if (v == null){
              evaluationError();
          }
          else var.setValue(v);
          return v != null;
    }

    /**
     *  
     */
    public void start() throws EngineException {
        mAPI.start();
    }

    /**
     * @param tid
     * @param over
     * @param cnt
     */
    public void statementStep(int tid, boolean over, int cnt)
            throws EngineException {
        synchronized(mQueryState) {
            if (tid == 0)
                mQueryState.clear();
            mQueryState.put(tid,IEngineAPI.STEP_PENDING);
        }
        mAPI.statementStep(tid, over, cnt);
    }

    /**
     * @param tid
     */
    public void stepOut(int tid) throws EngineException {
        synchronized(mQueryState) {
            if (tid == 0)
                mQueryState.clear();
            mQueryState.put(tid,IEngineAPI.STEP_PENDING);
        }
        mAPI.stepOut(tid);
    }

    /**
     *  
     */
    public void stop(int tid) throws EngineException {
        mAPI.stop(tid);
    }

    /**
     *  
     */
    public void terminate() throws EngineException {
        mAPI.terminate();
    }

    /**
     *  
     */
    public void shutdown() throws EngineException {
        invalidateCache();
        mAPI.shutdown();
    }

    /**
     *  
     */
    public boolean waitForStop(int timeout) throws InterruptedException,
            EngineException {
        return mAPI.waitForStop(timeout);
    }
    
    private static boolean isTracing(){
        return sTrace.on();
    }
    
    private static void trace(String msg){
        if (isTracing()){
            Log.log("ENGINE",msg);
        }
    }

    /**
     * @param args
     * @return true if successfull
     * @throws EngineException
     */
    public boolean restart(String args[]) throws EngineException {
        invalidateCache();
        mRestartPending = true;
        mBreakpointManager.resync(); // make sure we're in sync with engine.
        synchronized(mLocationCache){
            mLocationCache.clear(); // things may have changed.
        }
        /*synchronized(this)*/ {
            boolean result = mAPI.restart(args);
            //If the exe changed, the engine doesn't handle things very well. It
            // typically deletes all breakpoints silently! Reapply the best we can...
            mBreakpointManager.reapply();
            return result;
        }
    }
    /**
     * @param toggleName
     * @param on
     */
    public void setToggle(String toggleName, boolean on) throws EngineException {
        mAPI.setToggle(toggleName, on);
    }
    /**
     * @return the watchpoints that were responsible for
     * stopping the process.
     * @throws EngineException
     */
    public WatchpointHit[] getWatchpointHits(int tid) throws EngineException {
        if (mPendingWatchpointTID != tid || mHits == null) {
            mHits = mAPI.getWatchpointHits(tid);
            mPendingWatchpointTID = tid;
        }
        return mHits;
    }
    /**
     * Called from {@link Value#setElement(int,String,StackFrameRef)} to
     * set the value of an element.
     * @param cookie
     * @param elementIndex
     * @param newValue
     * @throws EngineException
     */
    @Override
    public /*synchronized */  void setValueElement(int cookie, int elementIndex, String newValue, int frameID)
            throws EngineException, EvaluationException {
        if (!mAPI.setValueElement(cookie, elementIndex, newValue, frameID)){
            evaluationError();
        }
    }
    /**
     * @param id
     * @return a string description of the exception.
     * @throws EngineException
     * @throws IllegalArgumentException
     */
    public String getExceptionDescription(int id) throws EngineException,
            IllegalArgumentException {
        return mAPI.getExceptionDescription(id);
    }
    /**
     * @param tid the associated thread whose exception ID we need.
     * @return the id number of the exception that was hit.
     * @throws EngineException
     */
    public int getExceptionHit(int tid) throws EngineException {
        return mAPI.getExceptionHit(tid);
    }
    /**
     * @return all recognized exceptions.
     * @throws EngineException
     */
    public int[] getExceptionIDs() throws EngineException {
        if (mExceptionIDs == null)
           mExceptionIDs = mAPI.getExceptionIDs();
        return mExceptionIDs;
    }
    
    private void checkExceptionID(int id) throws IllegalArgumentException{
        
        if (mExceptionIDs != null){
            for (int i = 0; i < mExceptionIDs.length; i++){
                if (mExceptionIDs[i] == id) return;
            }
            throw new IllegalArgumentException("Unrecognized exception ID: " + id);
        }
        
    }
    /**
     * @param id
     * @return the the name of an exception.
     * @throws EngineException
     */
    public String getExceptionName(int id) throws EngineException {
        checkExceptionID(id);
        return mAPI.getExceptionName(id);
    }
    /**
     * @param id
     * @param ignore
     * @throws EngineException
     * @throws IllegalArgumentException
     */
    public void setIgnoreException(int id, boolean ignore)
            throws EngineException, IllegalArgumentException {
        checkExceptionID(id);
        mAPI.setIgnoreException(id, ignore);
    }
    /**
     * @param id
     * @param suspend
     * @throws EngineException
     * @throws IllegalArgumentException
     */
    public void setSuspendOnException(int id, boolean suspend)
            throws EngineException, IllegalArgumentException {
        checkExceptionID(id);
        mAPI.setSuspendOnException(id, suspend);
    }
    /**
     * @param id
     * @throws EngineException
     */
    public void throwException(int id) throws EngineException {
        checkExceptionID(id);
        mAPI.throwException(id);
    }
    
    /**
     * Synchronized because this typically invoked from the UI thread and the "loadProgram()" method
     * is invoked from an event hread.
     * @param kind
     */
    public synchronized void createDisplay(int id, String kind) throws EngineException{ 
    	// Engine can't populate displays until the program is loaded.
    	// If the program isn't yet loaded, then cache the displays to be created.
    	if (mProgramLoaded)
            mAPI.createDisplay(id,kind);
    	else {
    		if (mDisplaysToCreateAtProgramLoad == null)
    			mDisplaysToCreateAtProgramLoad = new ArrayList<Object[]>();
    		mDisplaysToCreateAtProgramLoad.add(new Object[]{id,kind});
    	}
    }
    /**
     * @param command
     */
    public /*synchronized */  void invokeCommand(String command) throws EngineException{
        mAPI.invokeCommand(command);
    }
    
    private Object mDisplaySelectorLock = new Object();

    private String[] mVersionStrings = null;

    // Default register bank for register display.
    private int mDefaultBank = -1;

    private String mSplashPath = null;

    private boolean mHasThreadControl;

    private boolean mHasThreadControlCached = false;
    
    private final static String[] EMPTY_STRING_ARRAY = new String[0];
    
    /**
     * Returns an array of all recognized display selectors.
     * @return an array of all recognized display selectors.
     */
    public String[] getDisplaySelectors() throws EngineException{
    	// If the program isn't yet loaded, it may have send back an error message. If we query
    	// here, we could deadlock (CR2140).
    	// Actually, we have also fixed things elsewhere so this isn't invoked until program is loaded.
    	if (!this.mProgramLoaded) 
    		return EMPTY_STRING_ARRAY;
        //NOTE: this method is called from the UI thread, and so we need to be sensitive
        // about how it is invoked.
        // First of all, we cache the selectors so that we don't need to invoke
        // the engine each time.
        // The client must call invalidateDisplaySelectorCache() if he wants to
        // really get new stuff.
        //
        // Secondly, we don't synchronize with "this" unless we need to access the engine.
        // Thus, we use an alternate lock.
        //
        // Observe that it we must indeed access the engine, the caller will stall
        // if any competing thread is already talking to the engine.
        //
        synchronized(mDisplaySelectorLock){
            if (mDisplaySelectorsCache != null) {
                return mDisplaySelectorsCache;
            }
            /*synchronized(this) */{
                //NOTE: as of interface version 9, this method should never be called.
            if (mVersion < 9)
                // Instead, {@link ICustomDisplayCallback#setDisplaySelectors(String[])} sets the cache.
                mDisplaySelectorsCache  = mAPI.getDisplaySelectors();
            else
                return EMPTY_STRING_ARRAY;
            }
            return mDisplaySelectorsCache;
        }
    }
    
    /**
     * Since version 9 of the engine interface, it calls {@link ICustomDisplayCallback#setDisplaySelectors(String[])} to
     * communicate the display selectors. This avoids deadlock problems if the engine must be queried to get
     * the selectors.
     * <P>
     * The default implementation simply calls this method to set the selectors.
     * @param selectors the selectors that the engine recognizes.
     */
    public void setDisplaySelectors(String selectors[]){
        synchronized(mDisplaySelectorLock){
            mDisplaySelectorsCache = selectors;
        }      
    }
    
    /**
     * Called when engine has requested that they be refreshed.
     * @todo davidp needs to add a method comment.
     */
    public void invalidateDisplaySelectorCache() {
        synchronized(mDisplaySelectorLock) {
            mDisplaySelectorsCache = null;
        }
    }
    
    /**
     * @param displayID
     * @throws EngineException
     */
    public void closeDisplay(int displayID) throws EngineException {
        mAPI.closeDisplay(displayID);
    }
    /**
     * @param progressDisplayID
     * @throws EngineException
     */
    public void progressDisplayCanceled(int progressDisplayID)
            throws EngineException {
        mAPI.progressDisplayCanceled(progressDisplayID);
    }
    
    /**
     * @param pattern
     * @throws EngineException
     */
    public void setArgsFilePattern(String pattern) throws EngineException {
        mAPI.setArgsFilePattern(pattern);
    }
    /**
     * @param wd
     * @throws EngineException
     */
    public void setWorkingDirectory(String wd) throws EngineException {
        mAPI.setWorkingDirectory(wd);
    }
    
    /**
     * Set the stackframe for a SeeCode display.
     * @param displayID the display ID.
     * @param sf the stackframe, which belongs to an active, stopped thread.
     * @throws EngineException
     */
    public void setStackFrame(int displayID, StackFrameRef sf) throws EngineException{
        mAPI.setStackFrame(displayID,sf.getFrameID());
    }
    
    /**
     * Return whether or not a restart operation is pending.
     * This is so that a processTerminated observer can know whether or
     * not the process is about to be restarted.
     */
    public boolean isRestartPending(){
        return mRestartPending;
    }
    
    /**
     * Called from {@link EngineAPIObserver} to clear the "restartPending" property.
     */
    void clearRestartPending(){
        mRestartPending = false;
    }

    /**
     * @return true iftarget has individual thread control.
     * @throws EngineException 
     * @throws EngineException
     * @see com.arc.seecode.engine.IEngineAPI#hasThreadControl()
     */
    public boolean hasThreadControl () throws EngineException {
        if (mHasThreadControlCached) {
            mHasThreadControl = mAPI.hasThreadControl();
            mHasThreadControlCached  = true;
        }
        return mHasThreadControl;
    }
    
    /**
     * Return an output stream that forwards its output
     * to the associated {@link IEngineObserver#displayNote displayNote} method.
     * It can be used by command processors as the standard output
     * stream.
     * @return output stream.
     */
    public OutputStream getOutputStream () {
        if (mStdout == null) {
            mStdout = new OutputStream() {

                private StringBuffer _buf = new StringBuffer();

                @Override
                public void write (int b) throws IOException {
                    _buf.append((char) b);
                    if (b == '\n') {
                        flush();
                    }
                }

                @Override
                public void flush () {
                    if (_buf.length() > 0) {
                        IEngineObserver eo = getObserver();
                        eo.logMessage(EngineInterface.this, _buf.toString());
                        _buf.setLength(0);
                    }
                }
            };
        }
        return mStdout;

    }
    /**
     * Return an output stream that forwards its output
     * to the associated {@link IEngineObserver#displayNote displayError} method.
     * It can be used by command processors as the standard error
     * stream.
     * @return output stream.
     */
    public OutputStream getErrorStream () {
        if (mStderr == null) {
            mStderr = new OutputStream() {

                private StringBuffer _buf = new StringBuffer();

                @Override
                public void write (int b) throws IOException {
                    _buf.append((char) b);
                    if (b == '\n') {
                        flush();
                    }
                }

                @Override
                public void flush () {
                    if (_buf.length() > 0) {
                        IEngineObserver eo = getObserver();
                        eo.displayError(EngineInterface.this, _buf.toString());
                        _buf.setLength(0);
                    }
                }
            };
        }
        return mStderr;

    }
    
    /**
     * Return an array of 3 version strings pertaining to the engine.
     * <P>
     * The first is the product version; e.g., 8.0.1
     * <P>
     * The second is the build date; e.g. 20061030.14342853
     * <P>
     * The third is the internal build ID; e.g., 1263
     * @return array of version strings pertaining to the engine.
     * @throws EngineException
     */
    public String[] getEngineVersionStrings() throws EngineException{
        if (mVersionStrings == null) {
            mVersionStrings =  mAPI.getEngineVersionStrings();
        }
        return mVersionStrings;
    }
    
    public boolean isSplashPathSupported(){
        return mVersion >= 6;
    }
    
    public String getSplashPath() throws EngineException{
        if (mVersion >= 6){
            if (mSplashPath == null)
                mSplashPath  = this.mAPI.getSplashPath();
            return mSplashPath;
        }
        return null;
    }
    
    public int getLicenseExpirationDays() throws EngineException{
        if (mVersion >= 6){
            return this.mAPI.getLicenseExpirationDays();
        }
        return -3;  // Not known
    }
    
    /**
     * Return true if a license failure is pending after attempting to load a program.
     * @return whether or not a license failure is pending after attempting to load a program.
     * @throws EngineException
     */
    public boolean isLicenseFailurePending() throws EngineException {
        if (mVersion >= 7)
            return this.mAPI.isLicenseFailurePending();
        return false;
    }
    
    public RegisterContent getRegisterContent(int frameID, int regID) throws EngineException{
        if (mVersion >= 8){
            return this.mAPI.getRegisterContent(frameID,regID);
        }
        // Contrive result from a register value string.
        String value = this.mAPI.getRegisterValue(frameID,regID,Format.HEXADECIMAL.ordinal());
        if (value != null){
            try {
                String s = value;
                if (s.equals("???")) {
                    // Older versions of the engine returned "???" to mean invalid.
                    return RegisterContent.newInvalid(regID,4);
                }
                if (s.startsWith("0x")) s = s.substring(2);
                long v = Long.parseLong(s,16);
                return new RegisterContent(regID,v,4);
            }
            catch (NumberFormatException e) {
                return new RegisterContent(regID,value); // Special value
            }
        }
        throw new EngineException("Can't read reg " + regID); // shouldn't get here.
    }
    
    public RegisterContent[] getRegisterContent(int frameID, int regIDs[]) throws EngineException{
        if (mVersion >= 8){
            return this.mAPI.getRegisterContentArray(frameID,regIDs);
        }
        
        // Contrive result with older debugger engine:
        RegisterContent[] contents= new RegisterContent[regIDs.length];
        for (int i = 0; i < regIDs.length; i++){
            contents[i] = getRegisterContent(frameID,regIDs[i]);
        }
        return contents;
    }
    
    public int getDefaultBank() throws EngineException {
        if (mDefaultBank < 0) {
            if (mVersion >= 8) {
                mDefaultBank = mAPI.getDefaultBank();
            }
            else
                mDefaultBank = -1; // Punt; display all banks by default.
        }
        return mDefaultBank;
    }
    
    private Map<String,Object> fData = null;

    private QueuedRunner mQueuedRunner = null;
    
    /**
     * In case the client needs to associate data with this instance. It can be retrieved
     * by subsequently calling {@link #getData()}.
     * @param data data to be associated with this instance.
     */
    public void setData(String key, Object data){
        if (fData == null) fData = new HashMap<String,Object>();
        fData.put(key,data);
    }
    
    public Object getData(String key){
        return fData.get(key);
    }
    
    /**
     * Does this interface support the {@link #writeStdin} method?
     * @return true if this interface supports the {@link #writeStdin} method.
     */
    public boolean supportsWritingStdin() { 
        // CR91888: engine hangs waiting for stdin if it is read before
        // we supply the data. So, we forgo using "writeStdin()" call as of version 12.
        return mVersion >= 10 && mVersion < 12;
    }
    
    /**
     * Does the engine support <code>sendValueUpdate("init_state",state)</code> so as to restore
     * the state of a display at startup?
     * @return whether or not display state persistence is supported.
     */
    public boolean supportsDisplayInitStates() {
        return mVersion >= 31;
    }
    
    public void writeStdin(byte[] data) throws EngineException{
        if (mVersion < 10 || mVersion >= 12) throw new IllegalStateException("writeStdin not supported in target debugger");
        mAPI.writeStdin(data);
    }
    
    void setQueuedRunner(QueuedRunner runner){
        mQueuedRunner = runner;
    }
    
    /**
     * Enqueue an invocation that makes calls into the engine, but should be
     * executed asychronously in a per-engine thread to avoid locking the UI thread.
     * <P>
     * This method is used to enqueue selection changes to the UI in response to
     * stop events. If the engine is slow in responding, or is hung, this queue could
     * start getting large as things back up. To avoid this, we can associate a <code>key</code>
     * to the run object. If there exists a queued object with the same key, it will
     * be replaced. Thus, a selection queries can have the same key so that, at most, one
     * will be pending at any time.
     * @param run the thing to run.
     * @param key if not null, a key that is associated with the run object; if there is a pending
     * run object with the same key, it will be replaced.
     */
    @Override
    public void enqueue (Runnable run, Object key) {
        if (mTerminatedState == 0) {
            if (mQueuedRunner == null) {
                mQueuedRunner = new QueuedRunner("EngineWorkThread", new QueuedRunner.ITerminateQuery(){

                    @Override
                    public boolean isTerminated () {
                        return isShutdown();
                    }});
                mQueuedRunner.start();
            }
            mQueuedRunner.enqueue(run,key);
        }
    }
    
    /**
     * Do whatever is necessary to get the engine to refresh the display contents.
     */
    public void refreshDisplays() throws EngineException {
        if (this.mVersion >= 11) {
            this.mAPI.refreshDisplays();
        }
        else this.mAPI.invokeCommand("update");      // old way of doing it.  
    }
    
    /**
     * Indicate whether or not the interface to the engine can support creating
     * disabled break- or watchpoints.
     * If not, we must create them enabled and then immediately disabled.
     * @return whether or not the engine interface can created disabled action points.
     */
    public boolean canCreateDisabledActionPoints(){
        return mVersion >= 13;
    }
    
    /**
     * This method creates a watchpoint, and, as a side-effect, causes a 
     * "new watchpoint" event to occur.
     * <P>
     * This method is normally called from the {@link BreakpointManager}, but may also
     * be indirectly called by the new watchpoint dialog.
     * <P>
     * @param var variable on which watchpoint is to be set.
     * @param length the number of bytes to watch, or 0 if the
     * length is to be derived from the variable.
     * @param conditional if not null, an expression to be evaluated
     * to true before watchpoint takes.
     * @param tid the thread ID if the breakpoint is to be tied to
     * a thread, or 0 if breakpoint is to be applied globally
     * @param flags union of {@link IEngineAPI#WP_WRITE}, {@link IEngineAPI#WP_READ}, or {@link IEngineAPI#BP_HARDWARE}.
     * @param stackFrameID the stack frame in which to evaluate the
     * variable expression.
     * @param attributes a list of attributes of the form "key=value".
     * @return a watchpoint ID number, or 0 if an error occured.
     * @throws EngineException
     */
    public  int createWatchpoint(String var, 
        int length,
        String conditional, int tid, int flags, int stackFrameID,
        boolean enabled,
        String[]attributes) throws EngineException{
        if (!enabled) {
            flags |= IEngineAPI.WP_DISABLED;
            assert mVersion >= 13;
        }
        if (mVersion < 12 || attributes == null || attributes.length == 0) {
            return mAPI.createWatchpoint(var,length,conditional,tid,flags,stackFrameID);           
        }
        return mAPI.createWatchpoint2(var,length,conditional,tid,flags,stackFrameID,attributes);             
    }
    
    /**
     * This method creates a watchpoint on a register.
     * <P>
     * This method is normally called from the {@link BreakpointManager}, but may also
     * be indirectly called by the new watchpoint dialog.
     * <P>
     * @param regID the ID of the register to set a watchpoint on.
     * @param conditional if not null, an expression to be evaluated
     * to true before watchpoint takes.
     * @param tid the thread ID if the breakpoint is to be tied to
     * a thread, or 0 if breakpoint is to be applied globally
     * @param flags union of {@link IEngineAPI#WP_WRITE}, {@link IEngineAPI#WP_READ}, or {@link IEngineAPI#BP_HARDWARE}.
     * @param attributes a list of attributes of the form "key=value".
     * @return a watchpoint ID number, or 0 if an error occured.
     * @throws EngineException
     */
    public  int createWatchpointReg(int regID, 
        String conditional, int tid, int flags, 
        String[]attributes) throws EngineException{
        if (mVersion >= 12) {
            return mAPI.createWatchpointReg(regID,conditional,tid,flags,attributes);           
        }
        throw new EngineException("Debugger is too back-level to support Register watchpoints from the IDE");
              
    }
    
    /**
     * Return the ID of the thread that caused the last stop. Used as a default
     * context for evaluating expressions unless more information is acquired from the
     * UI.
     * @return the ID of the thread that caused the last stop.
     */
    public int getCurrentThread(){
        return mCurrentThreadID;
    }
    
    public int getBreakpointHitCount(int breakID) throws EngineException {
        if (mVersion >= 15) {
            return mAPI.getBreakpointHitCount(breakID);
        }
        return 0;
    }
    
    public void setPC(int tid, Location location) throws EngineException{
        if (doesEngineHandleSetPC()) {
            mAPI.setPC(tid,location);
            invalidateCacheForThread(tid); // who knows what could have changed.
        }
        else throw new EngineException("set-pc operation not supported in the debugger"); // shouldn't happen
    }
    
    /**
     * Set list of pairs of directory translation mappings.
     * path[n] is a compilation directory that is to be remapped to local directory path[n+1];
     * @param paths
     * @throws EngineException 
     */
    public void setDirectoryTranslation(String paths[]) throws EngineException{
        // if (mVersion > XXX) mAPI.setDirectoryTranslation(paths);
        // else {
        StringBuilder buf = new StringBuilder(10*paths.length);
        buf.append("set dir_xlation=");
        for (int i = 0; i < paths.length; i+=2){
            if (i > 0){
                buf.append(File.pathSeparator);
            }
            buf.append(paths[i]); buf.append(',');
            buf.append(paths[i+1]);
        }
        
        invokeCommand(buf.toString());
        //}
    }
    
    /**
     * Is the engine interface late enough to support the new CMPD controller?
     * @return true if the engine supports the CMPD Controller interface.
     */
    public boolean isCMPDControllerSupported(){
        return mVersion > 15;
    }
    
    public boolean doesEngineFireBreakpointCreationEventsWhenCreateBreakpointInvoked(){
        return mVersion >= 16;
    }
    
    public boolean doesEngineHandleSetPC(){
        return mVersion >= 22;
    }
    
    public boolean canEngineHandleCopyToClipboard(){
        return mVersion >= 27;
    }
    
    public void disconnect() throws EngineException {
        mAPI.disconnect();
    }
    
    /**
     * /**
     * The implementer is to either invoke {@link ICustomDisplayCallback#copyToClipboard(String)} or
     * {@link ICustomDisplayCallback#copyVisibleToClipboard(int)}.
     * 
     * @param displayID the applicable display ID.
     * @throws EngineException 
     */
    public void copyAllToClipboard(int displayID) throws EngineException{
        if (canEngineHandleCopyToClipboard()){
             mAPI.copyAllToClipboard(displayID);
        }
        else throw new EngineException("copyAllToClipboard operation not supported in the debugger"); // shouldn't happen
    }
    
    public boolean canDisconnect() {
        try {
            return mVersion >= 24 && mTerminatedState == 0 && mAPI.canDisconnect();
        }
        catch (EngineException e) {
            return false;
        }
    }
}
