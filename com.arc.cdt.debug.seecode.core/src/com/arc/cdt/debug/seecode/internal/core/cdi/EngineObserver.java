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

import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointHit;
import org.eclipse.cdt.debug.core.cdi.ICDIEndSteppingRange;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.ICDISignalReceived;
import org.eclipse.cdt.debug.core.cdi.ICDIWatchpointTrigger;
import org.eclipse.cdt.debug.core.cdi.event.ICDICreatedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIDestroyedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIRestartedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.core.cdi.IEngineErrorLog;
import com.arc.cdt.debug.seecode.internal.core.cdi.event.CreatedEvent;
import com.arc.cdt.debug.seecode.internal.core.cdi.event.DestroyedEvent;
import com.arc.cdt.debug.seecode.internal.core.cdi.event.DisconnectedEvent;
import com.arc.cdt.debug.seecode.internal.core.cdi.event.ExitedEvent;
import com.arc.cdt.debug.seecode.internal.core.cdi.event.RestartedEvent;
import com.arc.cdt.debug.seecode.internal.core.cdi.event.ResumedEvent;
import com.arc.cdt.debug.seecode.internal.core.cdi.event.SuspendedEvent;
import com.arc.seecode.engine.EngineDisconnectedException;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;
import com.arc.seecode.engine.IEngineAPI;
import com.arc.seecode.engine.IEngineObserver;
import com.arc.seecode.engine.WatchpointHit;

/**
 * The observer for SeeCode engine events.
 * 
 * @author David Pickens
 */
class EngineObserver implements IEngineObserver {

    private Session mSession;

    private EventManager mEventMgr;

    private StringBuffer mLogLineBuffer = new StringBuffer(100);
    
    EngineObserver(Session session, EventManager emgr) {
        mSession = session;
        mEventMgr = emgr;
    }
    
    private Target getTarget(EngineInterface engine){
        Target t = (Target)SeeCodePlugin.getEngineTarget(engine);
        if (t == null) throw new IllegalStateException("No associated target to engine!");
        return t;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.IEngineObserver#processCreated(com.arc.seecode.engine.EngineInterface)
     */
    @Override
    public void processCreated(EngineInterface engine) {
        Target target = getTarget(engine);
        SharedLibraryManager mgr = target.getSharedLibraryManager();
        mgr.clear();

        if (!target.isRestartPending()) {
            ICDICreatedEvent e = new CreatedEvent(target);
            mEventMgr.enqueueEvent(e);
        } else {
            //NOTE: RestartEvent not yet handled by infratructure!
            // A "resume" after the "restart" is what
            // gets things going.

            ICDIRestartedEvent e = new RestartedEvent(target);
            mEventMgr.enqueueEvent(e);
            // Infrastructure doesn't handle RestartedEvent correctly. We must contrive
            // a "suspended" event to make things work.
            // CR96189
            mEventMgr.enqueueEvent(new SuspendedEvent(target,mSession));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.IEngineObserver#processStarted(com.arc.seecode.engine.EngineInterface)
     */
    @Override
    public void processStarted(EngineInterface engine) {
        try {
            Target target = getTarget(engine);
            logMessage(engine, "Process started");
            int modules[] = engine.getModules();
            if (modules != null) {
                SharedLibraryManager mgr = target.getSharedLibraryManager();
                // Skip the first module (modules[0]). This is presumably the executable,
                // not a shared library.
                for (int i = 1; i < modules.length; i++) {
                    mgr.addLibrary(modules[i]);
                }
            }
            // We now get separate processResumed or threadResumed events
            //doResumedEvent(0);
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.IEngineObserver#processStopped(com.arc.seecode.engine.EngineInterface)
     */
    @Override
    public void processStopped(EngineInterface engine, int tid) {
        // NOTE: this is called only if the engine doesn't support independent thread control.
        Target target = getTarget(engine);
        if (tid != 0)
            target.setCurrentThread(target.findOrCreateThreadFromID(tid));
        handleStop(engine,tid,false);
    }
    
    @Override
    public void threadStopped(EngineInterface engine, int tid) {
        // NOTE: this is called only if the engine  supports independent thread control.
        /*ICDISessionObject reason  = */
        handleStop(engine, tid,getTarget(engine).supportsThreadControl());
    }

    /**
     * Handle a thread or process stop. If tid is non-zero, it is the thread ID.
     * Otherwise all threads of the process stopped.
     * @param engine
     * @param tid the thread that stopped, or 0 if the entire process stopped.
     * @return the reason why the thread or process stopped.
     */
    private ICDISessionObject handleStop (EngineInterface engine, int tid, boolean threadSpecific) {
        ICDISessionObject reason = mSession;
        Target target = getTarget(engine);
        try {
            switch (engine.queryState(tid)) {
            case IEngineAPI.BREAKPOINT_HIT: {
                int id = engine.getBreakpointHit(tid);
                reason = doBreakpointHit(engine, id);
            }
                break;
            case IEngineAPI.WATCHPOINT_HIT: {
                WatchpointHit hits[] = engine.getWatchpointHits(tid);
                if (hits != null && hits.length > 0) {
                    reason = doWatchpointHit(engine, hits[0]);
                }
            }
                break;
            case IEngineAPI.STEPPED:
                reason = new ICDIEndSteppingRange() {

                    @Override
                    public ICDISession getSession() {
                        return mSession;
                    }
                };
                break;
            case IEngineAPI.EXCEPTION_HIT: {
                int id = engine.getExceptionHit(tid);
                SignalManager smgr =  target.getSignalManager();
                final ICDISignal signal = smgr.getSignal(id);
                if (signal != null) {
                    reason = new ICDISignalReceived() {

                        @Override
                        public ICDISignal getSignal() {
                            return signal;
                        }

                        @Override
                        public ICDISession getSession() {
                            return mSession;
                        }
                    };
                }
                else {
                    reason = mSession; //shouldn't happen
                }
            }
                break;
            default:
            case IEngineAPI.STOPPED_BY_USER:
                reason = mSession;
                break;
            }
            if (reason != null) {
                ICDIEvent event = null;
                if (tid != 0 && threadSpecific) {
                    ICDIThread t = target.findOrCreateThreadFromID(tid);
                    if (t != null) {
                        event = new SuspendedEvent(t,reason);
                        target.setCurrentThread(t);
                    }
                }
                if (event == null)
                    event = new SuspendedEvent(target,reason);
               
                //mTarget.setSeeCodeCurrentThread(engine.getCurrentThread());
                mEventMgr.enqueueEvent(event);
                return reason;
            }
        } catch (Exception e) {
            SeeCodePlugin.log(e);
        }
        return reason;
    }


    /**
     * @param engine
     * @param id
     * @return session object
     */
    private ICDISessionObject doBreakpointHit(EngineInterface engine, int id) {
        ICDISessionObject reason;
        BreakpointManager bpmgr =  getTarget(engine).getBreakpointManager();
        final ICDIBreakpoint bp = bpmgr.findBreakpoint(id);
        if (bp != null)
            reason = new ICDIBreakpointHit() {

                @Override
                public ICDIBreakpoint getBreakpoint() {
                    return bp;
                }

                @Override
                public ICDISession getSession() {
                    return mSession;
                }
            };
        else {
            reason = mSession; // Unknown breakpoint???
            // VideoCore and others exit a program by having it hit a
            // breakpoint. The message can be misunderstood or annoying...
            //logMessage(engine, "Breakpoint instruction hit.\n");
        }
        return reason;
    }

    /**
     * @param engine
     * @param hit
     * @return an object that denotes that represents the watchpoint hit.
     */
    private ICDISessionObject doWatchpointHit(EngineInterface engine,
            final WatchpointHit hit) {
        ICDISessionObject reason;
        BreakpointManager bpmgr =  getTarget(engine).getBreakpointManager();
        final ICDIBreakpoint bp = bpmgr.findBreakpoint(hit.getWatchpointID());
        if (bp instanceof ICDIWatchpoint) {
            reason = new ICDIWatchpointTrigger() {

                @Override
                public ICDIWatchpoint getWatchpoint() {
                    return (ICDIWatchpoint) bp;
                }

                @Override
                public String getOldValue() {
                    return hit.getOldValue();
                }

                @Override
                public String getNewValue() {
                    return hit.getNewValue();
                }

                @Override
                public ICDISession getSession() {
                    return mSession;
                }

            };
        } else {
            reason = mSession; // Unknown breakpoint???
            logMessage(engine, "Unidentified watchpoint hit: "
                    + hit.getWatchpointID());
        }
        return reason;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.IEngineObserver#processResumed(com.arc.seecode.engine.EngineInterface)
     */
    @Override
    public void processResumed(EngineInterface engine) {
        doResumedEvent(getTarget(engine),0);
    }
    
    
    @Override
    public void threadResumed(EngineInterface engine, int tid) {
        Target t = getTarget(engine);
        doResumedEvent(t,t.supportsThreadControl()?tid:0);
    }

    /**
     *  
     */
    private void doResumedEvent(Target target, int tid) {
        ICDIResumedEvent e = null;
        if (tid == 0) {
            e = new ResumedEvent(target,target.getPendingRunState(0));
        }
        else {
            e = new ResumedEvent(target.findOrCreateThreadFromID(tid), target.getPendingRunState(tid));
        }
        mEventMgr.enqueueEvent(e);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.IEngineObserver#processTerminated(com.arc.seecode.engine.EngineInterface)
     */
    @Override
    public void processTerminated(EngineInterface engine) {
        Target target = getTarget(engine);
        // If we're terminating as part of restart, don't
        // do anything.
        // (We check the engine in case "restart" command processed)
        if (!target.isRestartPending() && !engine.isRestartPending()) {
            // If we're doing an emergency shutdown, then the target's
            // termination event will have already been fired. For such a case,
            // the "isTerminated()" property is already true.
            synchronized (target) {
                if (!target.isTerminated()) {
                    ICDIEvent e = new ExitedEvent(target,
                            mSession);
                    mEventMgr.enqueueEvent(e);
                }
            }
        } else {
            // Since "restart" isn't handled, we must
            // go through each thread and explicitly terminate
            // or else the stackframe views don't get updated.            
            ICDIThread threads[] = target.getThreadsBeforeRestart();
            if (threads != null) {
                for (int i = 0; i < threads.length; i++) {
                    ICDIDestroyedEvent e = new DestroyedEvent(threads[i]);
                    mEventMgr.enqueueEvent(e);
                    target.disposeThread(threads[i]);
                }
            }
        }
    }
    
    @Override
    public void processDisconnected(EngineInterface engine){
        getTarget(engine).setDisconnected();
        mEventMgr.enqueueEvent(new DisconnectedEvent(getTarget(engine)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.IEngineObserver#threadCreated(com.arc.seecode.engine.EngineInterface,
     *      int)
     */
    @Override
    public void threadCreated(EngineInterface engine, int tid) {
        Target target = getTarget(engine);
        ICDIThread thread = target.findOrCreateThreadFromID(tid);
        // We must not fire the create-thread event here
        // for the following subtle reason: when the process
        // stops, the CDT debugger layer refreshes its view
        // of the threads by calling "getThreads()" on the
        // target process. Unfortunately, a race condition can occur
        // in which "getThreads()" is called prior to this
        // method being invoked, or before it is completed. Thus,
        // it will get a list of empty threads will not materialize
        // them properly in the views.
        // 
        // To get around this, we merely make sure the
        // ICDIThread object is materialized in case
        // threadTerminate is called prior to the CDT
        // layer noticing that the new thread exists.
        // 
        // We make "getThreads()" actually invoke the
        // engine to get all known threads. If the
        // CDT layer sees a thread it hadn't known about
        // before, it fires a "thread-created" event
        // as expected.
        //
        //Hmmm. The above explanation isn't correct. At startup, the "target.getThreads()" may be
        // called between the time the target program is loaded and before this event occurs.
        // Thus, there will be no threads. Therefore, fire the event here.
        ICDICreatedEvent e = new CreatedEvent(thread);
        mEventMgr.enqueueEvent(e);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.IEngineObserver#threadTerminated(com.arc.seecode.engine.EngineInterface,
     *      int)
     */
    @Override
    public void threadTerminated(EngineInterface engine, int tid) {
        Target target = getTarget(engine);
        ICDIThread thread = target.lookupThreadFromID(tid);
        if (thread != null) { // should always be true.
            ICDIDestroyedEvent e = new DestroyedEvent(thread);
            mEventMgr.enqueueEvent(e);
        } else
            logMessage(engine, "Unidentified thread " + tid + " terminated.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.IEngineObserver#displayError(com.arc.seecode.engine.EngineInterface,
     *      java.lang.String)
     */
    @Override
    public void displayError (EngineInterface engine, String msg) {
        try {
            SeeCodePlugin.getDefault().displayError("Debugger Error", msg);
            // SeeCodePlugin.log(new Status(IStatus.ERROR,
            // SeeCodePlugin.PLUGIN_ID,
            // IStatus.ERROR, msg, null));
            mSession.getStderr().write(msg + "\n");
            mSession.getStderr().flush();
        }
        catch (NullPointerException x){
            // Session has closed down console reading stdout and stderr.
            // Presumably because the session is terminating.
            // Do nothing.
        }
        catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().indexOf("Pipe closed") >= 0){
                //Ignore "pipe close". The message is being emitted after the
                // engine shutdown or crashed.
            }
            else SeeCodePlugin.log(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.IEngineObserver#displayNote(com.arc.seecode.engine.EngineInterface,
     *      java.lang.String)
     */
    @Override
    public void displayNote(EngineInterface engine, String msg) {
        SeeCodePlugin.getDefault().displayNote("Debugger Note", msg);
        logMessage(engine, msg);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.IEngineObserver#displayFatal(com.arc.seecode.engine.EngineInterface,
     *      java.lang.String)
     */
    @Override
    public void displayFatal(EngineInterface engine, String msg) {
        SeeCodePlugin.log(new Status(IStatus.ERROR, SeeCodePlugin.PLUGIN_ID,
                IStatus.ERROR, msg, null));
    }

    private static String detab(String s) {
        int i = s.indexOf('\t');
        if (i < 0) return s;
        StringBuffer buf = new StringBuffer(s.length() + 32);
        for (int j = 0; j < s.length(); j++) {
            char c = s.charAt(j);
            if (c == '\t') {
                buf.append(' ');
                while (buf.length() % 8 == 0)
                    buf.append(' ');
            } else
                buf.append(c);
        }
        return buf.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.IEngineObserver#logMessage(com.arc.seecode.engine.EngineInterface,
     *      java.lang.String)
     */
    @Override
    public void logMessage(EngineInterface engine, String text) {
        try {
            //        SeeCodePlugin.log(new Status(IStatus.INFO,
            // SeeCodePlugin.PLUGIN_ID,
            //                IStatus.INFO, text, null));
            int i = text.lastIndexOf('\n');
            if (i >= 0) {
                mLogLineBuffer.append(text.substring(0, i + 1));
                // A bug in SWT StyledText causes an infinite
                // loop if it encounters a tab under misunderstood
                // conditions. So, we detab here.
                mSession.getStdout().write(detab(mLogLineBuffer.toString()));
                mSession.getStdout().flush();
                mLogLineBuffer.setLength(0);
                if (i < text.length() - 1) {
                    mLogLineBuffer.append(text.substring(i + 1));
                }
            } else
                mLogLineBuffer.append(text);
            //if (!text.endsWith("\n")) mSession.getStdout().write("\n");
        } catch (IOException e) {
            // We can have broken pipes as
            // session is abruptly terminating,
            // so only report error if this doesn't
            // seem to be the case.
            if (!mSession.isShuttingDown())
                SeeCodePlugin.log(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.IEngineObserver#moduleLoaded(com.arc.seecode.engine.EngineInterface,
     *      int)
     */
    @Override
    public void moduleLoaded(EngineInterface engine, int moduleID) {
        SharedLibraryManager mgr =  getTarget(engine).getSharedLibraryManager();
        mgr.addLibrary(moduleID);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.IEngineObserver#moduleUnloaded(com.arc.seecode.engine.EngineInterface,
     *      int)
     */
    @Override
    public void moduleUnloaded(EngineInterface engine, int moduleID) {
        SharedLibraryManager mgr = getTarget(engine).getSharedLibraryManager();
        try {
            if (!mgr.removeLibrary(moduleID)) {
                logMessage(engine, "Unknown module id " + moduleID
                        + " unloading (" + engine.getModuleName(moduleID) + ")");
            }
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.IEngineObserver#setStatus(com.arc.seecode.engine.EngineInterface,
     *      java.lang.String)
     */
    @Override
    public void setStatus(EngineInterface engine, String status) {
        SeeCodePlugin.getDefault().setStatus(status);
    }

    /* override */
    @Override
    public void logError(EngineInterface engine, String msg, Throwable t) {
        SeeCodePlugin.log(new Status(IStatus.ERROR, SeeCodePlugin.PLUGIN_ID,
                IStatus.ERROR, msg, t));

    }

    /* override */
    @Override
    public void engineShutdown(EngineInterface engine) {
        Target target = getTarget(engine);
        if (!target.isTerminated())
            target.fireTerminateEvent(); // Force it if it isn't yet terminated.
        if (!mSession.isShutdown()) mSession.onShutdown();
        // For CMPD, only a single engine proxy will get this event.      
        // The terminating of the engine should
        // trigger the appropriate termination of the
        // session.
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.IEngineObserver#licensingFailure(com.arc.seecode.engine.EngineInterface, java.lang.String)
     */
    @Override
    public void licensingFailure(EngineInterface engine, final String msg) {
        Target target = getTarget(engine);
		target.setLicenseFailureState(Target.LICENSE_FAILURE_POPUP_SHOWING);
		try {
			SeeCodePlugin.getDefault().reportLicensingFailure(msg);
		} finally {
			target.setLicenseFailureState(Target.LICENSE_FAILURE_POPUP_DISMISSED);
			try {
				engine.shutdown();
			} catch (EngineDisconnectedException e) {
				// ignore; engine shut itself down.
			} catch (EngineException e) {
				SeeCodePlugin.log(e);
				mSession.forceEmergencyShutdown();
			}
		}
	}

    @Override
    public void writeToStderr (EngineInterface engine, byte[] data) {
        Target target = getTarget(engine);
        target.writeToStderr(data);       
    }

    @Override
    public void writeToStdout (EngineInterface engine, byte[] data) {
        Target target = getTarget(engine);
        target.writeToStdout(data);      
    }

    @Override
    public void writeToErrorLog (EngineInterface engine, String msg) {
        IEngineErrorLog log = getTarget(engine).getErrorLog();
        if (log != null)
            log.write(engine,msg);
    }
}
