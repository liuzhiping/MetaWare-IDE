/*
 * WARNING!
 * This file was auto-generated from com.arc.seecode.engine.gen.GenCMPDClient
 * DO NOT MODIFY BY HAND!
 */

package com.arc.seecode.internal.cmpd;

import java.lang.reflect.Method;
import com.arc.seecode.cmpd.ICMPDController;
import com.arc.seecode.connect.IConnection;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.serialize.IMethodFilter;
import com.arc.seecode.serialize.IMethodSerializer;
import com.arc.seecode.serialize.MethodSerializationHandler;

public class RemoteCmpdController extends AbstractRemoteCmpdController {

    /**
     */
    public RemoteCmpdController(IConnection connection, int timeout) {
        super(connection,timeout);
        MethodSerializationHandler ms = new MethodSerializationHandler(this,ICMPDController.class,new MyMethodFilter(),null,null);
        mResume = ms.getSerializerFor("resume");
        mRestart = ms.getSerializerFor("restart");
        mSuspend = ms.getSerializerFor("suspend");
        mStepOut = ms.getSerializerFor("stepOut");
        mStatementStep = ms.getSerializerFor("statementStep");
        mInstructionStep = ms.getSerializerFor("instructionStep");
        mInvokeCommand = ms.getSerializerFor("invokeCommand");
    }

    /**
     * Implements {@link ICMPDController#resume()}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void resume() throws EngineException {
        Object[] argsArray = new Object[]{};
        this.invokeRemoteMethod(mResume, argsArray);
    }

    /**
     * Implements {@link ICMPDController#restart()}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void restart() throws EngineException {
        Object[] argsArray = new Object[]{};
        this.invokeRemoteMethod(mRestart, argsArray);
    }

    /**
     * Implements {@link ICMPDController#suspend()}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void suspend() throws EngineException {
        Object[] argsArray = new Object[]{};
        this.invokeRemoteMethod(mSuspend, argsArray);
    }

    /**
     * Implements {@link ICMPDController#stepOut()}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void stepOut() throws EngineException {
        Object[] argsArray = new Object[]{};
        this.invokeRemoteMethod(mStepOut, argsArray);
    }

    /**
     * Implements {@link ICMPDController#statementStep(boolean)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void statementStep(boolean over) throws EngineException {
        Object[] argsArray = new Object[]{
                new Boolean(over)};
        this.invokeRemoteMethod(mStatementStep, argsArray);
    }

    /**
     * Implements {@link ICMPDController#instructionStep(boolean)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void instructionStep(boolean over) throws EngineException {
        Object[] argsArray = new Object[]{
                new Boolean(over)};
        this.invokeRemoteMethod(mInstructionStep, argsArray);
    }

    /**
     * Implements {@link ICMPDController#invokeCommand(String)}
     *  by serializing the arguments, sending a command to the engine process,
     *  waiting for the reply, deserialize the result, and then return it.
     */
    @Override
    public void invokeCommand(String command) throws EngineException {
        Object[] argsArray = new Object[]{
                command};
        this.invokeRemoteMethod(mInvokeCommand, argsArray);
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

    private IMethodSerializer mResume;
    private IMethodSerializer mRestart;
    private IMethodSerializer mSuspend;
    private IMethodSerializer mStepOut;
    private IMethodSerializer mStatementStep;
    private IMethodSerializer mInstructionStep;
    private IMethodSerializer mInvokeCommand;
}
