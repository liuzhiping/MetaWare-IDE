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

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIAddressLocation;
import org.eclipse.cdt.debug.core.cdi.ICDIAnimatable;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDIEndSteppingRange;
import org.eclipse.cdt.debug.core.cdi.ICDIFileLocation;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation2;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation2;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.TargetInvocationException;
import org.eclipse.cdt.debug.core.cdi.event.ICDIDestroyedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.event.ICDIExitedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIRestartedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDISuspendedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIAddressBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement2;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExceptionpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIFunctionBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDILineBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMixedInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterGroup;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRuntimeOptions;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget3;
import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.core.cdi.ICMPDTarget;
import com.arc.cdt.debug.seecode.core.cdi.IEngineErrorLog;
import com.arc.cdt.debug.seecode.core.cdi.IEngineErrorLogContainer;
import com.arc.cdt.debug.seecode.internal.core.cdi.event.CreatedEvent;
import com.arc.cdt.debug.seecode.internal.core.cdi.event.DisconnectedEvent;
import com.arc.cdt.debug.seecode.internal.core.cdi.event.ExitedEvent;
import com.arc.dwarf2.model.IDwarf2ErrorReporter;
import com.arc.dwarf2.symbols.Dwarf2Symbols;
import com.arc.elf.ElfSymbolReader;
import com.arc.seecode.command.ICommandProcessor;
import com.arc.seecode.engine.EngineDisconnectedException;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;
import com.arc.seecode.engine.EngineTimeoutException;
import com.arc.seecode.engine.EvaluationException;
import com.arc.seecode.engine.IEngineAPI;
import com.arc.seecode.engine.Location;
import com.arc.seecode.engine.StackFrameRef;
import com.arc.seecode.engine.Value;
import com.arc.symbols.ISymbol;
import com.arc.symbols.ISymbolReader;

/**
 * CDI wrapper for the SeeCode Engine interface as it controls a process.
 * <P>
 * All issues related to the single-threadedness of the engine is handled in a
 * lower layer.
 * 
 * @author David Pickens
 */
class Target implements ICDITarget3, ICDIEventListener, IMemoryBlockUpdater, ICMPDTarget, 
        ICDIBreakpointManagement2, ICDIAnimatable, IEngineErrorLogContainer, IAdaptable {

    private Session mSession;

    private EngineInterface mEngine;

    private String mExe;

    private String[] mArgs = new String[0];
    
    private String mWorkingDir = null;

    enum ProgramLoadState { NOT_YET_LOADED, LOADED, FAILED_TO_LOAD }
    private ProgramLoadState mProgramLoadState = ProgramLoadState.NOT_YET_LOADED;

    private boolean mDisconnected = false;
    
    private boolean mTerminated = false;
    
    private ICDIThread mSeeCodeCurrentThread; // SeeCode's "current" thread

    private ThreadTable mThreadTable;

    // One of the constants defined in ICDIResumedEvent
    // to indicate why the resume is occurring.
    private transient int mPendingRunState = -1;

    private boolean mRestartPending;
    
    private ICDITargetConfiguration mConfig = new Configuration(this);

    private ICDIThread[] mThreadsBeforeRestart;

    private RegisterManager fRegisterManager;

    private DebuggeeProcess mProcess = null;
    
    private CDIThread mCurrentThread = null;
    
    private boolean mSuspendPending = false;
    
    // State 1 means license failure popup is showing.
    // State 2 means it was dismissed.
    private int mLicenseFailureState = NO_LICENSE_FAILURE;
    
    private String mSrcPaths[] = null; // Source paths if set before program loaded.
    
    private CDIException delayedLoadFailure = null; // set to exception that caused failure of load.

    private SourceManager fSourceManager;
    
    private IUpdatable fManagers[];

    private VariableManager fVariableManager;
    private BreakpointManager fBreakpointManager;

    private ExpressionManager fExpressionManager;

    private SharedLibraryManager fSharedLibraryManager;

    private SignalManager fSignalManager;

    private MemoryManager fMemoryManager;

    private int fProcessID;

    private int fProcessInstanceTotal;

    private String fProcessName;

    private IProject fProject;
    
    private boolean animating = false;
    
    private int fAnimateDelay = 1000;
    
    private int fLastAnimateDelay = 1000;

    private long fTimeOfLastAnimateStep;

    private int fAnimateStepType;

    private int fAnimateThreadID;

    private Timer fAnimateTimer = null;

    private int fAnimationCountPending = 0;

    // Set to true to indicate that a "stop" call was made to the engine to pre-empt a
    // multi-step animation that is taking too long.
    private boolean fAnimatePreemptPending = false;

    private int fAnimationCount = 0;

    private TimerTask fPendingTimerTask;

    private ICDIGlobalVariableDescriptor[] fGlobalVars;

    private String[] cwdSegments = null;

    private String[] mTranslationPaths;
    
    private IEngineErrorLog fErrorLog = null;

    /**
     * 
     * @param exe  -- the exe file that this target is to load and debug.
     * @param session  -- the parent sesssion
     * @param project -- associated project (in CMPD there may be several)
     * @param engine -- the engine access object.
     */
    Target(String exe, Session session, IProject project, EngineInterface engine) {
        mSession = session;
        mExe = exe;
        mEngine = engine;
        SeeCodePlugin.setEngineTarget(engine,this); // this is how we get the associated target from the engine object.
        fProject = project;
        mProgramLoadState = ProgramLoadState.NOT_YET_LOADED;
        mThreadTable = new ThreadTable(this);
        session.getEventManager().addEventListener(this);
        fRegisterManager = new RegisterManager(this,session.getEventManager());
        fSourceManager = new SourceManager(this);
        try {
            if (project != null)
                fSourceManager.addSourcePaths(new String[]{project.getLocation().toOSString()});
        }
        catch (CDIException e) {
            //Presumably whatever is wrong with the engine will occur again later.
        }
        fBreakpointManager = new BreakpointManager(this, (EventManager)session.getEventManager());
        fVariableManager = new VariableManager(this);
        fExpressionManager = new ExpressionManager(this);
        fSharedLibraryManager = new SharedLibraryManager(this);
        fSignalManager = new SignalManager(this);

        fMemoryManager = new MemoryManager(this);
        // List of managers to be updated each time the
        // debugger engine is suspended:
        fManagers = new IUpdatable[] { fSourceManager, fBreakpointManager,
                fVariableManager, fExpressionManager,
                fSharedLibraryManager, fSignalManager, fMemoryManager,
                fRegisterManager};
        for (Object manager: fManagers){
            if (manager instanceof ICDIEventListener){
                session.getEventManager().addEventListener((ICDIEventListener)manager);     
            }
        }
    }

    EngineInterface getEngineInterface() {
        return mEngine;
    }

    void setArguments(String args[]) {
        mArgs = args;
    }
    
    void setSeeCodeCurrentThread(int tid){
        if (tid <= 0) mSeeCodeCurrentThread = null;
        else 
            mSeeCodeCurrentThread = mThreadTable.findOrCreateThreadFor(tid);
    }
    
    int getThreadCount(){
        return mThreadTable.getThreadCount();
    }
    
    ICDIThread getSeeCodeCurrentThread() { return mSeeCodeCurrentThread; }

    /**
     * Return the "current" seecode stack frame of the current thread.
     * 
     * @return the "current" seecode stack frame of the current thread.
     */
    StackFrameRef getStackFrame() throws CDIException {
        CDIThread thread = getThread();
        if (thread == null) throw new CDIException("No current thread");
        StackFrame stackFrame = thread.getTopFrame();
        if (stackFrame == null) { throw new CDIException(
                "No current stackframe"); }
        return stackFrame.getSeeCodeStackFrame();
    }
    
    boolean supportsThreadControl(){
        return ((Configuration)mConfig).supportsThreadControl();
    }

    /**
     * Return one of the manifest constants of {@link ICDIResumedEvent}to
     * indicate what caused this target to go into a run state.
     * 
     * @return cause of entering the run state.
     */
    int getPendingRunState(int tid) {
        if (tid != 0)
            return mThreadTable.findOrCreateThreadFor(tid).getPendingRunState();
        // Should never be called when pending run state is -1, but...
        return mPendingRunState == -1?ICDIResumedEvent.CONTINUE:mPendingRunState;
    }

    /**
     * This is used by engine observer to know not to fire a terminating event
     * if we're simply restarting the program.
     * 
     * @return whether or not a restart action is pending.
     */
    boolean isRestartPending() {
        return mRestartPending;
    }

    /**
     * Called when program is loaded after being restarted.
     *  
     */
    void clearRestartPending() {
        mRestartPending = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getSession()
     */
    @Override
    public ICDISession getSession() {
        return mSession;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getProcess()
     */
    @Override
    public Process getProcess() {
        return mProcess;
    }
    
    /**
     * Set the "debuggee" process. The passed-in argument is actually
     * a wrapper around the SeeCode server process. It intercepts stdout
     * and stderr, assuming them to apply to the debuggee.
     * 
     * However, this method is called prior to the target program being
     * loaded. So, until the target program is loaded successfully,
     * the engine may be displaying messages that we want to see.
     * Thus, we spawn a pair of "reader" threads to  capture the stdout/stderr 
     * streams so that they
     * can be piped to the SeeCode session "process" -- thus appearing
     * in the SeeCode "Console" view.
     * 
     * After the target program is successfully loaded, then the debugger
     * view logic will start intercepting stdout/stderr. We then kill the
     * reader threads.
     * 
     * @param p
     */
    void setProcess(DebuggeeProcess p){
        mProcess = p;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getThreads()
     */
    @Override
    public ICDIThread[] getThreads() throws CDIException {
        // Problems: we have at least two threads querying
        // the threads, and a race condition can occur:
        // The  CDT layer wants the list of threads
        // when a process stops. But the thread that
        // fires the "thread-created" event may not have
        // yet completed, so the thread will not be
        // seen. This materializes as the Launch view
        // sometimes not displaying the threads at startup.
        // Thus, we go ahead and query the engine
        // for the threads.
        //
        // Actually, this can be quite expensive if we
        // do it each step. So we only do it if the
        // thread table is empty.
        //
        // By the way, as the threads are terminating as the engine is shutting down, there
        // will be no threads. If we call the engine to get the threads after its terminated, it may
        // be in an inconsistent state.
    	synchronized(mThreadTable) {
            if (mThreadTable.getThreadCount() != 0 || mSession.isShuttingDown())
                return mThreadTable.getThreads();
    	}

        int tid[];
		try {
			tid = mEngine.getThreads();
		} catch (EngineDisconnectedException e) {
			handleDroppedConnection();
			tid = new int[0];
		} catch (EngineException e) {
			SeeCodePlugin.log(e);
			throw new CDIException(e.getMessage());
		}
		ICDIThread t[] = new ICDIThread[tid.length];
		synchronized(mThreadTable) {
		    for (int i = 0; i < tid.length; i++) {
			    t[i] = mThreadTable.findOrCreateThreadFor(tid[i]);
		    }
		}
		return t;
    }

// /*
// * (non-Javadoc)
// *
// * @see
// org.eclipse.cdt.debug.core.cdi.model.ICDITarget#setCurrentThread(org.eclipse.cdt.debug.core.cdi.model.ICDIThread)
//     */
//    public void setCurrentThread(ICDIThread current) throws CDIException {
//        try {
//            confirmStopped();
//            int old = mEngine.getCurrentThread();
//            int tid = mThreadTable.getThreadID(current);
//            if (tid < 0) throw new CDIException("Can't set thread");
//            if (tid != old) {
//                mEngine.setCurrentThread(tid);
//                getSession().getRegisterManager().update();
//                getSession().getVariableManager().update();
//                getSession().getExpressionManager().update();
//            }
//        } catch (EngineDisconnectedException e) {
//            doDisconnect();
//        } catch (EngineException e) {
//            SeeCodePlugin.log(e);
//            throw new CDIException(e.getMessage());
//        }
//    }

    /**
     * Given a SeeCode engine thread ID, return a corresponding CDI thread
     * object, creating it if necessary.
     * 
     * @param threadID
     *            the engine-associated thread ID.
     * @return the corresponding CDI thread.
     */
    ICDIThread createThreadFromID(int threadID) {
        // There should not be any stale threads in the
        // table, but just in case we missed a threadTermination
        // event.
        synchronized(mThreadTable){
            mThreadTable.disposeThread(threadID);
            //Now create the new thread.
            return mThreadTable.findOrCreateThreadFor(threadID);
        }
    }
    
    /**
     * Given a SeeCode engine thread ID, return a corresponding CDI thread
     * object, creating it if necessary.
     * 
     * @param threadID
     *            the engine-associated thread ID.
     * @return the corresponding CDI thread.
     */
    ICDIThread findOrCreateThreadFromID(int threadID) {
        return mThreadTable.findOrCreateThreadFor(threadID);
    }

    /**
     * Given a SeeCode engine thread ID, return a corresponding CDI thread
     * object if it exists. Other, return null.
     * 
     * @param threadID
     *            the engine-associated thread ID.
     * @return the corresponding CDI thread, or null.
     */
    ICDIThread lookupThreadFromID(int threadID) {
        return mThreadTable.lookupThreadFor(threadID);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#isTerminated()
     */
    @Override
    public boolean isTerminated() {
        return mDisconnected || mTerminated;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#terminate()
     */
    @Override
    public void terminate() throws CDIException {
        if (!isTerminated()) {
            try {
                cancelAnimateTimer();
                if (!isSuspended()) {
                    suspend();
                    try {
                        if (!mEngine.waitForStop(10000)) {
                            SeeCodePlugin.log(new Status(IStatus.ERROR,
                                    SeeCodePlugin.PLUGIN_ID, IStatus.ERROR,
                                    "Engine appears to be hung", null));
                        }
                    } catch (InterruptedException e) {
                        //If interrupted, continue...
                    }
                }
                mEngine.terminate(); // Should fire "processTerminated" event.
                
                // If we're in the process of doing an emergency shutdown,
                // then fire the termination event right away. Otherwise,
                // if we wait for the engine's response, this plugin may have
                // already been terminated!
                // NO. it can send a terminate event prior to the pending suspend event that we're expecting!
                // CDT doesn't expect this.
//                if (mSession.isShuttingDown()){
//                    fireTerminateEvent();
//                }
            } catch (EngineDisconnectedException e) {
                //doDisconnect();
                fireTerminateEvent(); //otherwise launch display doesn't go away
            } catch (EngineException e) {
                SeeCodePlugin.log(e);
                fireTerminateEvent(); //otherwise launch display doesn't go away
                throw new CDIException(e.getMessage());
            }
        }
    }

    private void cancelAnimateTimer () {
        if (fAnimateTimer != null){
            fAnimateTimer.cancel();
            fAnimateTimer = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#isDisconnected()
     */
    @Override
    public boolean isDisconnected() {
        return mDisconnected;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#disconnect()
     */
    @Override
    public void disconnect () throws CDIException {
        if (!mDisconnected && !mTerminated) {
            cancelAnimateTimer();
            try {
                mEngine.disconnect(); 
            }
            catch (EngineDisconnectedException e) {
                handleDroppedConnection();
            }
            catch (EngineException e) {
                SeeCodePlugin.log(e);
                throw new CDIException(e.getMessage());
            }
        }
    }
    
    public void setDisconnected(){
        // Indicate that the engine has fired a ProcessDisconnected event. 
        mDisconnected = true;
        mTerminated = true;
    }

    /**
     *  
     */
    private void handleDroppedConnection() {
        if (!mDisconnected && !mTerminated) {
            cancelAnimateTimer();
            mDisconnected = true;
            EventManager emgr = (EventManager) mSession.getEventManager();
            emgr.enqueueEvent(new DisconnectedEvent(this));
        }
    }
    
    /**
     *  Called to fire target termination event.
     *  May be called when engine aborts.
     */
    synchronized void fireTerminateEvent() {
        if (!mTerminated && !mDisconnected) {
            cancelAnimateTimer();
            mTerminated = true;
            EventManager emgr = (EventManager) mSession.getEventManager();
            //If the session happened to be forceably shutdown, then there
            // will be no event manager. But that shouldn't be the case at this
            // point.
            //
            // NOTE: cdt 3.1 does not recognize a ICDITarget source object
            // in a DestroyedEvent (we fixed it in the 4.0.0 sources).
            // So, for the time being, generate an ExitedEvent until
            // we update all users with newer CDT version (in version 8.0.3).
            if (emgr != null) {
                //emgr.enqueueEvent(new DestroyedEvent(this));
                emgr.enqueueEvent(new ExitedEvent(this,mSession));
            }
        }
    }

    /**
     * Since "ICDIRestartEvent" is ignored, we must explicitly terminate all
     * threads at a restart so that stackframe displays get updated.
     * 
     * @return list of thread before a restart event.
     */
    ICDIThread[] getThreadsBeforeRestart() {
        return mThreadsBeforeRestart;
    }

    @Override
    public void restart() throws CDIException{
        restart(null);
    }
    
    @Override
    public void restart(String args[]) throws CDIException {
        mRestartPending = true;
        animating = false;
        mThreadsBeforeRestart = getThreads();
        try {
            if (!isTerminated()) mEngine.terminate();
            if (mEngine.restart(args)){
                // Things don't work unless we resume here.
                // The previously-generated RestartedEvent isn't handled completely by infrastructure
                mPendingRunState = ICDIResumedEvent.CONTINUE;
                //mEngine.resume(0);
            }
        } catch (EngineDisconnectedException e) {
            handleDroppedConnection();
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#isSuspended()
     */
    @Override
    public boolean isSuspended() {
        return isSuspended(0);
    }
    
    boolean isSuspended(int tid) {
        try {
            return mEngine.isStopped(tid);
        } catch (EngineDisconnectedException e) {
            //Engine has shutdown or aborted; that can be
            //considered suspended
            return true;
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            return true;
        }
    }

    void confirmStopped(int tid) throws CDIException {
        try {
            confirmLoaded(false);
            if (mEngine.isActive() && !mEngine.isStopped(tid)) { throw new CDIException(
                    "thread isn't stopped"); }
        } catch (EngineDisconnectedException e) {
            handleDroppedConnection();
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#resume()
     */
    @Override
    public void resume() throws CDIException {
        //confirmStopped();
        if (confirmLoaded(false)) { // Latest version of CDT 3.1 simply
									// "resumes" to start the program.
			mPendingRunState = ICDIResumedEvent.CONTINUE;
			mThreadTable.prepareForProcessResume();
			try {
			    CDIThread t;
                try {
                    t = getThread();
                }
                catch (CDIException e) {
                    // At startup, we can have a race condition in which the engine is being resumed prior
                    // to it identifying the initial startup thread. This is benign.
                    t = null;
                }
				mEngine.resume(t != null?t.getID():0);
				this.setSeeCodeCurrentThread(0);
			} catch (EngineDisconnectedException e) {
				handleDroppedConnection();
			} catch (EngineException e) {
				SeeCodePlugin.log(e);
				throw new CDIException(e.getMessage());
			}
		}
        // Could have had a "resume" from the time a license failure occured until we shut down.
        // So we could get here without doing anything.
    }
    
    void resume (int tid) throws CDIException {
        mPendingRunState = ICDIResumedEvent.CONTINUE;
        try {
            mEngine.resume(tid);
        }
        catch (EngineDisconnectedException e) {
            handleDroppedConnection();
        }
        catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        }
    }
    
    void animate (int stepType, int tid)  {
        if (!animating) {
            mPendingRunState = ICDIResumedEvent.CONTINUE;
            animating = true;
            fTimeOfLastAnimateStep = 0L;
            fAnimateStepType = stepType;
            fAnimateThreadID = tid;
            fAnimatePreemptPending = false;
            doAnimateStep();
        }
    }
    
    /**
     * Perform an animation step. If delay time has not transpired since the last
     * animation step was started, then delay the step appropriately.
     * If more than the amount of time has transpired, then start a multi-step animation step
     * to catch up. 
     * <P>
     * But if the multi-step animation takes too long, we'll pre-empt it and start another.
     * @todo davidp needs to add a method comment.
     */
    private synchronized void doAnimateStep () {
        int count = 1;
        final long currentTime = System.currentTimeMillis();
        if (fTimeOfLastAnimateStep != 0) {
            long delta = currentTime - fTimeOfLastAnimateStep;
            //System.out.println("ANIMATE DELTA="+ delta + "; delay=" + fAnimateDelay + "; pending=" + fAnimationCountPending);
            if (fAnimationCountPending == 0) {
                //No pending animation step in the timer queue
                if (fAnimateDelay <= delta + fAnimateDelay/2) {
                    //More time has passed than we would like. Emit multi-step so as to catch up.
                    //count = (int) delta / fAnimateDelay;  // Engine crashes if count is > 1!!
                    // If it is multi-step, then pre-empt it when the next animate step is
                    // supposed to occur.
                    this.fLastAnimateDelay = (int)delta;
                    if (fAnimationCountPending > 1 && !fAnimatePreemptPending) {
                        getAnimateTimer().schedule( new TimerTask() {
                            @Override
                            public void run () {
                                if (animating && !fAnimatePreemptPending && fAnimationCountPending > 0) {
                                    try {
                                        fAnimatePreemptPending = true;
                                        //System.out.println(">>>PREEMPTIVE SUSPEND");
                                        getEngineInterface().stop(fAnimateThreadID);
                                    }
                                    catch (EngineException e) {
                                        animating = false;
                                    }
                                }
                            }
                            
                        },delta);
                    }
                }
                else {
                    // Animate step came too early. Delay it appropriately.
                    fAnimationCountPending = 1;
                    getAnimateTimer().schedule(fPendingTimerTask = new TimerTask() {

                        @Override
                        public void run () {
                                long thisTime = System.currentTimeMillis();
                                fLastAnimateDelay = (int)(thisTime - fTimeOfLastAnimateStep);
                                fTimeOfLastAnimateStep = thisTime;
                                fAnimationCountPending = 0;
                                fPendingTimerTask = null;
                                //System.out.println(">>>+About to call initiateAnimateStep");
                                initiateAnimateStep(1);
                                //System.out.println(">>>-returning from call initiateAnimateStep");
                        }
                    }, fAnimateDelay-delta);
                    return;  // delayed
                }
            }
        }
        
        fTimeOfLastAnimateStep = currentTime;
        //System.out.println("TIME=" + currentTime + ": animateStep(" + fAnimationCountPending + ")");
        initiateAnimateStep(count);

    }

    private Timer getAnimateTimer () {
        if (fAnimateTimer == null) {
            fAnimateTimer = new Timer("AnimationTimer",true);
        }
        return fAnimateTimer;
    }
    
    private void initiateAnimateStep (int count) {
        //System.out.println("INITIATE STEP (cnt=" + count + ") time=" + fTimeOfLastAnimateStep);
        try {
            switch (fAnimateStepType) {
                case ANIMATE_STATEMENT_STEP_INTO:
                    this.stepInto(fAnimateThreadID,count);
                    break;
                case ANIMATE_STATEMENT_STEP_OVER:
                    this.stepOver(fAnimateThreadID,count);
                    break;
                case ANIMATE_INSTR_STEP_OVER:
                    this.stepOverInstruction(fAnimateThreadID,count);
                    break;
                case ANIMATE_INSTR_STEP_INTO:
                    this.stepIntoInstruction(fAnimateThreadID,count);
                    break;
                default:
                    animating = false;
                break;
            }
        }
        catch (CDIException e) {
            animating = false;
            SeeCodePlugin.log("Animation step failure",e);
        }
    }
    
    @Override
    public void setAnimateStepDelay(int milliseconds){
        if (milliseconds > 60000) milliseconds = 60000; // limit delay to a minute.
        if (milliseconds <= 0){
            throw new IllegalArgumentException("Invalid delay value: "+ milliseconds);
        }
        boolean decreased = false;
        synchronized(this){
            if (milliseconds < fAnimateDelay - 500) 
                decreased = true;
            fAnimateDelay = milliseconds;          
        }
        if (decreased){
            //We speed up animation; if we're waiting for the next step, cancel it and force it to
            //run immediately.
            final TimerTask task = fPendingTimerTask;
            if (task != null && task.cancel()){
                getEngineInterface().enqueue(new Runnable(){ @Override
                public void run(){ task.run(); }},"ANIMATE_STEP");          
            }
        }
    }
    
    @Override
    public int getAnimateStepDelay(){
      //Don't ever return 0 because the caller may be converting this to speed.
        return fAnimateDelay != 0?fAnimateDelay:1;
    }
    
    @Override
    public int getActualAnimateStepDelay(){
        //Don't ever return 0 because the caller may be converting this to speed.
        return fLastAnimateDelay != 0?fLastAnimateDelay:1;
    }
    
    @Override
    public void animate(int stepType) throws CDIException {
        if (mCurrentThread != null){
            mCurrentThread.animate(stepType);
        }
        else
            animate(stepType,0);
    }
    
    // Called by CDIThread when animation is suspended by user command.
    void suspendAnimation(){
        if (animating) {
            animating = false;
            try {
                for (ICDIThread t: getThreads()){
                    ((CDIThread)t).onSuspended();
                }
            }
            catch (CDIException e) {
               
            }
        }
    }
    
    @Override
    public boolean isAnimating(){
        return animating;
    }

    /**
     * @throws CDIException
     */
    private boolean confirmLoaded(boolean delayFailure) throws CDIException {
    	// Prevent recursive attempt to reload a program after it failed due to licensing.
    	if (mProgramLoadState == ProgramLoadState.FAILED_TO_LOAD)
    		return false;
    	if (delayedLoadFailure != null){
    		if (!delayFailure)
    			throw delayedLoadFailure;
    		return false;
    	}
        // We delay loading program because arguments
        // are specified after this object is created.
        if (mProgramLoadState == ProgramLoadState.NOT_YET_LOADED) {
            try {
                loadProgram();
                // We cannot reliably check for license expiration until the program is loaded.
                // So we do it here.
                try {
                    SeeCodePlugin.getDefault().checkLicenseExpirationAlert(mEngine.getLicenseExpirationDays());
                }
                catch (EngineException e) {  }
            }
            catch (CDIException x){
            	if (delayFailure) {
            		delayedLoadFailure = x;
            		return false;
            	}
            	throw x;
            }
        }
        return mProgramLoadState == ProgramLoadState.LOADED;
    }

//    private void confirmActive(int tid) throws CDIException {
//        try {
//            if (tid !=0 && !mEngine.isActiveThread(tid))
//                    throw new CDIException("Debugger thread " + tid + " isn't active");
//        } catch (EngineDisconnectedException e) {
//            doDisconnect();
//        } catch (EngineException e) {
//            SeeCodePlugin.log(e);
//            throw new CDIException(e.getMessage());
//        }
//    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#suspend()
     */
    @Override
    public void suspend() throws CDIException {
        mSuspendPending = true;
        suspend(0);
    }
    
    void suspend (int tid) throws CDIException {
        try {
            if (animating) {
                suspendAnimation();
                if (mEngine.isStopped(tid))
                    return;
            }
            mEngine.stop(tid);
        }
        catch (EngineDisconnectedException e) {
            handleDroppedConnection();
        }
        catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepOver()
     */
    @Override
    public void stepOver(int count) throws CDIException {
        getThread().stepOver(count);
    }

    void stepOver(int tid, int count) throws CDIException {
        mPendingRunState = ICDIResumedEvent.STEP_OVER;
        try {
            mEngine.statementStep(tid, true,count);
        } catch (EngineDisconnectedException e) {
            handleDroppedConnection();
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepInto()
     */
    @Override
    public void stepInto(int count) throws CDIException {
        getThread().stepInto(count);
    }

    void stepInto(int tid, int count) throws CDIException {
        try {
            mPendingRunState = ICDIResumedEvent.STEP_INTO;
            mEngine.statementStep(tid, false,count);
        } catch (EngineDisconnectedException e) {
            handleDroppedConnection();
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepOverInstruction()
     */
    @Override
    public void stepOverInstruction(int count) throws CDIException {
        getThread().stepOverInstruction(count);
    }

    void stepOverInstruction(int tid, int count) throws CDIException {
        mPendingRunState = ICDIResumedEvent.STEP_OVER_INSTRUCTION;
        try {
            mEngine.instructionStep(tid, true,count);
        } catch (EngineDisconnectedException e) {
            handleDroppedConnection();
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepIntoInstruction()
     */
    @Override
    public void stepIntoInstruction(int count) throws CDIException {
        getThread().stepIntoInstruction(count);
    }

    void stepIntoInstruction(int tid, int count) throws CDIException {
        mPendingRunState = ICDIResumedEvent.STEP_INTO_INSTRUCTION;
        try {
            mEngine.instructionStep(tid, false,count);
        } catch (EngineDisconnectedException e) {
            handleDroppedConnection();
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        }
    }
    
    void stepReturn(int tid) throws CDIException {
        mPendingRunState = ICDIResumedEvent.STEP_RETURN;
        try {
            mEngine.stepOut(tid);
        } catch (EngineDisconnectedException e) {
            handleDroppedConnection();
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#runUntil(org.eclipse.cdt.debug.core.cdi.ICDILocation)
     */
    @Override
    public void runUntil(ICDILocation location) throws CDIException {
            stepUntil(location);
    }

    void runUntil(ICDILocation location, int tid) throws CDIException {
        try {
            mEngine.runToAddress(tid, createLocation(location));
        } catch (EngineDisconnectedException e) {
            handleDroppedConnection();
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        }
    }

    Location createLocation(ICDILocation l) {
        if (l instanceof Location) return (Location) l;
        if (l instanceof CDILocation){
            Location loc = ((CDILocation)l).getLocation();
            if (loc != null) return loc;
        }
        Location loc = new Location();
        if (l instanceof ICDIFileLocation){
            loc.setSource(((ICDIFileLocation)l).getFile());
        }
        if (l instanceof ICDILineLocation){
            ICDILineLocation ll = (ICDILineLocation)l;
            int lineOff = 0;
            if (ll instanceof ICDILineLocation2){
                lineOff = ((ICDILineLocation2)ll).getOffsetFromLine();
            }
            loc.setSourceLine(ll.getLineNumber(), lineOff);
        }
        if (l instanceof ICDIFunctionLocation){
            ICDIFunctionLocation ll = (ICDIFunctionLocation)l;
            int funcOff = 0;
            if (ll instanceof ICDIFunctionLocation2){
                funcOff = ((ICDIFunctionLocation2)ll).getOffsetFromFunction();
            }
            loc.setFunction(ll.getFunction(), funcOff);
        }
        if (l instanceof ICDIAddressLocation) {
            loc.setAddress(((ICDIAddressLocation)l).getAddress().longValue());
        }
        return loc;
    }

    //deprecated
    @Override
    public void jump(ICDILocation location) throws CDIException {
            throw new CDIException("Jump not (yet) supported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#signal()
     */
    @Override
    public void signal() throws CDIException {
        throw new CDIException("signals not (yet) supported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#signal(org.eclipse.cdt.debug.core.cdi.model.ICDISignal)
     */
    @Override
    public void signal(ICDISignal signal) throws CDIException {
        throw new CDIException("signals not (yet) supported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getCurrentThread()
     */
    public CDIThread getThread() throws CDIException {
        
        if (mCurrentThread == null) {
            //Shouldn't happen
            ICDIThread threads[] = getThreads();
            if (threads.length > 0)
                mCurrentThread = (CDIThread)threads[0];
            else 
            	throw new CDIException("No current thread");
        }
        return mCurrentThread;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getTarget()
     */
    @Override
    public ICDITarget getTarget() {
        return this;
    }

    private String[] makeCommand() {
        String[] cmd = new String[mArgs.length + 1];
        cmd[0] = mExe;
        System.arraycopy(mArgs, 0, cmd, 1, mArgs.length);
        return cmd;
    }
    
    /**
     * Return whether or not the associated program as been loaded.
     * @return whether or not the associated program as been loaded.
     */
    boolean isLoaded(){
        return mProgramLoadState == ProgramLoadState.LOADED;
    }

    /**
     * Load program during initial "resume" operation.
     *  
     */
    private void loadProgram() throws CDIException {
        try {
        	mProgramLoadState = ProgramLoadState.FAILED_TO_LOAD; // Changed below if we succeed.
        	int state = mEngine.queryState(0);
            switch (state) {
            case IEngineAPI.NOT_STARTED:
            case IEngineAPI.TERMINATED:
            case IEngineAPI.INVALID:
                if (mWorkingDir != null){
                    mEngine.setWorkingDirectory(mWorkingDir);
                }
                if (mEngine.loadProgram(makeCommand())) {
                    mProgramLoadState = ProgramLoadState.LOADED;
                    // If source paths were set before program loaded, set them now.
                    // (Does engine still require this? It may now allow source paths to
                    // be set prior to program load)
                    if (mSrcPaths != null)
                        this.setSourcePaths(mSrcPaths);
                    if (mTranslationPaths != null)
                        this.setDirectoryTranslation(mTranslationPaths);
                    // In case this is the final CMPD process loaded, give Session object a chance
                    // to invoke CMPD startup commands.
                    this.mSession.onLoaded(this);
                }
                else {
                    // Check for license failure so that we can delay things while
                    // popup dialog remains.
                    // State 0: it hasn't popped up.
                    // State 1: it is showing.
                    // State 2: it was dismissed.
                    if (mEngine.isLicenseFailurePending()){
                        synchronized(this){
                            try {
                                if (mLicenseFailureState == NO_LICENSE_FAILURE){
                                    this.wait(3000); // wait for it to pop up.
                                }
                                if (mLicenseFailureState == LICENSE_FAILURE_POPUP_SHOWING) {
                                    // Wait up up to 45 seconds for the user to dismiss the
                                    // License failure dialog.
                                    this.wait(45000);                                   
                                }
                                // If in state 2, then License Failure dialog was just dismissed
                                // and we're about to terminate the session.
                                // Don't throw a invocation exception to avoid superfluous
                                // error dialog after the license failure dialog is dismissed. (CR2137)
                                if (mLicenseFailureState == LICENSE_FAILURE_POPUP_DISMISSED) {
                                    return;
                                }
                            }
                            catch (InterruptedException e) {
                                // Shouldn't happen, but just punt.
                            }
                        }
                        
                    }
                    // Program is missing, corrupt,
                    // or licensing failure.
                    throw new TargetInvocationException("Can't load \"" + mExe + "\"");
                }
                break;
            case IEngineAPI.SHUTDOWN:
                if (!((Session)this.getSession()).isShutdown()){
                    this.getSession().terminate();
                }
                break;
            default:
                throw new TargetInvocationException("Engine in wrong state to load (=" + state + ")");
            }
        } catch (EngineDisconnectedException e) {
            handleDroppedConnection();
            //CR96397: XISS config error will issue an error and abort the debug session.
            // Don't fill the error log with such things.
            //throw new CDIException("Debugger engine connection failed");
        } catch (EngineException e) {
            if (e instanceof EngineTimeoutException){
                SeeCodePlugin.getDefault().diagnoseProgramLoadTimeout(mExe);
            }
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvents(org.eclipse.cdt.debug.core.cdi.event.ICDIEvent[])
     */
    @Override
    public void handleDebugEvents(ICDIEvent[] event) {
        for (int i = 0; i < event.length; i++){
            ICDIEvent e = event[i];
            if (e instanceof ICDISuspendedEvent){
                handleSuspendedEvent((ICDISuspendedEvent)e);
            }
            else
            if (e instanceof ICDIDestroyedEvent){
                handleDestroyedEvent((ICDIDestroyedEvent)e);
            }
            else if (e instanceof ICDIRestartedEvent){
                handleRestartedEvent((ICDIRestartedEvent)e);
            }
            else if (e instanceof ICDIExitedEvent){
                handleExitedEvent((ICDIExitedEvent)e);
            }
        }       
    }
    
    private void handleAnimateStepEvent(ICDISuspendedEvent e){
        if (animating) {
           fAnimationCount++;
           updateViews();
           //NOTE: a "suspend" event has presumably been fired on behalf of the animation
           // thread before this event is seen. Thus, the various worker threads, Debug event thread
           // and UI thread are actively in operation updating the various displays.
           // We cannot resume the next step of the animation until this is completed, otherwise
           // the engine's state will be in a state  of flux, and the stackframe contents
           // will not be available.
           // Thus, we wait for the main Debug event queue to empty, then we wait for the
           // UI thread to get done, then we do the next animation step.
           DebugPlugin.getDefault().asyncExec(new Runnable(){

            @Override
            public void run () {
                // This is called when all Debug events have completed (presumably).
                // But the UI thread may still be updating things. So check here:
                try {
                    SeeCodePlugin.getDefault().getCallbackRunner().invoke(new Runnable(){

                        @Override
                        public void run () {
                            getEngineInterface().enqueue(new Runnable(){

                                @Override
                                public void run () {
                                    if (animating) {
                                        try {
                                            // If animation gets too fixed, then we step faster than
                                            // UI can read stackframes. Read them here to make sure they are available.
                                            ICDIThread t = getCurrentThread();
                                            if (t != null) {
                                                ICDIStackFrame frames[] = t.getStackFrames();
                                                for (ICDIStackFrame f: frames){
                                                    f.getLocator();
                                                }
                                            }
                                        }
                                        catch (CDIException e1) {
                                            
                                        }
                                        doAnimateStep();
                                    }
                                }}, "ANIMATE_STEP");
                            
                        }}, true);
                }
                catch (Throwable t) {
                    try {
                        suspend(0);
                    }
                    catch (CDIException e1) {
                    }
                    animating = false;
                    SeeCodePlugin.log(t);
                }
                
            }});
        }       
    }
    
    @Override
    public int getAnimationCount(){
        return fAnimationCount;
    }
    
    @Override
    public void resetAnimationCounter(){
        fAnimationCount  = 0;
    }
    
    public void updateViews(){
        for (int i = 0; i < fManagers.length; i++){
            IUpdatable m = fManagers[i];
            if (m.isAutoUpdate()){
                try {
                    m.update(this);
                } catch (CDIException e) {
                    SeeCodePlugin.log(e);
                } catch (RuntimeException e){
                    SeeCodePlugin.log(e);
                }
            }
        } 
    }

    /**
     * Handle the target termination event.
     * This is called when the process is being forceably terminated.
     * If the engine emitted a "processTerminated" event, then
     * ExitedEvent would have been generated.
     * @param e
     */
    private void handleDestroyedEvent(ICDIDestroyedEvent e) { 
        
        if (e.getSource() == this){
            onTerminated();
        }   
        else if (e.getSource() instanceof ICDIThread){
            mThreadTable.disposeThread((ICDIThread)e.getSource());
        }
    }
    
    /**
     * Handle the target suspend event.
     * This is called when a process or thread is being suspended. We want to update our views.
     * @param e
     */
    private void handleSuspendedEvent (ICDISuspendedEvent e) {
        if (animating && e.getReason() instanceof ICDIEndSteppingRange) {
            handleAnimateStepEvent(e);
        }
        else {
            animating = false;
            if (e.getSource() == this) {
                for (ICDIThread t : mThreadTable.getThreads()) {
                    ((CDIThread) t).onSuspended();
                }
                updateViews();
            }
            else if (e.getSource() instanceof ICDIThread) {
                ICDIThread thread = (ICDIThread) e.getSource();
                if (thread.getTarget() == this) {
                    ((CDIThread) thread).onSuspended();
                    updateViews();
                }
            }
        }
    }
    
    /**
     * Handle the target exited event.
     * This is called when the engine produces a "processTerminated" event.
     * @param e
     */
    private void handleExitedEvent(ICDIExitedEvent e) {         
        if (e.getSource() == this){
            onTerminated();
        }   
        else if (e.getSource() instanceof ICDIThread){
            mThreadTable.disposeThread((ICDIThread)e.getSource());
        }
    }

	private void onTerminated() {
		mTerminated = true;
		if (!mSession.isShuttingDown()){
			// If this process is being terminated, but the session isn't, and we're the only
			// process in the session, then shutdown the process.
			// Otherwise, the debugger process waits until killed by timeout.
			ICDITarget targets[] = mSession.getTargets();
			int cnt = 0;
			for (ICDITarget t: targets){
				if (!t.isTerminated()) cnt++;
			}
			if (cnt == 0) {
				try {
					mSession.terminate();
				} catch (CDIException e) {
					SeeCodePlugin.log(e);
				}
			}
		}
	}
    
    /**
     * Handle the target restart event.
     * @param e
     */
    private void handleRestartedEvent(ICDIRestartedEvent e) {
        if (e.getSource() == this){
            mTerminated= false;
            mCurrentThread = null;
            mRestartPending = false;
        }       
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepOver()
     */
    @Override
    public void stepOver() throws CDIException {
        stepOver(1);
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepInto()
     */
    @Override
    public void stepInto() throws CDIException {
        stepInto(1);
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepOverInstruction()
     */
    @Override
    public void stepOverInstruction() throws CDIException {
       stepOverInstruction(1);
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepIntoInstruction()
     */
    @Override
    public void stepIntoInstruction() throws CDIException {
        stepIntoInstruction(1);
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#createCondition(int, java.lang.String)
     */
    @Override
    public ICDICondition createCondition(int ignoreCount, String expression) {
        return new Condition(ignoreCount,expression,new String[0]);
    }
    
    @Override
    public ICDICondition createCondition(int ignoreCount, String expression, String[] threadIds) {
        return new Condition(ignoreCount,expression,threadIds);
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#createLocation(java.lang.String, java.lang.String, int)
     */
    public ICDILocation createLocation(String file, String function, int line, int offsetFromLine) {
        return fBreakpointManager.createLocation(file,function,line,offsetFromLine);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#createLocation(long)
     */
    @Override
    public ICDIAddressLocation createAddressLocation(BigInteger address) {
        return (ICDIAddressLocation)fBreakpointManager.createLocation(address.longValue());
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement#getBreakpoints()
     */
    @Override
    public ICDIBreakpoint[] getBreakpoints() throws CDIException {
        return fBreakpointManager.getBreakpoints();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement#setLocationBreakpoint(int, org.eclipse.cdt.debug.core.cdi.ICDILocation, org.eclipse.cdt.debug.core.cdi.ICDICondition, boolean)
     */
    @Override
    public ICDIAddressBreakpoint setAddressBreakpoint(int type, ICDIAddressLocation location, ICDICondition condition, boolean deferred, boolean enabled) throws CDIException {
        confirmLoaded(false); // in case its the temporary breakpoint on main
        return (ICDIAddressBreakpoint)fBreakpointManager.setAddressBreakpoint(type,location,condition,deferred,enabled);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement#setLocationBreakpoint(int, org.eclipse.cdt.debug.core.cdi.ICDILocation, org.eclipse.cdt.debug.core.cdi.ICDICondition, boolean)
     */
    @Override
    public ICDIAddressBreakpoint setAddressBreakpoint(int type, ICDIAddressLocation location, ICDICondition condition, boolean deferred) throws CDIException {
        return setAddressBreakpoint(type,location,condition,deferred,true);
    }
 
    @Override
    public ICDIWatchpoint setWatchpoint(int type, int watchType, String expression, ICDICondition condition, boolean enabled) throws CDIException {
        // cr92176: temporary hack: make the IDE think all watchpoints are hardware
        return fBreakpointManager.setWatchpoint(type | ICBreakpointType.HARDWARE,watchType,expression,0,condition, enabled);
    }
    
    @Override
    public ICDIWatchpoint setWatchpoint(int type, int watchType, String expression, ICDICondition condition) throws CDIException {
        return setWatchpoint(type,watchType,expression,condition,true);
    }
    
    @Override
    public ICDIWatchpoint setWatchpoint(int type, int watchType, String expression, String memorySpace, BigInteger range, 
        ICDICondition condition, boolean enabled) throws CDIException{
        return fBreakpointManager.setWatchpoint(type | ICBreakpointType.HARDWARE,watchType,expression,range.intValue(),condition, enabled);
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement#deleteBreakpoints(org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint[])
     */
    @Override
    public void deleteBreakpoints(ICDIBreakpoint[] breakpoints) throws CDIException {
        fBreakpointManager.deleteBreakpoints(breakpoints);
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement#deleteAllBreakpoints()
     */
    @Override
    public void deleteAllBreakpoints() throws CDIException {
        fBreakpointManager.deleteAllBreakpoints();
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepUntil(org.eclipse.cdt.debug.core.cdi.ICDILocation)
     */
    @Override
    public void stepUntil(ICDILocation location) throws CDIException {
        getThread().stepUntil(location);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteResume#resume(boolean)
     */
    @Override
    public void resume(boolean passSignal) throws CDIException {
        // TODO FIX when engine support added
        resume();
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteResume#resume(org.eclipse.cdt.debug.core.cdi.ICDILocation)
     */
    @Override
    public void resume(ICDILocation location) throws CDIException {
        // TODO Auto-generated method stub
        throw new CDIException("Operation not (yet) supported");
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteResume#resume(org.eclipse.cdt.debug.core.cdi.model.ICDISignal)
     */
    @Override
    public void resume(ICDISignal signal) throws CDIException {
        // TODO Auto-generated method stub
        throw new CDIException("Operation not (yet) supported");
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter.isInstance(this))
            return this;
        if (adapter.equals(ICDIThread.class)){
            try {
                return getCurrentThread();
            }
            catch (CDIException e) {
                //Do nothing
            }
        }
        if (adapter.equals(ICDIStackFrame.class) || adapter.equals(StackFrameRef.class)){
            try {
                ICDIThread threads[] = getThreads();
                if (threads.length > 0 && threads[0] instanceof IAdaptable) {
                    return ((IAdaptable)threads[0]).getAdapter(adapter);
                }
            } catch (CDIException e) {
                //ignore
            }
        }
        if (adapter.equals(EngineInterface.class)){
            return getEngineInterface();
        }
        
        if (adapter.equals(IEngineErrorLogContainer.class)){
            return this;
        }
        
        if (adapter.equals(IProject.class)) {
            return getProject();
        }
        
        if (adapter.equals(ICommandProcessor.class) && !isTerminated()){
            return SeeCodePlugin.getCommandProcessor(getEngineInterface());
        }
        // This is how we can grab the IStreamProxy so as to capture
        // stdout and stderr on behalf of the Terminal simulator view.
        if (getProcess() instanceof IAdaptable){
            Object o = ((IAdaptable)getProcess()).getAdapter(adapter);
            if (o != null) return o;        
        }
        return mSession.getAdapter(adapter);
    }

    @Override
    public ICDIRuntimeOptions getRuntimeOptions() {
        return new ICDIRuntimeOptions() {
            //KLOODGE: flaw in the ICDITarget interface: there is no "start()" method.
            // Somehow, the infrastructure expects the debug process to materialize
            // somewhere between having its arguments and working directory set, and
            // when the "getProcess()" is called.
            // So, when do we actually have the debugger load the program for execution?
            // It must be at some point after these methods are called.

            @Override
            public void setArguments(String[] args) throws CDIException {
                mArgs = args;
                // At this point, we can now load the program. But we wait until
                // all three of these methods are called.
 
            }

            @Override
            public void setEnvironment(Properties props) throws CDIException {
                // We had to have already set this before invoking the debugger process.
                // So we do nothing here.
                //mEnv = props;
            }

            @Override
            public void setWorkingDirectory(String wd) throws CDIException {
                // This was actually already extracted when the debug process was created.
                // But we set it again anyway.
                mWorkingDir = wd;
            }

            @Override
            public ICDITarget getTarget() {
                return Target.this;
            }
        };
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getConfiguration()
     */
    @Override
    public ICDITargetConfiguration getConfiguration() {
        return mConfig;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#evaluateExpressionToString(org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame, java.lang.String)
     */
    @Override
    public String evaluateExpressionToString(ICDIStackFrame context, String expressionText) throws CDIException {
        try {
            StackFrameRef sf = ((StackFrame)context).getSeeCodeStackFrame();
            Value v;
            if (sf != null)
                v = sf.evaluate(expressionText);
            else
                v = mEngine.evaluate(expressionText, 0);
            if (v != null) return v.toString();
            return "???";
        } catch (EngineDisconnectedException e) {
            handleDroppedConnection();
            return "???";
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        } catch (EvaluationException e) {
            throw new CDIException(e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getGlobalVariableDescriptors(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ICDIGlobalVariableDescriptor getGlobalVariableDescriptors(String filename, String function, String name) throws CDIException {
        return fVariableManager.getGlobalVariableDescriptor(filename,function,name);
    }
    
    

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getRegisterGroups()
     */
    @Override
    public ICDIRegisterGroup[] getRegisterGroups() throws CDIException {
        confirmLoaded(false); // registers can't be retreived until program is loaded so that
                                // the engine knows the target.
        return fRegisterManager.getRegisterGroups();
    }
    
    RegisterManager getRegisterManager(){
        return fRegisterManager;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDISignalManagement#getSignals()
     */
    @Override
    public ICDISignal[] getSignals() throws CDIException {
        return fSignalManager.getSignals();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExpressionManagement#createExpression(java.lang.String)
     */
    @Override
    public ICDIExpression createExpression(String code) throws CDIException {
        return fExpressionManager.createExpression(code);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExpressionManagement#getExpressions()
     */
    @Override
    public ICDIExpression[] getExpressions() throws CDIException {
        return fExpressionManager.getExpressions();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExpressionManagement#destroyExpressions(org.eclipse.cdt.debug.core.cdi.model.ICDIExpression[])
     */
    @Override
    public void destroyExpressions(ICDIExpression[] expressions) throws CDIException {
        fExpressionManager.destroyExpressions(expressions);
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExpressionManagement#destroyAllExpressions()
     */
    @Override
    public void destroyAllExpressions() throws CDIException {
        fExpressionManager.destroyAllExpressions();        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDISourceManagement#addSourcePaths(java.lang.String[])
     */
    @Override
    public void setSourcePaths(String[] srcPaths) throws CDIException {
        if (mProgramLoadState == ProgramLoadState.LOADED){
            fSourceManager.addSourcePaths(srcPaths);   
        }
        else if (srcPaths != null && srcPaths.length > 0){
            mSrcPaths = new String[srcPaths.length];
            System.arraycopy(srcPaths,0,mSrcPaths,0,srcPaths.length);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDISourceManagement#getSourcePaths()
     */
    @Override
    public String[] getSourcePaths() throws CDIException {
        return fSourceManager.getSourcePaths();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDISourceManagement#getInstructions(java.math.BigInteger, java.math.BigInteger)
     */
    @Override
    public ICDIInstruction[] getInstructions(BigInteger startAddress, BigInteger endAddress) throws CDIException {
        return fSourceManager.getInstructions(startAddress,endAddress);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDISourceManagement#getInstructions(java.lang.String, int)
     */
    @Override
    public ICDIInstruction[] getInstructions(String filename, int linenum) throws CDIException {
        return fSourceManager.getInstructions(filename,linenum);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDISourceManagement#getInstructions(java.lang.String, int, int)
     */
    @Override
    public ICDIInstruction[] getInstructions(String filename, int linenum, int lines) throws CDIException {
        return fSourceManager.getInstructions(filename,linenum,lines);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDISourceManagement#getMixedInstructions(java.math.BigInteger, java.math.BigInteger)
     */
    @Override
    public ICDIMixedInstruction[] getMixedInstructions(BigInteger startAddress, BigInteger endAddress) throws CDIException {
        return fSourceManager.getMixedInstructions(startAddress,endAddress);
    }

    @Override
    public ICDIMemoryBlock createMemoryBlock (String address, int units, int wordSize) throws CDIException {
        return fMemoryManager.createMemoryBlock(address,units,wordSize);
    }

    @Override
    public ICDILineLocation createLineLocation (String file, int line, int byteOffsetFromLine) {
        return (ICDILineLocation)fBreakpointManager.createLocation(file,null,line,byteOffsetFromLine);
    }
    
    @Override
    public ICDILineLocation createLineLocation (String file, int line) {
        return createLineLocation(file,line,0);
    }
    @Override
    public ICDIFunctionLocation createFunctionLocation (String file, String function) {
        return createFunctionLocation(file,function,0);
    }
    
    @Override
    public ICDIFunctionLocation createFunctionLocation (String file, String function, int byteOffsetFromFunction) {
        try {
			if (confirmLoaded(true))
			    return (ICDIFunctionLocation)fBreakpointManager.createLocation(file,function,0,byteOffsetFromFunction);
		} catch (CDIException e) {
		}
		return new CDILocation(0); // presumably diagnosed later.
    }

    @Override
    public ICDILineBreakpoint setLineBreakpoint (int type, ICDILineLocation location, ICDICondition condition, boolean deferred, boolean enabled) throws CDIException {
        if (confirmLoaded(false) || (type & ICBreakpointType.TEMPORARY) !=0) { // in case its the temporary breakpoint on main
            try {
                return (ICDILineBreakpoint)fBreakpointManager.setLineBreakpoint(type,location,condition,deferred,enabled);
            }
            catch (ClassCastException e) {
                //The breakpoint is already set as a function breakpoint. No need to worry about it.
                // Do nothing.
            }
        }
        // We could get here when a temp breakpoint is attempted during startup but after
        // a license failure has occurred. In such cases, the result isn't used.
        return null;
    }
    
    @Override
    public ICDILineBreakpoint setLineBreakpoint (int type, ICDILineLocation location, ICDICondition condition, boolean deferred) throws CDIException {
       return setLineBreakpoint(type,location,condition,deferred,true);
    }

    @Override
    public ICDIFunctionBreakpoint setFunctionBreakpoint (int type, ICDIFunctionLocation location, ICDICondition condition, boolean deferred) throws CDIException {
        return setFunctionBreakpoint(type,location,condition,deferred,true);
    }
    
    @Override
    public ICDIFunctionBreakpoint setFunctionBreakpoint (int type, ICDIFunctionLocation location, ICDICondition condition, boolean deferred, boolean enabled) throws CDIException {
        if (confirmLoaded(false)) { // Can't set breakpoint if exe failed to load.
            return (ICDIFunctionBreakpoint)fBreakpointManager.setFunctionBreakpoint(type,location,condition,deferred,enabled);
        }
        // We could get here when a temp breakpoint is attempted during startup but after
        // a license failure has occurred. In such cases, the result isn't used.
        return null;
    }
    /* (non-Javadoc)
    public ICDIAddressBreakpoint setAddressBreakpoint (int type, ICDIAddressLocation location, ICDICondition condition, boolean deferred) throws CDIException {
        confirmLoaded(); // in case its the temporary breakpoint on main
        BreakpointManager mgr = mSession.getBreakpointManager();
        return (ICDIAddressBreakpoint)mgr.setLocationBreakpoint(type,location,condition);
    }
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDISourceManagement#getMixedInstructions(java.lang.String, int)
     */
    @Override
    public ICDIMixedInstruction[] getMixedInstructions(String filename, int linenum) throws CDIException {
        return fSourceManager.getMixedInstructions(filename,linenum);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDISourceManagement#getMixedInstructions(java.lang.String, int, int)
     */
    @Override
    public ICDIMixedInstruction[] getMixedInstructions(String filename, int linenum, int lines) throws CDIException {
        return fSourceManager.getMixedInstructions(filename,linenum,lines);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibraryManagement#getSharedLibraries()
     */
    @Override
    public ICDISharedLibrary[] getSharedLibraries() throws CDIException {
        return fSharedLibraryManager.getSharedLibraries();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlockManagement#removeBlocks(org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock[])
     */
    @Override
    public void removeBlocks(ICDIMemoryBlock[] memoryBlocks) throws CDIException {
        fMemoryManager.removeBlocks(memoryBlocks);       
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlockManagement#removeAllBlocks()
     */
    @Override
    public void removeAllBlocks() throws CDIException {
        fMemoryManager.removeAllBlocks();     
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlockManagement#getMemoryBlocks()
     */
    @Override
    public ICDIMemoryBlock[] getMemoryBlocks() throws CDIException {
        return fMemoryManager.getMemoryBlocks(); 
    }

    /**
     * Call when memory blocks may have changed.
     * @throws CDIException
     */
    @Override
    public void updateMemoryBlocks() throws CDIException{
        fMemoryManager.update(this);        
    }
    
    VariableManager getVariableManager(){
        return fVariableManager;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement#setExceptionBreakpoint(java.lang.String, boolean, boolean)
     */
    @Override
    public ICDIExceptionpoint setExceptionBreakpoint(String clazz, boolean stopOnThrow, boolean stopOnCatch, boolean enabled) throws CDIException {
        throw new IllegalArgumentException("Breakpoint exceptions not supported");
    }
    
    @Override
    public ICDIExceptionpoint setExceptionBreakpoint(String clazz, boolean stopOnThrow, boolean stopOnCatch) throws CDIException {
        return setExceptionBreakpoint(clazz,stopOnThrow,stopOnCatch,true);
    }

    @Override
    public ICDIGlobalVariable createGlobalVariable (ICDIGlobalVariableDescriptor varDesc) throws CDIException {
        return (ICDIGlobalVariable)fVariableManager.createVariable((CDIVariableDescriptor)varDesc);
    }

    @Override
    public ICDIRegister createRegister (ICDIRegisterDescriptor varDesc) throws CDIException {
        ICDIRegister reg = new Register((RegisterDescriptor)varDesc,fRegisterManager);
        EventManager emgr = (EventManager) mSession.getEventManager();
        emgr.enqueueEvent(new CreatedEvent(reg));
        return reg;
    }

    @Override
    public ICDIThread getCurrentThread () throws CDIException {
        return getThread();
    }
    
    void setCurrentThread(ICDIThread t){
        mCurrentThread = (CDIThread)t;
    }
    
    /**
     * Return true if suspend operation is pending for the process. When the threadSuspend events
     * occur, a process suspended event will be generated after all threads are suspended.
     * @return true if suspend operation is pending for the associated process.
     */
    boolean isSuspendPending(){
        return mSuspendPending;
    }
    
    /**
     * Called immediately before the process-suspended event is being fired as a result of the
     * last thread being suspended. See {@link EngineObserver#threadStopped(EngineInterface, int)}.
     */
    void clearSuspendPending(){
        mSuspendPending = false;
    }
    
    /**
     * Return true if resume operation is pending for the process. When the thread-resumed events
     * occur, a process resumed event will be generated after all threads are resumed.
     * @return true if resume operation is pending for the associated process.
     */
    boolean isResumePending() {
        return this.mPendingRunState != -1;      
    }
    
    /**
     * Called immediately before the process-resumed event is being fired as a result of the
     * last thread being resumed. See {@link EngineObserver#threadResumed(EngineInterface, int)}.
     */
    void clearResumePending(){
        this.mPendingRunState = -1;
    }
    
    /**
     * 
     * @return true if all threads have been suspended by the debugger.
     */
    boolean isAllThreadsSuspended() {
        return mThreadTable.isAllSuspended();
    }
    
    /**
     * 
     * @return true if all threads have been resumed by the debugger.
     */
    boolean isAllThreadsResumed() {
        return mThreadTable.isAllResumed();
    }
    
    /**
     * Indicate that the given thread not longer exists. All events associated with its
     * demise are assumed to have already been fired and serviced.
     * @param t the thread that no longer exists.
     */
    void disposeThread(ICDIThread t){
        mThreadTable.disposeThread(t);
    }

    @Override
    public void start () throws CDIException {
        confirmLoaded(false);
        this.fBreakpointManager.setStartupCompleted(); // now allow watchpoint create events when resuming
    }
    
    /**
     * Called after start() and after break/watch points have been restored.
     * Now create watchpoints that the user may have defined in his .scrc file.
     * We must do this after CDT has restored the ones from the last session so that
     * we don't see duplicates.
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @throws CDIException
     */
    @Override
    public void onBreakpointsRestored() throws CDIException {
        this.fBreakpointManager.fireEarlyWatchpoints();
    }

    @Override
    public ICDIGlobalVariableDescriptor[] getGlobalVariables () {
        if (fGlobalVars == null) {
            Elf elf;
            try {
                elf = new Elf(mExe);
            }
            catch (IOException e) {
                SeeCodePlugin.log(e);
                return null;
            }
            try {
                ISymbolReader symReader = Dwarf2Symbols.readDwarf2(elf, new IDwarf2ErrorReporter() {

                    @Override
                    public void error (String section, int offset, String message) {
                        SeeCodePlugin.log("In section " +
                            section +
                            " at offset 0x" +
                            Integer.toHexString(offset) +
                            ": " +
                            message);

                    }
                });
                List<ISymbol> globalVariables = symReader.getGlobalVariables();
                List<ICDIGlobalVariableDescriptor> list = new ArrayList<ICDIGlobalVariableDescriptor>(globalVariables
                    .size());
                Set<String> existing = new HashSet<String>();
                for (ISymbol sym : globalVariables) {
                    existing.add(sym.getLinkageName());
                    list.add(new GlobalVar(sym, this));
                }
                // Add elf symbols...
                try {
                    for (ISymbol sym: ElfSymbolReader.getGlobalVariables(elf)){
                        if (!existing.contains(sym.getLinkageName())){
                            list.add(new GlobalVar(sym,this));
                        }
                    }
                }
                catch (IOException e) {
                    SeeCodePlugin.log(e);
                }
//                Collections.sort(list,new Comparator<ICDIGlobalVariableDescriptor>(){
//                    @Override
//                    public int compare(ICDIGlobalVariableDescriptor s1, ICDIGlobalVariableDescriptor s2){
//                        return s1.getName().compareTo(s2.getName());
//                        
//                    }
//                 });
                fGlobalVars = list.toArray(new ICDIGlobalVariableDescriptor[list.size()]);
            }
            catch (IllegalArgumentException e) {
                SeeCodePlugin.log(e);
            }
            catch (IOException e) {
                SeeCodePlugin.log(e);
            }
            finally {
                elf.dispose();
            }
        }
        // NOTE: by returning NULL, CDT resorts to crudely extracting the data symbols
        // from the ELF symbol table. No type information, no source correlation, etc.
        // This needs to be fixed: (cr92233). Would like to use our own Dwarf reader to
        // extract information without going to engine! But if there are DLLs involved,
        // we need to know their origin, which is available from ICDISharedLibrary.
        return fGlobalVars; // Not yet implemented, but doesn't yet seem necessary       
    }
    
    static final int NO_LICENSE_FAILURE = 0;
    static final int LICENSE_FAILURE_POPUP_SHOWING = 1;
    static final int LICENSE_FAILURE_POPUP_DISMISSED = 2;
    
    /**
     * This is called from the engine observer to indicate that a license failure is pending.
     * We want to delay program load errors when this occurs to avoid obscuring the
     * popup dialog.
     * @param v
     */
    void setLicenseFailureState(int state){
        synchronized(this){
            mLicenseFailureState = state;
            notifyAll();
        }
    }
    
    /**
     * Called immediately after construction if this target is one of many CMPD targets.
     * @param processName the name of the user-defined process.
     * @param pid the CMPD process number.
     * @param total the total number of instances with the given process name.
     * @param command the complete exepath plus argumetns.
     */
    void setCMPDInfo(String processName, int pid, int total, String[]command) {
        fProcessName = processName;
        fProcessID = pid;
        fProcessInstanceTotal = total;
        mArgs = new String[command.length-1];
        System.arraycopy(command,1,mArgs,0,command.length-1);
    }

    @Override
    public boolean hasCustomDisassemblyView () {
        return true;
    }

    @Override
    public IPath getExePath () {
        return new Path(mExe);
    }

    @Override
    public int getProcessId () {
        return fProcessID;
    }

    @Override
    public int getProcessInstanceTotal () {
        return fProcessInstanceTotal;
    }

    @Override
    public String getProcessName () {
        return fProcessName;
    }

    @Override
    public IProject getProject () {
        return fProject;
    }
    
    public void writeToStdout(byte[] data){
        if (this.mProcess != null) {
            try {
                this.mProcess.getProcessOutputStream().write(data);
            }
            catch (IOException e) {
                SeeCodePlugin.log("Couldn't write to stdout",e);
            }
        }
    }
    
    public void writeToStderr(byte[] data){
        if (this.mProcess != null) {
            try {
                this.mProcess.getProcessErrorStream().write(data);
            }
            catch (IOException e) {
                SeeCodePlugin.log("Couldn't write to stderr",e);
            }
        }
    }  
    
    public String getWorkingDirectory(){
    	return mWorkingDir;
    }
    
    SignalManager getSignalManager() { return fSignalManager; }
    BreakpointManager getBreakpointManager() { return fBreakpointManager; }
    SharedLibraryManager getSharedLibraryManager() { return fSharedLibraryManager; }
    MemoryManager getMemoryManager() { return fMemoryManager; }

    @Override
    public void refreshViews ()  {
        mEngine.invalidateCache(); //force re-read from engine
        //updateViews(); //done as side-effect of call below
        try {
            mEngine.refreshDisplays();
        }
        catch (EngineDisconnectedException e){
            //Engine was terminated. Ignore request.
        }
        catch (EngineException e) {
            SeeCodePlugin.log(e);
        }      
    }
    
    private static boolean isCaseSensitive(){
        return File.separatorChar != '\\'; // Non-windows is case-sensitive.
    }
    
    /**
     * Given a source file path, make it relative to the
     * working directory if it is absolute.
     * @param file the source file path.
     * @return the same path relative to the working directory.
     */
    String computeRelativeSourcePath(String file) {
        File f = new File(file);
        if (!f.isAbsolute())
            return file;
        if (cwdSegments == null) {
            String cwd = getWorkingDirectory();
            if (cwd != null) {
                cwdSegments = cwd.replace(File.separatorChar, '/').split("/");
            }
            else return file;
        }
        
        String fileSegs[] = file.replace(File.separatorChar, '/').split("/");
        int matchCount = 0;
        int cnt = Math.min(fileSegs.length, cwdSegments.length);
        if (isCaseSensitive()) {
            for (; matchCount < cnt; matchCount++) {
                if (!fileSegs[matchCount].equals(cwdSegments[matchCount]))
                    break;
            }
        } else {

            for (; matchCount < cnt; matchCount++) {
                if (!fileSegs[matchCount].equalsIgnoreCase(cwdSegments[matchCount]))
                    break;
            }
        }
        if (matchCount > 0){
            StringBuilder b = new StringBuilder();
            for (int i = matchCount; i < cwdSegments.length; i++){
                b.append("../");
            }
            for (int i = matchCount; i < fileSegs.length; i++){
                b.append(fileSegs[i]);
                if (i+1 < fileSegs.length) b.append('/');
            }
            if (b.length() < file.length()) return b.toString();
        }
        return file;

    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget3#setDirectoryTranslation(java.lang.String[])
     */
    @Override
    public void setDirectoryTranslation (String[] paths) throws CDIException {
        if (mProgramLoadState == ProgramLoadState.LOADED){
            fSourceManager.setDirectoryTranslationPaths(paths);   
        }
        else if (paths != null && paths.length > 0){
            mTranslationPaths = new String[paths.length];
            System.arraycopy(paths,0,mTranslationPaths,0,paths.length);
        }
    }
    
    @Override
    public void setErrorLog(IEngineErrorLog log){
        fErrorLog = log;
    }
    
    public IEngineErrorLog getErrorLog() {
        return fErrorLog;
    }
}
