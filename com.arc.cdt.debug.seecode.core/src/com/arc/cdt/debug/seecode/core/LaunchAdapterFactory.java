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
package com.arc.cdt.debug.seecode.core;


import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.model.IRestart;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.commands.IDebugCommandHandler;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.debug.core.commands.IResumeHandler;
import org.eclipse.debug.core.commands.IStepIntoHandler;
import org.eclipse.debug.core.commands.IStepOverHandler;
import org.eclipse.debug.core.commands.IStepReturnHandler;
import org.eclipse.debug.core.commands.ISuspendHandler;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStep;
import org.eclipse.debug.core.model.ISuspendResume;


class LaunchAdapterFactory implements IAdapterFactory {
    private static final Class<?>[] ADAPTER_CLASSES = new Class[] {
                    IResumeHandler.class,
                    IStepIntoHandler.class,
                    ISuspendHandler.class,
                    IStepOverHandler.class,
                    IRestart.class };

    private IAdapterFactory _delegate = null;

    static abstract class LaunchCommandHandler implements IDebugCommandHandler {

        protected abstract void canExecute (ICDISession session, IEnabledStateRequest request);

        protected abstract boolean execute (ICDISession session, IDebugCommandRequest request);

        @Override
        public void canExecute (IEnabledStateRequest request) {
            ILaunch launch = (ILaunch) request.getElements()[0];
            ICDISession session = (ICDISession) launch.getDebugTarget().getAdapter(ICDISession.class);
            if (session != null)
                canExecute(session, request);

        }

        @Override
        public boolean execute (IDebugCommandRequest request) {
            ILaunch launch = (ILaunch) request.getElements()[0];
            ICDISession session = (ICDISession) launch.getDebugTarget().getAdapter(ICDISession.class);
            if (session != null)
                return execute(session, request);
            return false;
        }
    }

    private IDebugCommandHandler fResumeHandler = new LaunchCommandHandler() {

        @Override
        protected void canExecute (ICDISession session, IEnabledStateRequest request) {
            if (session instanceof ISuspendResume) {
                ISuspendResume resume = (ISuspendResume) session;
                request.setEnabled(resume.canResume());
                request.done();
            }
        }

        @Override
        protected boolean execute (ICDISession session, IDebugCommandRequest request) {
            if (session instanceof ISuspendResume) {
                ISuspendResume resume = (ISuspendResume) session;
                try {
                    resume.resume();
                }
                catch (DebugException e) {
                    request.cancel();
                    request.done();
                    return false;
                }
                request.done();
                return true;
            }
            return false;
        }
    };

    private IDebugCommandHandler fSuspendHandler = new LaunchCommandHandler() {

        @Override
        protected void canExecute (ICDISession session, IEnabledStateRequest request) {
            if (session instanceof ISuspendResume) {
                ISuspendResume suspend = (ISuspendResume) session;
                request.setEnabled(suspend.canSuspend());
                request.done();
            }
        }

        @Override
        protected boolean execute (ICDISession session, IDebugCommandRequest request) {
            if (session instanceof ISuspendResume) {
                ISuspendResume suspend = (ISuspendResume) session;
                try {
                    suspend.suspend();
                }
                catch (DebugException e) {
                    request.cancel();
                    request.done();
                    return false;
                }
                request.done();
                return true;
            }
            return false;
        }
    };

    private IDebugCommandHandler fStepIntoHandler = new LaunchCommandHandler() {

        @Override
        protected void canExecute (ICDISession session, IEnabledStateRequest request) {
            if (session instanceof IStep) {
                IStep step = (IStep) session;
                request.setEnabled(step.canStepInto());
                request.done();
            }
        }

        @Override
        protected boolean execute (ICDISession session, IDebugCommandRequest request) {
            if (session instanceof ISuspendResume) {
                IStep step = (IStep) session;
                try {
                    step.stepInto();
                }
                catch (DebugException e) {
                    request.cancel();
                    request.done();
                    return false;
                }
                request.done();
                return true;
            }
            return false;
        }
    };
    
    private IDebugCommandHandler fStepOverHandler = new LaunchCommandHandler() {

        @Override
        protected void canExecute (ICDISession session, IEnabledStateRequest request) {
            if (session instanceof IStep) {
                IStep step = (IStep) session;
                request.setEnabled(step.canStepOver());
                request.done();
            }
        }

        @Override
        protected boolean execute (ICDISession session, IDebugCommandRequest request) {
            if (session instanceof ISuspendResume) {
                IStep step = (IStep) session;
                try {
                    step.stepOver();
                }
                catch (DebugException e) {
                    request.cancel();
                    request.done();
                    return false;
                }
                request.done();
                return true;
            }
            return false;
        }
    };
    
    private IDebugCommandHandler fStepReturnHandler = new LaunchCommandHandler() {

        @Override
        protected void canExecute (ICDISession session, IEnabledStateRequest request) {
            if (session instanceof IStep) {
                IStep step = (IStep) session;
                request.setEnabled(step.canStepReturn());
                request.done();
            }
        }

        @Override
        protected boolean execute (ICDISession session, IDebugCommandRequest request) {
            if (session instanceof ISuspendResume) {
                IStep step = (IStep) session;
                try {
                    step.stepReturn();
                }
                catch (DebugException e) {
                    request.cancel();
                    request.done();
                    return false;
                }
                request.done();
                return true;
            }
            return false;
        }
    };


    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter (Object adaptableObject, Class adapterType) {
        if (adaptableObject instanceof ILaunch) {
            ILaunch launch = (ILaunch) adaptableObject;
            IDebugTarget target = launch.getDebugTarget();
            if (target != null) {
                ICDISession session = (ICDISession) (target.getAdapter(ICDISession.class));
                if (session != null) {
                    if (adapterType == IResumeHandler.class) {
                        if (session instanceof ISuspendResume) {
                            return fResumeHandler;
                        }
                    }
                    else if (adapterType == ISuspendHandler.class) {
                        if (session instanceof ISuspendResume) {
                            return fSuspendHandler;
                        }
                    }
                    else if (adapterType == IStepIntoHandler.class) {
                        if (session instanceof IStep) {
                            return fStepIntoHandler;
                        }
                    }
                    else if (adapterType == IStepOverHandler.class) {
                        if (session instanceof IStep) {
                            return fStepOverHandler;
                        }
                    }
                    else if (adapterType == IStepReturnHandler.class) {
                        if (session instanceof IStep) {
                            return fStepReturnHandler;
                        }
                    }
                    else if (adapterType == IRestart.class){
                        if (session instanceof IRestart)
                            return session;
                    }
                }
            }

        }
        if (_delegate != null){
            return _delegate.getAdapter(adaptableObject, adapterType);
        }
        return null;
    }
    
    void setDelegate(IAdapterFactory base){
        _delegate = base;
    }

    @Override
    public Class< ? >[] getAdapterList () {
        if (_delegate != null) {
            Set<Class<?>>set = new HashSet<Class<?>>();
            for(Class<?>c: ADAPTER_CLASSES){
                set.add(c);
            }
            for (Class<?>c: _delegate.getAdapterList()){
                set.add(c);
            }
            return set.toArray(new Class<?>[set.size()]);
        }
        return ADAPTER_CLASSES;
    }

}
