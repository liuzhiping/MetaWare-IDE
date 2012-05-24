/*
 * WARNING!
 * This file was auto-generated from com.arc.seecode.engine.gen.GenClient
 * DO NOT MODIFY BY HAND!
 */

package com.arc.seecode.client;

import java.lang.reflect.Method;
import com.arc.seecode.engine.AssemblyRecord;
import com.arc.seecode.engine.JavaFactory;
import com.arc.seecode.engine.Location;
import com.arc.seecode.engine.Value;
import com.arc.seecode.engine.RegisterContent;
import com.arc.seecode.engine.Variable;
import com.arc.seecode.engine.IEngineAPI;
import com.arc.seecode.engine.WatchpointHit;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.connect.IConnection;
import com.arc.seecode.connect.ICommandReceiverRouter;
import com.arc.seecode.connect.ITimeoutCallback;
import com.arc.seecode.engine.type.ITypeFactory;
import com.arc.seecode.serialize.MethodSerializationHandler;
import com.arc.seecode.serialize.IMethodSerializer;
import com.arc.seecode.serialize.IMethodFilter;
import com.arc.seecode.engine.IRunner;
public class Client extends AbstractClient {

    /**
     */
    public Client(IConnection connection,
             ICommandReceiverRouter router,JavaFactory jFactory, ITypeFactory typeFactory, IRunner displayCallbackRunner, int defaultTimeout,
                           int loadTimeout, ITimeoutCallback loadTimeoutCallback, int cmpdProcessID) {
        super(connection,router,jFactory,typeFactory,displayCallbackRunner,defaultTimeout, loadTimeout, loadTimeoutCallback,cmpdProcessID);
        MethodSerializationHandler ms = new MethodSerializationHandler(this,IEngineAPI.class,new MyMethodFilter(),jFactory,typeFactory);
        mIsActive = ms.getSerializerFor("isActive");
        mGetPID = ms.getSerializerFor("getPID");
        mSendValueUpdate = ms.getSerializerFor("sendValueUpdate");
        mSendValueUpdate2 = ms.getSerializerFor("sendValueUpdate2");
        mSetEngineArguments = ms.getSerializerFor("setEngineArguments");
        mLoadProgram = ms.getSerializerFor("loadProgram");
        mRestart = ms.getSerializerFor("restart");
        mComputeLocation = ms.getSerializerFor("computeLocation");
        mGetErrorMessage = ms.getSerializerFor("getErrorMessage");
        mEvaluateLocation = ms.getSerializerFor("evaluateLocation");
        mLookupSource = ms.getSerializerFor("lookupSource");
        mAttach = ms.getSerializerFor("attach");
        mQueryState = ms.getSerializerFor("queryState");
        mGetBreakpointHit = ms.getSerializerFor("getBreakpointHit");
        mGetWatchpointHits = ms.getSerializerFor("getWatchpointHits");
        mStop = ms.getSerializerFor("stop");
        mIsSimulator = ms.getSerializerFor("isSimulator");
        mHasThreadControl = ms.getSerializerFor("hasThreadControl");
        mResume = ms.getSerializerFor("resume");
        mInstructionStep = ms.getSerializerFor("instructionStep");
        mStatementStep = ms.getSerializerFor("statementStep");
        mGetThreads = ms.getSerializerFor("getThreads");
        mCreateBreakpoint = ms.getSerializerFor("createBreakpoint");
        mCreateWatchpoint = ms.getSerializerFor("createWatchpoint");
        mCreateWatchpoint2 = ms.getSerializerFor("createWatchpoint2");
        mCreateWatchpointReg = ms.getSerializerFor("createWatchpointReg");
        mFreeBreakpoint = ms.getSerializerFor("freeBreakpoint");
        mSetBreakpointEnabled = ms.getSerializerFor("setBreakpointEnabled");
        mRemoveBreakpoint = ms.getSerializerFor("removeBreakpoint");
        mIsValidBreakpoint = ms.getSerializerFor("isValidBreakpoint");
        mTerminate = ms.getSerializerFor("terminate");
        mShutdown = ms.getSerializerFor("shutdown");
        mIsActiveThread = ms.getSerializerFor("isActiveThread");
        mGetLocals = ms.getSerializerFor("getLocals");
        mGetNonlocals = ms.getSerializerFor("getNonlocals");
        mIsValidStackFrameID = ms.getSerializerFor("isValidStackFrameID");
        mGetProcessExitCode = ms.getSerializerFor("getProcessExitCode");
        mEvaluate = ms.getSerializerFor("evaluate");
        mGetValueElement = ms.getSerializerFor("getValueElement");
        mSetValueElement = ms.getSerializerFor("setValueElement");
        mFreeValueCookie = ms.getSerializerFor("freeValueCookie");
        mLookupVariable = ms.getSerializerFor("lookupVariable");
        mLookupGlobalVariable = ms.getSerializerFor("lookupGlobalVariable");
        mGetExecutableLines = ms.getSerializerFor("getExecutableLines");
        mStart = ms.getSerializerFor("start");
        mStepOut = ms.getSerializerFor("stepOut");
        mRunToAddress = ms.getSerializerFor("runToAddress");
        mGetPC = ms.getSerializerFor("getPC");
        mFreeStackFrameID = ms.getSerializerFor("freeStackFrameID");
        mGetStackPointer = ms.getSerializerFor("getStackPointer");
        mGetThreadName = ms.getSerializerFor("getThreadName");
        mGetFramePointer = ms.getSerializerFor("getFramePointer");
        mGetRegisterBankCount = ms.getSerializerFor("getRegisterBankCount");
        mGetRegisterBankName = ms.getSerializerFor("getRegisterBankName");
        mGetRegisterIDsFromBank = ms.getSerializerFor("getRegisterIDsFromBank");
        mIsRegisterBankActive = ms.getSerializerFor("isRegisterBankActive");
        mGetRegisterName = ms.getSerializerFor("getRegisterName");
        mGetRegisterValue = ms.getSerializerFor("getRegisterValue");
        mSetRegisterValue = ms.getSerializerFor("setRegisterValue");
        mGetModules = ms.getSerializerFor("getModules");
        mGetModuleName = ms.getSerializerFor("getModuleName");
        mGetModuleBaseAddress = ms.getSerializerFor("getModuleBaseAddress");
        mGetModuleSize = ms.getSerializerFor("getModuleSize");
        mGetFunctionsWithinModule = ms.getSerializerFor("getFunctionsWithinModule");
        mDisassemble = ms.getSerializerFor("disassemble");
        mSetSourceDirectories = ms.getSerializerFor("setSourceDirectories");
        mIsValidMemory = ms.getSerializerFor("isValidMemory");
        mGetMemoryBytes = ms.getSerializerFor("getMemoryBytes");
        mSetMemoryByte = ms.getSerializerFor("setMemoryByte");
        mSetMemoryBytes = ms.getSerializerFor("setMemoryBytes");
        mSetMemoryWord = ms.getSerializerFor("setMemoryWord");
        mSetVariable = ms.getSerializerFor("setVariable");
        mSetBreakpointCondition = ms.getSerializerFor("setBreakpointCondition");
        mSetBreakpointHitCount = ms.getSerializerFor("setBreakpointHitCount");
        mSetBreakpointThreads = ms.getSerializerFor("setBreakpointThreads");
        mMakeStackFrameID = ms.getSerializerFor("makeStackFrameID");
        mComputeCallerFrame = ms.getSerializerFor("computeCallerFrame");
        mWaitForStop = ms.getSerializerFor("waitForStop");
        mSetToggle = ms.getSerializerFor("setToggle");
        mGetExceptionIDs = ms.getSerializerFor("getExceptionIDs");
        mGetExceptionName = ms.getSerializerFor("getExceptionName");
        mGetExceptionDescription = ms.getSerializerFor("getExceptionDescription");
        mGetExceptionHit = ms.getSerializerFor("getExceptionHit");
        mSetSuspendOnException = ms.getSerializerFor("setSuspendOnException");
        mSetIgnoreException = ms.getSerializerFor("setIgnoreException");
        mClearPendingException = ms.getSerializerFor("clearPendingException");
        mThrowException = ms.getSerializerFor("throwException");
        mCreateDisplay = ms.getSerializerFor("createDisplay");
        mInvokeCommand = ms.getSerializerFor("invokeCommand");
        mGetDisplaySelectors = ms.getSerializerFor("getDisplaySelectors");
        mProgressDisplayCanceled = ms.getSerializerFor("progressDisplayCanceled");
        mCloseDisplay = ms.getSerializerFor("closeDisplay");
        mSetArgsFilePattern = ms.getSerializerFor("setArgsFilePattern");
        mSetWorkingDirectory = ms.getSerializerFor("setWorkingDirectory");
        mSetStackFrame = ms.getSerializerFor("setStackFrame");
        mGetEngineInterfaceVersion = ms.getSerializerFor("getEngineInterfaceVersion");
        mGetEngineVersionStrings = ms.getSerializerFor("getEngineVersionStrings");
        mGetLicenseExpirationDays = ms.getSerializerFor("getLicenseExpirationDays");
        mGetSplashPath = ms.getSerializerFor("getSplashPath");
        mIsLicenseFailurePending = ms.getSerializerFor("isLicenseFailurePending");
        mGetRegisterContent = ms.getSerializerFor("getRegisterContent");
        mGetRegisterContentArray = ms.getSerializerFor("getRegisterContentArray");
        mGetDefaultBank = ms.getSerializerFor("getDefaultBank");
        mWriteStdin = ms.getSerializerFor("writeStdin");
        mRefreshDisplay = ms.getSerializerFor("refreshDisplay");
        mRefreshDisplays = ms.getSerializerFor("refreshDisplays");
        mGetBreakpointHitCount = ms.getSerializerFor("getBreakpointHitCount");
        mSetPC = ms.getSerializerFor("setPC");
        mDisconnect = ms.getSerializerFor("disconnect");
        mCanDisconnect = ms.getSerializerFor("canDisconnect");
        mCopyAllToClipboard = ms.getSerializerFor("copyAllToClipboard");
    }

    /**
     * Implements {@link IEngineAPI#isActive()}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public boolean isActive() throws EngineException {
        Object[] argsArray = new Object[]{};
        Object result = this.invokeRemoteMethod(mIsActive, argsArray);
        return ((Boolean)result).booleanValue();
    }

    /**
     * Implements {@link IEngineAPI#getPID()}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public int getPID() throws EngineException {
        Object[] argsArray = new Object[]{};
        Object result = this.invokeRemoteMethod(mGetPID, argsArray);
        return ((Integer)result).intValue();
    }

    /**
     * Implements {@link IEngineAPI#sendValueUpdate(int, String, String)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void sendValueUpdate(int displayID, String property, String value) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(displayID),
                property,
                value};
        this.invokeRemoteMethod(mSendValueUpdate, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#sendValueUpdate2(int, String, String, int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public boolean sendValueUpdate2(int displayID, String property, String value, int timeout) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(displayID),
                property,
                value,
                new Integer(timeout)};
        Object result = this.invokeRemoteMethod(mSendValueUpdate2, argsArray);
        return ((Boolean)result).booleanValue();
    }

    /**
     * Implements {@link IEngineAPI#setEngineArguments(String)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void setEngineArguments(String args) throws EngineException {
        Object[] argsArray = new Object[]{
                args};
        this.invokeRemoteMethod(mSetEngineArguments, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#loadProgram(String[])}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public boolean loadProgram(String[] command) throws EngineException {
        Object[] argsArray = new Object[]{
                command};
        Object result = this.invokeRemoteMethod(mLoadProgram, argsArray,mLoadTimeout,mLoadTimeoutCallback);
        return ((Boolean)result).booleanValue();
    }

    /**
     * Implements {@link IEngineAPI#restart(String[])}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public boolean restart(String[] args) throws EngineException {
        Object[] argsArray = new Object[]{
                args};
        Object result = this.invokeRemoteMethod(mRestart, argsArray);
        return ((Boolean)result).booleanValue();
    }

    /**
     * Implements {@link IEngineAPI#computeLocation(long)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public Location computeLocation(long address) throws EngineException {
        Object[] argsArray = new Object[]{
                new Long(address)};
        Object result = this.invokeRemoteMethod(mComputeLocation, argsArray);
        return (Location)result;
    }

    /**
     * Implements {@link IEngineAPI#getErrorMessage()}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public String[] getErrorMessage() throws EngineException {
        Object[] argsArray = new Object[]{};
        Object result = this.invokeRemoteMethod(mGetErrorMessage, argsArray);
        return (String[])result;
    }

    /**
     * Implements {@link IEngineAPI#evaluateLocation(String, int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public Location evaluateLocation(String expression, int stackFrameID) throws EngineException {
        Object[] argsArray = new Object[]{
                expression,
                new Integer(stackFrameID)};
        Object result = this.invokeRemoteMethod(mEvaluateLocation, argsArray);
        return (Location)result;
    }

    /**
     * Implements {@link IEngineAPI#lookupSource(String, int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public Location lookupSource(String source, int line) throws EngineException {
        Object[] argsArray = new Object[]{
                source,
                new Integer(line)};
        Object result = this.invokeRemoteMethod(mLookupSource, argsArray);
        return (Location)result;
    }

    /**
     * Implements {@link IEngineAPI#attach(int, String)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public boolean attach(int cmpdID, String pid) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(cmpdID),
                pid};
        Object result = this.invokeRemoteMethod(mAttach, argsArray);
        return ((Boolean)result).booleanValue();
    }

    /**
     * Implements {@link IEngineAPI#queryState(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public int queryState(int tid) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(tid)};
        Object result = this.invokeRemoteMethod(mQueryState, argsArray);
        return ((Integer)result).intValue();
    }

    /**
     * Implements {@link IEngineAPI#getBreakpointHit(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public int getBreakpointHit(int tid) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(tid)};
        Object result = this.invokeRemoteMethod(mGetBreakpointHit, argsArray);
        return ((Integer)result).intValue();
    }

    /**
     * Implements {@link IEngineAPI#getWatchpointHits(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public WatchpointHit[] getWatchpointHits(int tid) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(tid)};
        Object result = this.invokeRemoteMethod(mGetWatchpointHits, argsArray);
        return (WatchpointHit[])result;
    }

    /**
     * Implements {@link IEngineAPI#stop(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void stop(int tid) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(tid)};
        this.invokeRemoteMethod(mStop, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#isSimulator()}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public boolean isSimulator() throws EngineException {
        Object[] argsArray = new Object[]{};
        Object result = this.invokeRemoteMethod(mIsSimulator, argsArray);
        return ((Boolean)result).booleanValue();
    }

    /**
     * Implements {@link IEngineAPI#hasThreadControl()}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public boolean hasThreadControl() throws EngineException {
        Object[] argsArray = new Object[]{};
        Object result = this.invokeRemoteMethod(mHasThreadControl, argsArray);
        return ((Boolean)result).booleanValue();
    }

    /**
     * Implements {@link IEngineAPI#resume(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void resume(int tid) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(tid)};
        this.invokeRemoteMethod(mResume, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#instructionStep(int, boolean, int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void instructionStep(int tid, boolean over, int cnt) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(tid),
                new Boolean(over),
                new Integer(cnt)};
        this.invokeRemoteMethod(mInstructionStep, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#statementStep(int, boolean, int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void statementStep(int tid, boolean over, int cnt) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(tid),
                new Boolean(over),
                new Integer(cnt)};
        this.invokeRemoteMethod(mStatementStep, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#getThreads()}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public int[] getThreads() throws EngineException {
        Object[] argsArray = new Object[]{};
        Object result = this.invokeRemoteMethod(mGetThreads, argsArray);
        return (int[])result;
    }

    /**
     * Implements {@link IEngineAPI#createBreakpoint(Location, int, String, int, int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public int createBreakpoint(Location location, int hitCount, String conditional, int tid, int flags) throws EngineException {
        Object[] argsArray = new Object[]{
                location,
                new Integer(hitCount),
                conditional,
                new Integer(tid),
                new Integer(flags)};
        Object result = this.invokeRemoteMethod(mCreateBreakpoint, argsArray);
        return ((Integer)result).intValue();
    }

    /**
     * Implements {@link IEngineAPI#createWatchpoint(String, int, String, int, int, int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public int createWatchpoint(String var, int length, String conditional, int tid, int flags, int stackFrameID) throws EngineException {
        Object[] argsArray = new Object[]{
                var,
                new Integer(length),
                conditional,
                new Integer(tid),
                new Integer(flags),
                new Integer(stackFrameID)};
        Object result = this.invokeRemoteMethod(mCreateWatchpoint, argsArray);
        return ((Integer)result).intValue();
    }

    /**
     * Implements {@link IEngineAPI#createWatchpoint2(String, int, String, int, int, int, String[])}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public int createWatchpoint2(String var, int length, String conditional, int tid, int flags, int stackFrameID, String[] attributes) throws EngineException {
        Object[] argsArray = new Object[]{
                var,
                new Integer(length),
                conditional,
                new Integer(tid),
                new Integer(flags),
                new Integer(stackFrameID),
                attributes};
        Object result = this.invokeRemoteMethod(mCreateWatchpoint2, argsArray);
        return ((Integer)result).intValue();
    }

    /**
     * Implements {@link IEngineAPI#createWatchpointReg(int, String, int, int, String[])}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public int createWatchpointReg(int regID, String conditional, int tid, int flags, String[] attributes) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(regID),
                conditional,
                new Integer(tid),
                new Integer(flags),
                attributes};
        Object result = this.invokeRemoteMethod(mCreateWatchpointReg, argsArray);
        return ((Integer)result).intValue();
    }

    /**
     * Implements {@link IEngineAPI#freeBreakpoint(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void freeBreakpoint(int breakID) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(breakID)};
        this.invokeRemoteMethod(mFreeBreakpoint, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#setBreakpointEnabled(int, boolean)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void setBreakpointEnabled(int id, boolean v) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(id),
                new Boolean(v)};
        this.invokeRemoteMethod(mSetBreakpointEnabled, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#removeBreakpoint(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void removeBreakpoint(int breakID) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(breakID)};
        this.invokeRemoteMethod(mRemoveBreakpoint, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#isValidBreakpoint(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public boolean isValidBreakpoint(int breakID) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(breakID)};
        Object result = this.invokeRemoteMethod(mIsValidBreakpoint, argsArray);
        return ((Boolean)result).booleanValue();
    }

    /**
     * Implements {@link IEngineAPI#terminate()}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void terminate() throws EngineException {
        Object[] argsArray = new Object[]{};
        this.invokeRemoteMethod(mTerminate, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#shutdown()}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void shutdown() throws EngineException {
        Object[] argsArray = new Object[]{};
        this.invokeRemoteMethod(mShutdown, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#isActiveThread(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public boolean isActiveThread(int tid) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(tid)};
        Object result = this.invokeRemoteMethod(mIsActiveThread, argsArray);
        return ((Boolean)result).booleanValue();
    }

    /**
     * Implements {@link IEngineAPI#getLocals(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public Variable[] getLocals(int regSetSnapshot) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(regSetSnapshot)};
        Object result = this.invokeRemoteMethod(mGetLocals, argsArray);
        return (Variable[])result;
    }

    /**
     * Implements {@link IEngineAPI#getNonlocals(long)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public Variable[] getNonlocals(long pc) throws EngineException {
        Object[] argsArray = new Object[]{
                new Long(pc)};
        Object result = this.invokeRemoteMethod(mGetNonlocals, argsArray);
        return (Variable[])result;
    }

    /**
     * Implements {@link IEngineAPI#isValidStackFrameID(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public boolean isValidStackFrameID(int stackFrameID) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(stackFrameID)};
        Object result = this.invokeRemoteMethod(mIsValidStackFrameID, argsArray);
        return ((Boolean)result).booleanValue();
    }

    /**
     * Implements {@link IEngineAPI#getProcessExitCode()}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public int getProcessExitCode() throws EngineException {
        Object[] argsArray = new Object[]{};
        Object result = this.invokeRemoteMethod(mGetProcessExitCode, argsArray);
        return ((Integer)result).intValue();
    }

    /**
     * Implements {@link IEngineAPI#evaluate(String, int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public Value evaluate(String expression, int regSetSnapshot) throws EngineException {
        Object[] argsArray = new Object[]{
                expression,
                new Integer(regSetSnapshot)};
        Object result = this.invokeRemoteMethod(mEvaluate, argsArray);
        return (Value)result;
    }

    /**
     * Implements {@link IEngineAPI#getValueElement(int, int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public Value getValueElement(int cookie, int elementIndex) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(cookie),
                new Integer(elementIndex)};
        Object result = this.invokeRemoteMethod(mGetValueElement, argsArray);
        return (Value)result;
    }

    /**
     * Implements {@link IEngineAPI#setValueElement(int, int, String, int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public boolean setValueElement(int cookie, int elementIndex, String newValue, int frameID) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(cookie),
                new Integer(elementIndex),
                newValue,
                new Integer(frameID)};
        Object result = this.invokeRemoteMethod(mSetValueElement, argsArray);
        return ((Boolean)result).booleanValue();
    }

    /**
     * Implements {@link IEngineAPI#freeValueCookie(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void freeValueCookie(int cookie) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(cookie)};
        this.invokeRemoteMethod(mFreeValueCookie, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#lookupVariable(String, int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public Variable lookupVariable(String name, int regSetSnapshot) throws EngineException {
        Object[] argsArray = new Object[]{
                name,
                new Integer(regSetSnapshot)};
        Object result = this.invokeRemoteMethod(mLookupVariable, argsArray);
        return (Variable)result;
    }

    /**
     * Implements {@link IEngineAPI#lookupGlobalVariable(String)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public Variable lookupGlobalVariable(String name) throws EngineException {
        Object[] argsArray = new Object[]{
                name};
        Object result = this.invokeRemoteMethod(mLookupGlobalVariable, argsArray);
        return (Variable)result;
    }

    /**
     * Implements {@link IEngineAPI#getExecutableLines(String, int, int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public int[] getExecutableLines(String source, int rangeLo, int rangeHi) throws EngineException {
        Object[] argsArray = new Object[]{
                source,
                new Integer(rangeLo),
                new Integer(rangeHi)};
        Object result = this.invokeRemoteMethod(mGetExecutableLines, argsArray);
        return (int[])result;
    }

    /**
     * Implements {@link IEngineAPI#start()}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void start() throws EngineException {
        Object[] argsArray = new Object[]{};
        this.invokeRemoteMethod(mStart, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#stepOut(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void stepOut(int tid) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(tid)};
        this.invokeRemoteMethod(mStepOut, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#runToAddress(int, Location)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void runToAddress(int tid, Location location) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(tid),
                location};
        this.invokeRemoteMethod(mRunToAddress, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#getPC(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public long getPC(int regSetSnapshot) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(regSetSnapshot)};
        Object result = this.invokeRemoteMethod(mGetPC, argsArray);
        return ((Long)result).longValue();
    }

    /**
     * Implements {@link IEngineAPI#freeStackFrameID(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void freeStackFrameID(int stackFrameID) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(stackFrameID)};
        this.invokeRemoteMethod(mFreeStackFrameID, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#getStackPointer(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public long getStackPointer(int regSetSnapshot) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(regSetSnapshot)};
        Object result = this.invokeRemoteMethod(mGetStackPointer, argsArray);
        return ((Long)result).longValue();
    }

    /**
     * Implements {@link IEngineAPI#getThreadName(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public String getThreadName(int tid) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(tid)};
        Object result = this.invokeRemoteMethod(mGetThreadName, argsArray);
        return (String)result;
    }

    /**
     * Implements {@link IEngineAPI#getFramePointer(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public long getFramePointer(int regSetSnapshot) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(regSetSnapshot)};
        Object result = this.invokeRemoteMethod(mGetFramePointer, argsArray);
        return ((Long)result).longValue();
    }

    /**
     * Implements {@link IEngineAPI#getRegisterBankCount()}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public int getRegisterBankCount() throws EngineException {
        Object[] argsArray = new Object[]{};
        Object result = this.invokeRemoteMethod(mGetRegisterBankCount, argsArray);
        return ((Integer)result).intValue();
    }

    /**
     * Implements {@link IEngineAPI#getRegisterBankName(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public String getRegisterBankName(int bank) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(bank)};
        Object result = this.invokeRemoteMethod(mGetRegisterBankName, argsArray);
        return (String)result;
    }

    /**
     * Implements {@link IEngineAPI#getRegisterIDsFromBank(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public int[] getRegisterIDsFromBank(int bank) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(bank)};
        Object result = this.invokeRemoteMethod(mGetRegisterIDsFromBank, argsArray);
        return (int[])result;
    }

    /**
     * Implements {@link IEngineAPI#isRegisterBankActive(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public boolean isRegisterBankActive(int bank) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(bank)};
        Object result = this.invokeRemoteMethod(mIsRegisterBankActive, argsArray);
        return ((Boolean)result).booleanValue();
    }

    /**
     * Implements {@link IEngineAPI#getRegisterName(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public String getRegisterName(int regID) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(regID)};
        Object result = this.invokeRemoteMethod(mGetRegisterName, argsArray);
        return (String)result;
    }

    /**
     * Implements {@link IEngineAPI#getRegisterValue(int, int, int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public String getRegisterValue(int stackframeID, int regID, int format) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(stackframeID),
                new Integer(regID),
                new Integer(format)};
        Object result = this.invokeRemoteMethod(mGetRegisterValue, argsArray);
        return (String)result;
    }

    /**
     * Implements {@link IEngineAPI#setRegisterValue(int, int, String)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public boolean setRegisterValue(int frameID, int regID, String value) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(frameID),
                new Integer(regID),
                value};
        Object result = this.invokeRemoteMethod(mSetRegisterValue, argsArray);
        return ((Boolean)result).booleanValue();
    }

    /**
     * Implements {@link IEngineAPI#getModules()}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public int[] getModules() throws EngineException {
        Object[] argsArray = new Object[]{};
        Object result = this.invokeRemoteMethod(mGetModules, argsArray);
        return (int[])result;
    }

    /**
     * Implements {@link IEngineAPI#getModuleName(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public String getModuleName(int moduleID) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(moduleID)};
        Object result = this.invokeRemoteMethod(mGetModuleName, argsArray);
        return (String)result;
    }

    /**
     * Implements {@link IEngineAPI#getModuleBaseAddress(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public long getModuleBaseAddress(int moduleID) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(moduleID)};
        Object result = this.invokeRemoteMethod(mGetModuleBaseAddress, argsArray);
        return ((Long)result).longValue();
    }

    /**
     * Implements {@link IEngineAPI#getModuleSize(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public long getModuleSize(int moduleID) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(moduleID)};
        Object result = this.invokeRemoteMethod(mGetModuleSize, argsArray);
        return ((Long)result).longValue();
    }

    /**
     * Implements {@link IEngineAPI#getFunctionsWithinModule(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public Location[] getFunctionsWithinModule(int moduleID) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(moduleID)};
        Object result = this.invokeRemoteMethod(mGetFunctionsWithinModule, argsArray);
        return (Location[])result;
    }

    /**
     * Implements {@link IEngineAPI#disassemble(long, int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public AssemblyRecord[] disassemble(long addr, int instructionCount) throws EngineException {
        Object[] argsArray = new Object[]{
                new Long(addr),
                new Integer(instructionCount)};
        Object result = this.invokeRemoteMethod(mDisassemble, argsArray);
        return (AssemblyRecord[])result;
    }

    /**
     * Implements {@link IEngineAPI#setSourceDirectories(String[])}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void setSourceDirectories(String[] dirs) throws EngineException {
        Object[] argsArray = new Object[]{
                dirs};
        this.invokeRemoteMethod(mSetSourceDirectories, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#isValidMemory(long, int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public boolean isValidMemory(long address, int flags) throws EngineException {
        Object[] argsArray = new Object[]{
                new Long(address),
                new Integer(flags)};
        Object result = this.invokeRemoteMethod(mIsValidMemory, argsArray);
        return ((Boolean)result).booleanValue();
    }

    /**
     * Implements {@link IEngineAPI#getMemoryBytes(long, int, int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public byte[] getMemoryBytes(long address, int length, int flags) throws EngineException {
        Object[] argsArray = new Object[]{
                new Long(address),
                new Integer(length),
                new Integer(flags)};
        Object result = this.invokeRemoteMethod(mGetMemoryBytes, argsArray);
        return (byte[])result;
    }

    /**
     * Implements {@link IEngineAPI#setMemoryByte(long, int, int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public boolean setMemoryByte(long address, int value, int flags) throws EngineException {
        Object[] argsArray = new Object[]{
                new Long(address),
                new Integer(value),
                new Integer(flags)};
        Object result = this.invokeRemoteMethod(mSetMemoryByte, argsArray);
        return ((Boolean)result).booleanValue();
    }

    /**
     * Implements {@link IEngineAPI#setMemoryBytes(long, byte[], int, int, int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public int setMemoryBytes(long address, byte[] buffer, int offset, int length, int flags) throws EngineException {
        Object[] argsArray = new Object[]{
                new Long(address),
                buffer,
                new Integer(offset),
                new Integer(length),
                new Integer(flags)};
        Object result = this.invokeRemoteMethod(mSetMemoryBytes, argsArray);
        return ((Integer)result).intValue();
    }

    /**
     * Implements {@link IEngineAPI#setMemoryWord(long, int, int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public boolean setMemoryWord(long address, int value, int flags) throws EngineException {
        Object[] argsArray = new Object[]{
                new Long(address),
                new Integer(value),
                new Integer(flags)};
        Object result = this.invokeRemoteMethod(mSetMemoryWord, argsArray);
        return ((Boolean)result).booleanValue();
    }

    /**
     * Implements {@link IEngineAPI#setVariable(Variable, String)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public boolean setVariable(Variable var, String value) throws EngineException {
        Object[] argsArray = new Object[]{
                var,
                value};
        Object result = this.invokeRemoteMethod(mSetVariable, argsArray);
        return ((Boolean)result).booleanValue();
    }

    /**
     * Implements {@link IEngineAPI#setBreakpointCondition(int, String)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void setBreakpointCondition(int breakID, String expression) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(breakID),
                expression};
        this.invokeRemoteMethod(mSetBreakpointCondition, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#setBreakpointHitCount(int, int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void setBreakpointHitCount(int breakID, int count) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(breakID),
                new Integer(count)};
        this.invokeRemoteMethod(mSetBreakpointHitCount, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#setBreakpointThreads(int, int[])}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void setBreakpointThreads(int breakID, int[] threadIDs) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(breakID),
                threadIDs};
        this.invokeRemoteMethod(mSetBreakpointThreads, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#makeStackFrameID(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public int makeStackFrameID(int tid) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(tid)};
        Object result = this.invokeRemoteMethod(mMakeStackFrameID, argsArray);
        return ((Integer)result).intValue();
    }

    /**
     * Implements {@link IEngineAPI#computeCallerFrame(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public int computeCallerFrame(int stackFrameID) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(stackFrameID)};
        Object result = this.invokeRemoteMethod(mComputeCallerFrame, argsArray);
        return ((Integer)result).intValue();
    }

    /**
     * Implements {@link IEngineAPI#waitForStop(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public boolean waitForStop(int timeoutMillis) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(timeoutMillis)};
        Object result = this.invokeRemoteMethod(mWaitForStop, argsArray);
        return ((Boolean)result).booleanValue();
    }

    /**
     * Implements {@link IEngineAPI#setToggle(String, boolean)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void setToggle(String toggleName, boolean on) throws EngineException {
        Object[] argsArray = new Object[]{
                toggleName,
                new Boolean(on)};
        this.invokeRemoteMethod(mSetToggle, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#getExceptionIDs()}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public int[] getExceptionIDs() throws EngineException {
        Object[] argsArray = new Object[]{};
        Object result = this.invokeRemoteMethod(mGetExceptionIDs, argsArray);
        return (int[])result;
    }

    /**
     * Implements {@link IEngineAPI#getExceptionName(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public String getExceptionName(int id) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(id)};
        Object result = this.invokeRemoteMethod(mGetExceptionName, argsArray);
        return (String)result;
    }

    /**
     * Implements {@link IEngineAPI#getExceptionDescription(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public String getExceptionDescription(int id) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(id)};
        Object result = this.invokeRemoteMethod(mGetExceptionDescription, argsArray);
        return (String)result;
    }

    /**
     * Implements {@link IEngineAPI#getExceptionHit(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public int getExceptionHit(int tid) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(tid)};
        Object result = this.invokeRemoteMethod(mGetExceptionHit, argsArray);
        return ((Integer)result).intValue();
    }

    /**
     * Implements {@link IEngineAPI#setSuspendOnException(int, boolean)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void setSuspendOnException(int id, boolean suspend) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(id),
                new Boolean(suspend)};
        this.invokeRemoteMethod(mSetSuspendOnException, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#setIgnoreException(int, boolean)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void setIgnoreException(int id, boolean ignore) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(id),
                new Boolean(ignore)};
        this.invokeRemoteMethod(mSetIgnoreException, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#clearPendingException()}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void clearPendingException() throws EngineException {
        Object[] argsArray = new Object[]{};
        this.invokeRemoteMethod(mClearPendingException, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#throwException(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void throwException(int id) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(id)};
        this.invokeRemoteMethod(mThrowException, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#createDisplay(int, String)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void createDisplay(int id, String kind) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(id),
                kind};
        this.invokeRemoteMethod(mCreateDisplay, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#invokeCommand(String)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void invokeCommand(String command) throws EngineException {
        Object[] argsArray = new Object[]{
                command};
        this.invokeRemoteMethod(mInvokeCommand, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#getDisplaySelectors()}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public String[] getDisplaySelectors() throws EngineException {
        Object[] argsArray = new Object[]{};
        Object result = this.invokeRemoteMethod(mGetDisplaySelectors, argsArray);
        return (String[])result;
    }

    /**
     * Implements {@link IEngineAPI#progressDisplayCanceled(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void progressDisplayCanceled(int progressDisplayID) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(progressDisplayID)};
        this.invokeRemoteMethod(mProgressDisplayCanceled, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#closeDisplay(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void closeDisplay(int displayID) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(displayID)};
        this.invokeRemoteMethod(mCloseDisplay, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#setArgsFilePattern(String)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void setArgsFilePattern(String pattern) throws EngineException {
        Object[] argsArray = new Object[]{
                pattern};
        this.invokeRemoteMethod(mSetArgsFilePattern, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#setWorkingDirectory(String)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void setWorkingDirectory(String wd) throws EngineException {
        Object[] argsArray = new Object[]{
                wd};
        this.invokeRemoteMethod(mSetWorkingDirectory, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#setStackFrame(int, int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void setStackFrame(int displayID, int frameID) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(displayID),
                new Integer(frameID)};
        this.invokeRemoteMethod(mSetStackFrame, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#getEngineInterfaceVersion()}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public int getEngineInterfaceVersion() throws EngineException {
        Object[] argsArray = new Object[]{};
        Object result = this.invokeRemoteMethod(mGetEngineInterfaceVersion, argsArray);
        return ((Integer)result).intValue();
    }

    /**
     * Implements {@link IEngineAPI#getEngineVersionStrings()}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public String[] getEngineVersionStrings() throws EngineException {
        Object[] argsArray = new Object[]{};
        Object result = this.invokeRemoteMethod(mGetEngineVersionStrings, argsArray);
        return (String[])result;
    }

    /**
     * Implements {@link IEngineAPI#getLicenseExpirationDays()}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public int getLicenseExpirationDays() throws EngineException {
        Object[] argsArray = new Object[]{};
        Object result = this.invokeRemoteMethod(mGetLicenseExpirationDays, argsArray);
        return ((Integer)result).intValue();
    }

    /**
     * Implements {@link IEngineAPI#getSplashPath()}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public String getSplashPath() throws EngineException {
        Object[] argsArray = new Object[]{};
        Object result = this.invokeRemoteMethod(mGetSplashPath, argsArray);
        return (String)result;
    }

    /**
     * Implements {@link IEngineAPI#isLicenseFailurePending()}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public boolean isLicenseFailurePending() throws EngineException {
        Object[] argsArray = new Object[]{};
        Object result = this.invokeRemoteMethod(mIsLicenseFailurePending, argsArray);
        return ((Boolean)result).booleanValue();
    }

    /**
     * Implements {@link IEngineAPI#getRegisterContent(int, int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public RegisterContent getRegisterContent(int frameID, int regID) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(frameID),
                new Integer(regID)};
        Object result = this.invokeRemoteMethod(mGetRegisterContent, argsArray);
        return (RegisterContent)result;
    }

    /**
     * Implements {@link IEngineAPI#getRegisterContentArray(int, int[])}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public RegisterContent[] getRegisterContentArray(int frameID, int[] regIDs) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(frameID),
                regIDs};
        Object result = this.invokeRemoteMethod(mGetRegisterContentArray, argsArray);
        return (RegisterContent[])result;
    }

    /**
     * Implements {@link IEngineAPI#getDefaultBank()}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public int getDefaultBank() throws EngineException {
        Object[] argsArray = new Object[]{};
        Object result = this.invokeRemoteMethod(mGetDefaultBank, argsArray);
        return ((Integer)result).intValue();
    }

    /**
     * Implements {@link IEngineAPI#writeStdin(byte[])}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void writeStdin(byte[] data) throws EngineException {
        Object[] argsArray = new Object[]{
                data};
        this.invokeRemoteMethod(mWriteStdin, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#refreshDisplay(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void refreshDisplay(int id) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(id)};
        this.invokeRemoteMethod(mRefreshDisplay, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#refreshDisplays()}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void refreshDisplays() throws EngineException {
        Object[] argsArray = new Object[]{};
        this.invokeRemoteMethod(mRefreshDisplays, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#getBreakpointHitCount(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public int getBreakpointHitCount(int breakID) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(breakID)};
        Object result = this.invokeRemoteMethod(mGetBreakpointHitCount, argsArray);
        return ((Integer)result).intValue();
    }

    /**
     * Implements {@link IEngineAPI#setPC(int, Location)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void setPC(int tid, Location location) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(tid),
                location};
        this.invokeRemoteMethod(mSetPC, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#disconnect()}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void disconnect() throws EngineException {
        Object[] argsArray = new Object[]{};
        this.invokeRemoteMethod(mDisconnect, argsArray);
    }

    /**
     * Implements {@link IEngineAPI#canDisconnect()}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public boolean canDisconnect() throws EngineException {
        Object[] argsArray = new Object[]{};
        Object result = this.invokeRemoteMethod(mCanDisconnect, argsArray);
        return ((Boolean)result).booleanValue();
    }

    /**
     * Implements {@link IEngineAPI#copyAllToClipboard(int)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void copyAllToClipboard(int displayID) throws EngineException {
        Object[] argsArray = new Object[]{
                new Integer(displayID)};
        this.invokeRemoteMethod(mCopyAllToClipboard, argsArray);
    }

    static class MyMethodFilter implements IMethodFilter {
        @Override
        public boolean includeMethod(Method m){
             return

              !m.getName().equals("invokeRemoteMethod")  &&
              !m.getName().equals("invokeRemoteMethod")  &&
              !m.getName().equals("isTracing")  &&
              !m.getName().equals("trace")  &&
              !m.getName().equals("onShutdown")  &&
              !m.getName().equals("setBreakpointObserver")  &&
              !m.getName().equals("setCustomDisplayCallback")  &&
              !m.getName().equals("setEngineObserver");
        }
    }

    private IMethodSerializer mIsActive;
    private IMethodSerializer mGetPID;
    private IMethodSerializer mSendValueUpdate;
    private IMethodSerializer mSendValueUpdate2;
    private IMethodSerializer mSetEngineArguments;
    private IMethodSerializer mLoadProgram;
    private IMethodSerializer mRestart;
    private IMethodSerializer mComputeLocation;
    private IMethodSerializer mGetErrorMessage;
    private IMethodSerializer mEvaluateLocation;
    private IMethodSerializer mLookupSource;
    private IMethodSerializer mAttach;
    private IMethodSerializer mQueryState;
    private IMethodSerializer mGetBreakpointHit;
    private IMethodSerializer mGetWatchpointHits;
    private IMethodSerializer mStop;
    private IMethodSerializer mIsSimulator;
    private IMethodSerializer mHasThreadControl;
    private IMethodSerializer mResume;
    private IMethodSerializer mInstructionStep;
    private IMethodSerializer mStatementStep;
    private IMethodSerializer mGetThreads;
    private IMethodSerializer mCreateBreakpoint;
    private IMethodSerializer mCreateWatchpoint;
    private IMethodSerializer mCreateWatchpoint2;
    private IMethodSerializer mCreateWatchpointReg;
    private IMethodSerializer mFreeBreakpoint;
    private IMethodSerializer mSetBreakpointEnabled;
    private IMethodSerializer mRemoveBreakpoint;
    private IMethodSerializer mIsValidBreakpoint;
    private IMethodSerializer mTerminate;
    private IMethodSerializer mShutdown;
    private IMethodSerializer mIsActiveThread;
    private IMethodSerializer mGetLocals;
    private IMethodSerializer mGetNonlocals;
    private IMethodSerializer mIsValidStackFrameID;
    private IMethodSerializer mGetProcessExitCode;
    private IMethodSerializer mEvaluate;
    private IMethodSerializer mGetValueElement;
    private IMethodSerializer mSetValueElement;
    private IMethodSerializer mFreeValueCookie;
    private IMethodSerializer mLookupVariable;
    private IMethodSerializer mLookupGlobalVariable;
    private IMethodSerializer mGetExecutableLines;
    private IMethodSerializer mStart;
    private IMethodSerializer mStepOut;
    private IMethodSerializer mRunToAddress;
    private IMethodSerializer mGetPC;
    private IMethodSerializer mFreeStackFrameID;
    private IMethodSerializer mGetStackPointer;
    private IMethodSerializer mGetThreadName;
    private IMethodSerializer mGetFramePointer;
    private IMethodSerializer mGetRegisterBankCount;
    private IMethodSerializer mGetRegisterBankName;
    private IMethodSerializer mGetRegisterIDsFromBank;
    private IMethodSerializer mIsRegisterBankActive;
    private IMethodSerializer mGetRegisterName;
    private IMethodSerializer mGetRegisterValue;
    private IMethodSerializer mSetRegisterValue;
    private IMethodSerializer mGetModules;
    private IMethodSerializer mGetModuleName;
    private IMethodSerializer mGetModuleBaseAddress;
    private IMethodSerializer mGetModuleSize;
    private IMethodSerializer mGetFunctionsWithinModule;
    private IMethodSerializer mDisassemble;
    private IMethodSerializer mSetSourceDirectories;
    private IMethodSerializer mIsValidMemory;
    private IMethodSerializer mGetMemoryBytes;
    private IMethodSerializer mSetMemoryByte;
    private IMethodSerializer mSetMemoryBytes;
    private IMethodSerializer mSetMemoryWord;
    private IMethodSerializer mSetVariable;
    private IMethodSerializer mSetBreakpointCondition;
    private IMethodSerializer mSetBreakpointHitCount;
    private IMethodSerializer mSetBreakpointThreads;
    private IMethodSerializer mMakeStackFrameID;
    private IMethodSerializer mComputeCallerFrame;
    private IMethodSerializer mWaitForStop;
    private IMethodSerializer mSetToggle;
    private IMethodSerializer mGetExceptionIDs;
    private IMethodSerializer mGetExceptionName;
    private IMethodSerializer mGetExceptionDescription;
    private IMethodSerializer mGetExceptionHit;
    private IMethodSerializer mSetSuspendOnException;
    private IMethodSerializer mSetIgnoreException;
    private IMethodSerializer mClearPendingException;
    private IMethodSerializer mThrowException;
    private IMethodSerializer mCreateDisplay;
    private IMethodSerializer mInvokeCommand;
    private IMethodSerializer mGetDisplaySelectors;
    private IMethodSerializer mProgressDisplayCanceled;
    private IMethodSerializer mCloseDisplay;
    private IMethodSerializer mSetArgsFilePattern;
    private IMethodSerializer mSetWorkingDirectory;
    private IMethodSerializer mSetStackFrame;
    private IMethodSerializer mGetEngineInterfaceVersion;
    private IMethodSerializer mGetEngineVersionStrings;
    private IMethodSerializer mGetLicenseExpirationDays;
    private IMethodSerializer mGetSplashPath;
    private IMethodSerializer mIsLicenseFailurePending;
    private IMethodSerializer mGetRegisterContent;
    private IMethodSerializer mGetRegisterContentArray;
    private IMethodSerializer mGetDefaultBank;
    private IMethodSerializer mWriteStdin;
    private IMethodSerializer mRefreshDisplay;
    private IMethodSerializer mRefreshDisplays;
    private IMethodSerializer mGetBreakpointHitCount;
    private IMethodSerializer mSetPC;
    private IMethodSerializer mDisconnect;
    private IMethodSerializer mCanDisconnect;
    private IMethodSerializer mCopyAllToClipboard;
}
