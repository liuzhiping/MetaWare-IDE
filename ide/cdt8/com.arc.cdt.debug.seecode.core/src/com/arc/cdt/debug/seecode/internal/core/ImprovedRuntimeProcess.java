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
package com.arc.cdt.debug.seecode.internal.core;

import java.util.Map;

import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.model.ICDebugElement;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.RuntimeProcess;

/**
 * We subclass the generic RuntimeProcess to get around
 * its bugs that cause deadlocks when the engine aborts
 * due to segfaults.
 * <P>
 * Specifically:
 * <ul>
 * <li> {@link #terminated} no longer fires events
 * while within a synchronized lock on this object.
 * <li> {@link #isTerminated} is no longer synchronized.
 * </ul>
 * @author David Pickens
 */
class ImprovedRuntimeProcess extends RuntimeProcess {
    private boolean fTerminatedShadow = false;
    private boolean mInTerminated = false;
    private boolean mTerminatedEventToBeFired = false;
    /**
     * @param launch
     * @param process
     * @param name
     * @param attributes
     */
    public ImprovedRuntimeProcess(ILaunch launch, Process process, String name,
            Map<String,String> attributes) {
        super(launch, process, name, attributes);
        fTerminatedShadow = super.isTerminated();
    }
    
    

    /**
     * We override this so as to make it un-synchronized.
     * Having it synchronized was causing deadlocks when
     * the engine aborted. The "Terminated" event was
     * being fired while the synchronization lock was
     * locked (See {@link #terminated}. Hopefully, this
     * fixes the problem.
     */
    @Override
    public boolean isTerminated() {
        return fTerminatedShadow;
    }

    /**
     * We override this so that the termination listeners
     * are not invoked from synchronized method.
     * (The method we are overriding is synchronized).
     */
    @Override
    protected void terminated() {
        mInTerminated = true;
        mTerminatedEventToBeFired = false;
        try {
            super.terminated();
            fTerminatedShadow = super.isTerminated();
            // Note we fire it without the synchronization
            // lock!
            if (mTerminatedEventToBeFired){
                super.fireTerminateEvent();
            }
        }finally{
            mInTerminated = false;
        }
    }
    /**
     * We intercept this so as to do nothing when
     * invoked form super.{@link #terminated()}, because
     * the of the sychronization locked.
     * We actually call the real thing from the
     * overridden version of {@link #terminated()}.
     */
    @Override
    protected void fireTerminateEvent() {
        if (!mInTerminated){
            // This super version is synchronized.
            super.fireTerminateEvent();
        }
        else {
            // This tells us to fire the terminate
            // event outside of the synch lock.
            mTerminatedEventToBeFired = true;
        }
    }



    /**
     * Override this so that when the debugger is selected in the Debug View, all other displays
     * synchronize with that instance of the debugger.
     */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object getAdapter(Class adapter) {		
		Object result = super.getAdapter(adapter);
		if (result == null){
		    //NOTE: as of Eclipse 3.3, we get CastExceptions if we use the associated
		    // Launch as the default adapter unfiltered. The debugger UI expects
		    // that the ILaunch adapter turn itself, which is bogus.
		    // Thus, we filter things for what we're interested in.
		    if (adapter.isAssignableFrom(ICDISessionObject.class) ||
		        adapter.isAssignableFrom(ICDebugElement.class))
		    {
			    result = this.getLaunch().getAdapter(adapter);
		    }
		}
		return result;
	}  
}
